package com.duoc.bank.operaciones.service;

import com.duoc.bank.common.dto.CargoRequest;
import com.duoc.bank.common.dto.SaldoCuentaDTO;
import com.duoc.bank.common.event.RetiroRealizadoEvent;
import com.duoc.bank.common.exception.CuentaNoEncontradaException;
import com.duoc.bank.common.exception.SaldoInsuficienteException;
import com.duoc.bank.common.exception.ServicioNoDisponibleException;
import com.duoc.bank.operaciones.client.CuentasClient;
import com.duoc.bank.operaciones.dto.OperacionLigeraDTO;
import com.duoc.bank.operaciones.dto.RetiroResponse;
import com.duoc.bank.operaciones.event.RetiroEventProducer;
import com.duoc.bank.operaciones.mapper.OperacionesMapper;
import com.duoc.bank.operaciones.model.OperacionAnual;
import com.duoc.bank.operaciones.repository.OperacionAnualRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Logica de negocio del microservicio de operaciones.
 * Orquesta el retiro de dinero: consulta el saldo y ejecuta el cargo en
 * ms-cuentas (via {@link CuentasClient}), y publica el evento
 * {@code RetiroRealizadoEvent} en el topico {@code retiros-realizados} de Kafka.
 * El registro de la operacion anual lo hace de forma asincrona el consumidor
 * del propio servicio (saga coreografiada): como Kafka retiene el evento, ya no
 * puede quedar un retiro sin historial por un fallo transitorio de persistencia.
 *
 * Tolerancia a fallos: las llamadas a ms-cuentas estan protegidas con el Circuit
 * Breaker {@code ms-cuentas} mas reintentos {@code @Retry}, la publicacion del
 * evento con el Circuit Breaker {@code publicacion-eventos} (cuyo fallback
 * degrada al registro sincrono local) y la persistencia local con
 * {@code persistencia-operaciones}. Un retiro nunca se confirma sin haberse
 * ejecutado: si ms-cuentas no responde, el fallback responde 503.
 *
 * Las operaciones internas se invocan a traves del proxy de Spring
 * ({@code self}) para que los Circuit Breakers apliquen tambien cuando la
 * llamada parte del propio metodo {@link #retirar(Long, double)}.
 */
@Service
public class OperacionesService {

    private static final Logger log = LoggerFactory.getLogger(OperacionesService.class);
    private static final String DESCRIPCION_RETIRO = "Retiro cajero automatico";
    private static final int LIMITE_MAXIMO_OPERACIONES = 20;

    private final CuentasClient cuentasClient;
    private final OperacionAnualRepository operacionAnualRepository;
    private final RetiroEventProducer retiroEventProducer;
    private final OperacionesService self;

    public OperacionesService(CuentasClient cuentasClient,
                              OperacionAnualRepository operacionAnualRepository,
                              RetiroEventProducer retiroEventProducer,
                              @Lazy OperacionesService self) {
        this.cuentasClient = cuentasClient;
        this.operacionAnualRepository = operacionAnualRepository;
        this.retiroEventProducer = retiroEventProducer;
        this.self = self;
    }

    /**
     * Retiro de dinero: valida saldo en ms-cuentas, ejecuta el cargo remoto y
     * publica el evento del retiro; el registro local de la operacion anual lo
     * completa el consumidor del topico (saga coreografiada). Errores:
     * 404 cuenta inexistente, 409 saldo insuficiente, 503 servicio de cuentas
     * no disponible.
     */
    public RetiroResponse retirar(Long cuentaId, double monto) {
        // 1) Saldo vigente segun ms-cuentas (dueno del saldo)
        SaldoCuentaDTO saldo = self.consultarSaldo(cuentaId);
        double saldoActual = saldo.getSaldo() != null ? saldo.getSaldo() : 0.0;

        // 2) Validacion de negocio antes de ejecutar el cargo
        if (saldoActual < monto) {
            log.warn("evento=saldo_insuficiente cuentaId={} monto={} saldoActual={}", cuentaId, monto, saldoActual);
            throw new SaldoInsuficienteException(saldoActual);
        }

        // 3) El cargo lo ejecuta ms-cuentas; si falla, el retiro no se confirma
        SaldoCuentaDTO resultado = self.ejecutarCargo(cuentaId, monto);
        double saldoRestante = resultado.getSaldo() != null ? resultado.getSaldo() : 0.0;

        // 4) Se publica el evento del retiro; el historial se registra de forma asincrona
        RetiroRealizadoEvent evento = new RetiroRealizadoEvent(
                UUID.randomUUID().toString(), cuentaId, monto, saldoRestante,
                LocalDate.now(), MDC.get("requestId"));
        self.publicarEventoRetiro(evento);

        log.info("evento=retiro_exitoso cuentaId={} monto={} saldoRestante={}", cuentaId, monto, saldoRestante);
        return new RetiroResponse(cuentaId, monto, saldoRestante);
    }

    /** Saldo vigente de una cuenta segun ms-cuentas. */
    @Retry(name = "ms-cuentas")
    @CircuitBreaker(name = "ms-cuentas", fallbackMethod = "consultarSaldoFallback")
    public SaldoCuentaDTO consultarSaldo(Long cuentaId) {
        return cuentasClient.obtenerSaldo(cuentaId);
    }

    /** Ejecuta el cargo del retiro en ms-cuentas y devuelve el saldo restante. */
    @Retry(name = "ms-cuentas")
    @CircuitBreaker(name = "ms-cuentas", fallbackMethod = "ejecutarCargoFallback")
    public SaldoCuentaDTO ejecutarCargo(Long cuentaId, double monto) {
        return cuentasClient.registrarCargo(cuentaId, new CargoRequest(monto));
    }

    /**
     * Publica el evento del retiro en el topico de Kafka. Si el broker no esta
     * disponible, el fallback degrada al registro sincrono de la operacion.
     */
    @CircuitBreaker(name = "publicacion-eventos", fallbackMethod = "publicarEventoRetiroFallback")
    public void publicarEventoRetiro(RetiroRealizadoEvent evento) {
        try {
            retiroEventProducer.publicar(evento);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicacion del evento interrumpida", e);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo publicar el evento del retiro en Kafka", e);
        }
    }

    /**
     * Registra la operacion anual del retiro en la tabla local. Se usa como
     * degradacion sincrona cuando Kafka no esta disponible (el flujo normal
     * registra via el consumidor del evento, de forma asincrona).
     */
    @CircuitBreaker(name = "persistencia-operaciones", fallbackMethod = "registrarOperacionFallback")
    public void registrarOperacion(Long cuentaId, double monto) {
        OperacionAnual operacion = new OperacionAnual();
        operacion.setCuentaId(cuentaId);
        operacion.setFecha(LocalDate.now());
        operacion.setTransaccion("retiro");
        operacion.setMonto(monto);
        operacion.setDescripcion(DESCRIPCION_RETIRO);
        operacionAnualRepository.save(operacion);
    }

    /** Operaciones anuales de una cuenta, de la mas reciente a la mas antigua. */
    @CircuitBreaker(name = "persistencia-operaciones", fallbackMethod = "listarOperacionesFallback")
    public List<OperacionLigeraDTO> listarOperaciones(Long cuentaId, int limite) {
        int limiteAjustado = Math.max(1, Math.min(limite, LIMITE_MAXIMO_OPERACIONES));
        return operacionAnualRepository
                .findByCuentaIdOrderByFechaDesc(cuentaId, PageRequest.of(0, limiteAjustado))
                .stream()
                .map(OperacionesMapper::toOperacionLigeraDTO)
                .toList();
    }

    /** Total de operaciones anuales registradas. */
    @CircuitBreaker(name = "persistencia-operaciones", fallbackMethod = "contarOperacionesFallback")
    public long contarOperaciones() {
        return operacionAnualRepository.count();
    }

    /** Fallback del saldo: ms-cuentas no responde, se informa indisponibilidad (503). */
    private SaldoCuentaDTO consultarSaldoFallback(Long cuentaId, Throwable causa) {
        // Los errores de negocio no son fallos del servicio: se propagan tal cual (404)
        if (causa instanceof CuentaNoEncontradaException e) {
            throw e;
        }
        log.warn("evento=fallback_saldo cuentaId={} motivo={}", cuentaId, causa.toString());
        throw new ServicioNoDisponibleException(
                "Microservicio de cuentas no disponible; no se pudo consultar el saldo", causa);
    }

    /** Fallback del cargo: jamas se confirma un retiro que no se ejecuto; se responde 503. */
    private SaldoCuentaDTO ejecutarCargoFallback(Long cuentaId, double monto, Throwable causa) {
        // Los errores de negocio no son fallos del servicio: se propagan tal cual (404/409)
        if (causa instanceof CuentaNoEncontradaException e) {
            throw e;
        }
        if (causa instanceof SaldoInsuficienteException e) {
            throw e;
        }
        log.warn("evento=fallback_retiro cuentaId={} monto={} motivo={}", cuentaId, monto, causa.toString());
        throw new ServicioNoDisponibleException(
                "Microservicio de cuentas no disponible; el retiro no fue ejecutado", causa);
    }

    /**
     * Fallback de la publicacion del evento: si Kafka no esta disponible, se
     * degrada al registro sincrono de la operacion (comportamiento previo a la
     * mensajeria). El retiro ya fue ejecutado y debe quedar con historial.
     */
    private void publicarEventoRetiroFallback(RetiroRealizadoEvent evento, Throwable causa) {
        log.error("evento=publicacion_fallida eventoId={} cuentaId={} motivo={}; se registra sincrono",
                evento.getEventoId(), evento.getCuentaId(), causa.toString());
        self.registrarOperacion(evento.getCuentaId(), evento.getMonto());
    }

    /**
     * Fallback del registro sincrono: no lanza excepcion porque el dinero ya se
     * desconto en ms-cuentas y el retiro se confirma. Solo aplica cuando Kafka
     * tampoco esta disponible (fallback del fallback); en el flujo normal el
     * registro lo hace el consumidor del evento, que reintenta hasta lograrlo.
     */
    private void registrarOperacionFallback(Long cuentaId, double monto, Throwable causa) {
        log.error("evento=operacion_no_registrada cuentaId={} monto={} motivo={}",
                cuentaId, monto, causa.toString());
    }

    /** Fallback del listado: se responde con lista vacia (200) ante fallo de persistencia. */
    private List<OperacionLigeraDTO> listarOperacionesFallback(Long cuentaId, int limite, Throwable causa) {
        log.warn("evento=fallback_listado_operaciones cuentaId={} motivo={}", cuentaId, causa.toString());
        return List.of();
    }

    /** Fallback del conteo: se responde 0 ante fallo de persistencia. */
    private long contarOperacionesFallback(Throwable causa) {
        log.warn("evento=fallback_stats motivo={}", causa.toString());
        return 0L;
    }
}

package com.duoc.bank.cuentas.service;

import com.duoc.bank.common.dto.SaldoCuentaDTO;
import com.duoc.bank.common.exception.CuentaNoEncontradaException;
import com.duoc.bank.common.exception.SaldoInsuficienteException;
import com.duoc.bank.common.exception.ServicioNoDisponibleException;
import com.duoc.bank.cuentas.model.Cuenta;
import com.duoc.bank.cuentas.repository.CuentaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logica de negocio del microservicio de cuentas, dueno de la tabla legada
 * {@code cuentas}. Como la tabla guarda snapshots semanales, toda operacion
 * trabaja sobre el registro vigente (mayor {@code registro_id}) de cada cuenta.
 *
 * Tolerancia a fallos: las operaciones estan protegidas con Circuit Breaker
 * (instancia {@code persistencia-cuentas}). El listado y el conteo se degradan
 * a resultados vacios/cero; el detalle, el saldo y el cargo no se pueden
 * fabricar y responden 503 via {@link ServicioNoDisponibleException}.
 * Un cargo nunca se confirma sin ejecutarse.
 */
@Service
public class CuentasService {

    private static final Logger log = LoggerFactory.getLogger(CuentasService.class);

    private final CuentaRepository cuentaRepository;

    public CuentasService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    /** Pagina de cuentas distintas (registro mas reciente de cada una). */
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "persistencia-cuentas", fallbackMethod = "cuentasFallback")
    public Page<Cuenta> listarCuentasVigentes(Pageable pageable) {
        return cuentaRepository.findCuentasVigentes(pageable);
    }

    /** Cuenta vigente por cuentaId; lanza CuentaNoEncontradaException si no existe. */
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "persistencia-cuentas", fallbackMethod = "detalleFallback")
    public Cuenta obtenerCuentaVigente(Long cuentaId) {
        log.info("evento=detalle_cuenta cuentaId={}", cuentaId);
        return cuentaRepository.findFirstByCuentaIdOrderByRegistroIdDesc(cuentaId)
                .orElseThrow(() -> new CuentaNoEncontradaException(cuentaId));
    }

    /** Saldo vigente de una cuenta; lanza CuentaNoEncontradaException si no existe. */
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "persistencia-cuentas", fallbackMethod = "saldoFallback")
    public SaldoCuentaDTO obtenerSaldo(Long cuentaId) {
        Cuenta cuenta = obtenerCuentaVigente(cuentaId);
        return new SaldoCuentaDTO(cuenta.getCuentaId(), cuenta.getNombre(), cuenta.getSaldo());
    }

    /** Total de cuentas distintas en la tabla (sin importar los snapshots). */
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "persistencia-cuentas", fallbackMethod = "conteoFallback")
    public long contarCuentasDistintas() {
        return cuentaRepository.contarCuentasDistintas();
    }

    /**
     * Registra un cargo sobre el saldo vigente de la cuenta, en una sola
     * transaccion: re-valida el saldo, descuenta y guarda. El registro del
     * historial de la operacion corresponde a otro microservicio.
     */
    @Transactional
    @CircuitBreaker(name = "persistencia-cuentas", fallbackMethod = "cargoFallback")
    public SaldoCuentaDTO registrarCargo(Long cuentaId, double monto) {
        Cuenta cuenta = cuentaRepository.findFirstByCuentaIdOrderByRegistroIdDesc(cuentaId)
                .orElseThrow(() -> new CuentaNoEncontradaException(cuentaId));

        double saldoActual = cuenta.getSaldo() != null ? cuenta.getSaldo() : 0.0;
        if (saldoActual < monto) {
            log.warn("evento=saldo_insuficiente cuentaId={} monto={} saldoActual={}", cuentaId, monto, saldoActual);
            throw new SaldoInsuficienteException(saldoActual);
        }

        // Se descuenta el saldo del registro mas reciente de la cuenta
        double saldoRestante = saldoActual - monto;
        cuenta.setSaldo(saldoRestante);
        cuentaRepository.save(cuenta);

        log.info("evento=cargo_exitoso cuentaId={} monto={} saldoRestante={}", cuentaId, monto, saldoRestante);
        return new SaldoCuentaDTO(cuenta.getCuentaId(), cuenta.getNombre(), saldoRestante);
    }

    /** Fallback del listado: pagina vacia (respuesta degradada). */
    private Page<Cuenta> cuentasFallback(Pageable pageable, Throwable causa) {
        log.warn("evento=fallback_cuentas motivo={}", causa.toString());
        return Page.empty(pageable);
    }

    /** Fallback del detalle: no se puede fabricar una cuenta; se responde 503. */
    private Cuenta detalleFallback(Long cuentaId, Throwable causa) {
        // Los errores de negocio no son fallos de persistencia: se propagan tal cual (404)
        if (causa instanceof CuentaNoEncontradaException e) {
            throw e;
        }
        log.warn("evento=fallback_detalle_cuenta cuentaId={} motivo={}", cuentaId, causa.toString());
        throw new ServicioNoDisponibleException(
                "Persistencia no disponible; no se pudo obtener el detalle de la cuenta", causa);
    }

    /** Fallback del saldo: no se puede fabricar un saldo; se responde 503. */
    private SaldoCuentaDTO saldoFallback(Long cuentaId, Throwable causa) {
        if (causa instanceof CuentaNoEncontradaException e) {
            throw e;
        }
        log.warn("evento=fallback_saldo cuentaId={} motivo={}", cuentaId, causa.toString());
        throw new ServicioNoDisponibleException(
                "Persistencia no disponible; no se pudo obtener el saldo de la cuenta", causa);
    }

    /** Fallback del conteo: se responde 0 (respuesta degradada). */
    private long conteoFallback(Throwable causa) {
        log.warn("evento=fallback_conteo_cuentas motivo={}", causa.toString());
        return 0;
    }

    /**
     * Fallback del cargo: un cargo nunca se confirma sin ejecutarse.
     * Los errores de negocio se propagan tal cual; el resto responde 503.
     */
    private SaldoCuentaDTO cargoFallback(Long cuentaId, double monto, Throwable causa) {
        if (causa instanceof CuentaNoEncontradaException e) {
            throw e;
        }
        if (causa instanceof SaldoInsuficienteException e) {
            throw e;
        }
        log.warn("evento=fallback_cargo cuentaId={} monto={} motivo={}", cuentaId, monto, causa.toString());
        throw new ServicioNoDisponibleException(
                "Persistencia no disponible; el cargo no se ejecuto y no debe reintentarse a ciegas", causa);
    }
}

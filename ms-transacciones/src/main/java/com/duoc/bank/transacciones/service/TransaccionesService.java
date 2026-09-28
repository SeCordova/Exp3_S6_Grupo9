package com.duoc.bank.transacciones.service;

import com.duoc.bank.transacciones.model.Transaccion;
import com.duoc.bank.transacciones.repository.TransaccionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio del microservicio de transacciones.
 * Concentra las consultas de solo lectura contra la tabla {@code transacciones}
 * y la seleccion de la consulta segun los filtros recibidos.
 *
 * Tolerancia a fallos: todas las consultas estan protegidas con Circuit Breaker
 * (instancia {@code persistencia-transacciones}); si la persistencia falla,
 * los fallbacks devuelven respuestas degradadas (pagina o lista vacia, conteo 0)
 * en vez de dejar la llamada bloqueada.
 */
@Service
@Transactional(readOnly = true)
public class TransaccionesService {

    private static final Logger log = LoggerFactory.getLogger(TransaccionesService.class);

    private final TransaccionRepository transaccionRepository;

    public TransaccionesService(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    /**
     * Historial paginado de transacciones. El filtro de anomalia solo se aplica
     * cuando viene en true (anomalia=false equivale a no filtrar); el filtro
     * de tipo se aplica cuando no es null ni vacio.
     */
    @CircuitBreaker(name = "persistencia-transacciones", fallbackMethod = "buscarTransaccionesFallback")
    public Page<Transaccion> buscarTransacciones(String tipo, Boolean anomalia, Pageable pageable) {
        boolean filtrarAnomalia = Boolean.TRUE.equals(anomalia);
        boolean filtrarTipo = tipo != null && !tipo.isBlank();

        if (filtrarTipo && filtrarAnomalia) {
            return transaccionRepository.findByTipoAndAnomaliaTrue(tipo, pageable);
        }
        if (filtrarTipo) {
            return transaccionRepository.findByTipo(tipo, pageable);
        }
        if (filtrarAnomalia) {
            return transaccionRepository.findByAnomaliaTrue(pageable);
        }
        return transaccionRepository.findAll(pageable);
    }

    /** Ultimas 10 transacciones registradas, de la mas reciente a la mas antigua. */
    @CircuitBreaker(name = "persistencia-transacciones", fallbackMethod = "listarRecientesFallback")
    public List<Transaccion> listarRecientes() {
        return transaccionRepository.findTop10ByOrderByFechaDescRegistroIdDesc();
    }

    /** Total de transacciones registradas. */
    @CircuitBreaker(name = "persistencia-transacciones", fallbackMethod = "conteoFallback")
    public long contarTransacciones() {
        return transaccionRepository.count();
    }

    /** Total de transacciones marcadas como anomalia. */
    @CircuitBreaker(name = "persistencia-transacciones", fallbackMethod = "conteoFallback")
    public long contarAnomalias() {
        return transaccionRepository.countByAnomaliaTrue();
    }

    /** Fallback del historial: pagina vacia (respuesta degradada). */
    private Page<Transaccion> buscarTransaccionesFallback(String tipo, Boolean anomalia, Pageable pageable,
                                                          Throwable causa) {
        log.warn("evento=fallback_transacciones tipo={} anomalia={} motivo={}", tipo, anomalia, causa.toString());
        return Page.empty(pageable);
    }

    /** Fallback de movimientos recientes: lista vacia (respuesta degradada). */
    private List<Transaccion> listarRecientesFallback(Throwable causa) {
        log.warn("evento=fallback_recientes motivo={}", causa.toString());
        return List.of();
    }

    /** Fallback de los conteos: 0 (respuesta degradada). */
    private long conteoFallback(Throwable causa) {
        log.warn("evento=fallback_conteo motivo={}", causa.toString());
        return 0L;
    }
}

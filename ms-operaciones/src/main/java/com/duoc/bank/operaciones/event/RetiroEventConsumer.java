package com.duoc.bank.operaciones.event;

import com.duoc.bank.common.event.RetiroRealizadoEvent;
import com.duoc.bank.operaciones.model.EventoProcesado;
import com.duoc.bank.operaciones.model.OperacionAnual;
import com.duoc.bank.operaciones.repository.EventoProcesadoRepository;
import com.duoc.bank.operaciones.repository.OperacionAnualRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Consumidor del topico de retiros (grupo ms-operaciones): registra la
 * operacion anual del retiro en la tabla local. Es el paso final de la saga
 * coreografiada: si la base de datos no esta disponible, el error handler
 * reintenta y el evento permanece en el topico hasta ser procesado.
 *
 * Idempotencia: el eventoId ya procesado se marca en {@code eventos_procesados}
 * dentro de la misma transaccion que el insert, de modo que un evento duplicado
 * (entrega at-least-once) no registra la operacion dos veces.
 */
@Component
public class RetiroEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RetiroEventConsumer.class);
    private static final String CONSUMIDOR = "ms-operaciones";
    private static final String DESCRIPCION_RETIRO = "Retiro cajero automatico";

    private final OperacionAnualRepository operacionAnualRepository;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public RetiroEventConsumer(OperacionAnualRepository operacionAnualRepository,
                               EventoProcesadoRepository eventoProcesadoRepository) {
        this.operacionAnualRepository = operacionAnualRepository;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @KafkaListener(topics = "${eventos.topico-retiros}")
    @Transactional
    public void onRetiroRealizado(RetiroRealizadoEvent evento) {
        if (eventoProcesadoRepository.existsByEventoIdAndConsumidor(evento.getEventoId(), CONSUMIDOR)) {
            log.info("evento=retiro_duplicado_descartado eventoId={} cuentaId={}",
                    evento.getEventoId(), evento.getCuentaId());
            return;
        }

        OperacionAnual operacion = new OperacionAnual();
        operacion.setCuentaId(evento.getCuentaId());
        operacion.setFecha(evento.getFecha());
        operacion.setTransaccion("retiro");
        operacion.setMonto(evento.getMonto());
        operacion.setDescripcion(DESCRIPCION_RETIRO);
        operacionAnualRepository.save(operacion);

        eventoProcesadoRepository.save(
                new EventoProcesado(evento.getEventoId(), CONSUMIDOR, LocalDateTime.now()));

        log.info("evento=operacion_registrada_desde_evento eventoId={} cuentaId={} monto={}",
                evento.getEventoId(), evento.getCuentaId(), evento.getMonto());
    }
}

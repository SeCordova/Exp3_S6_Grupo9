package com.duoc.bank.transacciones.event;

import com.duoc.bank.common.event.RetiroRealizadoEvent;
import com.duoc.bank.transacciones.model.EventoProcesado;
import com.duoc.bank.transacciones.model.Transaccion;
import com.duoc.bank.transacciones.repository.EventoProcesadoRepository;
import com.duoc.bank.transacciones.repository.TransaccionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Consumidor del topico de retiros (grupo ms-transacciones): registra el retiro
 * como una transaccion del historial, visible luego en los endpoints del
 * servicio. Es el segundo consumidor de la saga coreografiada, independiente
 * del registro de operaciones anuales que hace ms-operaciones.
 *
 * Idempotencia: el eventoId ya procesado se marca en {@code eventos_procesados}
 * dentro de la misma transaccion que el insert, de modo que un evento duplicado
 * (entrega at-least-once) no registra la transaccion dos veces.
 */
@Component
public class RetiroEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RetiroEventConsumer.class);
    private static final String CONSUMIDOR = "ms-transacciones";
    private static final String TIPO_RETIRO = "retiro";

    private final TransaccionRepository transaccionRepository;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public RetiroEventConsumer(TransaccionRepository transaccionRepository,
                               EventoProcesadoRepository eventoProcesadoRepository) {
        this.transaccionRepository = transaccionRepository;
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

        Transaccion transaccion = new Transaccion();
        transaccion.setId(transaccionRepository.maxIdLegado() + 1);
        transaccion.setFecha(evento.getFecha());
        transaccion.setMonto(evento.getMonto());
        transaccion.setTipo(TIPO_RETIRO);
        transaccion.setAnomalia(false);
        transaccionRepository.save(transaccion);

        eventoProcesadoRepository.save(
                new EventoProcesado(evento.getEventoId(), CONSUMIDOR, LocalDateTime.now()));

        log.info("evento=transaccion_registrada_desde_evento eventoId={} cuentaId={} monto={}",
                evento.getEventoId(), evento.getCuentaId(), evento.getMonto());
    }
}

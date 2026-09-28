package com.duoc.bank.operaciones.event;

import com.duoc.bank.common.event.RetiroRealizadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Productor del evento {@link RetiroRealizadoEvent} en el topico de retiros.
 * La clave del mensaje es el cuentaId para que los eventos de una misma cuenta
 * caigan en la misma particion y se consuman en orden. El envio es
 * idempotente (enable.idempotence) y confirmado con acks=all.
 */
@Component
public class RetiroEventProducer {

    private static final Logger log = LoggerFactory.getLogger(RetiroEventProducer.class);

    private final KafkaTemplate<String, RetiroRealizadoEvent> kafkaTemplate;
    private final String topico;

    public RetiroEventProducer(KafkaTemplate<String, RetiroRealizadoEvent> kafkaTemplate,
                               @Value("${eventos.topico-retiros}") String topico) {
        this.kafkaTemplate = kafkaTemplate;
        this.topico = topico;
    }

    /** Publica el evento y espera la confirmacion del broker (max 5 s). */
    public void publicar(RetiroRealizadoEvent evento) throws Exception {
        kafkaTemplate.send(topico, String.valueOf(evento.getCuentaId()), evento).get(5, TimeUnit.SECONDS);
        log.info("evento=retiro_publicado topico={} eventoId={} cuentaId={} monto={}",
                topico, evento.getEventoId(), evento.getCuentaId(), evento.getMonto());
    }
}

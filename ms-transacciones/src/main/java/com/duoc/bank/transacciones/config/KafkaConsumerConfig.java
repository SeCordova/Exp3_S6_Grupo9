package com.duoc.bank.transacciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Manejo de errores del consumidor Kafka: ante un fallo de procesamiento
 * (p.ej. base de datos no disponible) reintenta 3 veces con 2 s de espera
 * antes de descartar el registro. Junto con la retencion del topico, evita
 * que un evento quede sin procesar por un fallo transitorio.
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(2000L, 3L));
    }
}

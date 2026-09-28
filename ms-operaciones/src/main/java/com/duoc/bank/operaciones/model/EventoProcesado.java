package com.duoc.bank.operaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Marca de idempotencia del consumidor de eventos. Como Kafka entrega
 * at-least-once, el mismo evento puede llegar duplicado; esta tabla permite
 * detectarlo y no repetir el efecto de negocio (registro de la operacion).
 * La tabla se crea con el script {@code scripts/sql/eventos_procesados.sql}.
 */
@Entity
@Table(name = "eventos_procesados")
@IdClass(EventoProcesadoId.class)
public class EventoProcesado {

    @Id
    @Column(name = "evento_id")
    private String eventoId;

    @Id
    @Column(name = "consumidor")
    private String consumidor;

    @Column(name = "procesado_en")
    private LocalDateTime procesadoEn;

    public EventoProcesado() {
    }

    public EventoProcesado(String eventoId, String consumidor, LocalDateTime procesadoEn) {
        this.eventoId = eventoId;
        this.consumidor = consumidor;
        this.procesadoEn = procesadoEn;
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public String getConsumidor() {
        return consumidor;
    }

    public void setConsumidor(String consumidor) {
        this.consumidor = consumidor;
    }

    public LocalDateTime getProcesadoEn() {
        return procesadoEn;
    }

    public void setProcesadoEn(LocalDateTime procesadoEn) {
        this.procesadoEn = procesadoEn;
    }
}

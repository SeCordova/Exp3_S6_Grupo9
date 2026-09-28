package com.duoc.bank.transacciones.model;

import java.io.Serializable;
import java.util.Objects;

/** Clave primaria compuesta de {@link EventoProcesado}. */
public class EventoProcesadoId implements Serializable {

    private String eventoId;
    private String consumidor;

    public EventoProcesadoId() {
    }

    public EventoProcesadoId(String eventoId, String consumidor) {
        this.eventoId = eventoId;
        this.consumidor = consumidor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventoProcesadoId that)) {
            return false;
        }
        return Objects.equals(eventoId, that.eventoId) && Objects.equals(consumidor, that.consumidor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventoId, consumidor);
    }
}

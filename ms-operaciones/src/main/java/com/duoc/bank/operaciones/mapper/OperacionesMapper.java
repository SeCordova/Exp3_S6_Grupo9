package com.duoc.bank.operaciones.mapper;

import com.duoc.bank.operaciones.dto.OperacionLigeraDTO;
import com.duoc.bank.operaciones.model.OperacionAnual;

/**
 * Conversor estatico de entidades de persistencia a DTOs del servicio.
 * Centraliza la transformacion para que los DTOs se mantengan como POJOs
 * sin dependencia hacia las entidades de persistencia.
 */
public final class OperacionesMapper {

    private OperacionesMapper() {
    }

    /** Convierte una operacion anual en su version ligera (sin descripcion ni IDs internos). */
    public static OperacionLigeraDTO toOperacionLigeraDTO(OperacionAnual operacion) {
        return new OperacionLigeraDTO(operacion.getFecha(), operacion.getTransaccion(), operacion.getMonto());
    }
}

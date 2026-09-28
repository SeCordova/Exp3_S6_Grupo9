package com.duoc.bank.transacciones.mapper;

import com.duoc.bank.transacciones.dto.TransaccionDTO;
import com.duoc.bank.transacciones.dto.TransaccionLigeraDTO;
import com.duoc.bank.transacciones.model.Transaccion;

/**
 * Conversiones entidad -&gt; DTO del microservicio de transacciones,
 * centralizadas en un solo lugar. Asi los DTOs quedan como POJOs planos
 * y la logica de transformacion no se repite entre controlador y servicios.
 */
public final class TransaccionesMapper {

    private TransaccionesMapper() {
    }

    /** Convierte una transaccion en su DTO completo del historial. */
    public static TransaccionDTO toTransaccionDTO(Transaccion transaccion) {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(transaccion.getId());
        dto.setFecha(transaccion.getFecha());
        dto.setMonto(transaccion.getMonto());
        dto.setTipo(transaccion.getTipo());
        dto.setAnomalia(transaccion.getAnomalia());
        return dto;
    }

    /** Convierte una transaccion en su version ligera (solo fecha, tipo y monto). */
    public static TransaccionLigeraDTO toTransaccionLigeraDTO(Transaccion transaccion) {
        return new TransaccionLigeraDTO(transaccion.getFecha(), transaccion.getTipo(), transaccion.getMonto());
    }
}

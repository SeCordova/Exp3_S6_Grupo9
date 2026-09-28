package com.duoc.bank.operaciones.dto;

import java.time.LocalDate;

/**
 * DTO ligero de operacion: sin descripcion ni IDs internos.
 * POJO plano: la conversion desde la entidad la hace {@code OperacionesMapper}.
 */
public class OperacionLigeraDTO {

    private LocalDate fecha;
    private String transaccion;
    private Double monto;

    public OperacionLigeraDTO() {
    }

    public OperacionLigeraDTO(LocalDate fecha, String transaccion, Double monto) {
        this.fecha = fecha;
        this.transaccion = transaccion;
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }
}

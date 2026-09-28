package com.duoc.bank.transacciones.dto;

import java.time.LocalDate;

/**
 * DTO ligero de transaccion reciente: solo fecha, tipo y monto.
 * POJO plano: la conversion desde la entidad la hace {@code TransaccionesMapper}.
 */
public class TransaccionLigeraDTO {

    private LocalDate fecha;
    private String tipo;
    private Double monto;

    public TransaccionLigeraDTO() {
    }

    public TransaccionLigeraDTO(LocalDate fecha, String tipo, Double monto) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }
}

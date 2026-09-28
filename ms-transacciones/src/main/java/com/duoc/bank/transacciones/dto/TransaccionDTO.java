package com.duoc.bank.transacciones.dto;

import java.time.LocalDate;

/**
 * DTO completo de una transaccion del historial.
 * POJO plano: la conversion desde la entidad la hace {@code TransaccionesMapper}.
 */
public class TransaccionDTO {

    private Long id;
    private LocalDate fecha;
    private Double monto;
    private String tipo;
    private Boolean anomalia;

    public TransaccionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getAnomalia() {
        return anomalia;
    }

    public void setAnomalia(Boolean anomalia) {
        this.anomalia = anomalia;
    }
}

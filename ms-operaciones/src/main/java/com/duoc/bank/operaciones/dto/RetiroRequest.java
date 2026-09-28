package com.duoc.bank.operaciones.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Cuerpo JSON de la peticion de retiro.
 * Las validaciones se resuelven de forma declarativa con Bean Validation.
 */
public class RetiroRequest {

    @NotNull(message = "El campo cuentaId es obligatorio")
    private Long cuentaId;

    @NotNull(message = "El monto debe ser un valor positivo")
    @Positive(message = "El monto debe ser un valor positivo")
    private Double monto;

    public RetiroRequest() {
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }
}

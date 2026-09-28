package com.duoc.bank.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Contrato del cargo (descuento de saldo) que ms-operaciones envia a
 * ms-cuentas al ejecutar un retiro. El monto es obligatorio y positivo.
 */
public class CargoRequest {

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    private Double monto;

    public CargoRequest() {
    }

    public CargoRequest(Double monto) {
        this.monto = monto;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }
}

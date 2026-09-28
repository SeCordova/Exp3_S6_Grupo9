package com.duoc.bank.operaciones.dto;

/**
 * Respuesta exitosa de un retiro: monto retirado y saldo restante.
 */
public class RetiroResponse {

    private Long cuentaId;
    private Double montoRetirado;
    private Double saldoRestante;

    public RetiroResponse() {
    }

    public RetiroResponse(Long cuentaId, Double montoRetirado, Double saldoRestante) {
        this.cuentaId = cuentaId;
        this.montoRetirado = montoRetirado;
        this.saldoRestante = saldoRestante;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public Double getMontoRetirado() {
        return montoRetirado;
    }

    public void setMontoRetirado(Double montoRetirado) {
        this.montoRetirado = montoRetirado;
    }

    public Double getSaldoRestante() {
        return saldoRestante;
    }

    public void setSaldoRestante(Double saldoRestante) {
        this.saldoRestante = saldoRestante;
    }
}

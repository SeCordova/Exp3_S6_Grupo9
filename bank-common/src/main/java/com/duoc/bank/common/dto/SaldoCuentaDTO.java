package com.duoc.bank.common.dto;

/**
 * Contrato de saldo de una cuenta, compartido entre microservicios.
 * Lo expone ms-cuentas (endpoint publico e interno) y lo consume
 * ms-operaciones via Feign al orquestar un retiro.
 */
public class SaldoCuentaDTO {

    private Long cuentaId;
    private String nombre;
    private Double saldo;

    public SaldoCuentaDTO() {
    }

    public SaldoCuentaDTO(Long cuentaId, String nombre, Double saldo) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.saldo = saldo;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }
}

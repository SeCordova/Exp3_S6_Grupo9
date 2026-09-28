package com.duoc.bank.cuentas.dto;

/**
 * DTO completo de una cuenta: todos los campos de negocio del registro vigente.
 * POJO plano: la conversion desde la entidad la hace {@code CuentasMapper}.
 */
public class CuentaCompletaDTO {

    private Long cuentaId;
    private String nombre;
    private Double saldo;
    private Double saldoFinal;
    private Integer edad;
    private String tipo;

    public CuentaCompletaDTO() {
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

    public Double getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(Double saldoFinal) {
        this.saldoFinal = saldoFinal;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}

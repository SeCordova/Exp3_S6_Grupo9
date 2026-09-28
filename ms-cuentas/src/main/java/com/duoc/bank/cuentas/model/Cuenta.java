package com.duoc.bank.cuentas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que mapea la tabla legada {@code cuentas}.
 *
 * IMPORTANTE: la tabla guarda snapshots semanales, por lo que {@code cuenta_id}
 * se repite en varias filas. La clave primaria es {@code registro_id}
 * (autoincremental) y el estado vigente de una cuenta es siempre el registro
 * con el mayor {@code registro_id} para ese {@code cuenta_id}.
 */
@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_id")
    private Long registroId;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "saldo")
    private Double saldo;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "saldo_final")
    private Double saldoFinal;

    public Cuenta() {
    }

    public Long getRegistroId() {
        return registroId;
    }

    public void setRegistroId(Long registroId) {
        this.registroId = registroId;
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

    public Double getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(Double saldoFinal) {
        this.saldoFinal = saldoFinal;
    }
}

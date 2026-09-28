package com.duoc.bank.operaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Entidad JPA que mapea la tabla legada {@code operaciones_anuales}.
 * Registra las operaciones anuales de cada cuenta (abonos, cargos, retiros, etc.).
 */
@Entity
@Table(name = "operaciones_anuales")
public class OperacionAnual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registro_id")
    private Long registroId;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "transaccion")
    private String transaccion;

    @Column(name = "monto")
    private Double monto;

    @Column(name = "descripcion")
    private String descripcion;

    public OperacionAnual() {
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

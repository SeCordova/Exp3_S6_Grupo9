package com.duoc.bank.operaciones.dto;

/**
 * Estadisticas del servicio: total de operaciones anuales registradas.
 */
public class StatsDTO {

    private long totalOperaciones;

    public StatsDTO() {
    }

    public StatsDTO(long totalOperaciones) {
        this.totalOperaciones = totalOperaciones;
    }

    public long getTotalOperaciones() {
        return totalOperaciones;
    }

    public void setTotalOperaciones(long totalOperaciones) {
        this.totalOperaciones = totalOperaciones;
    }
}

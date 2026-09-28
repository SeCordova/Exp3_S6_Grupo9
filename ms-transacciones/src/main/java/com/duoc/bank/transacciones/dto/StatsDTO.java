package com.duoc.bank.transacciones.dto;

/**
 * Resumen estadistico del historial de transacciones:
 * total de movimientos y total de movimientos marcados como anomalia.
 */
public class StatsDTO {

    private long totalTransacciones;
    private long totalAnomalias;

    public StatsDTO() {
    }

    public StatsDTO(long totalTransacciones, long totalAnomalias) {
        this.totalTransacciones = totalTransacciones;
        this.totalAnomalias = totalAnomalias;
    }

    public long getTotalTransacciones() {
        return totalTransacciones;
    }

    public void setTotalTransacciones(long totalTransacciones) {
        this.totalTransacciones = totalTransacciones;
    }

    public long getTotalAnomalias() {
        return totalAnomalias;
    }

    public void setTotalAnomalias(long totalAnomalias) {
        this.totalAnomalias = totalAnomalias;
    }
}

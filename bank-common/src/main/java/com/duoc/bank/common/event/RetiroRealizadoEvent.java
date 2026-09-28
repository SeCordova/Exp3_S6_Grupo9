package com.duoc.bank.common.event;

import java.time.LocalDate;

/**
 * Evento de dominio: un retiro fue ejecutado en ms-cuentas.
 * Lo publica ms-operaciones en el topico {@code retiros-realizados} y lo consumen
 * ms-operaciones (registro de la operacion anual) y ms-transacciones (historial).
 *
 * El {@code eventoId} (UUID) permite a los consumidores ser idempotentes ante la
 * entrega at-least-once de Kafka; el {@code requestId} mantiene la trazabilidad
 * con la solicitud HTTP original (header X-Request-ID).
 */
public class RetiroRealizadoEvent {

    private String eventoId;
    private Long cuentaId;
    private Double monto;
    private Double saldoRestante;
    private LocalDate fecha;
    private String requestId;

    public RetiroRealizadoEvent() {
    }

    public RetiroRealizadoEvent(String eventoId, Long cuentaId, Double monto,
                                Double saldoRestante, LocalDate fecha, String requestId) {
        this.eventoId = eventoId;
        this.cuentaId = cuentaId;
        this.monto = monto;
        this.saldoRestante = saldoRestante;
        this.fecha = fecha;
        this.requestId = requestId;
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
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

    public Double getSaldoRestante() {
        return saldoRestante;
    }

    public void setSaldoRestante(Double saldoRestante) {
        this.saldoRestante = saldoRestante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}

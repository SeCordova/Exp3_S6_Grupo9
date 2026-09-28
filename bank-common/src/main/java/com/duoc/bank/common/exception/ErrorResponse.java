package com.duoc.bank.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Cuerpo de error uniforme para los microservicios del ecosistema bancario.
 * Ademas del mensaje, entrega un {@code codigo} estable de maquina
 * (ver {@link CodigoError}) y el {@code requestId} de correlacion, que se
 * toma automaticamente del MDC poblado por
 * {@link com.duoc.bank.common.filter.RequestIdFilter}.
 * El campo {@code detalles} es opcional y se usa, por ejemplo, para entregar
 * los mensajes de validacion por campo cuando una peticion no pasa Bean Validation.
 * Los campos null se omiten del JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String codigo;
    private String error;
    private String requestId;
    private Map<String, String> detalles;

    public ErrorResponse() {
    }

    public ErrorResponse(String error) {
        this.error = error;
    }

    public ErrorResponse(String error, Map<String, String> detalles) {
        this.error = error;
        this.detalles = detalles;
    }

    public ErrorResponse(CodigoError codigoError, String error) {
        this(codigoError, error, null);
    }

    public ErrorResponse(CodigoError codigoError, String error, Map<String, String> detalles) {
        this.codigo = codigoError.getCodigo();
        this.error = error;
        this.requestId = MDC.get("requestId");
        this.detalles = detalles;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Map<String, String> getDetalles() {
        return detalles;
    }

    public void setDetalles(Map<String, String> detalles) {
        this.detalles = detalles;
    }
}

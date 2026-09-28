package com.duoc.bank.transacciones.config;

import com.duoc.bank.common.exception.CodigoError;
import com.duoc.bank.common.exception.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo centralizado de excepciones del microservicio de transacciones.
 * Toda respuesta de error usa el formato uniforme definido por
 * {@link ErrorResponse}, con su {@link CodigoError} estable, en vez de
 * Map.of dispersos en el controlador.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Parametros de consulta que no pasan la validacion (@Min, @Max, etc.) -> 400. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> parametrosInvalidos(ConstraintViolationException e) {
        Map<String, String> detalles = new LinkedHashMap<>();
        for (ConstraintViolation<?> violacion : e.getConstraintViolations()) {
            detalles.put(violacion.getPropertyPath().toString(), violacion.getMessage());
        }
        log.warn("evento=parametros_invalidos detalles={}", detalles);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(CodigoError.PARAMETROS_INVALIDOS,
                        "Parametros de consulta invalidos", detalles));
    }

    /** Body que no pasa Bean Validation (login) -> 400 con el mensaje de la primera violacion. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacionFallida(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Solicitud invalida");
        log.warn("evento=validacion_fallida detalle={}", mensaje);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(CodigoError.VALIDACION_FALLIDA, mensaje));
    }

    /** Circuito abierto: Resilience4j rechaza la llamada sin llegar al fallback -> 503. */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> circuitoAbierto(CallNotPermittedException e) {
        log.warn("evento=circuito_abierto circuitBreaker={}", e.getCausingCircuitBreakerName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(CodigoError.SERVICIO_NO_DISPONIBLE,
                        "Servicio temporalmente no disponible; reintente mas tarde"));
    }

    /** Cualquier otro error no controlado -> 500 generico (sin filtrar detalles internos). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInterno(Exception e) {
        log.error("evento=error_interno", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(CodigoError.ERROR_INTERNO, "Error interno del servidor"));
    }
}

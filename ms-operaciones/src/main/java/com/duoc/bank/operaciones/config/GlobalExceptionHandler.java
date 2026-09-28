package com.duoc.bank.operaciones.config;

import com.duoc.bank.common.exception.CodigoError;
import com.duoc.bank.common.exception.CuentaNoEncontradaException;
import com.duoc.bank.common.exception.ErrorResponse;
import com.duoc.bank.common.exception.SaldoInsuficienteException;
import com.duoc.bank.common.exception.ServicioNoDisponibleException;
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
 * Manejo centralizado de excepciones del microservicio de operaciones.
 * Mantiene los codigos de estado del contrato: 400 validacion, 404 cuenta
 * inexistente, 409 saldo insuficiente y 503 servicio dependiente no disponible.
 * Toda respuesta incluye el codigo de error estable ({@link CodigoError}) y el
 * requestId de correlacion (via {@link ErrorResponse}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Cuenta inexistente -> 404. */
    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> cuentaNoEncontrada(CuentaNoEncontradaException e) {
        log.warn("evento=cuenta_no_encontrada detalle={}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(CodigoError.CUENTA_NO_ENCONTRADA, e.getMessage()));
    }

    /**
     * Saldo insuficiente -> 409. El saldo actual se informa en detalles solo si
     * se conoce (es >= 0); cuando la excepcion llega reconstruida desde la
     * respuesta HTTP de ms-cuentas queda en -1 como centinela y se omite.
     */
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> saldoInsuficiente(SaldoInsuficienteException e) {
        log.warn("evento=retiro_rechazado motivo=saldo_insuficiente saldoActual={}", e.getSaldoActual());
        if (e.getSaldoActual() >= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(CodigoError.SALDO_INSUFICIENTE, "Saldo insuficiente",
                            Map.of("saldoActual", String.valueOf(e.getSaldoActual()))));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(CodigoError.SALDO_INSUFICIENTE, e.getMessage()));
    }

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

    /** Body que no pasa Bean Validation -> 400 con el mensaje de la primera violacion. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacionFallida(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Solicitud invalida");
        log.warn("evento=validacion_fallida detalle={}", mensaje);
        return ResponseEntity.badRequest().body(new ErrorResponse(CodigoError.VALIDACION_FALLIDA, mensaje));
    }

    /** Fallback de un Circuit Breaker (servicio dependiente caido) -> 503. */
    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> servicioNoDisponible(ServicioNoDisponibleException e) {
        log.warn("evento=servicio_no_disponible detalle={}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(CodigoError.SERVICIO_NO_DISPONIBLE, e.getMessage()));
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

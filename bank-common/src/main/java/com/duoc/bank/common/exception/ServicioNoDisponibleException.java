package com.duoc.bank.common.exception;

/**
 * Un microservicio dependiente (o la base de datos) no esta disponible
 * (circuito abierto o fallo en la llamada). Los fallbacks de los Circuit
 * Breakers la lanzan en las operaciones que no pueden responder con datos
 * por defecto; se traduce a 503 con el codigo
 * {@link CodigoError#SERVICIO_NO_DISPONIBLE}.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

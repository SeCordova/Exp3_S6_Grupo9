package com.duoc.bank.common.exception;

/**
 * Excepcion de dominio compartida por los microservicios.
 * Se lanza cuando no existe ningun registro (snapshot) para el cuentaId pedido.
 * Cada servicio la traduce a un 404 en su manejador global de excepciones.
 */
public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long cuentaId) {
        super("Cuenta no encontrada: " + cuentaId);
    }

    /** Para reconstruir la excepcion desde una respuesta HTTP de otro servicio. */
    public CuentaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}

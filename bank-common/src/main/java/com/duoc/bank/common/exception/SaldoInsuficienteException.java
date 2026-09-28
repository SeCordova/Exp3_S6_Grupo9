package com.duoc.bank.common.exception;

/**
 * Excepcion de negocio: el saldo vigente de la cuenta no alcanza para el cargo.
 * Es parte del contrato compartido: la lanza ms-cuentas al registrar un cargo
 * y la propaga ms-operaciones al orquestar un retiro; se traduce a 409 con el
 * codigo {@link CodigoError#SALDO_INSUFICIENTE}. Lleva el saldo actual para
 * informarlo en la respuesta de error.
 */
public class SaldoInsuficienteException extends RuntimeException {

    private final double saldoActual;

    public SaldoInsuficienteException(double saldoActual) {
        super("Saldo insuficiente");
        this.saldoActual = saldoActual;
    }

    /**
     * Para reconstruir la excepcion desde una respuesta HTTP de otro servicio,
     * cuando no se conoce el saldo actual (queda en -1 como centinela).
     */
    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
        this.saldoActual = -1;
    }

    public double getSaldoActual() {
        return saldoActual;
    }
}

package com.duoc.bank.common.exception;

/**
 * Codigos de error estables del contrato de las APIs bancarias.
 * El codigo es un identificador legible por maquina (no cambia aunque cambie
 * el mensaje de texto), pensado para que los clientes y las integraciones
 * futuras puedan reaccionar programaticamente a cada tipo de fallo.
 */
public enum CodigoError {

    CREDENCIALES_INVALIDAS("BANK-401-01"),
    API_KEY_INVALIDA("BANK-401-02"),
    TOKEN_INVALIDO("BANK-401-03"),
    SERVICIO_INCORRECTO("BANK-403-01"),
    ACCESO_DENEGADO("BANK-403-02"),
    CUENTA_NO_ENCONTRADA("BANK-404-01"),
    PARAMETROS_INVALIDOS("BANK-400-01"),
    VALIDACION_FALLIDA("BANK-400-02"),
    SALDO_INSUFICIENTE("BANK-409-01"),
    LIMITE_EXCEDIDO("BANK-429-01"),
    ERROR_INTERNO("BANK-500-01"),
    SERVICIO_NO_DISPONIBLE("BANK-503-01");

    private final String codigo;

    CodigoError(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}

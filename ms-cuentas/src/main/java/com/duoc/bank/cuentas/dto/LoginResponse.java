package com.duoc.bank.cuentas.dto;

/**
 * Respuesta exitosa del login: token JWT del servicio, su tipo y vigencia.
 */
public class LoginResponse {

    private String token;
    private String tipo;
    private long expiraEnMinutos;

    public LoginResponse() {
    }

    public LoginResponse(String token, String tipo, long expiraEnMinutos) {
        this.token = token;
        this.tipo = tipo;
        this.expiraEnMinutos = expiraEnMinutos;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getExpiraEnMinutos() {
        return expiraEnMinutos;
    }

    public void setExpiraEnMinutos(long expiraEnMinutos) {
        this.expiraEnMinutos = expiraEnMinutos;
    }
}

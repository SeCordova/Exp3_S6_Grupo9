package com.duoc.bank.operaciones.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo JSON del login del servicio.
 * Las validaciones se resuelven de forma declarativa con Bean Validation.
 */
public class LoginRequest {

    @NotBlank(message = "El campo usuario es obligatorio")
    private String usuario;

    @NotBlank(message = "El campo clave es obligatorio")
    private String clave;

    public LoginRequest() {
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}

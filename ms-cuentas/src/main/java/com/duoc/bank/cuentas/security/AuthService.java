package com.duoc.bank.cuentas.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Credenciales de los usuarios habilitados para el microservicio de cuentas,
 * cargadas desde {@code seguridad.usuarios} con formato
 * {@code usuario:clave:PERM1,PERM2;...}.
 * Fuente de credenciales de demostracion (contexto academico).
 */
@Service
public class AuthService {

    private record Credencial(String clave, List<String> permisos) {
    }

    private final Map<String, Credencial> usuarios = new HashMap<>();

    public AuthService(@Value("${seguridad.usuarios}") String usuariosConfig) {
        for (String entrada : usuariosConfig.split(";")) {
            String[] partes = entrada.split(":");
            if (partes.length == 3) {
                usuarios.put(partes[0], new Credencial(partes[1], Arrays.asList(partes[2].split(","))));
            }
        }
    }

    /**
     * Devuelve los permisos del usuario si las credenciales coinciden,
     * o {@code null} si la autenticacion falla.
     */
    public List<String> autenticar(String usuario, String clave) {
        Credencial credencial = usuarios.get(usuario);
        if (credencial == null || !credencial.clave().equals(clave)) {
            return null;
        }
        return credencial.permisos();
    }
}

package com.duoc.bank.operaciones.controller;

import com.duoc.bank.common.exception.CodigoError;
import com.duoc.bank.common.exception.ErrorResponse;
import com.duoc.bank.operaciones.dto.LoginRequest;
import com.duoc.bank.operaciones.dto.LoginResponse;
import com.duoc.bank.operaciones.security.AuthService;
import com.duoc.bank.operaciones.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Autenticacion del microservicio de operaciones: entrega el token JWT del servicio.
 * Sigue protegido por el API key (primera barrera) y queda exento del
 * interceptor JWT para poder obtener el token.
 */
@RestController
@RequestMapping("/api/operaciones/auth")
@Tag(name = "Autenticacion", description = "Emision del token JWT del servicio de operaciones")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica al usuario del servicio y entrega su token JWT")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        List<String> permisos = authService.autenticar(request.getUsuario(), request.getClave());
        if (permisos == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(CodigoError.CREDENCIALES_INVALIDAS, "Credenciales invalidas"));
        }
        String token = jwtService.generarToken(request.getUsuario(), permisos);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", jwtService.getExpiracionMinutos()));
    }
}

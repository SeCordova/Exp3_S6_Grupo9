package com.duoc.bank.operaciones.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Emite y valida los tokens JWT del servicio (firma HS256).
 * Cada microservicio firma con un secreto propio y graba el claim
 * {@code servicio}, de modo que un token emitido por otro servicio
 * no es valido en este.
 */
@Service
public class JwtService {

    private final SecretKey claveFirma;
    private final String servicio;
    private final long expiracionMinutos;

    public JwtService(
            @Value("${seguridad.jwt-secret}") String secreto,
            @Value("${seguridad.servicio}") String servicio,
            @Value("${seguridad.jwt-expiracion-minutos}") long expiracionMinutos) {
        this.claveFirma = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.servicio = servicio;
        this.expiracionMinutos = expiracionMinutos;
    }

    /** Emite un token para el usuario autenticado, con sus permisos del servicio. */
    public String generarToken(String usuario, List<String> permisos) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario)
                .claim("servicio", servicio)
                .claim("permisos", permisos)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(claveFirma)
                .compact();
    }

    /** Valida firma y expiracion; lanza JwtException si el token no es valido. */
    public Claims validar(String token) {
        return Jwts.parser().verifyWith(claveFirma).build()
                .parseSignedClaims(token).getPayload();
    }

    public String getServicio() {
        return servicio;
    }

    public long getExpiracionMinutos() {
        return expiracionMinutos;
    }
}

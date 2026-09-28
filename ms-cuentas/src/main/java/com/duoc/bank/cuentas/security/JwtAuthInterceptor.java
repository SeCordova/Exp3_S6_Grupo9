package com.duoc.bank.cuentas.security;

import com.duoc.bank.common.exception.CodigoError;
import com.duoc.bank.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Segunda barrera de seguridad del microservicio de cuentas (despues del API key).
 * Exige {@code Authorization: Bearer <token>} con un JWT emitido por este servicio:
 * valida firma, expiracion, el claim {@code servicio} y el permiso que requiere
 * la operacion. Responde 401 si el token falta o no es valido, y 403 si el
 * token es de otro servicio o no tiene el permiso necesario, siempre con
 * {@link ErrorResponse}.
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);
    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith(PREFIJO_BEARER)) {
            return rechazar(response, HttpServletResponse.SC_UNAUTHORIZED, CodigoError.TOKEN_INVALIDO,
                    "Token de autenticacion ausente o con formato invalido");
        }

        Claims claims;
        try {
            claims = jwtService.validar(cabecera.substring(PREFIJO_BEARER.length()));
        } catch (JwtException | IllegalArgumentException e) {
            // Nunca se registra el token recibido, solo la ruta rechazada
            log.warn("evento=token_rechazado metodo={} uri={}", request.getMethod(), request.getRequestURI());
            return rechazar(response, HttpServletResponse.SC_UNAUTHORIZED, CodigoError.TOKEN_INVALIDO,
                    "Token invalido o expirado");
        }

        if (!jwtService.getServicio().equals(claims.get("servicio", String.class))) {
            log.warn("evento=token_de_otro_servicio usuario={} uri={}",
                    claims.getSubject(), request.getRequestURI());
            return rechazar(response, HttpServletResponse.SC_FORBIDDEN, CodigoError.SERVICIO_INCORRECTO,
                    "El token no esta autorizado para este servicio");
        }

        String permisoRequerido = permisoRequerido(request);
        List<?> permisos = claims.get("permisos", List.class);
        if (permisoRequerido != null && (permisos == null || !permisos.contains(permisoRequerido))) {
            log.warn("evento=permiso_denegado usuario={} permiso={} uri={}",
                    claims.getSubject(), permisoRequerido, request.getRequestURI());
            return rechazar(response, HttpServletResponse.SC_FORBIDDEN, CodigoError.ACCESO_DENEGADO,
                    "El usuario no tiene permiso para esta operacion");
        }
        return true;
    }

    /** Permiso que exige cada operacion del microservicio de cuentas. */
    private String permisoRequerido(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/cuentas/stats")) {
            return "REPORTES";
        }
        return "CONSULTA";
    }

    private boolean rechazar(HttpServletResponse response, int estado, CodigoError codigo, String mensaje)
            throws Exception {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(codigo, mensaje)));
        return false;
    }
}

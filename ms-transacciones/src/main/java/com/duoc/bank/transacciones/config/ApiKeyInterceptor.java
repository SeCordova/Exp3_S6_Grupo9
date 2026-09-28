package com.duoc.bank.transacciones.config;

import com.duoc.bank.common.exception.CodigoError;
import com.duoc.bank.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * Interceptor de seguridad del microservicio (sin spring-security).
 * Exige el header {@code X-API-KEY} con la clave configurada en
 * {@code seguridad.api-key}; si falta o no coincide responde 401
 * con el formato uniforme {@link ErrorResponse}.
 */
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyInterceptor.class);
    private static final String HEADER_API_KEY = "X-API-KEY";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${seguridad.api-key}")
    private String apiKeyEsperada;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String apiKeyRecibida = request.getHeader(HEADER_API_KEY);
        if (apiKeyRecibida == null || !apiKeyRecibida.equals(apiKeyEsperada)) {
            // Nunca se registra el valor de la clave recibida, solo la ruta rechazada
            log.warn("evento=api_key_rechazada metodo={} uri={}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse(CodigoError.API_KEY_INVALIDA, "API key invalida o ausente")));
            return false;
        }
        return true;
    }
}

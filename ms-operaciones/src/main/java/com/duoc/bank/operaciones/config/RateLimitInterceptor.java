package com.duoc.bank.operaciones.config;

import com.duoc.bank.common.exception.CodigoError;
import com.duoc.bank.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Limite de solicitudes del servicio (rate limiting por cliente).
 * Cada cliente (identificado por su {@code X-API-KEY}, o por IP si no envia
 * clave) dispone de un bucket de tokens que se recarga de forma continua;
 * al agotarse se responde 429 con el formato uniforme {@link ErrorResponse}
 * y el header {@code Retry-After}. Protege al servicio ante sobrecarga.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private static final String HEADER_API_KEY = "X-API-KEY";
    private static final String HEADER_RETRY_AFTER = "Retry-After";

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final long capacidad;
    private final Duration ventana;

    public RateLimitInterceptor(@Value("${ratelimit.capacidad}") long capacidad,
                                @Value("${ratelimit.ventana-segundos}") long ventanaSegundos) {
        this.capacidad = capacidad;
        this.ventana = Duration.ofSeconds(ventanaSegundos);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Bucket bucket = buckets.computeIfAbsent(identidadCliente(request), k -> nuevoBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return true;
        }
        long esperaSegundos = Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
        log.warn("evento=rate_limit_excedido metodo={} uri={}", request.getMethod(), request.getRequestURI());
        response.setStatus(429);
        response.setHeader(HEADER_RETRY_AFTER, String.valueOf(esperaSegundos));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorResponse(CodigoError.LIMITE_EXCEDIDO, "Limite de solicitudes excedido; reintente mas tarde")));
        return false;
    }

    private Bucket nuevoBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacidad)
                        .refillGreedy(capacidad, ventana)
                        .build())
                .build();
    }

    /** La API key identifica al cliente del servicio; sin clave se usa la IP remota. */
    private String identidadCliente(HttpServletRequest request) {
        String apiKey = request.getHeader(HEADER_API_KEY);
        return (apiKey != null && !apiKey.isBlank()) ? apiKey : request.getRemoteAddr();
    }
}

package com.duoc.bank.operaciones.client;

import com.duoc.bank.common.exception.CuentaNoEncontradaException;
import com.duoc.bank.common.exception.SaldoInsuficienteException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Configuracion exclusiva del cliente Feign hacia ms-cuentas.
 * No se anota con {@code @Configuration} a proposito: al estar referenciada solo
 * desde {@link CuentasClient}, sus beans aplican unicamente a este cliente y no
 * al resto del contexto de Spring.
 *
 * Aporta dos piezas: el header {@code X-API-KEY} en todas las llamadas salientes
 * y la traduccion de los errores HTTP de ms-cuentas a las excepciones de negocio
 * compartidas (404 -> cuenta no encontrada, 409 -> saldo insuficiente).
 */
public class CuentasClientConfig {

    private static final String HEADER_API_KEY = "X-API-KEY";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${integracion.cuentas.api-key}")
    private String apiKey;

    /** Agrega la API key interna a todas las llamadas hacia ms-cuentas. */
    @Bean
    public RequestInterceptor apiKeyRequestInterceptor() {
        return template -> template.header(HEADER_API_KEY, apiKey);
    }

    /** Traduce los codigos de error del contrato interno de ms-cuentas. */
    @Bean
    public ErrorDecoder errorDecoder() {
        ErrorDecoder porDefecto = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new CuentaNoEncontradaException(extraerMensaje(response, "Cuenta no encontrada"));
            }
            if (response.status() == 409) {
                return new SaldoInsuficienteException(extraerMensaje(response, "Saldo insuficiente"));
            }
            return porDefecto.decode(methodKey, response);
        };
    }

    /** Extrae el campo {@code error} del cuerpo de error uniforme de ms-cuentas. */
    private String extraerMensaje(Response response, String mensajeGenerico) {
        if (response.body() == null) {
            return mensajeGenerico;
        }
        try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
            JsonNode error = objectMapper.readTree(reader).get("error");
            return (error != null && !error.isNull()) ? error.asText() : mensajeGenerico;
        } catch (IOException e) {
            return mensajeGenerico;
        }
    }
}

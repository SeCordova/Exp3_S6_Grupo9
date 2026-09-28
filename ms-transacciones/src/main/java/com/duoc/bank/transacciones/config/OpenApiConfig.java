package com.duoc.bank.transacciones.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contrato OpenAPI del microservicio de transacciones: documenta los dos
 * esquemas de seguridad del servicio (API key en header y JWT Bearer) y deja
 * el contrato explorable en {@code /swagger-ui.html} y {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Transacciones - Banco XYZ")
                        .description("API del microservicio de transacciones: historial paginado, "
                                + "anomalias, movimientos recientes y estadisticas.")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("apiKeyAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-KEY"))
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("apiKeyAuth")
                        .addList("bearerAuth"));
    }
}

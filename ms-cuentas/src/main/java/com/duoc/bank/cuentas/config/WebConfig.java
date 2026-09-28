package com.duoc.bank.cuentas.config;

import com.duoc.bank.cuentas.security.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra las barreras transversales del microservicio de cuentas, en orden
 * de ejecucion:
 * 1) {@link ApiKeyInterceptor} para todos los endpoints (excluye {@code /error}
 *    y la documentacion OpenAPI/Swagger).
 * 2) {@link RateLimitInterceptor} para la API: tope de solicitudes por cliente.
 * 3) {@link JwtAuthInterceptor} para la API, excluyendo el login (que emite el
 *    token) y los endpoints internos {@code /api/internal/**} (que van solo con
 *    API key, para consumo inter-servicio).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final JwtAuthInterceptor jwtAuthInterceptor;

    public WebConfig(ApiKeyInterceptor apiKeyInterceptor, RateLimitInterceptor rateLimitInterceptor,
                     JwtAuthInterceptor jwtAuthInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**");
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/cuentas/auth/**", "/api/internal/**", "/error");
    }
}

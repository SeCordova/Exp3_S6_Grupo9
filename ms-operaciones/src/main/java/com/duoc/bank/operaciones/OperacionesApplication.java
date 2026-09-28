package com.duoc.bank.operaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Microservicio de operaciones de Banco XYZ.
 * Dueno de la tabla legada {@code operaciones_anuales}: orquesta el retiro de
 * dinero invocando a ms-cuentas via OpenFeign (descubrimiento Eureka) y registra
 * la operacion anual local. Es el servicio que demuestra la integracion entre
 * microservicios del ecosistema.
 */
@SpringBootApplication(scanBasePackages = "com.duoc.bank")
@EntityScan("com.duoc.bank.operaciones.model")
@EnableJpaRepositories("com.duoc.bank.operaciones.repository")
@EnableFeignClients
public class OperacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperacionesApplication.class, args);
    }
}

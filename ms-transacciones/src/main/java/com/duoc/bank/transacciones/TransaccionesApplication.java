package com.duoc.bank.transacciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Punto de entrada del microservicio de transacciones.
 * El escaneo de componentes cubre {@code com.duoc.bank} para incorporar los
 * componentes compartidos de bank-common (por ejemplo RequestIdFilter);
 * las entidades y repositorios JPA se limitan a este microservicio,
 * que es el dueno de la tabla {@code transacciones}.
 */
@SpringBootApplication(scanBasePackages = "com.duoc.bank")
@EntityScan("com.duoc.bank.transacciones.model")
@EnableJpaRepositories("com.duoc.bank.transacciones.repository")
public class TransaccionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransaccionesApplication.class, args);
    }
}

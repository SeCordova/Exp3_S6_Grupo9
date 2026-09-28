package com.duoc.bank.cuentas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Microservicio de cuentas: dueno de la tabla legada {@code cuentas}
 * (saldos, listado paginado y cargos sobre el saldo vigente).
 * El escaneo de componentes cubre {@code com.duoc.bank} para incluir los
 * componentes compartidos de bank-common (por ejemplo {@code RequestIdFilter}).
 */
@SpringBootApplication(scanBasePackages = "com.duoc.bank")
@EntityScan("com.duoc.bank.cuentas.model")
@EnableJpaRepositories("com.duoc.bank.cuentas.repository")
public class CuentasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CuentasApplication.class, args);
    }
}

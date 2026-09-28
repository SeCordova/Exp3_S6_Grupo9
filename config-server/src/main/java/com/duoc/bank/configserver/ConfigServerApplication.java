package com.duoc.bank.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Servidor centralizado de configuracion del ecosistema bancario.
 * Expone las configuraciones de todos los microservicios (perfil native,
 * repo embebido en classpath:/config-repo) en el puerto 8888.
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

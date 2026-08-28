package mx.ine.gestiona_t;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal de la aplicacion Gestiona-T Backend Core.
 * 
 * Instituto Nacional Electoral (INE)
 * Plataforma de Gestion de Reclutamiento y Seleccion
 * 
 * @author Arquitectura de Software - Reto Dos INE
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "mx.ine.gestiona_t.modules")
@EnableAsync
public class GestionaTApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionaTApplication.class, args);
    }
}
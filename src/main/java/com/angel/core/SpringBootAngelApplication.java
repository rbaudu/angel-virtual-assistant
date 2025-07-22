package com.angel.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Point d'entrée principal pour l'application Angel Virtual Assistant avec Spring Boot.
 * Utilise les fichiers de configuration existants dans le dossier config/.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.angel")
public class SpringBootAngelApplication {

    public static void main(String[] args) {
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        
        // Configurer Spring Boot pour utiliser les fichiers de configuration externes
        System.setProperty("spring.config.location", "optional:classpath:/,optional:classpath:/config/");
        System.setProperty("spring.config.name", "application");
        
        SpringApplication.run(SpringBootAngelApplication.class, args);
    }
}

/**
 * Composant qui démarre l'application Angel après l'initialisation de Spring Boot.
 */
@Component
class AngelApplicationStarter {
    
    @Autowired(required = false)
    private AngelApplication angelApplication;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // Démarrer l'application Angel après que Spring Boot soit prêt
        if (angelApplication != null) {
            angelApplication.start();
        }
    }
}
package com.angel.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    @Autowired
    private AngelApplication angelApplication;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            LOGGER.log(Level.INFO, "Spring Boot prêt, démarrage d'Angel...");
            angelApplication.start();
            LOGGER.log(Level.INFO, "Angel démarré avec succès via Spring Boot");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du démarrage d'Angel", e);
            throw new RuntimeException("Échec du démarrage d'Angel", e);
        }
   }
    private static final Logger LOGGER = Logger.getLogger(AngelApplicationStarter.class.getName());
}
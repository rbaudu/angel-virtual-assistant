package com.angel.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principale Spring Boot pour l'application Angel.
 * Point d'entrée de l'application qui démarre le contexte Spring.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.angel")
public class AngelApplicationMain {
    
    public static void main(String[] args) {
        SpringApplication.run(AngelApplicationMain.class, args);
    }
}
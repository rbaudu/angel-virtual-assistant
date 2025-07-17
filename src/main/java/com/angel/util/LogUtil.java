package com.angel.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.IOException;

/**
 * Utilitaires pour la gestion des logs de l'application.
 */
public class LogUtil {

    private static boolean configured = false;

    /**
     * Configure le système de logging de l'application.
     */
    public static void configureLogging() {
        if (configured) {
            return;
        }

        try {
            // Configuration du logger root
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.INFO);
            
            // Supprimer les handlers par défaut
            rootLogger.getHandlers()[0].setLevel(Level.WARNING);
            
            // Créer un handler pour la console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(consoleHandler);
            
            // Créer un handler pour les fichiers
            try {
                FileHandler fileHandler = new FileHandler("./logs/angel.log", true);
                fileHandler.setLevel(Level.ALL);
                fileHandler.setFormatter(new SimpleFormatter());
                rootLogger.addHandler(fileHandler);
            } catch (IOException e) {
                System.err.println("Impossible de créer le fichier de log: " + e.getMessage());
            }
            
            configured = true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des logs: " + e.getMessage());
        }
    }

    /**
     * Obtient un logger configuré pour une classe donnée.
     * 
     * @param clazz La classe pour laquelle obtenir le logger
     * @return Le logger configuré
     */
    public static Logger getLogger(Class<?> clazz) {
        if (!configured) {
            configureLogging();
        }
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Obtient un logger configuré pour un nom donné.
     * 
     * @param name Le nom du logger
     * @return Le logger configuré
     */
    public static Logger getLogger(String name) {
        if (!configured) {
            configureLogging();
        }
        return Logger.getLogger(name);
    }
}
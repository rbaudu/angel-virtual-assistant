package com.angel.voice;

import com.angel.config.ConfigManager;
import com.angel.util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Détecteur de mot-clé qui écoute en permanence le microphone
 * pour détecter quand l'utilisateur dit "Angel".
 */
public class WakeWordDetector {

    private static final Logger LOGGER = LogUtil.getLogger(WakeWordDetector.class);
    
    private final ConfigManager configManager;
    private final String wakeWord;
    private boolean isListening;
    private final ExecutorService executorService;
    private Consumer<Void> wakeWordCallback;
    
    /**
     * Constructeur avec injection du gestionnaire de configuration.
     * 
     * @param configManager Le gestionnaire de configuration
     */
    public WakeWordDetector(ConfigManager configManager) {
        this.configManager = configManager;
        this.wakeWord = configManager.getString("system.wakeWord", "Angel");
        this.isListening = false;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Démarre l'écoute du mot-clé.
     * 
     * @param callback Action à exécuter lorsque le mot-clé est détecté
     */
    public void startListening(Consumer<Void> callback) {
        if (isListening) {
            LOGGER.log(Level.WARNING, "Le détecteur est déjà en écoute");
            return;
        }
        
        this.wakeWordCallback = callback;
        this.isListening = true;
        
        LOGGER.log(Level.INFO, "Démarrage de l'écoute du mot-clé: {0}", wakeWord);
        
        executorService.submit(this::listenForWakeWord);
    }
    
    /**
     * Arrête l'écoute du mot-clé.
     */
    public void stopListening() {
        if (!isListening) {
            return;
        }
        
        isListening = false;
        LOGGER.log(Level.INFO, "Arrêt de l'écoute du mot-clé");
        
        // Ne pas arrêter l'ExecutorService car on pourrait vouloir relancer l'écoute plus tard
    }
    
    /**
     * Libère les ressources lors de la fermeture de l'application.
     */
    public void shutdown() {
        stopListening();
        executorService.shutdown();
    }
    
    /**
     * Méthode principale d'écoute qui tourne en continu.
     * Dans une implémentation réelle, cette méthode utiliserait une bibliothèque
     * de reconnaissance vocale pour détecter le mot-clé.
     */
    private void listenForWakeWord() {
        // Note: Ceci est une simulation pour l'exemple.
        // Dans une implémentation réelle, on utiliserait une bibliothèque comme
        // Porcupine, Snowboy, ou une API de reconnaissance vocale.
        
        LOGGER.log(Level.INFO, "Écoute active du mot-clé: {0}", wakeWord);
        
        while (isListening) {
            try {
                // Simuler le traitement audio
                Thread.sleep(100);
                
                // Dans une vraie implémentation, on traiterait ici les données audio
                // pour détecter le mot-clé
                
                // Pour les besoins de l'exemple, simulons une détection aléatoire
                // (dans une vraie implémentation, cette partie serait remplacée par
                // l'algorithme de détection du mot-clé)
                if (Math.random() < 0.00005 && wakeWordCallback != null) { // Très faible probabilité pour l'exemple
                    LOGGER.log(Level.INFO, "Mot-clé {0} détecté!", wakeWord);
                    
                    // Exécuter le callback sur un autre thread pour ne pas bloquer l'écoute
                    new Thread(() -> wakeWordCallback.accept(null)).start();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interruption du thread d'écoute", e);
                break;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'écoute du mot-clé", e);
                // Courte pause avant de réessayer pour éviter de saturer le CPU en cas d'erreur
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        LOGGER.log(Level.INFO, "Arrêt de l'écoute du mot-clé");
    }
    
    /**
     * Vérifie si le détecteur est actuellement en écoute.
     * 
     * @return true si le détecteur est en écoute, false sinon
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Obtient le mot-clé configuré.
     * 
     * @return Le mot-clé à détecter
     */
    public String getWakeWord() {
        return wakeWord;
    }
}
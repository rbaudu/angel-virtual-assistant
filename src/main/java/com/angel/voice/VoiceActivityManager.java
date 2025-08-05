package com.angel.voice;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.angel.avatar.WebSocketService;
import com.angel.config.ConfigManager;
import com.angel.util.LogUtil;

/**
 * Gestionnaire d'activité vocale pour Angel.
 * Gère l'inactivité, l'écoute continue et l'état de l'interface.
 * 
 * Fichier : src/main/java/com/angel/voice/VoiceActivityManager.java
 */
@Component
@EnableScheduling
public class VoiceActivityManager {

    private static final Logger LOGGER = LogUtil.getLogger(VoiceActivityManager.class);
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired
    private WebSocketService webSocketService;
    
    private LocalDateTime lastActivity;
    private boolean isListening = false;
    private boolean isActive = false;
    private boolean controlsVisible = false;
    private boolean isDarkMode = false;
    
    public VoiceActivityManager() {
        this.lastActivity = LocalDateTime.now();
        this.controlsVisible = false; // Contrôles cachés par défaut
    }
    
    /**
     * Démarre l'écoute continue automatique.
     */
    public CompletableFuture<Void> startContinuousListening() {
        return CompletableFuture.runAsync(() -> {
            try {
                LOGGER.log(Level.INFO, "🎤 Démarrage écoute continue automatique");
                
                // Envoyer commande de démarrage automatique
                sendVoiceCommand("START_CONTINUOUS_LISTENING", null);
                
                this.isListening = true;
                this.lastActivity = LocalDateTime.now();
                
                // Sortir du mode sombre si nécessaire
                if (isDarkMode) {
                    exitDarkMode();
                }
                
                LOGGER.log(Level.INFO, "✅ Écoute continue activée");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "❌ Erreur démarrage écoute continue", e);
            }
        });
    }
    
    /**
     * Marque une activité utilisateur.
     */
    public void markActivity() {
        this.lastActivity = LocalDateTime.now();
        this.isActive = true;
        
        // Sortir du mode sombre si nécessaire
        if (isDarkMode) {
            exitDarkMode();
        }
        
        LOGGER.log(Level.FINE, "👤 Activité utilisateur marquée");
    }
    
    /**
     * Gère l'activation par mot-clé.
     */
    public void handleWakeWordActivation(String wakeWord) {
        LOGGER.log(Level.INFO, "🎯 Activation détectée: {0}", wakeWord);
        
        markActivity();
        
        // Activer l'avatar s'il était caché
        if (isDarkMode) {
            exitDarkMode();
        }
        
        // Envoyer confirmation d'activation
        sendVoiceCommand("WAKE_WORD_CONFIRMED", wakeWord);
    }
    
    /**
     * Traite les commandes de contrôle d'interface.
     */
    public boolean handleUICommand(String command) {
        String lowerCommand = command.toLowerCase().trim();
        
        // Commandes d'affichage/masquage des contrôles
        String showConfigCommands = configManager.getString("voice.command.show.config", 
            "affiche la configuration,montre la configuration,affiche les contrôles");
        String hideConfigCommands = configManager.getString("voice.command.hide.config", 
            "cache la configuration,masque la configuration,cache les contrôles");
        
        if (containsAnyKeyword(lowerCommand, showConfigCommands.split(","))) {
            showControls();
            return true;
        }
        
        if (containsAnyKeyword(lowerCommand, hideConfigCommands.split(","))) {
            hideControls();
            return true;
        }
        
        // Commande d'arrêt
        String stopCommands = configManager.getString("voice.command.stop", 
            "arrête,stop,silence,tais-toi");
        if (containsAnyKeyword(lowerCommand, stopCommands.split(","))) {
            handleStopCommand();
            return true;
        }
        
        return false;
    }
    
    /**
     * Affiche les contrôles de configuration.
     */
    private void showControls() {
        LOGGER.log(Level.INFO, "👁️ Affichage des contrôles");
        this.controlsVisible = true;
        markActivity();
        
        sendVoiceCommand("SHOW_CONTROLS", null);
    }
    
    /**
     * Cache les contrôles de configuration.
     */
    private void hideControls() {
        LOGGER.log(Level.INFO, "🙈 Masquage des contrôles");
        this.controlsVisible = false;
        markActivity();
        
        sendVoiceCommand("HIDE_CONTROLS", null);
    }
    
    /**
     * Gère la commande d'arrêt.
     */
    private void handleStopCommand() {
        LOGGER.log(Level.INFO, "⏹️ Commande d'arrêt reçue");
        
        // Arrêter la synthèse vocale en cours
        sendVoiceCommand("STOP_SPEAKING", null);
        
        // Retourner en mode écoute
        this.isActive = false;
        markActivity();
    }
    
    /**
     * Vérifie périodiquement l'inactivité.
     */
    @Scheduled(fixedRate = 30000) // Toutes les 30 secondes
    public void checkInactivity() {
        if (lastActivity == null) {
            return;
        }
        
        long minutesSinceActivity = ChronoUnit.MINUTES.between(lastActivity, LocalDateTime.now());
        int inactivityTimeout = configManager.getInt("voice.inactivity.timeout.minutes", 5);
        
        if (minutesSinceActivity >= inactivityTimeout && !isDarkMode) {
            LOGGER.log(Level.INFO, "😴 Inactivité détectée ({0} min), passage en mode sombre", minutesSinceActivity);
            enterDarkMode();
        }
    }
    
    /**
     * Passe en mode sombre (écran sombre).
     */
    private void enterDarkMode() {
        this.isDarkMode = true;
        this.isActive = false;
        
        boolean hideAvatar = configManager.getBoolean("voice.inactivity.avatar.hide", true);
        boolean continueLisetning = configManager.getBoolean("voice.inactivity.listening.continue", true);
        
        // Envoyer commande de mode sombre
        String darkModeData = String.format(
            "{\"hideAvatar\":%b,\"continueLisetning\":%b}", 
            hideAvatar, continueLisetning
        );
        
        sendVoiceCommand("ENTER_DARK_MODE", darkModeData);
        
        LOGGER.log(Level.INFO, "🌙 Mode sombre activé");
    }
    
    /**
     * Sort du mode sombre.
     */
    private void exitDarkMode() {
        if (!isDarkMode) return;
        
        this.isDarkMode = false;
        this.isActive = true;
        
        sendVoiceCommand("EXIT_DARK_MODE", null);
        
        LOGGER.log(Level.INFO, "☀️ Sortie du mode sombre");
    }
    
    /**
     * Envoie une commande vocale via WebSocket.
     * Utilise la méthode existante de WebSocketService.
     */
    private void sendVoiceCommand(String command, String data) {
        try {
            String message = String.format(
                "{\"type\":\"%s\",\"data\":\"%s\",\"timestamp\":%d}",
                command,
                data != null ? data.replace("\"", "\\\"") : "",
                System.currentTimeMillis()
            );
            
            // Utiliser la méthode qui existe dans votre WebSocketService
            if (webSocketService.hasConnectedAvatarSessions()) {
                webSocketService.getActiveSessions().values().forEach(session -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Erreur envoi commande vocale à une session", e);
                    }
                });
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur envoi commande vocale", e);
        }
    }
    
    /**
     * Vérifie si le texte contient un des mots-clés.
     */
    private boolean containsAnyKeyword(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    // === GETTERS ET SETTERS ===
    
    public boolean isListening() {
        return isListening;
    }
    
    public void setListening(boolean listening) {
        this.isListening = listening;
        if (listening) {
            markActivity();
        }
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean areControlsVisible() {
        return controlsVisible;
    }
    
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
}
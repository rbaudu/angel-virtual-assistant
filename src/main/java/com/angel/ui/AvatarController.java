package com.angel.ui;

import com.angel.config.ConfigManager;
import com.angel.intelligence.proposals.Proposal;
import com.angel.util.LogUtil;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur pour l'avatar qui sert d'interface visuelle avec l'utilisateur.
 * Gère l'affichage, l'animation et les interactions de l'avatar.
 */
public class AvatarController {

    private static final Logger LOGGER = LogUtil.getLogger(AvatarController.class);
    
    private final ConfigManager configManager;
    private boolean isVisible;
    private String currentMood;
    private String currentText;
    
    /**
     * Constructeur avec injection du gestionnaire de configuration.
     * 
     * @param configManager Le gestionnaire de configuration
     */
    public AvatarController(ConfigManager configManager) {
        this.configManager = configManager;
        this.isVisible = false;
        this.currentMood = configManager.getString("avatar.defaultMood", "neutral");
        this.currentText = "";
    }
    
    /**
     * Affiche l'avatar avec une proposition.
     * 
     * @param proposal La proposition à présenter
     * @return CompletableFuture qui se termine lorsque l'avatar a fini de présenter
     */
    public CompletableFuture<Void> displayProposal(Proposal proposal) {
        if (!configManager.getBoolean("avatar.enabled", true)) {
            LOGGER.log(Level.INFO, "Avatar désactivé, proposition non affichée");
            return CompletableFuture.completedFuture(null);
        }
        
        // Génération du texte pour l'avatar
        String avatarPrompt = proposal.generateAvatarPrompt();
        String proposalContent = proposal.getContent();
        
        // Logique d'affichage (simulation car nous n'avons pas l'UI réelle ici)
        LOGGER.log(Level.INFO, "Affichage de l'avatar avec la proposition: {0}", proposal.getTitle());
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Rendre l'avatar visible
                showAvatar();
                setCurrentText(avatarPrompt);
                
                // Simuler l'affichage du texte (l'avatar "parle")
                Thread.sleep(1000); // Temps de transition
                
                // Afficher le contenu de la proposition
                setCurrentText(proposalContent);
                
                // Maintenir l'avatar visible pendant un certain temps
                long displayTime = configManager.getLong("avatar.displayTime", 30000);
                Thread.sleep(displayTime);
                
                // Cacher l'avatar
                hideAvatar();
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interruption lors de l'affichage de l'avatar", e);
            }
        });
    }
    
    /**
     * Affiche l'avatar avec un message texte simple.
     * 
     * @param text Le texte à afficher
     * @param mood L'humeur de l'avatar (affecte son expression)
     * @param durationMs Durée d'affichage en millisecondes
     * @return CompletableFuture qui se termine lorsque l'avatar a fini de présenter
     */
    public CompletableFuture<Void> displayMessage(String text, String mood, long durationMs) {
        if (!configManager.getBoolean("avatar.enabled", true)) {
            LOGGER.log(Level.INFO, "Avatar désactivé, message non affiché");
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                showAvatar();
                setCurrentMood(mood);
                setCurrentText(text);
                
                Thread.sleep(durationMs);
                
                hideAvatar();
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interruption lors de l'affichage du message", e);
            }
        });
    }
    
    /**
     * Affiche l'avatar et déclenche son animation d'apparition.
     */
    private void showAvatar() {
        if (!isVisible) {
            isVisible = true;
            LOGGER.log(Level.FINE, "Avatar affiché");
            
            // Ici, on déclencherait l'animation d'apparition
            // Par exemple, en envoyant un événement JavaScript au frontend
        }
    }
    
    /**
     * Cache l'avatar et déclenche son animation de disparition.
     */
    private void hideAvatar() {
        if (isVisible) {
            isVisible = false;
            LOGGER.log(Level.FINE, "Avatar masqué");
            
            // Ici, on déclencherait l'animation de disparition
        }
    }
    
    /**
     * Définit l'humeur actuelle de l'avatar (affecte son expression faciale).
     * 
     * @param mood L'humeur à définir (ex: "happy", "sad", "neutral", etc.)
     */
    private void setCurrentMood(String mood) {
        this.currentMood = mood;
        LOGGER.log(Level.FINE, "Humeur de l'avatar définie à: {0}", mood);
        
        // Ici, on mettrait à jour l'expression faciale de l'avatar
    }
    
    /**
     * Définit le texte que l'avatar est en train de dire.
     * 
     * @param text Le texte à afficher
     */
    private void setCurrentText(String text) {
        this.currentText = text;
        LOGGER.log(Level.FINE, "Texte de l'avatar défini: {0}", text);
        
        // Ici, on mettrait à jour le texte affiché par l'avatar
    }
    
    /**
     * Vérifie si l'avatar est actuellement visible.
     * 
     * @return true si l'avatar est visible, false sinon
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Obtient l'humeur actuelle de l'avatar.
     * 
     * @return L'humeur actuelle
     */
    public String getCurrentMood() {
        return currentMood;
    }
    
    /**
     * Obtient le texte actuellement affiché par l'avatar.
     * 
     * @return Le texte actuel
     */
    public String getCurrentText() {
        return currentText;
    }
}
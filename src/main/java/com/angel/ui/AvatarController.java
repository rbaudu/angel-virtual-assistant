package com.angel.ui;

import com.angel.avatar.AvatarManager;
import com.angel.config.ConfigManager;
import com.angel.intelligence.proposals.Proposal;
import com.angel.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur pour l'avatar qui sert d'interface visuelle avec l'utilisateur.
 * Intégré avec AvatarManager pour gérer l'affichage, l'animation et les interactions de l'avatar.
 */
@Controller
public class AvatarController {

    private static final Logger LOGGER = LogUtil.getLogger(AvatarController.class);
    
    private final ConfigManager configManager;
    private final AvatarManager avatarManager;
    private boolean isInitialized = false;
    
    /**
     * Constructeur avec injection des dépendances.
     * 
     * @param configManager Le gestionnaire de configuration
     * @param avatarManager Le gestionnaire d'avatar intégré
     */
    @Autowired
    public AvatarController(ConfigManager configManager, AvatarManager avatarManager) {
        this.configManager = configManager;
        this.avatarManager = avatarManager;
    }
    
    /**
     * Initialise l'avatar si ce n'est pas déjà fait.
     * 
     * @return CompletableFuture qui se termine lorsque l'avatar est initialisé
     */
    public CompletableFuture<Void> initialize() {
        if (isInitialized) {
            return CompletableFuture.completedFuture(null);
        }
        
        if (!configManager.getBoolean("avatar.enabled", true)) {
            LOGGER.log(Level.INFO, "Avatar désactivé dans la configuration");
            return CompletableFuture.completedFuture(null);
        }
        
        return avatarManager.initializeAvatar()
            .thenRun(() -> {
                isInitialized = true;
                LOGGER.log(Level.INFO, "AvatarController initialisé avec succès");
            })
            .exceptionally(throwable -> {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation de l'AvatarController", throwable);
                return null;
            });
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
        
        // S'assurer que l'avatar est initialisé
        return initialize()
            .thenCompose(v -> {
                // Générer le contenu pour l'avatar
                String avatarPrompt = proposal.generateAvatarPrompt();
                String proposalContent = proposal.getContent();
                
                LOGGER.log(Level.INFO, "Affichage de l'avatar avec la proposition: {0}", proposal.getTitle());
                
                // Analyser l'émotion appropriée pour la proposition
                String emotion = determineEmotionFromProposal(proposal);
                
                // Afficher l'avatar
                return avatarManager.show()
                    .thenCompose(v2 -> {
                        // Faire parler l'avatar avec le prompt d'introduction
                        return avatarManager.speak(avatarPrompt, emotion);
                    })
                    .thenCompose(v3 -> {
                        // Attendre un peu puis présenter le contenu principal
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return avatarManager.speak(proposalContent, emotion);
                    })
                    .thenCompose(v4 -> {
                        // Maintenir l'avatar visible pendant la durée configurée
                        long displayTime = configManager.getLong("avatar.displayTime", 30000);
                        return CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(displayTime);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    })
                    .thenRun(() -> {
                        // Cacher l'avatar après présentation
                        avatarManager.hide();
                    });
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
        
        return initialize()
            .thenCompose(v -> {
                LOGGER.log(Level.INFO, "Affichage d'un message par l'avatar: {0}", text);
                
                return avatarManager.show()
                    .thenCompose(v2 -> {
                        // Définir l'émotion avant de parler
                        avatarManager.setEmotion(mood, 0.7);
                        return avatarManager.speak(text, mood);
                    })
                    .thenCompose(v3 -> {
                        // Maintenir l'avatar visible pendant la durée spécifiée
                        return CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(durationMs);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    })
                    .thenRun(() -> {
                        avatarManager.hide();
                    });
            });
    }
    
    /**
     * Salue l'utilisateur avec un geste d'accueil.
     * 
     * @return CompletableFuture qui se termine lorsque le salut est terminé
     */
    public CompletableFuture<Void> greetUser() {
        return initialize()
            .thenCompose(v -> {
                String greetingMessage = configManager.getString("avatar.greetingMessage", 
                    "Bonjour ! Je suis votre assistante virtuelle Angel. Comment puis-je vous aider aujourd'hui ?");
                
                return avatarManager.show()
                    .thenRun(() -> {
                        // Effectuer un geste de salut
                        avatarManager.playGesture("wave");
                        avatarManager.setEmotion("happy", 0.8);
                    })
                    .thenCompose(v2 -> avatarManager.speak(greetingMessage, "happy"));
            });
    }
    
    /**
     * Fait dire au revoir à l'avatar.
     * 
     * @return CompletableFuture qui se termine lorsque les adieux sont terminés
     */
    public CompletableFuture<Void> sayGoodbye() {
        if (!isInitialized) {
            return CompletableFuture.completedFuture(null);
        }
        
        String goodbyeMessage = configManager.getString("avatar.goodbyeMessage", 
            "Au revoir ! N'hésitez pas à revenir si vous avez besoin d'aide.");
        
        return avatarManager.show()
            .thenRun(() -> {
                avatarManager.playGesture("goodbye_wave");
                avatarManager.setEmotion("friendly", 0.6);
            })
            .thenCompose(v -> avatarManager.speak(goodbyeMessage, "friendly"))
            .thenRun(() -> {
                try {
                    Thread.sleep(2000); // Attendre 2 secondes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                avatarManager.hide();
            });
    }
    
    /**
     * Change l'apparence de l'avatar.
     * 
     * @param gender Genre de l'avatar
     * @param age Âge de l'avatar
     * @param style Style vestimentaire
     * @return CompletableFuture qui se termine lorsque le changement est effectué
     */
    public CompletableFuture<Void> changeAppearance(String gender, int age, String style) {
        return initialize()
            .thenCompose(v -> avatarManager.changeAppearance(gender, age, style));
    }
    
    /**
     * Force l'affichage immédiat de l'avatar.
     */
    public void showAvatar() {
        if (isInitialized) {
            avatarManager.show();
        }
    }
    
    /**
     * Force le masquage immédiat de l'avatar.
     */
    public void hideAvatar() {
        if (isInitialized) {
            avatarManager.hide();
        }
    }
    
    /**
     * Détermine l'émotion appropriée selon le type de proposition.
     * 
     * @param proposal La proposition à analyser
     * @return L'émotion recommandée
     */
    private String determineEmotionFromProposal(Proposal proposal) {
        String type = proposal.getType();
        if (type == null) return "neutral";
        
        switch (type.toLowerCase()) {
            case "suggestion":
            case "improvement":
                return "thoughtful";
            case "alert":
            case "warning":
                return "concerned";
            case "achievement":
            case "success":
                return "happy";
            case "information":
            default:
                return "neutral";
        }
    }
    
    /**
     * Vérifie si l'avatar est actuellement initialisé et actif.
     * 
     * @return true si l'avatar est prêt à être utilisé
     */
    public boolean isReady() {
        return isInitialized && avatarManager.isActive();
    }
    
    /**
     * Obtient la configuration actuelle de l'avatar.
     * 
     * @return La configuration de l'avatar
     */
    public Object getCurrentConfig() {
        return avatarManager.getCurrentConfig();
    }
}

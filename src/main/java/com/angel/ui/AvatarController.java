package com.angel.ui;

import com.angel.avatar.AvatarConfig;
import com.angel.avatar.AvatarManager;
import com.angel.avatar.WebSocketService;
import com.angel.config.ConfigManager;
import com.angel.intelligence.proposals.Proposal;
import com.angel.util.LogUtil;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur pour l'interface utilisateur de l'avatar.
 * Version corrigée compatible avec l'API existante.
 */
@Component
public class AvatarController {
    
    private static final Logger LOGGER = LogUtil.getLogger(AvatarController.class);
    
    private final ConfigManager configManager;
    private final AvatarManager avatarManager;
    private final WebSocketService webSocketService;
    private boolean isReady = false;
    
    public AvatarController(ConfigManager configManager, 
                           AvatarManager avatarManager,
                           WebSocketService webSocketService) {
        this.configManager = configManager;
        this.avatarManager = avatarManager;
        this.webSocketService = webSocketService;
        
        // Initialiser automatiquement l'avatar
        initialize();
    }
    
    /**
     * Initialise le contrôleur d'avatar.
     * 
     * @return CompletableFuture pour l'initialisation
     */
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                LOGGER.log(Level.INFO, "Initialisation de l'AvatarController...");
                
                // Initialiser l'avatar manager
                avatarManager.initializeAvatar().get();
                
                isReady = true;
                LOGGER.log(Level.INFO, "AvatarController initialisé avec succès");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation de l'AvatarController", e);
                isReady = false;
            }
        });
    }
    
    /**
     * Affiche un message via l'avatar avec synthèse vocale.
     * 
     * @param message Le message à afficher et dire
     * @param emotion L'émotion à exprimer
     * @param displayDurationMs Durée d'affichage en millisecondes
     * @return CompletableFuture qui se termine quand l'avatar a fini
     */
    public CompletableFuture<Void> displayMessage(String message, String emotion, long displayDurationMs) {
        LOGGER.log(Level.INFO, "Affichage message avec émotion {0}: {1}", new Object[]{emotion, message});
        
        return CompletableFuture.runAsync(() -> {
            try {
                // Vérifier si des clients WebSocket sont connectés
                if (!webSocketService.hasConnectedAvatarSessions()) {
                    LOGGER.log(Level.WARNING, "Aucun client WebSocket connecté pour l'avatar");
                    return;
                }
                
                // Utilisation directe du WebSocketService pour envoyer le message vocal
                webSocketService.sendSpeechMessage(message, emotion);

                // Debug: test direct
                LOGGER.log(Level.INFO, "🧪 DEBUG - Test direct sendSpeechMessage");                
                webSocketService.debugListSessions();
                webSocketService.sendSpeechMessage("Test direct depuis AvatarController", "neutral");                
                // Si l'AvatarManager est initialisé, l'utiliser aussi pour les animations
                if (isReady) {
                    try {
                        // Utiliser l'AvatarManager pour les animations (sans attendre)
                        avatarManager.speak(message, emotion);
                    } catch (Exception e) {
                        // En cas d'erreur avec AvatarManager, continuer avec le WebSocket seulement
                        LOGGER.log(Level.FINE, "AvatarManager non disponible, utilisation WebSocket uniquement", e);
                    }
                }
                
                LOGGER.log(Level.INFO, "Message envoyé via WebSocket et AvatarManager");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage du message", e);
            }
        });
    }
    
    /**
     * Affiche un message via l'avatar avec synthèse vocale (version avec int pour compatibilité).
     */
    public CompletableFuture<Void> displayMessage(String message, String emotion, int displayDuration) {
        return displayMessage(message, emotion, (long) displayDuration);
    }
    
    /**
     * Affiche une proposition via l'avatar.
     * 
     * @param proposal La proposition à afficher
     * @return CompletableFuture qui se termine quand la proposition est affichée
     */
    public CompletableFuture<Void> displayProposal(Proposal proposal) {
        try {
            // Préparer la proposition
            proposal.prepare(null, null, java.time.LocalDateTime.now());
            
            String content = proposal.getContent();
            String emotion = determineEmotionForProposal(proposal);
            
            LOGGER.log(Level.INFO, "Affichage proposition: {0}", content);
            
            // Utiliser la méthode displayMessage pour la cohérence
            return displayMessage(content, emotion, 0L);
                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage de la proposition", e);
            return CompletableFuture.completedFuture(null);
        }
    }
    
    /**
     * Change l'apparence de l'avatar.
     * 
     * @param gender Genre
     * @param age Âge
     * @param style Style
     * @return CompletableFuture qui se termine quand le changement est effectué
     */
    public CompletableFuture<Void> changeAppearance(String gender, int age, String style) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOGGER.log(Level.INFO, "Changement d'apparence: {0}, {1} ans, style {2}", 
                          new Object[]{gender, age, style});
                
                if (isReady) {
                    avatarManager.changeAppearance(gender, age, style).get();
                } else {
                    LOGGER.log(Level.WARNING, "Avatar non initialisé pour le changement d'apparence");
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du changement d'apparence", e);
            }
        });
    }
    
    /**
     * Salue l'utilisateur.
     */
    public CompletableFuture<Void> greetUser() {
        String userName = configManager.getString("user.name", "");
        String greeting = getTimeBasedGreeting();
        
        String greetingMessage = userName.isEmpty() ? 
            greeting + " ! Comment allez-vous ?" :
            greeting + " " + userName + " ! Comment allez-vous ?";
            
        return displayMessage(greetingMessage, "friendly", 0L);
    }
    
    /**
     * Dit au revoir à l'utilisateur.
     */
    public CompletableFuture<Void> sayGoodbye() {
        String userName = configManager.getString("user.name", "");
        String goodbyeMessage = userName.isEmpty() ? 
            "Au revoir ! À bientôt !" :
            "Au revoir " + userName + " ! À bientôt !";
            
        return displayMessage(goodbyeMessage, "friendly", 0L);
    }
    
    /**
     * Change l'émotion de l'avatar.
     * 
     * @param emotion Nouvelle émotion
     * @param intensity Intensité de l'émotion
     */
    public void setEmotion(String emotion, double intensity) {
        if (isReady) {
            avatarManager.setEmotion(emotion, intensity);
        }
        
        // Aussi envoyer via WebSocket si nécessaire
        if (webSocketService.hasConnectedAvatarSessions()) {
            try {
                String emotionMessage = String.format(
                    "{\"type\":\"AVATAR_EMOTION\",\"emotion\":\"%s\",\"intensity\":%f,\"timestamp\":%d}",
                    emotion, intensity, System.currentTimeMillis()
                );
                
                webSocketService.getActiveSessions().values().forEach(session -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new org.springframework.web.socket.TextMessage(emotionMessage));
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Erreur envoi émotion via WebSocket", e);
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de l'envoi de l'émotion", e);
            }
        }
    }
    
    /**
     * Cache l'avatar.
     */
    public void hideAvatar() {
        if (isReady) {
            avatarManager.hide();
        }
    }
    
    /**
     * Affiche l'avatar.
     */
    public void showAvatar() {
        if (isReady) {
            avatarManager.show();
        }
    }
    
    /**
     * Génère une salutation basée sur l'heure.
     */
    private String getTimeBasedGreeting() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int hour = now.getHour();
        
        if (hour >= 5 && hour < 12) {
            return configManager.getString("voice.greetings.morning", "Bonjour");
        } else if (hour >= 12 && hour < 17) {
            return configManager.getString("voice.greetings.afternoon", "Bon après-midi");
        } else if (hour >= 17 && hour < 21) {
            return configManager.getString("voice.greetings.evening", "Bonsoir");
        } else {
            return configManager.getString("voice.greetings.night", "Bonne soirée");
        }
    }
    
    /**
     * Détermine l'émotion appropriée pour une proposition.
     */
    private String determineEmotionForProposal(Proposal proposal) {
        String proposalType = proposal.getClass().getSimpleName().toLowerCase();
        
        switch (proposalType) {
            case "weatherproposal":
                return "informative";
            case "newsproposal":
                return "attentive";
            case "storyproposal":
                return "friendly";
            case "reminderproposal":
                return "helpful";
            default:
                return "neutral";
        }
    }
    
    // Getters pour l'API
    public boolean isReady() {
        return isReady;
    }
    
    public boolean isInitialized() {
        return isReady;
    }
    
    public AvatarConfig getCurrentConfig() {
        if (isReady && avatarManager != null) {
            return avatarManager.getCurrentConfig();
        }
        // Retourner une configuration par défaut si l'avatar n'est pas prêt
        return new AvatarConfig();
    }
    
    /**
     * Obtient le nombre de sessions WebSocket connectées.
     */
    public int getConnectedSessionCount() {
        return webSocketService.getConnectedAvatarSessionCount();
    }
}
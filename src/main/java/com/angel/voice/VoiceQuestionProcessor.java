package com.angel.voice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.angel.avatar.WebSocketService;
import com.angel.config.ConfigManager;
import com.angel.ui.AvatarController;
import com.angel.util.LogUtil;

/**
 * Version améliorée du processeur de questions vocales avec synthèse vocale intégrée.
 * Compatible avec l'API existante d'AngelApplication.
 * 
 * Fichier : src/main/java/com/angel/voice/EnhancedVoiceQuestionProcessor.java
 */
@Component
public class VoiceQuestionProcessor {

    private static final Logger LOGGER = LogUtil.getLogger(VoiceQuestionProcessor.class);
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired
    private AvatarController avatarController;
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * Génère le message d'activation personnalisé (compatible avec AngelApplication).
     */
    public CompletableFuture<String> generateActivationMessage() {
        return CompletableFuture.supplyAsync(() -> {
            String userName = getUserName();
            String greeting = getTimeBasedGreeting();
            String questionPrompt = "que voulez-vous savoir ?";
            
            String message;
            if (userName != null && !userName.trim().isEmpty()) {
                message = String.format("%s %s, %s", greeting, userName, questionPrompt);
            } else {
                message = String.format("%s, %s", greeting, questionPrompt);
            }
            
            // Déclencher immédiatement la synthèse vocale
            sendSpeechMessage(message, "friendly");
            
            return message;
        });
    }
    
    /**
     * Traite une question ou commande vocale (compatible avec AngelApplication).
     */
    public CompletableFuture<String> processQuestion(String input, float confidence, VoiceQuestionContext context) {
        LOGGER.log(Level.INFO, "🗣️ Traitement question vocale: {0} (confidence: {1})", 
                  new Object[]{input, confidence});
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validation de l'entrée
                if (!isValidInput(input, confidence)) {
                    String errorMsg = "Je n'ai pas bien compris. Pouvez-vous répéter ?";
                    sendSpeechMessage(errorMsg, "apologetic");
                    return errorMsg;
                }
                
                // Analyser et répondre
                String answer = analyzeAndAnswer(input);
                
                // Déterminer l'émotion appropriée
                String emotion = determineEmotionForAnswer(input, answer);
                
                // Affichage visuel dans l'avatar
                avatarController.displayMessage(answer, emotion, calculateDisplayDuration(answer));
                
                // DÉCLENCHER LA SYNTHÈSE VOCALE
                sendSpeechMessage(answer, emotion);
                
                return answer;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur traitement question vocale", e);
                String errorMsg = "Désolé, je n'ai pas pu traiter votre demande.";
                sendSpeechMessage(errorMsg, "apologetic");
                return errorMsg;
            }
        });
    }
    
    /**
     * Version simplifiée pour compatibilité (sans context).
     */
    public CompletableFuture<String> processVoiceInput(String input, float confidence) {
        return processQuestion(input, confidence, null);
    }
    
    /**
     * NOUVELLE MÉTHODE: Envoie un message pour synthèse vocale via WebSocket.
     */
    private void sendSpeechMessage(String text, String emotion) {
        if (!configManager.getBoolean("voice.speech.enabled", true)) {
            LOGGER.log(Level.FINE, "Synthèse vocale désactivée dans la configuration");
            return;
        }
        
        try {
            LOGGER.log(Level.INFO, "🎯 DÉCLENCHEMENT SYNTHÈSE VOCALE: \"{0}\" (émotion: {1})", 
                      new Object[]{text, emotion});
            
            // Créer le message pour le frontend
            String message = String.format(
                "{\"type\":\"AVATAR_SPEAK\",\"text\":\"%s\",\"emotion\":\"%s\",\"timestamp\":%d}",
                text.replace("\"", "\\\""),
                emotion,
                System.currentTimeMillis()
            );
            
            // Envoyer via WebSocket - utiliser la méthode qui existe dans votre WebSocketService
            if (webSocketService.hasConnectedAvatarSessions()) {
                webSocketService.getActiveSessions().values().forEach(session -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Erreur envoi message vocal à une session", e);
                    }
                });
            }
            
            LOGGER.log(Level.INFO, "✅ Message vocal envoyé au frontend");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du déclenchement de la synthèse vocale", e);
        }
    }
    
    /**
     * Obtient le prénom configuré de l'utilisateur.
     */
    private String getUserName() {
        return configManager.getString("user.name", "");
    }
    
    /**
     * Génère une salutation basée sur l'heure.
     */
    private String getTimeBasedGreeting() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        if (hour >= 5 && hour < 12) {
            return "Bonjour";
        } else if (hour >= 12 && hour < 17) {
            return "Bon après-midi";
        } else if (hour >= 17 && hour < 21) {
            return "Bonsoir";
        } else {
            return "Bonne soirée";
        }
    }
    
    /**
     * Valide si une entrée est acceptable pour traitement.
     */
    private boolean isValidInput(String input, float confidence) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        float minConfidence = configManager.getFloat("voice.recognition.confidence.minimum", 0.6f);
        if (confidence < minConfidence) {
            LOGGER.log(Level.FINE, "Entrée rejetée - confiance trop faible: {0}", confidence);
            return false;
        }
        
        return true;
    }
    
    /**
     * Analyse l'entrée et génère une réponse appropriée.
     */
    private String analyzeAndAnswer(String input) {
        String lowerInput = input.toLowerCase().trim();
        
        // Questions sur l'heure
        if (containsKeywords(lowerInput, "heure", "temps") && 
            containsKeywords(lowerInput, "quelle", "il est", "maintenant")) {
            return getTimeResponse();
        }
        
        // Questions sur la date
        if (containsKeywords(lowerInput, "date", "jour", "aujourd'hui", "quel jour")) {
            return getDateResponse();
        }
        
        // Questions météo
        if (containsKeywords(lowerInput, "météo", "temps qu'il fait", "température", "pluie", "soleil")) {
            return "Pour la météo, consultez votre application météo habituelle ou regardez par la fenêtre.";
        }
        
        // Questions TV/programmes
        if (containsKeywords(lowerInput, "télé", "tv", "programme", "chaîne", "émission")) {
            return getTVResponse();
        }
        
        // Questions actualités
        if (containsKeywords(lowerInput, "actualité", "news", "nouvelles", "infos", "journal")) {
            return "Pour les dernières actualités, consultez vos sources d'information habituelles ou allumez la télévision.";
        }
        
        // Questions générales sur Angel
        if (containsKeywords(lowerInput, "qui es-tu", "que fais-tu", "tes capacités", "qui êtes-vous")) {
            return "Je suis Angèle, votre assistante virtuelle. Je peux vous renseigner sur l'heure, la météo, les programmes TV et répondre à vos questions. Vous pouvez me parler naturellement.";
        }
        
        // Salutations
        if (containsKeywords(lowerInput, "bonjour", "salut", "hello", "coucou", "bonsoir")) {
            return getGreetingResponse();
        }
        
        // Au revoir
        if (containsKeywords(lowerInput, "au revoir", "à bientôt", "goodbye", "bye")) {
            return "Au revoir ! N'hésitez pas à me parler quand vous le souhaitez.";
        }
        
        // Réponse générale
        if (input.trim().endsWith("?")) {
            return "C'est une excellente question ! Je réfléchis encore à ce type de réponse.";
        } else {
            return "Je vous ai bien entendu. Comment puis-je vous être utile ?";
        }
    }
    
    /**
     * Vérifie si l'entrée contient certains mots-clés.
     */
    private boolean containsKeywords(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Génère une réponse sur l'heure.
     */
    private String getTimeResponse() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH'h'mm");
        return "Il est " + now.format(formatter) + ".";
    }
    
    /**
     * Génère une réponse sur la date.
     */
    private String getDateResponse() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        return "Nous sommes " + now.format(formatter) + ".";
    }
    
    /**
     * Génère une réponse sur les programmes TV.
     */
    private String getTVResponse() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        if (hour < 8) {
            return "Il est encore tôt. Les programmes commencent généralement vers 6h du matin.";
        } else if (hour < 12) {
            return "En matinée, vous trouverez des émissions d'information et des magazines sur les principales chaînes.";
        } else if (hour < 14) {
            return "À l'heure du déjeuner, consultez votre guide TV pour les journaux télévisés de 13h.";
        } else if (hour < 19) {
            return "L'après-midi propose souvent des documentaires et des émissions de divertissement.";
        } else if (hour < 21) {
            return "En soirée, retrouvez les journaux télévisés à 19h et 20h sur les principales chaînes.";
        } else {
            return "En première partie de soirée, vous trouverez films, séries et grandes émissions.";
        }
    }
    
    /**
     * Génère une salutation basée sur l'heure.
     */
    private String getGreetingResponse() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Bonjour";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Bon après-midi";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Bonsoir";
        } else {
            greeting = "Bonne soirée";
        }
        
        return greeting + " ! Comment allez-vous ? Comment puis-je vous aider ?";
    }
    
    /**
     * Détermine l'émotion appropriée pour la réponse.
     */
    private String determineEmotionForAnswer(String input, String answer) {
        String lowerInput = input.toLowerCase();
        
        if (containsKeywords(lowerInput, "bonjour", "salut", "comment allez-vous")) {
            return "friendly";
        } else if (containsKeywords(lowerInput, "météo", "temps")) {
            return "informative";
        } else if (containsKeywords(lowerInput, "heure", "date")) {
            return "neutral";
        } else if (answer.contains("désolé") || answer.contains("erreur")) {
            return "apologetic";
        } else {
            return "attentive";
        }
    }
    
    /**
     * Calcule la durée d'affichage basée sur la longueur de la réponse.
     */
    private int calculateDisplayDuration(String text) {
        int baseTime = 3000; // 3 secondes de base
        int timePerChar = 50; // 50ms par caractère
        return Math.max(baseTime, text.length() * timePerChar);
    }
}
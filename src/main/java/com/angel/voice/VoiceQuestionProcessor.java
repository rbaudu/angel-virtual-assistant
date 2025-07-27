package com.angel.voice;

import com.angel.config.ConfigManager;
import com.angel.intelligence.ProposalEngine;
import com.angel.intelligence.proposals.WeatherProposal;
import com.angel.model.Activity;
import com.angel.model.UserProfile;
import com.angel.ui.AvatarController;
import com.angel.util.LogUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processeur spécialisé pour le traitement des questions vocales.
 * Cette classe centralise toute la logique d'analyse et de réponse aux questions.
 */
@Component
public class VoiceQuestionProcessor {

    private static final Logger LOGGER = LogUtil.getLogger(VoiceQuestionProcessor.class);
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired
    private AvatarController avatarController;
    
    @Autowired
    private ProposalEngine proposalEngine;
    
    /**
     * Génère le message d'activation personnalisé.
     * 
     * @return Message d'activation avec prénom et salutation selon l'heure
     */
    public String generateActivationMessage() {
        String userName = getUserName();
        String greeting = getTimeBasedGreeting();
        String questionPrompt = configManager.getString("voice.activation.question", "que voulez-vous savoir ?");
        
        if (userName != null && !userName.trim().isEmpty()) {
            return String.format("%s %s, %s", greeting, userName, questionPrompt);
        } else {
            return String.format("%s, %s", greeting, questionPrompt);
        }
    }
    
    /**
     * Traite une question vocale de l'utilisateur.
     * 
     * @param question La question posée
     * @param confidence Niveau de confiance de la reconnaissance
     * @param context Contexte utilisateur (activité, profil, historique)
     * @return CompletableFuture avec la réponse
     */
    public CompletableFuture<String> processQuestion(String question, float confidence, VoiceQuestionContext context) {
        LOGGER.log(Level.INFO, "Traitement question: {0} (confidence: {1})", 
                  new Object[]{question, confidence});
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validation de la question
                if (!isValidQuestion(question, confidence)) {
                    return getInvalidQuestionResponse();
                }
                
                // Analyser et répondre
                String answer = analyzeAndAnswer(question, context);
                
                // Faire parler l'avatar
                String emotion = determineEmotionForAnswer(question, answer);
                avatarController.displayMessage(answer, emotion, calculateDisplayDuration(answer));
                
                return answer;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur traitement question", e);
                String errorMsg = configManager.getString("voice.responses.error", 
                                                        "Désolé, je n'ai pas pu traiter votre question.");
                avatarController.displayMessage(errorMsg, "apologetic", 5000);
                return errorMsg;
            }
        });
    }
    
    /**
     * Obtient le prénom configuré de l'utilisateur.
     */
    private String getUserName() {
        return configManager.getString("user.name", "");
    }
    
    /**
     * Génère une salutation basée sur l'heure et la configuration.
     */
    private String getTimeBasedGreeting() {
        LocalDateTime now = LocalDateTime.now();
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
     * Valide si une question est acceptable pour traitement.
     */
    private boolean isValidQuestion(String question, float confidence) {
        if (question == null || question.trim().isEmpty()) {
            return false;
        }
        
        float minConfidence = configManager.getFloat("voice.question.min-confidence", 0.5f);
        if (confidence < minConfidence) {
            LOGGER.log(Level.FINE, "Question rejetée - confiance trop faible: {0}", confidence);
            return false;
        }
        
        int maxLength = configManager.getInt("voice.question.max-length", 300);
        if (question.length() > maxLength) {
            LOGGER.log(Level.FINE, "Question rejetée - trop longue: {0} caractères", question.length());
            return false;
        }
        
        return true;
    }
    
    /**
     * Réponse pour une question invalide.
     */
    private String getInvalidQuestionResponse() {
        return configManager.getString("voice.responses.invalid-question", 
                                     "Je n'ai pas bien compris votre question. Pouvez-vous répéter ?");
    }
    
    /**
     * Analyse la question et génère une réponse appropriée.
     */
    private String analyzeAndAnswer(String question, VoiceQuestionContext context) {
        String lowerQuestion = question.toLowerCase().trim();
        
        // Questions sur l'heure
        if (containsKeywords(lowerQuestion, "heure", "temps") && 
            containsKeywords(lowerQuestion, "quelle", "il est", "maintenant")) {
            return getTimeResponse();
        }
        
        // Questions sur la date
        if (containsKeywords(lowerQuestion, "date", "jour", "aujourd'hui", "quel jour")) {
            return getDateResponse();
        }
        
        // Questions météo
        if (containsKeywords(lowerQuestion, "météo", "temps qu'il fait", "température", "pluie", "soleil")) {
            return getWeatherResponse(context);
        }
        
        // Questions TV/programmes
        if (containsKeywords(lowerQuestion, "télé", "tv", "programme", "chaîne", "émission")) {
            return getTVResponse();
        }
        
        // Questions actualités
        if (containsKeywords(lowerQuestion, "actualité", "news", "nouvelles", "infos", "journal")) {
            return getNewsResponse();
        }
        
        // Salutations
        if (containsKeywords(lowerQuestion, "bonjour", "salut", "comment allez-vous", "ça va", "comment vas-tu")) {
            return getGreetingResponse();
        }
        
        // Questions sur Angel
        if (containsKeywords(lowerQuestion, "qui es-tu", "que fais-tu", "tes capacités", "qui êtes-vous")) {
            return getAboutAngelResponse();
        }
        
        // Intégration future avec IA
        return processWithAI(question, context);
    }
    
    /**
     * Vérifie si la question contient certains mots-clés.
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
        String timeFormat = configManager.getString("voice.responses.time.format", "HH'h'mm");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
        
        String timeTemplate = configManager.getString("voice.responses.time.template", "Il est {time}.");
        return timeTemplate.replace("{time}", now.format(formatter));
    }
    
    /**
     * Génère une réponse sur la date.
     */
    private String getDateResponse() {
        LocalDateTime now = LocalDateTime.now();
        String dateFormat = configManager.getString("voice.responses.date.format", "EEEE d MMMM yyyy");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.FRENCH);
        
        String dateTemplate = configManager.getString("voice.responses.date.template", "Nous sommes {date}.");
        return dateTemplate.replace("{date}", now.format(formatter));
    }
    
    /**
     * Génère une réponse météo.
     */
    private String getWeatherResponse(VoiceQuestionContext context) {
        try {
            WeatherProposal weatherProposal = new WeatherProposal(configManager);
            if (context != null) {
                // Utiliser la bonne méthode avec tous les paramètres requis
                boolean isAppropriate = weatherProposal.isAppropriate(
                    context.getCurrentActivity(),
                    context.getActivityHistory(),
                    context.getUserProfile(),
                    LocalDateTime.now(),
                    new ArrayList<>() // Liste vide pour l'historique des propositions
                );
                
                if (isAppropriate) {
                    // Préparer la proposition
                    weatherProposal.prepare(
                        context.getCurrentActivity(),
                        context.getUserProfile(),
                        LocalDateTime.now()
                    );
                    return weatherProposal.getContent();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur récupération météo", e);
        }
        
        return configManager.getString("voice.responses.weather.unavailable", 
                                     "Je ne peux pas récupérer les informations météo pour le moment.");
    }
    
    /**
     * Génère une réponse sur les programmes TV.
     */
    private String getTVResponse() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        String responseKey = "voice.responses.tv.default";
        
        if (hour < 8) {
            responseKey = "voice.responses.tv.early";
        } else if (hour < 12) {
            responseKey = "voice.responses.tv.morning";
        } else if (hour < 14) {
            responseKey = "voice.responses.tv.noon";
        } else if (hour < 19) {
            responseKey = "voice.responses.tv.afternoon";
        } else if (hour < 21) {
            responseKey = "voice.responses.tv.evening";
        } else {
            responseKey = "voice.responses.tv.primetime";
        }
        
        return configManager.getString(responseKey, 
                                     "En ce moment, consultez votre guide TV pour les programmes en cours.");
    }
    
    /**
     * Génère une réponse sur les actualités.
     */
    private String getNewsResponse() {
        return configManager.getString("voice.responses.news.default", 
                                     "Pour les dernières actualités, consultez vos sources d'information habituelles.");
    }
    
    /**
     * Génère une réponse de salutation.
     */
    private String getGreetingResponse() {
        String userName = getUserName();
        String baseResponse = configManager.getString("voice.responses.greeting.base", "Je vais très bien, merci !");
        String helpOffer = configManager.getString("voice.responses.greeting.help", "Comment puis-je vous aider ?");
        
        if (userName != null && !userName.trim().isEmpty()) {
            return String.format("%s %s, %s", baseResponse, userName, helpOffer);
        } else {
            return String.format("%s %s", baseResponse, helpOffer);
        }
    }
    
    /**
     * Génère une réponse sur Angel.
     */
    private String getAboutAngelResponse() {
        return configManager.getString("voice.responses.about.angel", 
                                     "Je suis Angel, votre assistant virtuel. Je peux vous renseigner sur l'heure, " +
                                     "la météo, les programmes TV et bien d'autres choses !");
    }
    
    /**
     * Traitement avec IA (à implémenter selon vos besoins).
     */
    private String processWithAI(String question, VoiceQuestionContext context) {
        // TODO: Intégration avec OpenAI, Claude, ou autre service d'IA
        
        if (question.trim().endsWith("?")) {
            return configManager.getString("voice.responses.ai.question", 
                                         "C'est une excellente question ! Je travaille encore sur ce type de réponse.");
        } else {
            return configManager.getString("voice.responses.ai.statement", 
                                         "Je vous ai bien entendu. Comment puis-je vous être utile ?");
        }
    }
    
    /**
     * Détermine l'émotion appropriée pour la réponse.
     */
    private String determineEmotionForAnswer(String question, String answer) {
        String lowerQuestion = question.toLowerCase();
        
        if (containsKeywords(lowerQuestion, "bonjour", "salut", "comment allez-vous")) {
            return "friendly";
        } else if (containsKeywords(lowerQuestion, "météo", "temps")) {
            return "informative";
        } else if (containsKeywords(lowerQuestion, "heure", "date")) {
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
        int baseTime = configManager.getInt("voice.display.base-duration", 3000);
        int timePerChar = configManager.getInt("voice.display.time-per-char", 50);
        
        return Math.max(baseTime, text.length() * timePerChar);
    }
}
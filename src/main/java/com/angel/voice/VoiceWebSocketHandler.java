package com.angel.voice;

import com.angel.avatar.WebSocketService;
import com.angel.config.ConfigManager;
import com.angel.util.LogUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class VoiceWebSocketHandler implements WebSocketHandler {
    private static final Logger LOGGER = LogUtil.getLogger(VoiceWebSocketHandler.class);
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired  
    private WakeWordDetector wakeWordDetector;

    @Autowired
    private com.angel.core.AngelApplication angelApplication;
    
    @Autowired
    private WebSocketService webSocketService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.log(Level.INFO, "WebSocket vocal connecté: {0}", session.getId());
        
        // Enregistrer la session avec le WebSocketService pour l'avatar
        webSocketService.registerAvatarSession(session.getId(), session);
        
        // Envoyer la configuration
        try {
            String config = String.format(
                "{\"type\":\"config\",\"wakeWord\":\"%s\",\"language\":\"fr-FR\",\"speechEnabled\":true}",
                wakeWordDetector.getWakeWord()
            );
            session.sendMessage(new TextMessage(config));
            
            LOGGER.log(Level.INFO, "Configuration WebSocket envoyée à {0}", session.getId());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur envoi config", e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            String payload = ((TextMessage) message).getPayload();
            LOGGER.log(Level.FINE, "Message reçu: {0}", payload);
            
            JsonNode msg = mapper.readTree(payload);
            String type = msg.get("type").asText();
            
            switch (type) {
                case "wake_word_detected":
                    handleWakeWordDetected(session, msg);
                    break;
                    
                case "speech_command":
                    handleSpeechCommand(session, msg);
                    break;
                    
                case "speech_error":
                    LOGGER.log(Level.WARNING, "Erreur vocale: {0}", 
                              msg.has("error") ? msg.get("error").asText() : "Unknown");
                    break;
                    
                case "speech_status":
                    handleSpeechStatus(session, msg);
                    break;
                    
                case "connection":
                    LOGGER.log(Level.INFO, "Client connecté: {0}", session.getId());
                    break;
                    
                case "speech_state":
                    handleSpeechState(session, msg);
                    break;
                    
                default:
                    LOGGER.log(Level.FINE, "Type de message non géré: {0}", type);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur traitement message", e);
        }
    }
    
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOGGER.log(Level.WARNING, "Erreur WebSocket pour session {0}", session.getId());
        LOGGER.log(Level.FINE, "Détail erreur WebSocket", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        LOGGER.log(Level.INFO, "WebSocket fermé: {0} (code: {1})", 
                  new Object[]{session.getId(), closeStatus.getCode()});
        
        // Désenregistrer la session
        webSocketService.unregisterAvatarSession(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() { return false; }
    
    private void handleWakeWordDetected(WebSocketSession session, JsonNode msg) {
        try {
            String word = msg.has("word") ? msg.get("word").asText() : "Angel";
            float confidence = msg.has("confidence") ? (float) msg.get("confidence").asDouble() : 0.8f;
            
            LOGGER.log(Level.INFO, "Wake word détecté: {0} (confidence: {1})", 
                      new Object[]{word, confidence});
            
            // Déclencher la logique Angel existante
            wakeWordDetector.onWakeWordDetected();
            
            // Confirmer au client
            String response = "{\"type\":\"wake_word_confirmed\",\"status\":\"activated\",\"message\":\"Angel activé\"}";
            session.sendMessage(new TextMessage(response));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur traitement wake word", e);
        }
    }
    
    private void handleSpeechCommand(WebSocketSession session, JsonNode msg) {
        try {
            String command = msg.get("command").asText();
            float confidence = msg.has("confidence") ? (float) msg.get("confidence").asDouble() : 0.8f;
            
            LOGGER.log(Level.INFO, "Question utilisateur: {0} (confidence: {1})", 
                      new Object[]{command, confidence});
            
            // Traiter la question via AngelApplication qui délègue au processeur
            angelApplication.processUserQuestion(command, confidence)
                .thenAccept(answer -> {
                    try {
                        // Envoyer la réponse textuelle au client pour l'affichage
                        String jsonResponse = String.format(
                            "{\"type\":\"ai_response\",\"data\":{\"response\":\"%s\"},\"timestamp\":%d}",
                            answer.replace("\"", "\\\"").replace("\n", "\\n"),
                            System.currentTimeMillis()
                        );
                        
                        session.sendMessage(new TextMessage(jsonResponse));
                        LOGGER.log(Level.INFO, "Réponse textuelle envoyée: {0}", answer);
                        
                        // La synthèse vocale sera gérée par l'AvatarController via WebSocketService
                        // Pas besoin d'envoyer un message vocal séparé ici
                        
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur envoi réponse", e);
                    }
                })
                .exceptionally(ex -> {
                    LOGGER.log(Level.SEVERE, "Erreur traitement question", ex);
                    try {
                        String errorResponse = String.format(
                            "{\"type\":\"error\",\"message\":\"Erreur de traitement\",\"timestamp\":%d}",
                            System.currentTimeMillis()
                        );
                        session.sendMessage(new TextMessage(errorResponse));
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur envoi erreur", e);
                    }
                    return null;
                });
                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur traitement commande", e);
        }
    }
    
    private void handleSpeechStatus(WebSocketSession session, JsonNode msg) {
        try {
            String status = msg.get("status").asText();
            LOGGER.log(Level.FINE, "Statut reconnaissance vocale: {0}", status);
            
            // Notifier les autres composants si nécessaire
            if ("stopped".equals(status)) {
                // L'utilisateur a arrêté de parler, on peut démarrer la synthèse vocale
                LOGGER.log(Level.FINE, "Utilisateur a terminé de parler");
            } else if ("started".equals(status)) {
                // L'utilisateur commence à parler, arrêter la synthèse vocale si active
                LOGGER.log(Level.FINE, "Utilisateur commence à parler");
                
                // Envoyer un signal pour arrêter la synthèse vocale
                String stopSpeechMessage = String.format(
                    "{\"type\":\"AVATAR_STOP_SPEAKING\",\"timestamp\":%d}",
                    System.currentTimeMillis()
                );
                
                webSocketService.getActiveSessions().values().forEach(activeSession -> {
                    try {
                        if (activeSession.isOpen()) {
                            activeSession.sendMessage(new TextMessage(stopSpeechMessage));
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.FINE, "Erreur envoi stop speech", e);
                    }
                });
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur traitement statut vocal", e);
        }
    }
    
    private void handleSpeechState(WebSocketSession session, JsonNode msg) {
        try {
            boolean isSpeaking = msg.get("isSpeaking").asBoolean();
            LOGGER.log(Level.FINE, "État synthèse vocale: {0}", isSpeaking ? "parle" : "arrêtée");
            
            // Cette information peut être utilisée pour coordonner les animations
            // ou d'autres aspects de l'interface
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur traitement état vocal", e);
        }
    }
}
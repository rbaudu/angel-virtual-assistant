package com.angel.voice;

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

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.log(Level.INFO, "WebSocket vocal connecté: {0}", session.getId());
        
        // Envoyer la configuration
        try {
            String config = String.format(
                "{\"type\":\"config\",\"wakeWord\":\"%s\",\"language\":\"fr-FR\"}",
                wakeWordDetector.getWakeWord()
            );
            session.sendMessage(new TextMessage(config));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur envoi config", e);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            String payload = ((TextMessage) message).getPayload();
            LOGGER.log(Level.INFO, "Message reçu: {0}", payload);
            
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
                    
                case "connection":
                    LOGGER.log(Level.INFO, "Client connecté");
                    break;
                    
                default:
                    LOGGER.log(Level.WARNING, "Type de message non géré: {0}", type);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur traitement message", e);
        }
    }
    
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOGGER.log(Level.WARNING, "Erreur WebSocket", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        LOGGER.log(Level.INFO, "WebSocket fermé: {0}", session.getId());
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
                        // Envoyer la réponse au client
                        String jsonResponse = String.format(
                            "{\"type\":\"ai_response\",\"data\":{\"response\":\"%s\"}}",
                            answer.replace("\"", "\\\"").replace("\n", "\\n")
                        );
                        
                        session.sendMessage(new TextMessage(jsonResponse));
                        LOGGER.log(Level.INFO, "Réponse envoyée: {0}", answer);
                        
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur envoi réponse", e);
                    }
                })
                .exceptionally(ex -> {
                    LOGGER.log(Level.SEVERE, "Erreur traitement question", ex);
                    try {
                        String errorResponse = "{\"type\":\"error\",\"message\":\"Erreur de traitement\"}";
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
    
 }
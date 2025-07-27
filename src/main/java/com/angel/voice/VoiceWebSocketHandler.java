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
            
            LOGGER.log(Level.INFO, "Commande vocale: {0} (confidence: {1})", 
                      new Object[]{command, confidence});
            
            // Traitement simple des commandes
            String response = processCommand(command);
            
            // Envoyer la réponse
            String jsonResponse = String.format(
                "{\"type\":\"ai_response\",\"data\":{\"response\":\"%s\"}}",
                response.replace("\"", "\\\"")
            );
            
            session.sendMessage(new TextMessage(jsonResponse));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur traitement commande", e);
        }
    }
    
    private String processCommand(String command) {
        String lowerCommand = command.toLowerCase();
        
        if (lowerCommand.contains("heure")) {
            return "Il est " + java.time.LocalTime.now().withSecond(0).withNano(0) + ".";
        }
        
        if (lowerCommand.contains("météo") || lowerCommand.contains("temps")) {
            return "Il fait beau aujourd'hui !";
        }
        
        if (lowerCommand.contains("bonjour") || lowerCommand.contains("salut")) {
            return "Bonjour ! Comment allez-vous ?";
        }
        
        // Réponse par défaut
        return "J'ai bien reçu votre message : " + command;
    }
}
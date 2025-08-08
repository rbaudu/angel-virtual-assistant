package com.angel.voice.service.providers;

import com.angel.voice.model.AIProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Base64;

@Service
public class OpenAIRealtimeService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String OPENAI_REALTIME_URL = "https://api.openai.com/v1/audio/speech";
    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    
    /**
     * Obtient une réponse audio directe d'OpenAI
     */
    public String getAudioResponse(String question, AIProvider provider) throws Exception {
        try {
            // Pour l'instant, on utilise chat + TTS car Realtime API est encore en beta
            // Quand Realtime API sera stable, on pourra faire un appel WebSocket direct
            
            // 1. Obtenir la réponse textuelle
            String textResponse = getChatResponse(question, provider);
            
            // 2. Convertir en audio via TTS
            return convertToAudio(textResponse, provider);
            
        } catch (Exception e) {
            throw new Exception("Erreur OpenAI Realtime: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appel à l'API Chat d'OpenAI
     */
    private String getChatResponse(String question, AIProvider provider) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(provider.getApiKey());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", provider.getModel());
        requestBody.put("max_tokens", provider.getMaxTokens());
        requestBody.put("temperature", provider.getTemperature());
        
        // Messages
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", question);
        requestBody.set("messages", objectMapper.createArrayNode().add(message));
        
        // Instructions système pour réponses vocales
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", 
            "Tu es Angèle, un assistant vocal intelligent. " +
            "Réponds de manière naturelle et concise, comme dans une conversation parlée. " +
            "Évite les listes à puces et privilégie un ton conversationnel.");
        requestBody.set("messages", 
            objectMapper.createArrayNode().add(systemMessage).add(message));
        
        HttpEntity<String> request = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            OPENAI_CHAT_URL, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return jsonResponse.get("choices").get(0).get("message").get("content").asText();
        } else {
            throw new Exception("Erreur API OpenAI Chat: " + response.getStatusCode());
        }
    }
    
    /**
     * Conversion texte vers audio via OpenAI TTS
     */
    private String convertToAudio(String text, AIProvider provider) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(provider.getApiKey());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "tts-1");
        requestBody.put("input", text);
        requestBody.put("voice", provider.getVoice());
        requestBody.put("response_format", "mp3");
        requestBody.put("speed", 1.0);
        
        HttpEntity<String> request = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers);
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            OPENAI_REALTIME_URL, HttpMethod.POST, request, byte[].class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            // Retourner l'audio en base64 pour le transport
            return Base64.getEncoder().encodeToString(response.getBody());
        } else {
            throw new Exception("Erreur API OpenAI TTS: " + response.getStatusCode());
        }
    }
    
    /**
     * Implémentation future pour la vraie Realtime API
     */
    public String getRealtimeAudioResponse(String audioInput, AIProvider provider) throws Exception {
        // TODO: Implémenter WebSocket connection vers Realtime API
        // Cette méthode sera utilisée quand Realtime API sera stable
        
        /*
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketSession session = client.doHandshake(
            new RealtimeWebSocketHandler(), 
            "wss://api.openai.com/v1/realtime?model=" + provider.getModel(),
            headers
        ).get();
        
        // Envoyer audio input
        // Recevoir audio response
        */
        
        throw new UnsupportedOperationException("Realtime API WebSocket pas encore implémentée");
    }
    
    /**
     * Health check
     */
    public boolean isHealthy() {
        try {
            // Test simple avec l'API Models
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(System.getenv("OPENAI_API_KEY"));
            
            HttpEntity<?> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.openai.com/v1/models", 
                HttpMethod.GET, request, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("OpenAI Health Check Failed: " + e.getMessage());
            return false;
        }
    }
}
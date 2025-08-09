package com.angel.voice.service.providers;

import com.angel.voice.model.AIProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.angel.util.LogUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class OpenAIRealtimeService {
    
    private static final Logger LOGGER = LogUtil.getLogger(OpenAIRealtimeService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_TTS_URL = "https://api.openai.com/v1/audio/speech";
    
    public OpenAIRealtimeService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    /**
     * Point d'entrée principal - choisit automatiquement le mode
     */
    public String getAudioResponse(String question, AIProvider provider) throws Exception {
        if ("direct".equals(provider.getMode()) || !hasApiKeyForSpringMode(provider)) {
            LOGGER.log(Level.INFO, "🔗 OpenAI mode DIRECT");
            return getAudioResponseDirect(question, provider);
        } else {
            LOGGER.log(Level.INFO, "⚙️ OpenAI mode API (Spring)");
            return getAudioResponseAPI(question, provider);
        }
    }
    
    /**
     * Mode DIRECT : Appel HTTP direct via HttpClient
     */
    private String getAudioResponseDirect(String question, AIProvider provider) throws Exception {
        String apiKey = resolveApiKey(provider.getApiKey());
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Clé API OpenAI manquante pour mode direct");
        }
        
        try {
            // 1. Obtenir la réponse textuelle via chat
            String textResponse = getChatResponseDirect(question, provider, apiKey);
            
            // 2. Convertir en audio via TTS
            return convertToAudioDirect(textResponse, provider, apiKey);
            
        } catch (Exception e) {
            throw new Exception("Erreur OpenAI Direct: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mode API : Appel via RestTemplate (méthode Spring classique)
     */
    private String getAudioResponseAPI(String question, AIProvider provider) throws Exception {
        try {
            // 1. Obtenir la réponse textuelle
            String textResponse = getChatResponseAPI(question, provider);
            
            // 2. Convertir en audio via TTS
            return convertToAudioAPI(textResponse, provider);
            
        } catch (Exception e) {
            throw new Exception("Erreur OpenAI API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appel Chat Direct
     */
    private String getChatResponseDirect(String question, AIProvider provider, String apiKey) throws Exception {
        // Préparer la requête JSON
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", provider.getModel());
        requestBody.put("max_tokens", provider.getMaxTokens());
        requestBody.put("temperature", provider.getTemperature());
        
        ArrayNode messages = objectMapper.createArrayNode();
        
        // System prompt
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", provider.getSystemPrompt() != null ? 
            provider.getSystemPrompt() :
            "Tu es Angèle, un assistant vocal intelligent. " +
            "Réponds de manière naturelle et concise, comme dans une conversation parlée.");
        messages.add(systemMessage);
        
        // Question utilisateur
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        
        // Appel HTTP direct
        String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : OPENAI_CHAT_URL;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build();
            
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            return jsonResponse.get("choices").get(0).get("message").get("content").asText();
        } else {
            throw new Exception("Erreur OpenAI Chat Direct: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Appel Chat API (Spring)
     */
    private String getChatResponseAPI(String question, AIProvider provider) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resolveApiKey(provider.getApiKey()));
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", provider.getModel());
        requestBody.put("max_tokens", provider.getMaxTokens());
        requestBody.put("temperature", provider.getTemperature());
        
        // Messages
        ArrayNode messages = objectMapper.createArrayNode();
        
        // System prompt
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", provider.getSystemPrompt() != null ? 
            provider.getSystemPrompt() :
            "Tu es Angèle, un assistant vocal intelligent. " +
            "Réponds de manière naturelle et concise, comme dans une conversation parlée.");
        messages.add(systemMessage);
        
        // Question de l'utilisateur
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        
        HttpEntity<String> request = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers);
        
        String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : OPENAI_CHAT_URL;
        ResponseEntity<String> response = restTemplate.postForEntity(
            endpoint, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return jsonResponse.get("choices").get(0).get("message").get("content").asText();
        } else {
            throw new Exception("Erreur API OpenAI Chat: " + response.getStatusCode());
        }
    }
    
    /**
     * Conversion texte vers audio via OpenAI TTS - Mode Direct
     */
    private String convertToAudioDirect(String text, AIProvider provider, String apiKey) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "tts-1");
        requestBody.put("input", text);
        requestBody.put("voice", provider.getVoice() != null ? provider.getVoice() : "nova");
        requestBody.put("response_format", "mp3");
        requestBody.put("speed", 1.0);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OPENAI_TTS_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build();
        
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            return Base64.getEncoder().encodeToString(response.body());
        } else {
            throw new Exception("Erreur OpenAI TTS Direct: " + response.statusCode());
        }
    }
    
    /**
     * Conversion texte vers audio via OpenAI TTS - Mode API
     */
    private String convertToAudioAPI(String text, AIProvider provider) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resolveApiKey(provider.getApiKey()));
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "tts-1");
        requestBody.put("input", text);
        requestBody.put("voice", provider.getVoice() != null ? provider.getVoice() : "nova");
        requestBody.put("response_format", "mp3");
        requestBody.put("speed", 1.0);
        
        HttpEntity<String> request = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers);
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            OPENAI_TTS_URL, HttpMethod.POST, request, byte[].class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            return Base64.getEncoder().encodeToString(response.getBody());
        } else {
            throw new Exception("Erreur API OpenAI TTS: " + response.getStatusCode());
        }
    }
    
    /**
     * Implémentation future pour la vraie Realtime API (WebSocket)
     */
    public String getRealtimeAudioResponse(String audioInput, AIProvider provider) throws Exception {
        // TODO: Implémenter WebSocket connection vers Realtime API
        // Cette méthode sera utilisée quand Realtime API sera stable
        
        throw new UnsupportedOperationException("Realtime API WebSocket pas encore implémentée");
    }
    
    /**
     * Health check unifié
     */
    public boolean isHealthy() {
        return isHealthy(null);
    }
    
    /**
     * Health check avec provider spécifique
     */
    public boolean isHealthy(AIProvider provider) {
        try {
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null) return false;
            
            if (provider != null && "direct".equals(provider.getMode())) {
                return testHealthDirect(apiKey, provider);
            } else {
                return testHealthAPI(apiKey);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "OpenAI Health Check Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode direct
     */
    private boolean testHealthDirect(String apiKey, AIProvider provider) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "gpt-3.5-turbo"); // Modèle le plus rapide
            requestBody.put("max_tokens", 5);
            requestBody.put("temperature", 0.1);
            
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", "Hi");
            messages.add(message);
            requestBody.set("messages", messages);
            
            String endpoint = provider != null && provider.getEndpoint() != null ? 
                provider.getEndpoint() : OPENAI_CHAT_URL;
                
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "OpenAI Health Check Direct Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode API
     */
    private boolean testHealthAPI(String apiKey) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            
            HttpEntity<?> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.openai.com/v1/models", 
                HttpMethod.GET, request, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "OpenAI Health Check API Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérification de disponibilité de la clé API pour mode Spring
     */
    private boolean hasApiKeyForSpringMode(AIProvider provider) {
        String apiKey = resolveApiKey(provider.getApiKey());
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Résolution des variables d'environnement
     */
    private String resolveApiKey(String apiKeyTemplate) {
        if (apiKeyTemplate == null) return null;
        
        if (apiKeyTemplate.startsWith("${") && apiKeyTemplate.endsWith("}")) {
            String envVar = apiKeyTemplate.substring(2, apiKeyTemplate.length() - 1);
            return System.getenv(envVar);
        }
        
        return apiKeyTemplate;
    }
}
package com.angel.voice.service.providers;

import com.angel.voice.model.AIProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class ClaudeService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_API_VERSION = "2023-06-01";
    
    /**
     * Obtient une réponse textuelle de Claude
     */
    public String getTextResponse(String question, AIProvider provider) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", provider.getApiKey());
            headers.set("anthropic-version", CLAUDE_API_VERSION);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", provider.getModel());
            requestBody.put("max_tokens", provider.getMaxTokens());
            requestBody.put("temperature", provider.getTemperature());
            
            // System prompt pour réponses vocales
            requestBody.put("system", 
                "Tu es Angèle, un assistant vocal intelligent et empathique. " +
                "Réponds de manière naturelle, concise et conversationnelle. " +
                "Adapte ton ton à une conversation parlée, évite les formats trop formels. " +
                "Privilégie des réponses claires et engageantes.");
            
            // Messages
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", question);
            messages.add(message);
            requestBody.set("messages", messages);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                CLAUDE_API_URL, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return extractTextFromResponse(jsonResponse);
            } else {
                throw new Exception("Erreur API Claude: " + response.getStatusCode() + 
                    " - " + response.getBody());
            }
            
        } catch (Exception e) {
            throw new Exception("Erreur Claude Service: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extrait le texte de la réponse Claude
     */
    private String extractTextFromResponse(JsonNode response) throws Exception {
        try {
            JsonNode content = response.get("content");
            if (content != null && content.isArray() && content.size() > 0) {
                JsonNode firstContent = content.get(0);
                if (firstContent.has("text")) {
                    return firstContent.get("text").asText();
                }
            }
            throw new Exception("Format de réponse Claude inattendu");
        } catch (Exception e) {
            throw new Exception("Erreur parsing réponse Claude: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appel avec streaming (pour réponses longues)
     */
    public String getStreamingTextResponse(String question, AIProvider provider) throws Exception {
        // TODO: Implémenter streaming si nécessaire pour de longues réponses
        // Pour l'instant, utilise l'API standard
        return getTextResponse(question, provider);
    }
    
    /**
     * Analyse de complexité spécifique pour Claude
     */
    public boolean isComplexQuestion(String question) {
        String questionLower = question.toLowerCase();
        
        // Claude excelle dans l'analyse et le raisonnement
        String[] complexPatterns = {
            "analyse", "compare", "explique pourquoi", "différence entre",
            "avantage", "inconvénient", "stratégie", "approche",
            "nuance", "contexte", "implication", "conséquence",
            "philosophie", "éthique", "morale", "justice",
            "complexe", "détaillé", "approfondi", "développe"
        };
        
        for (String pattern : complexPatterns) {
            if (questionLower.contains(pattern)) {
                return true;
            }
        }
        
        // Questions longues généralement plus complexes
        return question.length() > 100;
    }
    
    /**
     * Optimise la requête selon le type de question
     */
    private ObjectNode optimizeRequestForQuestion(String question, AIProvider provider) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        if (isComplexQuestion(question)) {
            // Plus de tokens et température plus basse pour l'analyse
            requestBody.put("max_tokens", Math.min(provider.getMaxTokens() * 2, 1000));
            requestBody.put("temperature", Math.max(provider.getTemperature() - 0.2, 0.1));
        } else {
            // Configuration standard
            requestBody.put("max_tokens", provider.getMaxTokens());
            requestBody.put("temperature", provider.getTemperature());
        }
        
        return requestBody;
    }
    
    /**
     * Health check pour Claude
     */
    public boolean isHealthy() {
        try {
            // Test simple avec une requête minimale
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", System.getenv("ANTHROPIC_API_KEY"));
            headers.set("anthropic-version", CLAUDE_API_VERSION);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "claude-3-haiku-20240307"); // Modèle le plus rapide
            requestBody.put("max_tokens", 10);
            requestBody.put("messages", objectMapper.createArrayNode()
                .add(objectMapper.createObjectNode()
                    .put("role", "user")
                    .put("content", "Hi")));
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                CLAUDE_API_URL, request, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            System.err.println("Claude Health Check Failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gestion d'erreur avec retry
     */
    private String callWithRetry(String question, AIProvider provider, int maxRetries) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return getTextResponse(question, provider);
            } catch (Exception e) {
                lastException = e;
                System.err.println("Tentative " + (attempt + 1) + " échouée pour Claude: " + e.getMessage());
                
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(1000 * (attempt + 1)); // Backoff exponentiel
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new Exception("Échec Claude après " + maxRetries + " tentatives", lastException);
    }
}
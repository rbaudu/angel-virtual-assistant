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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.angel.util.LogUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class ClaudeService {
    
    private static final Logger LOGGER = LogUtil.getLogger(ClaudeService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_API_VERSION = "2023-06-01";
    
    public ClaudeService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    /**
     * Point d'entrée principal - choisit automatiquement le mode
     */
    public String getTextResponse(String question, AIProvider provider) throws Exception {
        if ("direct".equals(provider.getMode()) || !hasApiKeyForSpringMode(provider)) {
            LOGGER.log(Level.INFO, "🔗 Claude mode DIRECT");
            return getTextResponseDirect(question, provider);
        } else {
            LOGGER.log(Level.INFO, "⚙️ Claude mode API (Spring)");
            return getTextResponseAPI(question, provider);
        }
    }
    
    /**
     * Mode DIRECT : Appel HTTP direct via HttpClient
     */
    private String getTextResponseDirect(String question, AIProvider provider) throws Exception {
        String apiKey = resolveApiKey(provider.getApiKey());
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Clé API Claude manquante pour mode direct");
        }
        
        try {
            // Préparer la requête JSON
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", provider.getModel());
            requestBody.put("max_tokens", provider.getMaxTokens());
            requestBody.put("temperature", provider.getTemperature());
            
            // System prompt
            if (provider.getSystemPrompt() != null && !provider.getSystemPrompt().isEmpty()) {
                requestBody.put("system", provider.getSystemPrompt());
            } else {
                requestBody.put("system", 
                    "Tu es Angèle, un assistant vocal français intelligent et bienveillant. " +
                    "Réponds de manière naturelle et conversationnelle, avec des réponses concises adaptées à l'oral.");
            }
            
            // Messages
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", question);
            messages.add(message);
            requestBody.set("messages", messages);
            
            // Appel HTTP direct
            String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : CLAUDE_API_URL;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", CLAUDE_API_VERSION)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                return extractTextFromResponse(jsonResponse);
            } else {
                throw new Exception("Erreur Claude API directe: " + response.statusCode() + " - " + response.body());
            }
            
        } catch (Exception e) {
            throw new Exception("Erreur Claude Service Direct: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mode API : Appel via RestTemplate (méthode Spring classique)
     */
    private String getTextResponseAPI(String question, AIProvider provider) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", resolveApiKey(provider.getApiKey()));
            headers.set("anthropic-version", CLAUDE_API_VERSION);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", provider.getModel());
            requestBody.put("max_tokens", provider.getMaxTokens());
            requestBody.put("temperature", provider.getTemperature());
            
            // System prompt
            if (provider.getSystemPrompt() != null && !provider.getSystemPrompt().isEmpty()) {
                requestBody.put("system", provider.getSystemPrompt());
            } else {
                requestBody.put("system", 
                    "Tu es Angèle, un assistant vocal français intelligent et bienveillant. " +
                    "Réponds de manière naturelle et conversationnelle, avec des réponses concises adaptées à l'oral.");
            }
            
            // Messages
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", 
                "Tu es Angèle, un assistant vocal intelligent et empathique. " +
                "Réponds de manière naturelle, concise et conversationnelle. " +
                "Adapte ton ton à une conversation parlée, évite les formats trop formels. " +
                "Privilégie des réponses claires et engageantes.");
            messages.add(systemMessage);
            
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.add(userMessage);
            requestBody.set("messages", messages);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : CLAUDE_API_URL;
            ResponseEntity<String> response = restTemplate.postForEntity(
                endpoint, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return extractTextFromResponse(jsonResponse);
            } else {
                throw new Exception("Erreur API Claude: " + response.getStatusCode() + 
                    " - " + response.getBody());
            }
            
        } catch (Exception e) {
            throw new Exception("Erreur Claude Service API: " + e.getMessage(), e);
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
     * Appel avec streaming (pour réponses longues) - Mode API uniquement
     */
    public String getStreamingTextResponse(String question, AIProvider provider) throws Exception {
        if ("direct".equals(provider.getMode())) {
            // En mode direct, pas de streaming pour simplifier
            return getTextResponseDirect(question, provider);
        } else {
            // TODO: Implémenter streaming pour mode API si nécessaire
            return getTextResponseAPI(question, provider);
        }
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
     * Health check unifié (teste le mode configuré)
     */
    public boolean isHealthy() {
        return isHealthy(null);
    }
    
    /**
     * Health check avec provider spécifique
     */
    public boolean isHealthy(AIProvider provider) {
        try {
            String apiKey = System.getenv("ANTHROPIC_API_KEY");
            if (apiKey == null) return false;
            
            if (provider != null && "direct".equals(provider.getMode())) {
                return testHealthDirect(apiKey, provider);
            } else {
                return testHealthAPI(apiKey);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Claude Health Check Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode direct
     */
    private boolean testHealthDirect(String apiKey, AIProvider provider) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "claude-3-haiku-20240307"); // Modèle le plus rapide
            requestBody.put("max_tokens", 10);
            requestBody.put("system", "Test");
            
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", "Hi");
            messages.add(message);
            requestBody.set("messages", messages);
            
            String endpoint = provider != null && provider.getEndpoint() != null ? 
                provider.getEndpoint() : CLAUDE_API_URL;
                
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", CLAUDE_API_VERSION)
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Claude Health Check Direct Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode API
     */
    private boolean testHealthAPI(String apiKey) {
        try {
            // Test simple avec l'API RestTemplate
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", CLAUDE_API_VERSION);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "claude-3-haiku-20240307");
            requestBody.put("max_tokens", 10);
            requestBody.put("system", "Test");
            
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", "Hi");
            messages.add(message);
            requestBody.set("messages", messages);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                CLAUDE_API_URL, request, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Claude Health Check API Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gestion d'erreur avec retry automatique
     */
    private String callWithRetry(String question, AIProvider provider, int maxRetries) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return getTextResponse(question, provider);
            } catch (Exception e) {
                lastException = e;
                LOGGER.log(Level.WARNING, "Tentative {0} échouée pour Claude: {1}", 
                          new Object[]{attempt + 1, e.getMessage()});
                
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
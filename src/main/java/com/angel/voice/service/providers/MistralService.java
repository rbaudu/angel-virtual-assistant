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
public class MistralService {
    
    private static final Logger LOGGER = LogUtil.getLogger(MistralService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String MISTRAL_API_URL = "https://api.mistral.ai/v1/chat/completions";
    
    public MistralService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    /**
     * Point d'entrée principal - choisit automatiquement le mode
     */
    public String getTextResponse(String question, AIProvider provider) throws Exception {
        if ("direct".equals(provider.getMode()) || !hasApiKeyForSpringMode(provider)) {
            LOGGER.log(Level.INFO, "🔗 Mistral mode DIRECT");
            return getTextResponseDirect(question, provider);
        } else {
            LOGGER.log(Level.INFO, "⚙️ Mistral mode API (Spring)");
            return getTextResponseAPI(question, provider);
        }
    }
    
    /**
     * Mode DIRECT : Appel HTTP direct via HttpClient
     */
    private String getTextResponseDirect(String question, AIProvider provider) throws Exception {
        String apiKey = resolveApiKey(provider.getApiKey());
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Clé API Mistral manquante pour mode direct");
        }
        
        try {
            // Préparer la requête JSON
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", provider.getModel());
            requestBody.put("max_tokens", provider.getMaxTokens());
            requestBody.put("temperature", provider.getTemperature());
            requestBody.put("top_p", 0.9);
            requestBody.put("stream", false);
            requestBody.put("safe_prompt", true);
            
            // Messages avec system prompt
            ArrayNode messages = objectMapper.createArrayNode();
            
            // System message pour personnalité
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", provider.getSystemPrompt() != null ? 
                provider.getSystemPrompt() :
                "Tu es Angèle, un assistant vocal français intelligent et bienveillant. " +
                "Tu réponds de manière naturelle et conversationnelle, avec un ton chaleureux. " +
                "Privilégie des réponses concises et claires, adaptées à une interaction vocale. " +
                "Évite les listes à puces et préfère un style parlé naturel.");
            messages.add(systemMessage);
            
            // User message
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.add(userMessage);
            
            requestBody.set("messages", messages);
            
            // Appel HTTP direct
            String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : MISTRAL_API_URL;
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
                return extractTextFromResponse(jsonResponse);
            } else {
                throw new Exception("Erreur API Mistral Direct: " + response.statusCode() + 
                    " - " + response.body());
            }
            
        } catch (Exception e) {
            throw new Exception("Erreur Mistral Service Direct: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mode API : Appel via RestTemplate (méthode Spring classique)
     */
    private String getTextResponseAPI(String question, AIProvider provider) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resolveApiKey(provider.getApiKey()));
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", provider.getModel());
            requestBody.put("max_tokens", provider.getMaxTokens());
            requestBody.put("temperature", provider.getTemperature());
            requestBody.put("top_p", 0.9);
            requestBody.put("stream", false);
            requestBody.put("safe_prompt", true);
            
            // Messages avec system prompt
            ArrayNode messages = objectMapper.createArrayNode();
            
            // System message pour personnalité
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", provider.getSystemPrompt() != null ? 
                provider.getSystemPrompt() :
                "Tu es Angèle, un assistant vocal français intelligent et bienveillant. " +
                "Tu réponds de manière naturelle et conversationnelle, avec un ton chaleureux. " +
                "Privilégie des réponses concises et claires, adaptées à une interaction vocale. " +
                "Évite les listes à puces et préfère un style parlé naturel.");
            messages.add(systemMessage);
            
            // User message
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.add(userMessage);
            
            requestBody.set("messages", messages);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            String endpoint = provider.getEndpoint() != null ? provider.getEndpoint() : MISTRAL_API_URL;
            ResponseEntity<String> response = restTemplate.postForEntity(
                endpoint, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return extractTextFromResponse(jsonResponse);
            } else {
                throw new Exception("Erreur API Mistral: " + response.getStatusCode() + 
                    " - " + response.getBody());
            }
            
        } catch (Exception e) {
            throw new Exception("Erreur Mistral Service API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extrait le texte de la réponse Mistral
     */
    private String extractTextFromResponse(JsonNode response) throws Exception {
        try {
            JsonNode choices = response.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }
            throw new Exception("Format de réponse Mistral inattendu");
        } catch (Exception e) {
            throw new Exception("Erreur parsing réponse Mistral: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appel avec streaming pour longues réponses
     */
    public String getStreamingTextResponse(String question, AIProvider provider) throws Exception {
        if ("direct".equals(provider.getMode())) {
            // En mode direct, pas de streaming pour simplifier
            return getTextResponseDirect(question, provider);
        } else {
            // TODO: Implémenter streaming si nécessaire
            return getTextResponseAPI(question, provider);
        }
    }
    
    /**
     * Optimise la requête selon le type de question
     */
    private ObjectNode optimizeRequestForQuestion(String question, AIProvider provider) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // Mistral excelle dans certains domaines
        String questionLower = question.toLowerCase();
        
        if (questionLower.contains("code") || questionLower.contains("programmation") || 
            questionLower.contains("technique")) {
            // Plus précis pour les questions techniques
            requestBody.put("temperature", Math.max(provider.getTemperature() - 0.2, 0.1));
            requestBody.put("top_p", 0.8);
        } else if (questionLower.contains("créatif") || questionLower.contains("histoire") || 
                   questionLower.contains("imagination")) {
            // Plus créatif pour les questions artistiques
            requestBody.put("temperature", Math.min(provider.getTemperature() + 0.2, 1.0));
            requestBody.put("top_p", 0.95);
        } else {
            // Configuration standard
            requestBody.put("temperature", provider.getTemperature());
            requestBody.put("top_p", 0.9);
        }
        
        return requestBody;
    }
    
    /**
     * Fonction tools pour Mistral (si nécessaire)
     */
    public String getTextResponseWithTools(String question, AIProvider provider) throws Exception {
        // TODO: Implémenter function calling avec Mistral si nécessaire
        // Mistral supporte les function calls pour certaines tâches
        return getTextResponse(question, provider);
    }
    
    /**
     * Gestion des embeddings Mistral (optionnel)
     */
    public double[] getEmbeddings(String text, String model) throws Exception {
        // TODO: Implémenter si on veut utiliser les embeddings Mistral
        // pour améliorer la compréhension contextuelle
        throw new UnsupportedOperationException("Embeddings Mistral pas encore implémentés");
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
            String apiKey = System.getenv("MISTRAL_API_KEY");
            if (apiKey == null) return false;
            
            if (provider != null && "direct".equals(provider.getMode())) {
                return testHealthDirect(apiKey, provider);
            } else {
                return testHealthAPI(apiKey);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Mistral Health Check Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode direct
     */
    private boolean testHealthDirect(String apiKey, AIProvider provider) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "mistral-tiny"); // Modèle le plus rapide
            requestBody.put("max_tokens", 10);
            requestBody.put("temperature", 0.1);
            
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", "Hi");
            messages.add(message);
            requestBody.set("messages", messages);
            
            String endpoint = provider != null && provider.getEndpoint() != null ? 
                provider.getEndpoint() : MISTRAL_API_URL;
                
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
            LOGGER.log(Level.FINE, "Mistral Health Check Direct Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode API
     */
    private boolean testHealthAPI(String apiKey) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "mistral-tiny"); // Modèle le plus rapide
            requestBody.put("max_tokens", 10);
            requestBody.put("temperature", 0.1);
            
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", "Hi");
            messages.add(message);
            requestBody.set("messages", messages);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                MISTRAL_API_URL, request, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Mistral Health Check API Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gestion d'erreur avec retry et fallback
     */
    private String callWithRetryAndFallback(String question, AIProvider provider) throws Exception {
        try {
            return getTextResponse(question, provider);
        } catch (Exception e) {
            // Log de l'erreur
            LOGGER.log(Level.WARNING, "Erreur Mistral première tentative: {0}", e.getMessage());
            
            // Retry avec des paramètres plus conservateurs
            AIProvider fallbackProvider = createFallbackProvider(provider);
            return getTextResponse(question, fallbackProvider);
        }
    }
    
    /**
     * Crée un provider avec des paramètres plus conservateurs
     */
    private AIProvider createFallbackProvider(AIProvider original) {
        AIProvider fallback = new AIProvider();
        fallback.setName(original.getName());
        fallback.setType(original.getType());
        fallback.setMode("direct"); // Fallback en mode direct
        fallback.setApiKey(original.getApiKey());
        fallback.setModel("mistral-small-latest"); // Modèle plus stable
        fallback.setEndpoint(original.getEndpoint());
        fallback.setMaxTokens(Math.min(original.getMaxTokens(), 150));
        fallback.setTemperature(0.3); // Plus conservateur
        fallback.setTtsProvider(original.getTtsProvider());
        fallback.setVoice(original.getVoice());
        fallback.setSystemPrompt(original.getSystemPrompt());
        return fallback;
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
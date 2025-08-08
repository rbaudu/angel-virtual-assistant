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
public class MistralService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String MISTRAL_API_URL = "https://api.mistral.ai/v1/chat/completions";
    
    /**
     * Obtient une réponse textuelle de Mistral
     */
    public String getTextResponse(String question, AIProvider provider) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(provider.getApiKey());
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", provider.getModel());
            requestBody.put("max_tokens", provider.getMaxTokens());
            requestBody.put("temperature", provider.getTemperature());
            requestBody.put("top_p", 0.9);
            requestBody.put("stream", false);
            
            // Messages avec system prompt
            ArrayNode messages = objectMapper.createArrayNode();
            
            // System message pour personnalité
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", 
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
            
            // Safe prompt pour éviter les contenus inappropriés
            requestBody.put("safe_prompt", true);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                MISTRAL_API_URL, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return extractTextFromResponse(jsonResponse);
            } else {
                throw new Exception("Erreur API Mistral: " + response.getStatusCode() + 
                    " - " + response.getBody());
            }
            
        } catch (Exception e) {
            throw new Exception("Erreur Mistral Service: " + e.getMessage(), e);
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
        // TODO: Implémenter streaming si nécessaire
        // Pour l'instant utilise l'API standard
        return getTextResponse(question, provider);
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
     * Health check pour Mistral
     */
    public boolean isHealthy() {
        try {
            String apiKey = System.getenv("MISTRAL_API_KEY");
            if (apiKey == null) return false;
            
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
            System.err.println("Mistral Health Check Failed: " + e.getMessage());
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
            System.err.println("Erreur Mistral première tentative: " + e.getMessage());
            
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
        fallback.setApiKey(original.getApiKey());
        fallback.setModel("mistral-small-latest"); // Modèle plus stable
        fallback.setMaxTokens(Math.min(original.getMaxTokens(), 150));
        fallback.setTemperature(0.3); // Plus conservateur
        fallback.setTtsProvider(original.getTtsProvider());
        fallback.setVoice(original.getVoice());
        return fallback;
    }
}
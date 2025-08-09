package com.angel.voice.service.providers;

import com.angel.voice.model.AIProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CopilotSpeechService {
    
    private static final Logger LOGGER = LogUtil.getLogger(CopilotSpeechService.class);
    
    @Autowired
    private com.angel.voice.service.TTSService ttsService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // URLs Azure OpenAI (Copilot utilise Azure OpenAI sous le capot)
    private static final String AZURE_OPENAI_URL_TEMPLATE = "https://{endpoint}.openai.azure.com/openai/deployments/{deployment}/chat/completions?api-version=2024-02-15-preview";
    
    public CopilotSpeechService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    /**
     * Point d'entrée principal - choisit automatiquement le mode
     */
    public String getAudioResponse(String question, AIProvider provider) throws Exception {
        if ("direct".equals(provider.getMode()) || !hasApiKeyForSpringMode(provider)) {
            LOGGER.log(Level.INFO, "🔗 Copilot mode DIRECT");
            return getAudioResponseDirect(question, provider);
        } else {
            LOGGER.log(Level.INFO, "⚙️ Copilot mode API (Spring)");
            return getAudioResponseAPI(question, provider);
        }
    }
    
    /**
     * Mode DIRECT : Appel HTTP direct via HttpClient
     */
    private String getAudioResponseDirect(String question, AIProvider provider) throws Exception {
        String apiKey = resolveApiKey(provider.getApiKey());
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Clé API Azure OpenAI manquante pour mode direct");
        }
        
        try {
            // 1. Obtenir réponse de Azure OpenAI (backend de Copilot)
            String textResponse = getCopilotTextResponseDirect(question, provider, apiKey);
            
            // 2. Convertir en audio via Azure Speech Services
            return convertToAudioWithAzureSpeechDirect(textResponse, provider);
            
        } catch (Exception e) {
            throw new Exception("Erreur Copilot Speech Direct: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mode API : Appel via RestTemplate (méthode Spring classique)
     */
    private String getAudioResponseAPI(String question, AIProvider provider) throws Exception {
        try {
            // 1. Obtenir réponse de Azure OpenAI
            String textResponse = getCopilotTextResponseAPI(question, provider);
            
            // 2. Convertir en audio via Azure Speech Services
            return convertToAudioWithAzureSpeechAPI(textResponse, provider);
            
        } catch (Exception e) {
            throw new Exception("Erreur Copilot Speech API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appel à Azure OpenAI (backend de Copilot) - Mode Direct
     */
    private String getCopilotTextResponseDirect(String question, AIProvider provider, String apiKey) throws Exception {
        String endpoint = resolveEnvVariable(provider.getEndpoint());
        String deployment = provider.getModel(); // deployment name dans Azure
        
        if (endpoint == null || deployment == null) {
            throw new IllegalStateException("Endpoint Azure ou deployment manquant pour Copilot");
        }
        
        String url = AZURE_OPENAI_URL_TEMPLATE
            .replace("{endpoint}", endpoint)
            .replace("{deployment}", deployment);
        
        // Préparer la requête JSON
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("max_tokens", provider.getMaxTokens());
        requestBody.put("temperature", provider.getTemperature());
        requestBody.put("top_p", 0.95);
        requestBody.put("frequency_penalty", 0.0);
        requestBody.put("presence_penalty", 0.0);
        requestBody.put("stream", false);
        
        // Messages avec personnalité Copilot/Angèle
        ArrayNode messages = objectMapper.createArrayNode();
        
        // System prompt adapté à Copilot
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", provider.getSystemPrompt() != null ? 
            provider.getSystemPrompt() :
            "Tu es Angèle, un assistant vocal intelligent alimenté par Microsoft Copilot. " +
            "Tu es utile, précise et bienveillante. Réponds de manière conversationnelle " +
            "et naturelle, comme dans une discussion parlée. Privilégie des réponses claires " +
            "et concises, adaptées à l'interaction vocale.");
        messages.add(systemMessage);
        
        // Question de l'utilisateur
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        
        // Appel HTTP direct
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("api-key", apiKey)
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build();
            
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            return extractTextFromResponse(jsonResponse);
        } else {
            throw new Exception("Erreur Azure OpenAI (Copilot) Direct: " + response.statusCode() + 
                " - " + response.body());
        }
    }
    
    /**
     * Appel à Azure OpenAI (backend de Copilot) - Mode API
     */
    private String getCopilotTextResponseAPI(String question, AIProvider provider) throws Exception {
        String endpoint = resolveEnvVariable(provider.getEndpoint());
        String deployment = provider.getModel(); // deployment name dans Azure
        
        String url = AZURE_OPENAI_URL_TEMPLATE
            .replace("{endpoint}", endpoint)
            .replace("{deployment}", deployment);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", resolveApiKey(provider.getApiKey()));
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("max_tokens", provider.getMaxTokens());
        requestBody.put("temperature", provider.getTemperature());
        requestBody.put("top_p", 0.95);
        requestBody.put("frequency_penalty", 0.0);
        requestBody.put("presence_penalty", 0.0);
        requestBody.put("stream", false);
        
        // Messages avec personnalité Copilot/Angèle
        ArrayNode messages = objectMapper.createArrayNode();
        
        // System prompt adapté à Copilot
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", provider.getSystemPrompt() != null ? 
            provider.getSystemPrompt() :
            "Tu es Angèle, un assistant vocal intelligent alimenté par Microsoft Copilot. " +
            "Tu es utile, précise et bienveillante. Réponds de manière conversationnelle " +
            "et naturelle, comme dans une discussion parlée. Privilégie des réponses claires " +
            "et concises, adaptées à l'interaction vocale.");
        messages.add(systemMessage);
        
        // Question de l'utilisateur
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        
        HttpEntity<String> request = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return extractTextFromResponse(jsonResponse);
        } else {
            throw new Exception("Erreur Azure OpenAI (Copilot) API: " + response.getStatusCode() + 
                " - " + response.getBody());
        }
    }
    
    /**
     * Extrait le texte de la réponse Azure OpenAI
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
            throw new Exception("Format de réponse Azure OpenAI inattendu");
        } catch (Exception e) {
            throw new Exception("Erreur parsing réponse Copilot: " + e.getMessage(), e);
        }
    }
    
    /**
     * Conversion texte vers audio via Azure Speech Services - Mode Direct
     */
    private String convertToAudioWithAzureSpeechDirect(String text, AIProvider provider) throws Exception {
        // Utiliser le service TTS centralisé avec configuration Azure
        AIProvider ttsProvider = createAzureTTSProvider(provider);
        return ttsService.synthesizeSpeech(text, ttsProvider);
    }
    
    /**
     * Conversion texte vers audio via Azure Speech Services - Mode API
     */
    private String convertToAudioWithAzureSpeechAPI(String text, AIProvider provider) throws Exception {
        // Utiliser le service TTS centralisé avec configuration Azure
        AIProvider ttsProvider = createAzureTTSProvider(provider);
        return ttsService.synthesizeSpeech(text, ttsProvider);
    }
    
    /**
     * Crée un provider TTS spécifique pour Azure Speech
     */
    private AIProvider createAzureTTSProvider(AIProvider originalProvider) {
        AIProvider ttsProvider = new AIProvider();
        ttsProvider.setName("azure_tts");
        ttsProvider.setType(originalProvider.getType());
        ttsProvider.setTtsProvider("azure");
        ttsProvider.setVoice(originalProvider.getVoice() != null ? 
            originalProvider.getVoice() : "fr-FR-DeniseNeural");
        ttsProvider.setApiKey(System.getenv("AZURE_SPEECH_KEY")); // Clé TTS séparée
        return ttsProvider;
    }
    
    /**
     * Optimisation spécifique pour Copilot
     */
    private ObjectNode optimizeForCopilot(ObjectNode requestBody, String question) {
        String questionLower = question.toLowerCase();
        
        // Copilot excelle dans certains domaines
        if (questionLower.contains("microsoft") || questionLower.contains("office") || 
            questionLower.contains("windows") || questionLower.contains("azure")) {
            // Plus précis pour les questions Microsoft
            requestBody.put("temperature", 0.3);
            requestBody.put("top_p", 0.8);
        } else if (questionLower.contains("productivité") || questionLower.contains("travail") || 
                   questionLower.contains("organisation")) {
            // Optimisé pour la productivité (spécialité de Copilot)
            requestBody.put("temperature", 0.5);
            requestBody.put("top_p", 0.9);
        } else {
            // Configuration standard conversationnelle
            requestBody.put("temperature", 0.7);
            requestBody.put("top_p", 0.95);
        }
        
        return requestBody;
    }
    
    /**
     * Gestion des conversations contextuelles avec Copilot
     */
    public String getContextualResponse(String question, String[] conversationHistory, AIProvider provider) throws Exception {
        // TODO: Implémenter la gestion du contexte conversationnel
        // Copilot peut maintenir le contexte sur plusieurs échanges
        return getAudioResponse(question, provider);
    }
    
    /**
     * Intégration avec Microsoft Graph (fonctionnalités Copilot avancées)
     */
    public String getCopilotWithGraphData(String question, AIProvider provider) throws Exception {
        // TODO: Implémenter l'intégration Microsoft Graph
        // Permet à Copilot d'accéder aux données Office 365, calendrier, emails, etc.
        throw new UnsupportedOperationException("Intégration Microsoft Graph pas encore implémentée");
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
            String apiKey = System.getenv("AZURE_OPENAI_KEY");
            String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
            
            if (apiKey == null || endpoint == null) return false;
            
            if (provider != null && "direct".equals(provider.getMode())) {
                return testHealthDirect(apiKey, endpoint, provider);
            } else {
                return testHealthAPI(apiKey, endpoint, provider);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Copilot Health Check Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode direct
     */
    private boolean testHealthDirect(String apiKey, String endpoint, AIProvider provider) {
        try {
            String deployment = provider != null ? provider.getModel() : "gpt-4";
            String url = AZURE_OPENAI_URL_TEMPLATE
                .replace("{endpoint}", endpoint)
                .replace("{deployment}", deployment);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("max_tokens", 10);
            requestBody.put("temperature", 0.1);
            
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", "Hi");
            messages.add(message);
            requestBody.set("messages", messages);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Copilot Health Check Direct Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode API
     */
    private boolean testHealthAPI(String apiKey, String endpoint, AIProvider provider) {
        try {
            String deployment = provider != null ? provider.getModel() : "gpt-4";
            String url = AZURE_OPENAI_URL_TEMPLATE
                .replace("{endpoint}", endpoint)
                .replace("{deployment}", deployment);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
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
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Copilot Health Check API Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérification de disponibilité de la clé API pour mode Spring
     */
    private boolean hasApiKeyForSpringMode(AIProvider provider) {
        String apiKey = resolveApiKey(provider.getApiKey());
        String endpoint = resolveEnvVariable(provider.getEndpoint());
        return apiKey != null && !apiKey.isEmpty() && endpoint != null && !endpoint.isEmpty();
    }
    
    /**
     * Résolution des variables d'environnement
     */
    private String resolveEnvVariable(String value) {
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            String envVar = value.substring(2, value.length() - 1);
            return System.getenv(envVar);
        }
        return value;
    }
    
    /**
     * Résolution des clés API
     */
    private String resolveApiKey(String apiKeyTemplate) {
        if (apiKeyTemplate == null) return null;
        
        if (apiKeyTemplate.startsWith("${") && apiKeyTemplate.endsWith("}")) {
            String envVar = apiKeyTemplate.substring(2, apiKeyTemplate.length() - 1);
            return System.getenv(envVar);
        }
        
        return apiKeyTemplate;
    }
    
    /**
     * Configuration spécifique Azure OpenAI pour Copilot
     */
    private ObjectNode createAzureOpenAIConfig(AIProvider provider) {
        ObjectNode config = objectMapper.createObjectNode();
        config.put("deployment_id", provider.getModel());
        config.put("api_version", "2024-02-15-preview");
        config.put("azure_endpoint", resolveEnvVariable(provider.getEndpoint()));
        return config;
    }
}
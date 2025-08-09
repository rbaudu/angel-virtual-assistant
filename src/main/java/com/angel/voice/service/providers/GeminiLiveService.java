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
public class GeminiLiveService {
    
    private static final Logger LOGGER = LogUtil.getLogger(GeminiLiveService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta";
    private static final String GEMINI_TEXT_URL = GEMINI_API_BASE + "/models/{model}:generateContent";
    private static final String GEMINI_TTS_URL = "https://texttospeech.googleapis.com/v1/text:synthesize";
    
    public GeminiLiveService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    /**
     * Point d'entrée principal - choisit automatiquement le mode
     */
    public String getAudioResponse(String question, AIProvider provider) throws Exception {
        if ("direct".equals(provider.getMode()) || !hasApiKeyForSpringMode(provider)) {
            LOGGER.log(Level.INFO, "🔗 Gemini mode DIRECT");
            return getAudioResponseDirect(question, provider);
        } else {
            LOGGER.log(Level.INFO, "⚙️ Gemini mode API (Spring)");
            return getAudioResponseAPI(question, provider);
        }
    }
    
    /**
     * Mode DIRECT : Appel HTTP direct via HttpClient
     */
    private String getAudioResponseDirect(String question, AIProvider provider) throws Exception {
        String apiKey = resolveApiKey(provider.getApiKey());
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Clé API Google manquante pour mode direct");
        }
        
        try {
            // 1. Obtenir la réponse textuelle de Gemini
            String textResponse = getGeminiTextResponseDirect(question, provider, apiKey);
            
            // 2. Convertir en audio via Google TTS
            return convertToAudioWithGoogleTTSDirect(textResponse, provider, apiKey);
            
        } catch (Exception e) {
            throw new Exception("Erreur Gemini Live Direct: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mode API : Appel via RestTemplate (méthode Spring classique)
     */
    private String getAudioResponseAPI(String question, AIProvider provider) throws Exception {
        try {
            // 1. Obtenir la réponse textuelle de Gemini
            String textResponse = getGeminiTextResponseAPI(question, provider);
            
            // 2. Convertir en audio via Google TTS
            return convertToAudioWithGoogleTTSAPI(textResponse, provider);
            
        } catch (Exception e) {
            throw new Exception("Erreur Gemini Live API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appel à l'API Gemini pour obtenir du texte - Mode Direct
     */
    private String getGeminiTextResponseDirect(String question, AIProvider provider, String apiKey) throws Exception {
        String url = GEMINI_TEXT_URL.replace("{model}", provider.getModel()) + "?key=" + apiKey;
        
        // Préparer la requête JSON
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // Configuration de génération
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", provider.getTemperature());
        generationConfig.put("maxOutputTokens", provider.getMaxTokens());
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        requestBody.set("generationConfig", generationConfig);
        
        // System instruction pour réponses vocales
        if (provider.getSystemPrompt() != null && !provider.getSystemPrompt().isEmpty()) {
            ObjectNode systemInstruction = objectMapper.createObjectNode();
            ArrayNode systemParts = objectMapper.createArrayNode();
            ObjectNode systemPart = objectMapper.createObjectNode();
            systemPart.put("text", provider.getSystemPrompt());
            systemParts.add(systemPart);
            systemInstruction.set("parts", systemParts);
            requestBody.set("systemInstruction", systemInstruction);
        }
        
        // Contenu de la question
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        
        String promptText = question;
        if (provider.getSystemPrompt() == null || provider.getSystemPrompt().isEmpty()) {
            promptText = "Tu es Angèle, un assistant vocal intelligent et bienveillant. " +
                        "Réponds de façon naturelle et conversationnelle, comme si tu parlais à un ami. " +
                        "Sois concise mais chaleureuse, évite les listes et préfère un ton parlé naturel.\n\n" + question;
        }
        
        part.put("text", promptText);
        parts.add(part);
        content.set("parts", parts);
        contents.add(content);
        requestBody.set("contents", contents);
        
        // Safety settings
        ArrayNode safetySettings = objectMapper.createArrayNode();
        String[] categories = {"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH", 
                              "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"};
        for (String category : categories) {
            ObjectNode safetySetting = objectMapper.createObjectNode();
            safetySetting.put("category", category);
            safetySetting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
            safetySettings.add(safetySetting);
        }
        requestBody.set("safetySettings", safetySettings);
        
        // Appel HTTP direct
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            return extractTextFromGeminiResponse(jsonResponse);
        } else {
            throw new Exception("Erreur API Gemini Direct: " + response.statusCode() + 
                " - " + response.body());
        }
    }
    
    /**
     * Appel à l'API Gemini pour obtenir du texte - Mode API
     */
    private String getGeminiTextResponseAPI(String question, AIProvider provider) throws Exception {
        String url = GEMINI_TEXT_URL.replace("{model}", provider.getModel()) + "?key=" + resolveApiKey(provider.getApiKey());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // Configuration de génération
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", provider.getTemperature());
        generationConfig.put("maxOutputTokens", provider.getMaxTokens());
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        requestBody.set("generationConfig", generationConfig);
        
        // System instruction pour réponses vocales
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ArrayNode systemParts = objectMapper.createArrayNode();
        ObjectNode systemPart = objectMapper.createObjectNode();
        systemPart.put("text", provider.getSystemPrompt() != null ? 
            provider.getSystemPrompt() :
            "Tu es Angèle, un assistant vocal intelligent et bienveillant. " +
            "Réponds de façon naturelle et conversationnelle, comme si tu parlais à un ami. " +
            "Sois concise mais chaleureuse, évite les listes et préfère un ton parlé naturel.");
        systemParts.add(systemPart);
        systemInstruction.set("parts", systemParts);
        requestBody.set("systemInstruction", systemInstruction);
        
        // Contenu de la question
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();
        part.put("text", question);
        parts.add(part);
        content.set("parts", parts);
        contents.add(content);
        requestBody.set("contents", contents);
        
        // Safety settings
        ArrayNode safetySettings = objectMapper.createArrayNode();
        String[] categories = {"HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH", 
                              "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"};
        for (String category : categories) {
            ObjectNode safetySetting = objectMapper.createObjectNode();
            safetySetting.put("category", category);
            safetySetting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
            safetySettings.add(safetySetting);
        }
        requestBody.set("safetySettings", safetySettings);
        
        HttpEntity<String> request = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return extractTextFromGeminiResponse(jsonResponse);
        } else {
            throw new Exception("Erreur API Gemini: " + response.getStatusCode() + 
                " - " + response.getBody());
        }
    }
    
    /**
     * Extrait le texte de la réponse Gemini
     */
    private String extractTextFromGeminiResponse(JsonNode response) throws Exception {
        try {
            JsonNode candidates = response.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.get("content");
                if (content != null && content.has("parts")) {
                    JsonNode parts = content.get("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        JsonNode firstPart = parts.get(0);
                        if (firstPart.has("text")) {
                            return firstPart.get("text").asText();
                        }
                    }
                }
            }
            throw new Exception("Format de réponse Gemini inattendu");
        } catch (Exception e) {
            throw new Exception("Erreur parsing réponse Gemini: " + e.getMessage(), e);
        }
    }
    
    /**
     * Conversion texte vers audio via Google TTS - Mode Direct
     */
    private String convertToAudioWithGoogleTTSDirect(String text, AIProvider provider, String apiKey) throws Exception {
        String url = GEMINI_TTS_URL + "?key=" + apiKey;
        
        // Préparer la requête JSON
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // Input
        ObjectNode input = objectMapper.createObjectNode();
        input.put("text", text);
        requestBody.set("input", input);
        
        // Voice
        ObjectNode voice = objectMapper.createObjectNode();
        voice.put("languageCode", "fr-FR");
        voice.put("name", provider.getVoice() != null ? provider.getVoice() : "fr-FR-Wavenet-C");
        voice.put("ssmlGender", "FEMALE");
        requestBody.set("voice", voice);
        
        // Audio config
        ObjectNode audioConfig = objectMapper.createObjectNode();
        audioConfig.put("audioEncoding", "MP3");
        audioConfig.put("speakingRate", 1.0);
        audioConfig.put("pitch", 0.0);
        audioConfig.put("volumeGainDb", 0.0);
        requestBody.set("audioConfig", audioConfig);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            if (jsonResponse.has("audioContent")) {
                return jsonResponse.get("audioContent").asText();
            } else {
                throw new Exception("Pas de contenu audio dans la réponse Google TTS Direct");
            }
        } else {
            throw new Exception("Erreur Google TTS Direct: " + response.statusCode());
        }
    }
    
    /**
     * Conversion texte vers audio via Google TTS - Mode API
     */
    private String convertToAudioWithGoogleTTSAPI(String text, AIProvider provider) throws Exception {
        String url = GEMINI_TTS_URL + "?key=" + resolveApiKey(provider.getApiKey());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // Input
        ObjectNode input = objectMapper.createObjectNode();
        input.put("text", text);
        requestBody.set("input", input);
        
        // Voice
        ObjectNode voice = objectMapper.createObjectNode();
        voice.put("languageCode", "fr-FR");
        voice.put("name", provider.getVoice() != null ? provider.getVoice() : "fr-FR-Wavenet-C");
        voice.put("ssmlGender", "FEMALE");
        requestBody.set("voice", voice);
        
        // Audio config
        ObjectNode audioConfig = objectMapper.createObjectNode();
        audioConfig.put("audioEncoding", "MP3");
        audioConfig.put("speakingRate", 1.0);
        audioConfig.put("pitch", 0.0);
        audioConfig.put("volumeGainDb", 0.0);
        requestBody.set("audioConfig", audioConfig);
        
        HttpEntity<String> request = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            if (jsonResponse.has("audioContent")) {
                return jsonResponse.get("audioContent").asText();
            } else {
                throw new Exception("Pas de contenu audio dans la réponse Google TTS");
            }
        } else {
            throw new Exception("Erreur Google TTS: " + response.getStatusCode());
        }
    }
    
    /**
     * Implémentation future pour Gemini Live réel (WebRTC)
     */
    public String getRealtimeLiveResponse(String audioInput, AIProvider provider) throws Exception {
        // TODO: Implémenter WebRTC connection vers Gemini Live
        throw new UnsupportedOperationException("Gemini Live WebRTC pas encore implémenté");
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
            String apiKey = System.getenv("GOOGLE_API_KEY");
            if (apiKey == null) return false;
            
            if (provider != null && "direct".equals(provider.getMode())) {
                return testHealthDirect(apiKey, provider);
            } else {
                return testHealthAPI(apiKey);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Gemini Health Check Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode direct
     */
    private boolean testHealthDirect(String apiKey, AIProvider provider) {
        try {
            String model = provider != null ? provider.getModel() : "gemini-pro";
            String url = GEMINI_TEXT_URL.replace("{model}", model) + "?key=" + apiKey;
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();
            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();
            part.put("text", "Hello");
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            requestBody.set("contents", contents);
            
            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("maxOutputTokens", 10);
            requestBody.set("generationConfig", generationConfig);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
                
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Gemini Health Check Direct Failed: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check mode API
     */
    private boolean testHealthAPI(String apiKey) {
        try {
            String url = GEMINI_TEXT_URL.replace("{model}", "gemini-pro") + "?key=" + apiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();
            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();
            part.put("text", "Hello");
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            requestBody.set("contents", contents);
            
            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("maxOutputTokens", 10);
            requestBody.set("generationConfig", generationConfig);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Gemini Health Check API Failed: {0}", e.getMessage());
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
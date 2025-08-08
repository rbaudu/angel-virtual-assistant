package com.angel.voice.service.providers;

import com.angel.voice.model.AIProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Base64;

@Service
public class GeminiLiveService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta";
    private static final String GEMINI_TEXT_URL = GEMINI_API_BASE + "/models/{model}:generateContent";
    private static final String GEMINI_TTS_URL = "https://texttospeech.googleapis.com/v1/text:synthesize";
    
    /**
     * Obtient une réponse audio de Gemini
     */
    public String getAudioResponse(String question, AIProvider provider) throws Exception {
        try {
            // 1. Obtenir la réponse textuelle de Gemini
            String textResponse = getGeminiTextResponse(question, provider);
            
            // 2. Convertir en audio via Google TTS
            return convertToAudioWithGoogleTTS(textResponse, provider);
            
        } catch (Exception e) {
            throw new Exception("Erreur Gemini Live: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appel à l'API Gemini pour obtenir du texte
     */
    private String getGeminiTextResponse(String question, AIProvider provider) throws Exception {
        String url = GEMINI_TEXT_URL.replace("{model}", provider.getModel()) + "?key=" + provider.getApiKey();
        
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
        systemPart.put("text", 
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
     * Conversion texte vers audio via Google TTS
     */
    private String convertToAudioWithGoogleTTS(String text, AIProvider provider) throws Exception {
        String url = GEMINI_TTS_URL + "?key=" + provider.getApiKey();
        
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
        voice.put("name", provider.getVoice());
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
        // Cette méthode sera utilisée quand Gemini Live sera disponible
        
        throw new UnsupportedOperationException("Gemini Live WebRTC pas encore implémenté");
    }
    
    /**
     * Optimisation spécifique pour Gemini
     */
    private ObjectNode optimizeForConversation(ObjectNode requestBody) {
        // Gemini fonctionne bien avec ces paramètres pour la conversation
        JsonNode genConfig = requestBody.get("generationConfig");
        if (genConfig instanceof ObjectNode) {
            ObjectNode config = (ObjectNode) genConfig;
            config.put("topP", 0.95);
            config.put("topK", 40);
            config.put("candidateCount", 1);
        }
        return requestBody;
    }
    
    /**
     * Health check pour Gemini
     */
    public boolean isHealthy() {
        try {
            String apiKey = System.getenv("GOOGLE_API_KEY");
            if (apiKey == null) return false;
            
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
            System.err.println("Gemini Health Check Failed: " + e.getMessage());
            return false;
        }
    }
}
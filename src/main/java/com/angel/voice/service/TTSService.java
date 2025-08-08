package com.angel.voice.service;

import com.angel.voice.model.AIProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Base64;

@Service
public class TTSService {
    
    @Autowired
    private ConfigurationService configService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // URLs des services TTS
    private static final String AZURE_TTS_URL = "https://{region}.tts.speech.microsoft.com/cognitiveservices/v1";
    private static final String GOOGLE_TTS_URL = "https://texttospeech.googleapis.com/v1/text:synthesize";
    
    /**
     * Synthétise de la parole à partir du texte
     */
    public String synthesizeSpeech(String text, AIProvider provider) throws Exception {
        String ttsProvider = provider.getTtsProvider();
        
        if (ttsProvider == null) {
            throw new IllegalArgumentException("Aucun provider TTS défini pour: " + provider.getName());
        }
        
        System.out.println("Synthèse TTS avec " + ttsProvider + " pour: " + 
            text.substring(0, Math.min(50, text.length())));
        
        switch (ttsProvider.toLowerCase()) {
            case "azure":
                return synthesizeWithAzure(text, provider);
            case "google":
                return synthesizeWithGoogle(text, provider);
            default:
                throw new IllegalArgumentException("Provider TTS non supporté: " + ttsProvider);
        }
    }
    
    /**
     * Synthèse avec Azure Cognitive Services
     */
    private String synthesizeWithAzure(String text, AIProvider provider) throws Exception {
        JsonNode ttsConfig = configService.getAIConfig().get("ttsServices").get("azure");
        
        String region = resolveEnvVariable(ttsConfig.get("region").asText());
        String apiKey = resolveEnvVariable(ttsConfig.get("apiKey").asText());
        String voice = provider.getVoice() != null ? provider.getVoice() : 
            ttsConfig.get("defaultVoice").asText();
        
        String url = AZURE_TTS_URL.replace("{region}", region);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/ssml+xml"));
        headers.set("Ocp-Apim-Subscription-Key", apiKey);
        headers.set("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3");
        headers.set("User-Agent", "AngelVoiceAssistant");
        
        // Construction du SSML pour un contrôle plus fin
        String ssml = buildSSML(text, voice, ttsConfig);
        
        HttpEntity<String> request = new HttpEntity<>(ssml, headers);
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            url, HttpMethod.POST, request, byte[].class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            return Base64.getEncoder().encodeToString(response.getBody());
        } else {
            throw new Exception("Erreur Azure TTS: " + response.getStatusCode());
        }
    }
    
    /**
     * Synthèse avec Google Cloud Text-to-Speech
     */
    private String synthesizeWithGoogle(String text, AIProvider provider) throws Exception {
        JsonNode ttsConfig = configService.getAIConfig().get("ttsServices").get("google");
        
        String apiKey = resolveEnvVariable(ttsConfig.get("apiKey").asText());
        String voice = provider.getVoice() != null ? provider.getVoice() : 
            ttsConfig.get("defaultVoice").asText();
        
        String url = GOOGLE_TTS_URL + "?key=" + apiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // Input text
        ObjectNode input = objectMapper.createObjectNode();
        input.put("text", text);
        requestBody.set("input", input);
        
        // Voice configuration
        ObjectNode voiceConfig = objectMapper.createObjectNode();
        voiceConfig.put("languageCode", "fr-FR");
        voiceConfig.put("name", voice);
        voiceConfig.put("ssmlGender", "FEMALE");
        requestBody.set("voice", voiceConfig);
        
        // Audio configuration
        ObjectNode audioConfig = objectMapper.createObjectNode();
        audioConfig.put("audioEncoding", "MP3");
        audioConfig.put("speakingRate", ttsConfig.get("speed").asDouble(1.0));
        audioConfig.put("pitch", ttsConfig.get("pitch").asDouble(0.0));
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
     * Construction du SSML pour Azure avec contrôle de la prosodie
     */
    private String buildSSML(String text, String voice, JsonNode ttsConfig) {
        double speed = ttsConfig.get("speed").asDouble(1.0);
        String pitch = ttsConfig.get("pitch").asText("default");
        
        StringBuilder ssml = new StringBuilder();
        ssml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ssml.append("<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"fr-FR\">");
        ssml.append("<voice name=\"").append(voice).append("\">");
        
        // Ajout de prosodie pour un rendu plus naturel
        ssml.append("<prosody rate=\"").append(speed).append("\" pitch=\"").append(pitch).append("\">");
        
        // Nettoyage et échappement du texte
        String cleanText = escapeSSMLText(text);
        ssml.append(cleanText);
        
        ssml.append("</prosody>");
        ssml.append("</voice>");
        ssml.append("</speak>");
        
        return ssml.toString();
    }
    
    /**
     * Échappement des caractères spéciaux pour SSML
     */
    private String escapeSSMLText(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
    
    /**
     * Optimise le texte pour la synthèse vocale
     */
    public String optimizeTextForSpeech(String text) {
        // Remplacements pour améliorer la prononciation
        String optimized = text
            // Abréviations communes
            .replaceAll("\\bM\\.", "Monsieur")
            .replaceAll("\\bMme\\.", "Madame")
            .replaceAll("\\bDr\\.", "Docteur")
            .replaceAll("\\betc\\.", "et cetera")
            
            // Nombres et symboles
            .replaceAll("\\b%", " pour cent")
            .replaceAll("\\b€", " euros")
            .replaceAll("\\b\\$", " dollars")
            
            // Emoticons simples
            .replaceAll(":\\)", " sourire")
            .replaceAll(":\\(", " triste")
            
            // Nettoyage final
            .replaceAll("\\s+", " ")
            .trim();
        
        return optimized;
    }
    
    /**
     * Ajoute des pauses naturelles dans le texte
     */
    public String addNaturalPauses(String text) {
        return text
            .replaceAll("\\.", ".<break time=\"500ms\"/>")
            .replaceAll("\\,", ",<break time=\"200ms\"/>")
            .replaceAll("\\;", ";<break time=\"300ms\"/>")
            .replaceAll("\\:", ":<break time=\"300ms\"/>")
            .replaceAll("\\!", "!<break time=\"500ms\"/>")
            .replaceAll("\\?", "?<break time=\"500ms\"/>");
    }
    
    /**
     * Test de disponibilité des services TTS
     */
    public boolean isTTSAvailable(String provider) {
        try {
            switch (provider.toLowerCase()) {
                case "azure":
                    return testAzureAvailability();
                case "google":
                    return testGoogleAvailability();
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Test disponibilité TTS " + provider + " échoué: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test de disponibilité Azure TTS
     */
    private boolean testAzureAvailability() {
        try {
            JsonNode ttsConfig = configService.getAIConfig().get("ttsServices").get("azure");
            String region = resolveEnvVariable(ttsConfig.get("region").asText());
            String apiKey = resolveEnvVariable(ttsConfig.get("apiKey").asText());
            
            if (apiKey == null || region == null) return false;
            
            // Test simple avec une courte phrase
            String url = AZURE_TTS_URL.replace("{region}", region);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/ssml+xml"));
            headers.set("Ocp-Apim-Subscription-Key", apiKey);
            headers.set("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3");
            
            String testSSML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"fr-FR\">" +
                "<voice name=\"fr-FR-DeniseNeural\">Test</voice></speak>";
            
            HttpEntity<String> request = new HttpEntity<>(testSSML, headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url, HttpMethod.POST, request, byte[].class);
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Test de disponibilité Google TTS
     */
    private boolean testGoogleAvailability() {
        try {
            JsonNode ttsConfig = configService.getAIConfig().get("ttsServices").get("google");
            String apiKey = resolveEnvVariable(ttsConfig.get("apiKey").asText());
            
            if (apiKey == null) return false;
            
            String url = GOOGLE_TTS_URL + "?key=" + apiKey;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            ObjectNode input = objectMapper.createObjectNode();
            input.put("text", "Test");
            requestBody.set("input", input);
            
            ObjectNode voice = objectMapper.createObjectNode();
            voice.put("languageCode", "fr-FR");
            voice.put("name", "fr-FR-Wavenet-C");
            requestBody.set("voice", voice);
            
            ObjectNode audioConfig = objectMapper.createObjectNode();
            audioConfig.put("audioEncoding", "MP3");
            requestBody.set("audioConfig", audioConfig);
            
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
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
}
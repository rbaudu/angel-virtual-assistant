package com.angel.voice.service.providers;

import com.angel.voice.model.AIProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Base64;

@Service
public class CopilotSpeechService {
    
    @Autowired
    private com.angel.voice.service.TTSService ttsService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // URLs Azure OpenAI (Copilot utilise Azure OpenAI sous le capot)
    private static final String AZURE_OPENAI_URL = "https://{endpoint}.openai.azure.com/openai/deployments/{deployment}/chat/completions?api-version=2024-02-15-preview";
    private static final String AZURE_SPEECH_URL = "https://{region}.tts.speech.microsoft.com/cognitiveservices/v1";
    
    /**
     * Obtient une réponse audio de Copilot Speech
     */
    public String getAudioResponse(String question, AIProvider provider) throws Exception {
        try {
            // 1. Obtenir réponse de Azure OpenAI (backend de Copilot)
            String textResponse = getCopilotTextResponse(question, provider);
            
            // 2. Convertir en audio via Azure Speech Services
            return convertToAudioWithAzureSpeech(textResponse, provider);
            
        } catch (Exception e) {
            throw new Exception("Erreur Copilot Speech: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appel à Azure OpenAI (backend de Copilot)
     */
    private String getCopilotTextResponse(String question, AIProvider provider) throws Exception {
        String endpoint = resolveEnvVariable(provider.getEndpoint());
        String deployment = provider.getModel(); // deployment name dans Azure
        
        String url = AZURE_OPENAI_URL
            .replace("{endpoint}", endpoint)
            .replace("{deployment}", deployment);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", provider.getApiKey());
        
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
        systemMessage.put("content", 
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
            throw new Exception("Erreur Azure OpenAI (Copilot): " + response.getStatusCode() + 
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
     * Conversion texte vers audio via Azure Speech Services
     */
    private String convertToAudioWithAzureSpeech(String text, AIProvider provider) throws Exception {
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
        ttsProvider.setApiKey(originalProvider.getApiKey());
        return ttsProvider;
    }
    
    /**
     * Implémentation avec Azure Speech SDK (alternative)
     */
    public String getAudioResponseWithSpeechSDK(String question, AIProvider provider) throws Exception {
        // TODO: Implémenter avec Azure Speech SDK pour performance optimale
        // Nécessite d'ajouter la dépendance Azure Speech SDK
        
        /*
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(
            provider.getApiKey(), 
            resolveEnvVariable("${AZURE_REGION}")
        );
        speechConfig.setSpeechSynthesisVoiceName(provider.getVoice());
        
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig);
        
        // D'abord obtenir le texte
        String textResponse = getCopilotTextResponse(question, provider);
        
        // Puis synthétiser
        SpeechSynthesisResult result = synthesizer.SpeakTextAsync(textResponse).get();
        
        if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
            return Base64.getEncoder().encodeToString(result.getAudioData());
        } else {
            throw new Exception("Erreur synthèse Azure Speech SDK: " + result.getReason());
        }
        */
        
        throw new UnsupportedOperationException("Azure Speech SDK pas encore configuré");
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
        
        String url = AZURE_OPENAI_URL
            .replace("{endpoint}", resolveEnvVariable(provider.getEndpoint()))
            .replace("{deployment}", provider.getModel());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", provider.getApiKey());
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("max_tokens", provider.getMaxTokens());
        requestBody.put("temperature", provider.getTemperature());
        
        // Construire l'historique des messages
        ArrayNode messages = objectMapper.createArrayNode();
        
        // System message
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Tu es Angèle, assistant vocal intelligent. " +
            "Maintiens le contexte de notre conversation et réponds naturellement.");
        messages.add(systemMessage);
        
        // Ajouter l'historique si disponible
        if (conversationHistory != null) {
            for (int i = 0; i < conversationHistory.length; i += 2) {
                if (i + 1 < conversationHistory.length) {
                    // Question utilisateur
                    ObjectNode userMsg = objectMapper.createObjectNode();
                    userMsg.put("role", "user");
                    userMsg.put("content", conversationHistory[i]);
                    messages.add(userMsg);
                    
                    // Réponse assistant
                    ObjectNode assistantMsg = objectMapper.createObjectNode();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", conversationHistory[i + 1]);
                    messages.add(assistantMsg);
                }
            }
        }
        
        // Question actuelle
        ObjectNode currentQuestion = objectMapper.createObjectNode();
        currentQuestion.put("role", "user");
        currentQuestion.put("content", question);
        messages.add(currentQuestion);
        
        requestBody.set("messages", messages);
        
        HttpEntity<String> request = new HttpEntity<>(
            objectMapper.writeValueAsString(requestBody), headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return extractTextFromResponse(jsonResponse);
        } else {
            throw new Exception("Erreur conversation contextuelle Copilot: " + response.getStatusCode());
        }
    }
    
    /**
     * Intégration avec Microsoft Graph (fonctionnalités Copilot avancées)
     */
    public String getCopilotWithGraphData(String question, AIProvider provider) throws Exception {
        // TODO: Implémenter l'intégration Microsoft Graph
        // Permet à Copilot d'accéder aux données Office 365, calendrier, emails, etc.
        
        /*
        // Configuration Graph API
        GraphServiceClient graphClient = GraphServiceClient.builder()
            .authenticationProvider(authProvider)
            .buildClient();
            
        // Récupérer données pertinentes selon la question
        if (question.toLowerCase().contains("réunion") || question.toLowerCase().contains("calendrier")) {
            // Récupérer événements du calendrier
            EventCollectionPage events = graphClient.me().events()
                .buildRequest()
                .top(10)
                .get();
                
            // Intégrer dans le prompt Copilot
        }
        */
        
        throw new UnsupportedOperationException("Intégration Microsoft Graph pas encore implémentée");
    }
    
    /**
     * Health check pour Copilot Speech
     */
    public boolean isHealthy() {
        try {
            String apiKey = System.getenv("AZURE_OPENAI_KEY");
            String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
            
            if (apiKey == null || endpoint == null) return false;
            
            String url = AZURE_OPENAI_URL
                .replace("{endpoint}", endpoint)
                .replace("{deployment}", "gpt-4"); // deployment par défaut
            
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
            System.err.println("Copilot Health Check Failed: " + e.getMessage());
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
package com.angel.voice.service;

import com.angel.voice.model.AIProvider;
import com.angel.voice.service.providers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AIProviderService {
    
    @Autowired
    private OpenAIRealtimeService openAIRealtimeService;
    
    @Autowired
    private GeminiLiveService geminiLiveService;
    
    @Autowired
    private CopilotSpeechService copilotSpeechService;
    
    @Autowired
    private ClaudeService claudeService;
    
    @Autowired
    private MistralService mistralService;
    
    @Autowired
    private TTSService ttsService;
    
    @Autowired
    private ConfigurationService configService;
    
    /**
     * Obtient une réponse de l'IA sélectionnée
     */
    public String getResponse(String question, AIProvider provider) throws Exception {
        System.out.println(String.format("Appel IA: %s pour question: %s", 
            provider.getName(), question.substring(0, Math.min(50, question.length()))));
        
        try {
            // Configuration du timeout
            int timeoutMs = configService.getAIConfig()
                .get("aiSelectionConfig")
                .get("timeoutMs")
                .asInt(5000);
            
            CompletableFuture<String> responseFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return callSpecificProvider(question, provider);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            
            return responseFuture.get(timeoutMs, TimeUnit.MILLISECONDS);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à " + provider.getName() + ": " + e.getMessage());
            throw new Exception("Échec de l'appel IA: " + provider.getName(), e);
        }
    }
    
    /**
     * Appelle le service spécifique selon le provider
     */
    private String callSpecificProvider(String question, AIProvider provider) throws Exception {
        switch (provider.getName().toLowerCase()) {
            case "openai_realtime":
                return openAIRealtimeService.getAudioResponse(question, provider);
                
            case "gemini_live":
                return geminiLiveService.getAudioResponse(question, provider);
                
            case "copilot_speech":
                return copilotSpeechService.getAudioResponse(question, provider);
                
            case "claude":
                return claudeService.getTextResponse(question, provider);
                
            case "mistral":
                return mistralService.getTextResponse(question, provider);
                
            default:
                throw new IllegalArgumentException("Provider non supporté: " + provider.getName());
        }
    }
    
    /**
     * Convertit du texte en audio via TTS
     */
    public String convertToSpeech(String text, AIProvider provider) throws Exception {
        if (!provider.needsTTS()) {
            return text; // Déjà en audio
        }
        
        System.out.println("Conversion TTS avec: " + provider.getTtsProvider());
        return ttsService.synthesizeSpeech(text, provider);
    }
    
    /**
     * Vérifie la disponibilité d'un provider
     */
    public boolean isProviderAvailable(AIProvider provider) {
        try {
            return callHealthCheck(provider);
        } catch (Exception e) {
            System.err.println("Provider " + provider.getName() + " non disponible: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Health check spécifique par provider
     */
    private boolean callHealthCheck(AIProvider provider) {
        switch (provider.getName().toLowerCase()) {
            case "openai_realtime":
                return openAIRealtimeService.isHealthy();
            case "gemini_live":
                return geminiLiveService.isHealthy();
            case "copilot_speech":
                return copilotSpeechService.isHealthy();
            case "claude":
                return claudeService.isHealthy();
            case "mistral":
                return mistralService.isHealthy();
            default:
                return false;
        }
    }
}
package com.angel.voice.service;

import com.angel.voice.model.AIProvider;
import com.angel.voice.service.providers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.angel.util.LogUtil;

@Service
public class AIProviderService {
    
    private static final Logger LOGGER = LogUtil.getLogger(AIProviderService.class);
    
    @Autowired
    private ConfigurationService configService;
    
    // Services providers (tous optionnels car contiennent leur logique hybride)
    @Autowired(required = false)
    private OpenAIRealtimeService openAIRealtimeService;
    
    @Autowired(required = false)
    private GeminiLiveService geminiLiveService;
    
    @Autowired(required = false)
    private CopilotSpeechService copilotSpeechService;
    
    @Autowired(required = false)
    private ClaudeService claudeService;
    
    @Autowired(required = false)
    private MistralService mistralService;
    
    @Autowired
    private TTSService ttsService;
    
    /**
     * Obtient une réponse de l'IA sélectionnée (délègue au service approprié)
     */
    public String getResponse(String question, AIProvider provider) throws Exception {
        LOGGER.log(Level.INFO, "Appel IA: {0} en mode {1} pour question: {2}", 
            new Object[]{provider.getName(), provider.getMode(), 
                        question.substring(0, Math.min(50, question.length()))});
        
        try {
            int timeoutMs = getTimeoutForProvider(provider);
            
            CompletableFuture<String> responseFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return callSpecificProvider(question, provider);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            
            return responseFuture.get(timeoutMs, TimeUnit.MILLISECONDS);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l''appel à {0}: {1}", 
                      new Object[]{provider.getName(), e.getMessage()});
            throw new Exception("Échec de l'appel IA: " + provider.getName(), e);
        }
    }
    
    /**
     * Délègue l'appel au service approprié (chaque service gère ses propres modes)
     */
    private String callSpecificProvider(String question, AIProvider provider) throws Exception {
        switch (provider.getName().toLowerCase()) {
            case "openai_realtime":
            case "openai_text":
                if (openAIRealtimeService == null) {
                    throw new IllegalStateException("OpenAIRealtimeService non disponible");
                }
                return openAIRealtimeService.getAudioResponse(question, provider);
                
            case "gemini_live":
                if (geminiLiveService == null) {
                    throw new IllegalStateException("GeminiLiveService non disponible");
                }
                return geminiLiveService.getAudioResponse(question, provider);
                
            case "copilot_speech":
                if (copilotSpeechService == null) {
                    throw new IllegalStateException("CopilotSpeechService non disponible");
                }
                return copilotSpeechService.getAudioResponse(question, provider);
                
            case "claude":
                if (claudeService == null) {
                    throw new IllegalStateException("ClaudeService non disponible");
                }
                return claudeService.getTextResponse(question, provider);
                
            case "mistral":
                if (mistralService == null) {
                    throw new IllegalStateException("MistralService non disponible");
                }
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
        
        LOGGER.log(Level.INFO, "Conversion TTS avec: {0}", provider.getTtsProvider());
        return ttsService.synthesizeSpeech(text, provider);
    }
    
    /**
     * Vérifie la disponibilité d'un provider (délègue au service)
     */
    public boolean isProviderAvailable(AIProvider provider) {
        try {
            switch (provider.getName().toLowerCase()) {
                case "openai_realtime":
                case "openai_text":
                    return openAIRealtimeService != null && 
                           openAIRealtimeService.isHealthy(provider);
                           
                case "gemini_live":
                    return geminiLiveService != null && 
                           geminiLiveService.isHealthy(provider);
                           
                case "copilot_speech":
                    return copilotSpeechService != null && 
                           copilotSpeechService.isHealthy(provider);
                           
                case "claude":
                    return claudeService != null && 
                           claudeService.isHealthy(provider);
                           
                case "mistral":
                    return mistralService != null && 
                           mistralService.isHealthy(provider);
                           
                default:
                    return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Provider {0} non disponible: {1}", 
                      new Object[]{provider.getName(), e.getMessage()});
            return false;
        }
    }
    
    /**
     * Obtient le timeout pour un provider
     */
    private int getTimeoutForProvider(AIProvider provider) {
        // Timeout selon le mode configuré
        String mode = provider.getMode();
        int defaultTimeout = "direct".equals(mode) ? 30000 : 10000;
        
        try {
            return configService.getAITimeout();
        } catch (Exception e) {
            return defaultTimeout;
        }
    }
    
    /**
     * Statistiques des appels par provider
     */
    public void logProviderCall(AIProvider provider, long duration, boolean success) {
        if (configService.isStatisticsEnabled()) {
            LOGGER.log(Level.INFO, "[STATS] Provider: {0}, Mode: {1}, Duration: {2}ms, Success: {3}", 
                      new Object[]{provider.getName(), provider.getMode(), duration, success});
        }
    }
}
package com.angel.voice.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class ConfigurationService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode aiConfig;
    private final Map<String, Object> configCache = new ConcurrentHashMap<>();
    
    @Value("${angel.voice.config.path:config/ai-config.json}")
    private String configPath;
    
    @Value("${angel.voice.config.reload.enabled:true}")
    private boolean reloadEnabled;
    
    @Value("${angel.voice.config.reload.interval:300000}") // 5 minutes
    private long reloadInterval;
    
    private long lastLoadTime = 0;
    
    /**
     * Initialisation du service de configuration
     */
    @PostConstruct
    public void init() {
        try {
            loadConfiguration();
            System.out.println("Configuration AI chargée depuis: " + configPath);
        } catch (Exception e) {
            System.err.println("Erreur chargement configuration: " + e.getMessage());
            throw new RuntimeException("Impossible de charger la configuration AI", e);
        }
    }
    
    /**
     * Charge la configuration depuis le fichier JSON
     */
    private void loadConfiguration() throws IOException {
        ClassPathResource resource = new ClassPathResource(configPath);
        if (!resource.exists()) {
            throw new IOException("Fichier de configuration non trouvé: " + configPath);
        }
        
        aiConfig = objectMapper.readTree(resource.getInputStream());
        lastLoadTime = System.currentTimeMillis();
        
        // Validation de la configuration
        validateConfiguration();
        
        // Vider le cache lors du rechargement
        configCache.clear();
        
        System.out.println("Configuration rechargée à: " + new java.util.Date(lastLoadTime));
    }
    
    /**
     * Obtient la configuration AI complète
     */
    public JsonNode getAIConfig() {
        checkAndReloadConfig();
        return aiConfig;
    }
    
    /**
     * Obtient une configuration spécifique par chemin
     */
    public JsonNode getConfigValue(String path) {
        checkAndReloadConfig();
        
        if (configCache.containsKey(path)) {
            return (JsonNode) configCache.get(path);
        }
        
        JsonNode value = navigateToPath(aiConfig, path);
        if (value != null) {
            configCache.put(path, value);
        }
        
        return value;
    }
    
    /**
     * Obtient un provider audio par nom
     */
    public JsonNode getAudioProvider(String providerName) {
        JsonNode providers = getConfigValue("audioProviders");
        return providers != null ? providers.get(providerName) : null;
    }
    
    /**
     * Obtient un provider texte par nom
     */
    public JsonNode getTextProvider(String providerName) {
        JsonNode providers = getConfigValue("textProviders");
        return providers != null ? providers.get(providerName) : null;
    }
    
    /**
     * Obtient la configuration TTS
     */
    public JsonNode getTTSConfig(String ttsProvider) {
        JsonNode ttsServices = getConfigValue("ttsServices");
        return ttsServices != null ? ttsServices.get(ttsProvider) : null;
    }
    
    /**
     * Obtient les mots-clés de complexité
     */
    public String[] getComplexityKeywords() {
        JsonNode keywords = getConfigValue("questionAnalysis.complexityKeywords");
        if (keywords != null && keywords.isArray()) {
            String[] result = new String[keywords.size()];
            for (int i = 0; i < keywords.size(); i++) {
                result[i] = keywords.get(i).asText();
            }
            return result;
        }
        return new String[0];
    }
    
    /**
     * Obtient les mots-clés simples
     */
    public String[] getSimpleKeywords() {
        JsonNode keywords = getConfigValue("questionAnalysis.simpleKeywords");
        if (keywords != null && keywords.isArray()) {
            String[] result = new String[keywords.size()];
            for (int i = 0; i < keywords.size(); i++) {
                result[i] = keywords.get(i).asText();
            }
            return result;
        }
        return new String[0];
    }
    
    /**
     * Obtient le seuil de complexité
     */
    public int getComplexityThreshold() {
        JsonNode threshold = getConfigValue("questionAnalysis.complexityThreshold");
        return threshold != null ? threshold.asInt(3) : 3;
    }
    
    /**
     * Obtient le timeout pour les appels AI
     */
    public int getAITimeout() {
        JsonNode timeout = getConfigValue("aiSelectionConfig.timeoutMs");
        return timeout != null ? timeout.asInt(5000) : 5000;
    }
    
    /**
     * Vérifie si le fallback est activé
     */
    public boolean isFallbackEnabled() {
        JsonNode fallback = getConfigValue("aiSelectionConfig.fallbackOnError");
        return fallback != null ? fallback.asBoolean(true) : true;
    }
    
    /**
     * Obtient le nombre max de retry
     */
    public int getMaxRetries() {
        JsonNode retries = getConfigValue("aiSelectionConfig.maxRetries");
        return retries != null ? retries.asInt(2) : 2;
    }
    
    /**
     * Vérifie si le tracking statistique est activé
     */
    public boolean isStatisticsEnabled() {
        JsonNode stats = getConfigValue("statisticsTracking.enabled");
        return stats != null ? stats.asBoolean(true) : true;
    }
    
    /**
     * Navigation dans le JSON avec chemin point-séparé
     */
    private JsonNode navigateToPath(JsonNode root, String path) {
        if (path == null || path.isEmpty()) {
            return root;
        }
        
        String[] parts = path.split("\\.");
        JsonNode current = root;
        
        for (String part : parts) {
            if (current == null || !current.has(part)) {
                return null;
            }
            current = current.get(part);
        }
        
        return current;
    }
    
    /**
     * Vérifie si la config doit être rechargée
     */
    private void checkAndReloadConfig() {
        if (!reloadEnabled) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLoadTime > reloadInterval) {
            try {
                loadConfiguration();
                System.out.println("Configuration automatiquement rechargée");
            } catch (Exception e) {
                System.err.println("Erreur rechargement automatique config: " + e.getMessage());
                // Continue avec l'ancienne configuration
            }
        }
    }
    
    /**
     * Recharge manuellement la configuration
     */
    public boolean reloadConfiguration() {
        try {
            loadConfiguration();
            return true;
        } catch (Exception e) {
            System.err.println("Erreur rechargement manuel config: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validation de la structure de configuration
     */
    private void validateConfiguration() {
        // Vérifications essentielles
        if (!aiConfig.has("audioProviders")) {
            throw new IllegalStateException("Configuration manquante: audioProviders");
        }
        
        if (!aiConfig.has("textProviders")) {
            throw new IllegalStateException("Configuration manquante: textProviders");
        }
        
        if (!aiConfig.has("questionAnalysis")) {
            throw new IllegalStateException("Configuration manquante: questionAnalysis");
        }
        
        if (!aiConfig.has("ttsServices")) {
            throw new IllegalStateException("Configuration manquante: ttsServices");
        }
        
        // Validation des providers audio
        JsonNode audioProviders = aiConfig.get("audioProviders");
        audioProviders.fieldNames().forEachRemaining(providerName -> {
            JsonNode provider = audioProviders.get(providerName);
            validateProvider(provider, providerName, "audio");
        });
        
        // Validation des providers texte
        JsonNode textProviders = aiConfig.get("textProviders");
        textProviders.fieldNames().forEachRemaining(providerName -> {
            JsonNode provider = textProviders.get(providerName);
            validateProvider(provider, providerName, "text");
        });
        
        System.out.println("Configuration validée avec succès");
    }
    
    /**
     * Validation d'un provider individuel
     */
    private void validateProvider(JsonNode provider, String name, String type) {
        String[] requiredFields = {"enabled", "priority", "weight", "apiKey", "model"};
        
        for (String field : requiredFields) {
            if (!provider.has(field)) {
                throw new IllegalStateException(
                    String.format("Configuration manquante pour %s provider '%s': %s", 
                        type, name, field));
            }
        }
        
        // Validation des valeurs
        int priority = provider.get("priority").asInt();
        int weight = provider.get("weight").asInt();
        
        if (priority < 1) {
            throw new IllegalStateException(
                String.format("Priority invalide pour %s provider '%s': %d", type, name, priority));
        }
        
        if (weight < 1) {
            throw new IllegalStateException(
                String.format("Weight invalide pour %s provider '%s': %d", type, name, weight));
        }
    }
    
    /**
     * Obtient les statistiques de la configuration
     */
    public Map<String, Object> getConfigurationStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("configPath", configPath);
        stats.put("lastLoadTime", new java.util.Date(lastLoadTime));
        stats.put("reloadEnabled", reloadEnabled);
        stats.put("reloadInterval", reloadInterval);
        stats.put("cacheSize", configCache.size());
        
        // Compter les providers
        JsonNode audioProviders = aiConfig.get("audioProviders");
        JsonNode textProviders = aiConfig.get("textProviders");
        
        int audioCount = 0, textCount = 0;
        if (audioProviders != null) {
            audioProviders.fieldNames().forEachRemaining(name -> {});
            audioCount = audioProviders.size();
        }
        if (textProviders != null) {
            textCount = textProviders.size();
        }
        
        stats.put("audioProvidersCount", audioCount);
        stats.put("textProvidersCount", textCount);
        
        return stats;
    }
}
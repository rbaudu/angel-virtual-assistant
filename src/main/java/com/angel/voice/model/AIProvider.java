package com.angel.voice.model;

import java.util.Map;

import com.angel.voice.service.AISelectionService.QuestionType;

/**
 * Modèle représentant un fournisseur d'IA avec support des modes direct/API
 */
public class AIProvider {
    private String name;
    private QuestionType type;
    private int priority;
    private int weight;
    private String mode; // "direct" ou "api"
    private String apiKey;
    private String model;
    private int maxTokens;
    private double temperature;
    private String voice;
    private String responseFormat;
    private String ttsProvider;
    private String endpoint;
    private Map<String, String> headers;
    private String systemPrompt;
    private boolean enabled;
    
    // Constructeurs
    public AIProvider() {
        this.enabled = true;
        this.maxTokens = 150;
        this.temperature = 0.7;
        this.responseFormat = "text";
        this.mode = "direct"; // Mode par défaut
    }
    
    public AIProvider(String name, QuestionType type) {
        this();
        this.name = name;
        this.type = type;
    }
    
    public AIProvider(String name, QuestionType type, String mode) {
        this(name, type);
        this.mode = mode;
    }
    
    // Getters et Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public QuestionType getType() {
        return type;
    }
    
    public void setType(QuestionType type) {
        this.type = type;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    /**
     * GETTER MODE - avec fallback sécurisé
     */
    public String getMode() {
        // Retourner mode par défaut si null
        return mode != null ? mode : "direct";
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public String getVoice() {
        return voice;
    }
    
    public void setVoice(String voice) {
        this.voice = voice;
    }
    
    public String getResponseFormat() {
        return responseFormat;
    }
    
    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }
    
    public String getTtsProvider() {
        return ttsProvider;
    }
    
    public void setTtsProvider(String ttsProvider) {
        this.ttsProvider = ttsProvider;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    /**
     * GETTER SYSTEM PROMPT - avec fallback sécurisé
     */
    public String getSystemPrompt() {
        return systemPrompt != null ? systemPrompt : "";
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    // Méthodes utilitaires
    
    /**
     * Vérifie si ce provider peut générer de l'audio directement
     */
    public boolean isAudioCapable() {
        return "audio".equals(responseFormat);
    }
    
    /**
     * Vérifie si ce provider nécessite un service TTS externe
     */
    public boolean needsTTS() {
        return "text".equals(responseFormat) && ttsProvider != null && !ttsProvider.isEmpty();
    }
    
    /**
     * Vérifie si ce provider utilise le mode direct (HTTP) - MÉTHODE SÉCURISÉE
     */
    public boolean isDirectMode() {
        return "direct".equals(getMode());
    }
    
    /**
     * Vérifie si ce provider utilise le mode API (Services Spring) - MÉTHODE SÉCURISÉE
     */
    public boolean isAPIMode() {
        return "api".equals(getMode());
    }
    
    /**
     * Détermine le mode effectif selon la disponibilité des clés API
     */
    public String getEffectiveMode() {
        String configuredMode = getMode();
        
        // Si pas de clé API, forcer le mode direct n'est pas viable
        String resolvedApiKey = resolveApiKey(this.apiKey);
        if (resolvedApiKey == null || resolvedApiKey.isEmpty()) {
            // Pas de clé API disponible
            if ("api".equals(configuredMode)) {
                // Le mode API pourrait avoir des fallbacks, garder la config
                return configuredMode;
            } else {
                // Mode direct impossible sans clé
                return null; // Provider non viable
            }
        }
        
        // Clé API disponible, utiliser le mode configuré
        return configuredMode;
    }
    
    /**
     * Vérifie si ce provider est configuré correctement
     */
    public boolean isValidConfiguration() {
        if (!enabled) return false;
        if (name == null || name.trim().isEmpty()) return false;
        if (model == null || model.trim().isEmpty()) return false;
        if (priority < 1 || weight < 1) return false;
        
        String effectiveMode = getEffectiveMode();
        if (effectiveMode == null) return false;
        
        // En mode direct, l'endpoint est requis
        if ("direct".equals(effectiveMode)) {
            if (endpoint == null || endpoint.trim().isEmpty()) return false;
        }
        
        // Validation spécifique selon le type
        if (type == QuestionType.SIMPLE_AUDIO) {
            return isAudioCapable() || needsTTS();
        } else if (type == QuestionType.COMPLEX_TEXT) {
            return needsTTS() || isAudioCapable();
        }
        
        return true;
    }
    
    /**
     * Crée une copie avec des paramètres modifiés pour fallback
     */
    public AIProvider createFallbackProvider() {
        AIProvider fallback = new AIProvider();
        fallback.setName(this.name + "_fallback");
        fallback.setType(this.type);
        fallback.setMode("direct"); // Fallback toujours en mode direct
        fallback.setApiKey(this.apiKey);
        fallback.setModel(this.model);
        fallback.setEndpoint(this.endpoint);
        fallback.setHeaders(this.headers);
        fallback.setSystemPrompt(this.systemPrompt);
        fallback.setTtsProvider(this.ttsProvider);
        fallback.setVoice(this.voice);
        fallback.setResponseFormat(this.responseFormat);
        
        // Paramètres plus conservateurs pour le fallback
        fallback.setPriority(this.priority + 10);
        fallback.setWeight(Math.max(1, this.weight / 2));
        fallback.setMaxTokens(Math.min(100, this.maxTokens));
        fallback.setTemperature(0.3);
        fallback.setEnabled(true);
        
        return fallback;
    }
    
    /**
     * Met à jour les paramètres depuis une configuration JSON
     */
    public void updateFromConfig(com.fasterxml.jackson.databind.JsonNode config) {
        if (config.has("enabled")) {
            this.enabled = config.get("enabled").asBoolean();
        }
        if (config.has("priority")) {
            this.priority = config.get("priority").asInt();
        }
        if (config.has("weight")) {
            this.weight = config.get("weight").asInt();
        }
        if (config.has("mode")) {
            this.mode = config.get("mode").asText();
        }
        if (config.has("apiKey")) {
            this.apiKey = config.get("apiKey").asText();
        }
        if (config.has("model")) {
            this.model = config.get("model").asText();
        }
        if (config.has("maxTokens")) {
            this.maxTokens = config.get("maxTokens").asInt();
        }
        if (config.has("temperature")) {
            this.temperature = config.get("temperature").asDouble();
        }
        if (config.has("voice")) {
            this.voice = config.get("voice").asText();
        }
        if (config.has("ttsProvider")) {
            this.ttsProvider = config.get("ttsProvider").asText();
        }
        if (config.has("endpoint")) {
            this.endpoint = config.get("endpoint").asText();
        }
        if (config.has("systemPrompt")) {
            this.systemPrompt = config.get("systemPrompt").asText();
        }
        if (config.has("responseFormat")) {
            this.responseFormat = config.get("responseFormat").asText();
        }
    }
    
    /**
     * Résolution simple des variables d'environnement pour validation
     */
    private String resolveApiKey(String apiKeyTemplate) {
        if (apiKeyTemplate == null) return null;
        
        if (apiKeyTemplate.startsWith("${") && apiKeyTemplate.endsWith("}")) {
            String envVar = apiKeyTemplate.substring(2, apiKeyTemplate.length() - 1);
            return System.getenv(envVar);
        }
        
        return apiKeyTemplate;
    }
    
    // Méthodes Object standard
    
    @Override
    public String toString() {
        return String.format("AIProvider{name='%s', type=%s, mode='%s', priority=%d, weight=%d, enabled=%b, model='%s'}", 
            name, type, getMode(), priority, weight, enabled, model);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AIProvider that = (AIProvider) obj;
        return name != null && name.equals(that.name) && 
               type == that.type &&
               getMode().equals(that.getMode());
    }
    
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + getMode().hashCode();
        return result;
    }
    
    /**
     * Crée une représentation JSON pour logging/debug
     */
    public String toJsonString() {
        return String.format(
            "{\"name\":\"%s\",\"type\":\"%s\",\"mode\":\"%s\",\"priority\":%d,\"weight\":%d," +
            "\"enabled\":%b,\"model\":\"%s\",\"responseFormat\":\"%s\",\"endpoint\":\"%s\"}",
            name, type, getMode(), priority, weight, enabled, model, responseFormat, 
            endpoint != null ? endpoint : "null"
        );
    }
    
    /**
     * Compare les providers par priorité puis par poids
     */
    public int compareTo(AIProvider other) {
        if (other == null) return 1;
        
        // D'abord par priorité (plus petit = plus prioritaire)
        int priorityCompare = Integer.compare(this.priority, other.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        
        // Ensuite par poids (plus grand = plus de chance d'être sélectionné)
        return Integer.compare(other.weight, this.weight);
    }
}
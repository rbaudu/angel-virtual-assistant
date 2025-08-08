package com.angel.voice.model;

import com.angel.voice.service.AISelectionService.QuestionType;

/**
 * Modèle représentant un fournisseur d'IA avec ses paramètres de configuration
 */
public class AIProvider {
    private String name;
    private QuestionType type;
    private int priority;
    private int weight;
    private String apiKey;
    private String model;
    private int maxTokens;
    private double temperature;
    private String voice;
    private String responseFormat;
    private String ttsProvider;
    private String endpoint;
    private boolean enabled;
    
    // Constructeurs
    public AIProvider() {
        this.enabled = true;
        this.maxTokens = 150;
        this.temperature = 0.7;
        this.responseFormat = "text";
    }
    
    public AIProvider(String name, QuestionType type) {
        this();
        this.name = name;
        this.type = type;
    }
    
    public AIProvider(String name, QuestionType type, int priority, int weight) {
        this(name, type);
        this.priority = priority;
        this.weight = weight;
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
     * Vérifie si ce provider est configuré correctement
     */
    public boolean isValidConfiguration() {
        if (!enabled) return false;
        if (name == null || name.trim().isEmpty()) return false;
        if (apiKey == null || apiKey.trim().isEmpty()) return false;
        if (model == null || model.trim().isEmpty()) return false;
        if (priority < 1 || weight < 1) return false;
        
        // Validation spécifique selon le type
        if (type == QuestionType.SIMPLE_AUDIO) {
            // Pour l'audio, soit capable d'audio direct, soit TTS configuré
            return isAudioCapable() || needsTTS();
        } else if (type == QuestionType.COMPLEX_TEXT) {
            // Pour le texte, doit avoir TTS configuré si pas audio direct
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
        fallback.setApiKey(this.apiKey);
        fallback.setModel(this.model);
        fallback.setEndpoint(this.endpoint);
        fallback.setTtsProvider(this.ttsProvider);
        fallback.setVoice(this.voice);
        fallback.setResponseFormat(this.responseFormat);
        
        // Paramètres plus conservateurs pour le fallback
        fallback.setPriority(this.priority + 10);
        fallback.setWeight(Math.max(1, this.weight / 2));
        fallback.setMaxTokens(Math.min(100, this.maxTokens));
        fallback.setTemperature(0.3); // Plus conservateur
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
    }
    
    // Méthodes Object standard
    
    @Override
    public String toString() {
        return String.format("AIProvider{name='%s', type=%s, priority=%d, weight=%d, enabled=%b, model='%s'}", 
            name, type, priority, weight, enabled, model);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AIProvider that = (AIProvider) obj;
        return name != null && name.equals(that.name) && 
               type == that.type;
    }
    
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
    
    /**
     * Crée une représentation JSON pour logging/debug
     */
    public String toJsonString() {
        return String.format(
            "{\"name\":\"%s\",\"type\":\"%s\",\"priority\":%d,\"weight\":%d," +
            "\"enabled\":%b,\"model\":\"%s\",\"responseFormat\":\"%s\"}",
            name, type, priority, weight, enabled, model, responseFormat
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
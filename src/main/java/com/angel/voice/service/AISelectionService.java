package com.angel.voice.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.angel.voice.model.AIProvider;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class AISelectionService {
    
    @Autowired
    private ConfigurationService configService;
    
    private final SecureRandom random = new SecureRandom();
    
    public enum QuestionType {
        SIMPLE_AUDIO,    // Questions simples -> Audio direct
        COMPLEX_TEXT     // Questions complexes -> Text + TTS
    }
    
    /**
     * Analyse la complexité de la question
     */
    public QuestionType analyzeQuestionComplexity(String question) {
        JsonNode config = configService.getAIConfig();
        JsonNode analysis = config.get("questionAnalysis");
        
        String questionLower = question.toLowerCase();
        int complexityScore = 0;
        
        // Vérification mots-clés complexes
        JsonNode complexKeywords = analysis.get("complexityKeywords");
        if (complexKeywords != null) {
            for (JsonNode keyword : complexKeywords) {
                if (questionLower.contains(keyword.asText())) {
                    complexityScore += 2;
                }
            }
        }
        
        // Vérification mots-clés simples
        JsonNode simpleKeywords = analysis.get("simpleKeywords");
        if (simpleKeywords != null) {
            for (JsonNode keyword : simpleKeywords) {
                if (questionLower.contains(keyword.asText())) {
                    complexityScore -= 1;
                }
            }
        }
        
        // Critères supplémentaires
        if (question.length() > 100) complexityScore++;
        if (question.contains("?") && question.split("\\?").length > 1) complexityScore++;
        if (questionLower.matches(".*\\b(pourquoi|comment|expliqu).*")) complexityScore += 2;
        
        int threshold = analysis.get("complexityThreshold").asInt(3);
        return complexityScore >= threshold ? QuestionType.COMPLEX_TEXT : QuestionType.SIMPLE_AUDIO;
    }
    
    /**
     * Sélectionne une IA selon la pondération configurée
     */
    public AIProvider selectProvider(QuestionType questionType) {
        JsonNode config = configService.getAIConfig();
        String providersKey = questionType == QuestionType.SIMPLE_AUDIO ? 
            "audioProviders" : "textProviders";
        
        JsonNode providers = config.get(providersKey);
        if (providers == null) {
            throw new IllegalStateException("Aucune configuration trouvée pour: " + providersKey);
        }
        
        List<WeightedProvider> weightedProviders = new ArrayList<>();
        
        // Construire la liste pondérée
        providers.fieldNames().forEachRemaining(providerName -> {
            JsonNode provider = providers.get(providerName);
            if (provider.get("enabled").asBoolean(true)) {
                int weight = provider.get("weight").asInt(10);
                weightedProviders.add(new WeightedProvider(providerName, provider, weight));
            }
        });
        
        if (weightedProviders.isEmpty()) {
            throw new IllegalStateException("Aucun fournisseur IA disponible pour: " + questionType);
        }
        
        // Sélection pondérée aléatoire
        AIProvider selected = selectWeightedRandom(weightedProviders, questionType);
        
        // Log de la sélection
        logSelection(selected.getName(), questionType);
        
        return selected;
    }
    
    /**
     * Algorithme de sélection pondérée aléatoire
     */
    private AIProvider selectWeightedRandom(List<WeightedProvider> providers, QuestionType questionType) {
        // Calcul du poids total
        int totalWeight = providers.stream().mapToInt(WeightedProvider::getWeight).sum();
        
        // Génération nombre aléatoire
        int randomValue = random.nextInt(totalWeight);
        
        // Sélection selon les poids
        int currentWeight = 0;
        for (WeightedProvider wp : providers) {
            currentWeight += wp.getWeight();
            if (randomValue < currentWeight) {
                return createAIProvider(wp.getName(), wp.getConfig(), questionType);
            }
        }
        
        // Fallback (ne devrait jamais arriver)
        WeightedProvider first = providers.get(0);
        return createAIProvider(first.getName(), first.getConfig(), questionType);
    }
    
    /**
     * Création de l'objet AIProvider avec gestion sécurisée des propriétés
     */
    private AIProvider createAIProvider(String name, JsonNode config, QuestionType questionType) {
        AIProvider provider = new AIProvider();
        provider.setName(name);
        provider.setType(questionType);
        
        // Propriétés obligatoires avec valeurs par défaut
        provider.setPriority(config.has("priority") ? config.get("priority").asInt() : 999);
        provider.setWeight(config.has("weight") ? config.get("weight").asInt() : 1);
        provider.setEnabled(config.has("enabled") ? config.get("enabled").asBoolean() : true);
        
        // Propriétés avec gestion null-safe
        if (config.has("mode")) {
            provider.setMode(config.get("mode").asText());
        }
        // Si pas de mode défini, garder la valeur par défaut "direct"
        
        if (config.has("apiKey")) {
            provider.setApiKey(config.get("apiKey").asText());
        }
        
        if (config.has("model")) {
            provider.setModel(config.get("model").asText());
        }
        
        if (config.has("endpoint")) {
            provider.setEndpoint(config.get("endpoint").asText());
        }
        
        if (config.has("systemPrompt")) {
            provider.setSystemPrompt(config.get("systemPrompt").asText());
        }
        
        // Propriétés numériques avec valeurs par défaut
        provider.setMaxTokens(config.has("maxTokens") ? config.get("maxTokens").asInt() : 150);
        provider.setTemperature(config.has("temperature") ? config.get("temperature").asDouble() : 0.7);
        
        // Configuration selon le type de question
        if (questionType == QuestionType.SIMPLE_AUDIO) {
            provider.setResponseFormat(config.has("responseFormat") ? 
                config.get("responseFormat").asText() : "audio");
            if (config.has("voice")) {
                provider.setVoice(config.get("voice").asText());
            }
        } else {
            provider.setResponseFormat("text");
            if (config.has("ttsProvider")) {
                provider.setTtsProvider(config.get("ttsProvider").asText());
            }
            if (config.has("voice")) {
                provider.setVoice(config.get("voice").asText());
            }
        }
        
        return provider;
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
     * Log des sélections pour statistiques
     */
    private void logSelection(String providerName, QuestionType questionType) {
        try {
            JsonNode config = configService.getAIConfig();
            JsonNode statsConfig = config.get("statisticsTracking");
            if (statsConfig != null && statsConfig.get("logSelections").asBoolean(true)) {
                System.out.println(String.format(
                    "[AI_SELECTION] Provider: %s, Type: %s, Time: %d", 
                    providerName, questionType, System.currentTimeMillis()
                ));
            }
        } catch (Exception e) {
            // Ne pas faire échouer la sélection pour un problème de log
            System.err.println("Erreur logging sélection IA: " + e.getMessage());
        }
    }
    
    /**
     * Obtenir les statistiques de sélection
     */
    public Map<String, Integer> getSelectionStatistics() {
        // Implémentation pour tracking des statistiques
        // Pourrait être stocké en base ou en cache
        return new HashMap<>();
    }
    
    /**
     * Validation qu'un provider est utilisable
     */
    public boolean isProviderUsable(AIProvider provider) {
        if (provider == null || !provider.isEnabled()) {
            return false;
        }
        
        // Vérifier que les propriétés essentielles sont définies
        if (provider.getName() == null || provider.getModel() == null) {
            return false;
        }
        
        // En mode direct, vérifier que l'endpoint est défini
        if ("direct".equals(provider.getMode()) && provider.getEndpoint() == null) {
            return false;
        }
        
        return provider.isValidConfiguration();
    }
    
    /**
     * Classe interne pour la pondération
     */
    private static class WeightedProvider {
        private final String name;
        private final JsonNode config;
        private final int weight;
        
        public WeightedProvider(String name, JsonNode config, int weight) {
            this.name = name;
            this.config = config;
            this.weight = weight;
        }
        
        public String getName() { return name; }
        public JsonNode getConfig() { return config; }
        public int getWeight() { return weight; }
    }
}
package com.angel.test;

import com.angel.model.Activity;
import com.angel.util.LogUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Gestionnaire de scénarios pour la simulation d'activités.
 * Charge et exécute des séquences prédéfinies d'activités.
 */
@Component
public class ScenarioManager {
    
    private static final Logger logger = LogUtil.getLogger(ScenarioManager.class);
    
    private final Random random = new Random();
    private ScenarioData scenarioData;
    private String currentScenarioId;
    private int currentActivityIndex = 0;
    private LocalDateTime scenarioStartTime;
    
    /**
     * Charge les scénarios depuis un fichier JSON.
     */
    public void loadScenarios(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File scenarioFile = new File(filePath);
        
        if (!scenarioFile.exists()) {
            logger.warning("Fichier de scénarios introuvable: " + filePath);
            createDefaultScenarios();
            return;
        }
        
        try {
            scenarioData = mapper.readValue(scenarioFile, ScenarioData.class);
            logger.info("Scénarios chargés depuis: " + filePath);
            logger.info("Scénarios disponibles: " + scenarioData.getScenarios().keySet());
        } catch (IOException e) {
            logger.severe("Erreur lors du chargement des scénarios: " + e.getMessage());
            createDefaultScenarios();
        }
    }
    
    /**
     * Exécute un scénario donné.
     */
    public void executeScenario(String scenarioName) {
        if (loadScenario(scenarioName)) {
            logger.info("Exécution du scénario: " + scenarioName);
        } else {
            logger.warning("Impossible d'exécuter le scénario: " + scenarioName);
            // Charger un scénario par défaut
            loadScenario("default");
        }
    }
    
    /**
     * Sélectionne et démarre un scénario.
     */
    public boolean loadScenario(String scenarioId) {
        if (scenarioData == null) {
            createDefaultScenarios();
        }
        
        if (!scenarioData.getScenarios().containsKey(scenarioId)) {
            logger.warning("Scénario introuvable: " + scenarioId);
            return false;
        }
        
        currentScenarioId = scenarioId;
        currentActivityIndex = 0;
        scenarioStartTime = LocalDateTime.now();
        
        Scenario scenario = scenarioData.getScenarios().get(scenarioId);
        logger.info(String.format("Scénario chargé: %s - %s", scenario.getName(), scenario.getDescription()));
        
        return true;
    }
    
    /**
     * Retourne la prochaine activité du scénario en cours.
     */
    public Activity getNextActivityFromScenario() {
        if (currentScenarioId == null || scenarioData == null) {
            logger.warning("Aucun scénario actif, retour à une activité aléatoire");
            return getRandomActivity();
        }
        
        Scenario scenario = scenarioData.getScenarios().get(currentScenarioId);
        if (scenario == null) {
            logger.warning("Scénario actuel introuvable: " + currentScenarioId);
            return getRandomActivity();
        }
        
        // Vérifier si c'est un scénario aléatoire
        if ("RANDOM_FROM_ALL".equals(scenario.getActivities())) {
            return getRandomActivity();
        }
        
        // Obtenir la liste des activités du scénario
        @SuppressWarnings("unchecked")
        List<ActivityItem> activities = (List<ActivityItem>) scenario.getActivities();
        
        if (activities == null || activities.isEmpty()) {
            logger.warning("Scénario sans activités: " + currentScenarioId);
            return getRandomActivity();
        }
        
        // Boucler sur les activités du scénario
        ActivityItem item = activities.get(currentActivityIndex % activities.size());
        currentActivityIndex++;
        
        try {
            Activity activity = Activity.valueOf(item.getActivity());
            logger.info(String.format("Activité du scénario: %s (%s)", 
                activity.getFrenchName(), item.getDescription()));
            return activity;
        } catch (IllegalArgumentException e) {
            logger.warning("Activité inconnue dans le scénario: " + item.getActivity());
            return getRandomActivity();
        }
    }
    
    /**
     * Retourne la liste des scénarios disponibles.
     */
    public List<ScenarioInfo> getAvailableScenarios() {
        List<ScenarioInfo> scenarios = new ArrayList<>();
        
        if (scenarioData != null && scenarioData.getScenarios() != null) {
            for (Map.Entry<String, Scenario> entry : scenarioData.getScenarios().entrySet()) {
                scenarios.add(new ScenarioInfo(
                    entry.getKey(),
                    entry.getValue().getName(),
                    entry.getValue().getDescription()
                ));
            }
        }
        
        return scenarios;
    }
    
    /**
     * Retourne des informations sur le scénario en cours.
     */
    public CurrentScenarioInfo getCurrentScenarioInfo() {
        if (currentScenarioId == null) {
            return null;
        }
        
        Scenario scenario = scenarioData.getScenarios().get(currentScenarioId);
        if (scenario == null) {
            return null;
        }
        
        return new CurrentScenarioInfo(
            currentScenarioId,
            scenario.getName(),
            scenario.getDescription(),
            currentActivityIndex,
            scenarioStartTime
        );
    }
    
    /**
     * Arrête le scénario en cours.
     */
    public void stopCurrentScenario() {
        if (currentScenarioId != null) {
            logger.info("Arrêt du scénario: " + currentScenarioId);
            currentScenarioId = null;
            currentActivityIndex = 0;
            scenarioStartTime = null;
        }
    }
    
    /**
     * Retourne une activité aléatoire parmi toutes les activités disponibles.
     */
    private Activity getRandomActivity() {
        if (scenarioData != null && scenarioData.getGlobalSettings() != null 
            && scenarioData.getGlobalSettings().getAllowedActivities() != null) {
            
            List<String> allowedActivities = scenarioData.getGlobalSettings().getAllowedActivities();
            String activityName = allowedActivities.get(random.nextInt(allowedActivities.size()));
            
            try {
                return Activity.valueOf(activityName);
            } catch (IllegalArgumentException e) {
                logger.warning("Activité autorisée inconnue: " + activityName);
            }
        }
        
        // Fallback vers les activités de base
        Activity[] basicActivities = {
            Activity.EATING, Activity.READING, Activity.WATCHING_TV, 
            Activity.WAITING, Activity.COOKING, Activity.CLEANING
        };
        return basicActivities[random.nextInt(basicActivities.length)];
    }
    
    /**
     * Crée des scénarios par défaut si aucun fichier n'est trouvé.
     */
    private void createDefaultScenarios() {
        logger.info("Création de scénarios par défaut");
        
        scenarioData = new ScenarioData();
        
        // Scénario simple par défaut
        Scenario defaultScenario = new Scenario();
        defaultScenario.setName("Activités de base");
        defaultScenario.setDescription("Scénario par défaut avec activités courantes");
        defaultScenario.setActivities("RANDOM_FROM_ALL");
        
        scenarioData.getScenarios().put("default", defaultScenario);
        
        // Configuration globale par défaut
        GlobalSettings globalSettings = new GlobalSettings();
        globalSettings.setAllowedActivities(Arrays.asList(
            "EATING", "READING", "WATCHING_TV", "COOKING", 
            "CLEANING", "WAITING", "USING_SCREEN", "WASHING"
        ));
        globalSettings.setDefaultConfidence(0.75);
        
        scenarioData.setGlobalSettings(globalSettings);
    }
    
    // Classes pour la désérialisation JSON
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScenarioData {
        @JsonProperty("scenarios")
        private Map<String, Scenario> scenarios = new java.util.HashMap<>();
        
        @JsonProperty("globalSettings")
        private GlobalSettings globalSettings;
        
        public Map<String, Scenario> getScenarios() { return scenarios; }
        public void setScenarios(Map<String, Scenario> scenarios) { this.scenarios = scenarios; }
        
        public GlobalSettings getGlobalSettings() { return globalSettings; }
        public void setGlobalSettings(GlobalSettings globalSettings) { this.globalSettings = globalSettings; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Scenario {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("activities")
        private Object activities; // Peut être une List<ActivityItem> ou une String "RANDOM_FROM_ALL"
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Object getActivities() { return activities; }
        public void setActivities(Object activities) { this.activities = activities; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivityItem {
        @JsonProperty("activity")
        private String activity;
        
        @JsonProperty("duration")
        private long duration;
        
        @JsonProperty("confidence")
        private double confidence;
        
        @JsonProperty("description")
        private String description;
        
        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GlobalSettings {
        @JsonProperty("allowedActivities")
        private List<String> allowedActivities;
        
        @JsonProperty("defaultConfidence")
        private double defaultConfidence;
        
        public List<String> getAllowedActivities() { return allowedActivities; }
        public void setAllowedActivities(List<String> allowedActivities) { this.allowedActivities = allowedActivities; }
        
        public double getDefaultConfidence() { return defaultConfidence; }
        public void setDefaultConfidence(double defaultConfidence) { this.defaultConfidence = defaultConfidence; }
    }
    
    // Classes d'information
    
    public static class ScenarioInfo {
        private final String id;
        private final String name;
        private final String description;
        
        public ScenarioInfo(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    public static class CurrentScenarioInfo {
        private final String id;
        private final String name;
        private final String description;
        private final int currentIndex;
        private final LocalDateTime startTime;
        
        public CurrentScenarioInfo(String id, String name, String description, int currentIndex, LocalDateTime startTime) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.currentIndex = currentIndex;
            this.startTime = startTime;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getCurrentIndex() { return currentIndex; }
        public LocalDateTime getStartTime() { return startTime; }
    }
}
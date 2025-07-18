package com.angel.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Configuration pour le mode test de l'assistant virtuel.
 * Permet de simuler des activités sans dépendre du serveur dl4j-server-capture.
 */
@Component
public class TestModeConfig {
    
    @JsonProperty("enabled")
    private boolean enabled = false;
    
    @JsonProperty("autoStart")
    private boolean autoStart = true;
    
    @JsonProperty("simulation")
    private SimulationConfig simulation = new SimulationConfig();
    
    @JsonProperty("activities")
    private ActivitiesConfig activities = new ActivitiesConfig();
    
    @JsonProperty("schedule")
    private ScheduleConfig schedule = new ScheduleConfig();
    
    @JsonProperty("web")
    private WebConfig web = new WebConfig();
    
    @JsonProperty("logging")
    private LoggingConfig logging = new LoggingConfig();

    /**
     * Charge la configuration depuis un fichier JSON.
     */
    public static TestModeConfig loadFromFile(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File configFile = new File(filePath);
        
        if (!configFile.exists()) {
            throw new IOException("Fichier de configuration introuvable: " + filePath);
        }
        
        TestModeConfigWrapper wrapper = mapper.readValue(configFile, TestModeConfigWrapper.class);
        return wrapper.getTestMode();
    }
    
    // Getters et setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public boolean isAutoStart() { return autoStart; }
    public void setAutoStart(boolean autoStart) { this.autoStart = autoStart; }
    
    public SimulationConfig getSimulation() { return simulation; }
    public void setSimulation(SimulationConfig simulation) { this.simulation = simulation; }
    
    public ActivitiesConfig getActivities() { return activities; }
    public void setActivities(ActivitiesConfig activities) { this.activities = activities; }
    
    public ScheduleConfig getSchedule() { return schedule; }
    public void setSchedule(ScheduleConfig schedule) { this.schedule = schedule; }
    
    public WebConfig getWeb() { return web; }
    public void setWeb(WebConfig web) { this.web = web; }
    
    public LoggingConfig getLogging() { return logging; }
    public void setLogging(LoggingConfig logging) { this.logging = logging; }
    
    /**
     * Configuration de la simulation.
     */
    public static class SimulationConfig {
        @JsonProperty("interval")
        private long interval = 30000; // 30 secondes
        
        @JsonProperty("randomness")
        private double randomness = 0.2; // 20% de variation
        
        @JsonProperty("scenarioFile")
        private String scenarioFile = "config/test/activity-scenarios.json";
        
        // Getters et setters
        public long getInterval() { return interval; }
        public void setInterval(long interval) { this.interval = interval; }
        
        public double getRandomness() { return randomness; }
        public void setRandomness(double randomness) { this.randomness = randomness; }
        
        public String getScenarioFile() { return scenarioFile; }
        public void setScenarioFile(String scenarioFile) { this.scenarioFile = scenarioFile; }
    }
    
    /**
     * Configuration des activités.
     */
    public static class ActivitiesConfig {
        @JsonProperty("sequence")
        private String sequence = "random"; // random, sequential, scenario
        
        @JsonProperty("duration")
        private DurationConfig duration = new DurationConfig();
        
        @JsonProperty("transitions")
        private TransitionConfig transitions = new TransitionConfig();
        
        // Getters et setters
        public String getSequence() { return sequence; }
        public void setSequence(String sequence) { this.sequence = sequence; }
        
        public DurationConfig getDuration() { return duration; }
        public void setDuration(DurationConfig duration) { this.duration = duration; }
        
        public TransitionConfig getTransitions() { return transitions; }
        public void setTransitions(TransitionConfig transitions) { this.transitions = transitions; }
    }
    
    /**
     * Configuration de la durée des activités.
     */
    public static class DurationConfig {
        @JsonProperty("min")
        private long min = 60000; // 1 minute
        
        @JsonProperty("max")
        private long max = 300000; // 5 minutes
        
        // Getters et setters
        public long getMin() { return min; }
        public void setMin(long min) { this.min = min; }
        
        public long getMax() { return max; }
        public void setMax(long max) { this.max = max; }
    }
    
    /**
     * Configuration des transitions entre activités.
     */
    public static class TransitionConfig {
        @JsonProperty("gradual")
        private boolean gradual = true;
        
        @JsonProperty("confidence")
        private ConfidenceConfig confidence = new ConfidenceConfig();
        
        // Getters et setters
        public boolean isGradual() { return gradual; }
        public void setGradual(boolean gradual) { this.gradual = gradual; }
        
        public ConfidenceConfig getConfidence() { return confidence; }
        public void setConfidence(ConfidenceConfig confidence) { this.confidence = confidence; }
    }
    
    /**
     * Configuration du niveau de confiance.
     */
    public static class ConfidenceConfig {
        @JsonProperty("min")
        private double min = 0.6;
        
        @JsonProperty("max")
        private double max = 0.95;
        
        // Getters et setters
        public double getMin() { return min; }
        public void setMin(double min) { this.min = min; }
        
        public double getMax() { return max; }
        public void setMax(double max) { this.max = max; }
    }
    
    /**
     * Configuration de l'emploi du temps.
     */
    public static class ScheduleConfig {
        @JsonProperty("followDailyPattern")
        private boolean followDailyPattern = true;
        
        @JsonProperty("patterns")
        private Map<String, List<String>> patterns;
        
        // Getters et setters
        public boolean isFollowDailyPattern() { return followDailyPattern; }
        public void setFollowDailyPattern(boolean followDailyPattern) { this.followDailyPattern = followDailyPattern; }
        
        public Map<String, List<String>> getPatterns() { return patterns; }
        public void setPatterns(Map<String, List<String>> patterns) { this.patterns = patterns; }
    }
    
    /**
     * Configuration de l'interface web.
     */
    public static class WebConfig {
        @JsonProperty("dashboardEnabled")
        private boolean dashboardEnabled = true;
        
        @JsonProperty("dashboardPort")
        private int dashboardPort = 8081;
        
        @JsonProperty("dashboardPath")
        private String dashboardPath = "/test-dashboard";
        
        // Getters et setters
        public boolean isDashboardEnabled() { return dashboardEnabled; }
        public void setDashboardEnabled(boolean dashboardEnabled) { this.dashboardEnabled = dashboardEnabled; }
        
        public int getDashboardPort() { return dashboardPort; }
        public void setDashboardPort(int dashboardPort) { this.dashboardPort = dashboardPort; }
        
        public String getDashboardPath() { return dashboardPath; }
        public void setDashboardPath(String dashboardPath) { this.dashboardPath = dashboardPath; }
    }
    
    /**
     * Configuration du logging.
     */
    public static class LoggingConfig {
        @JsonProperty("enabled")
        private boolean enabled = true;
        
        @JsonProperty("level")
        private String level = "INFO";
        
        @JsonProperty("logActivities")
        private boolean logActivities = true;
        
        @JsonProperty("logTransitions")
        private boolean logTransitions = true;
        
        // Getters et setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        
        public boolean isLogActivities() { return logActivities; }
        public void setLogActivities(boolean logActivities) { this.logActivities = logActivities; }
        
        public boolean isLogTransitions() { return logTransitions; }
        public void setLogTransitions(boolean logTransitions) { this.logTransitions = logTransitions; }
    }
    
    /**
     * Wrapper pour la désérialisation JSON.
     */
    private static class TestModeConfigWrapper {
        @JsonProperty("testMode")
        private TestModeConfig testMode;
        
        public TestModeConfig getTestMode() { return testMode; }
        public void setTestMode(TestModeConfig testMode) { this.testMode = testMode; }
    }
}
package com.angel.test;

import com.angel.api.dto.TestActivityDTO;
import com.angel.config.TestModeConfig;
import com.angel.model.Activity;
import com.angel.util.LogUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Simulateur d'activités pour le mode test.
 * Génère des activités selon différents modes : aléatoire, séquentiel, ou basé sur des scénarios.
 */
@Component
public class ActivitySimulator {
    
    private static final Logger logger = LogUtil.getLogger(ActivitySimulator.class);
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();
    private final TestModeConfig config;
    private final ScenarioManager scenarioManager;
    
    private TestActivityDTO currentActivity;
    private boolean running = false;
    private ScheduledFuture<?> currentTask;
    private int sequentialIndex = 0;
    
    // Activités disponibles pour le mode aléatoire
    private final Activity[] availableActivities = {
        Activity.EATING, Activity.READING, Activity.WATCHING_TV, Activity.COOKING,
        Activity.CLEANING, Activity.WAITING, Activity.USING_SCREEN, Activity.WASHING,
        Activity.WAKING_UP, Activity.GOING_TO_SLEEP, Activity.LISTENING_MUSIC,
        Activity.CONVERSING, Activity.MOVING, Activity.PUTTING_AWAY
    };
    
    public ActivitySimulator(TestModeConfig config, ScenarioManager scenarioManager) {
        this.config = config;
        this.scenarioManager = scenarioManager;
        this.currentActivity = createDefaultActivity();
        
        logger.info("ActivitySimulator initialisé avec mode: " + config.getActivities().getSequence());
    }
    
    /**
     * Démarre la simulation d'activités.
     */
    public synchronized void start() {
        if (!running) {
            running = true;
            logger.info("Démarrage de la simulation d'activités");
            scheduleNextActivityChange();
        }
    }
    
    /**
     * Arrête la simulation d'activités.
     */
    public synchronized void stop() {
        if (running) {
            running = false;
            if (currentTask != null) {
                currentTask.cancel(false);
            }
            logger.info("Arrêt de la simulation d'activités");
        }
    }
    
    /**
     * Retourne l'activité courante.
     */
    public TestActivityDTO getCurrentActivity() {
        return currentActivity;
    }
    
    /**
     * Définit manuellement l'activité courante (pour contrôle manuel).
     */
    public void setCurrentActivity(String activityName, double confidence) {
        try {
            Activity activity = Activity.valueOf(activityName.toUpperCase());
            this.currentActivity = new TestActivityDTO(activity, confidence, System.currentTimeMillis());
            
            if (config.getLogging().isLogActivities()) {
                logger.info(String.format("Activité définie manuellement: %s (confiance: %.2f)", 
                    activity.getFrenchName(), confidence));
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Activité inconnue: " + activityName);
            throw new IllegalArgumentException("Activité non reconnue: " + activityName);
        }
    }
    
    /**
     * Vérifie si la simulation est en cours.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Retourne les statistiques de la simulation.
     */
    public SimulationStats getStats() {
        return new SimulationStats(
            running,
            currentActivity,
            calculateNextInterval(),
            config.getActivities().getSequence()
        );
    }
    
    /**
     * Programme le prochain changement d'activité.
     */
    private void scheduleNextActivityChange() {
        if (!running) return;
        
        long interval = calculateNextInterval();
        
        if (config.getLogging().isLogTransitions()) {
            logger.info(String.format("Prochaine activité dans %d ms", interval));
        }
        
        currentTask = scheduler.schedule(this::generateNextActivity, interval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Génère la prochaine activité selon la configuration.
     */
    private void generateNextActivity() {
        if (!running) return;
        
        try {
            Activity nextActivity = selectNextActivity();
            double confidence = generateConfidence();
            
            TestActivityDTO previousActivity = currentActivity;
            this.currentActivity = new TestActivityDTO(nextActivity, confidence, System.currentTimeMillis());
            
            if (config.getLogging().isLogActivities()) {
                logger.info(String.format("Transition: %s -> %s (confiance: %.2f)",
                    previousActivity != null ? previousActivity.getActivity().getFrenchName() : "aucune",
                    nextActivity.getFrenchName(),
                    confidence));
            }
            
            // Programme la prochaine activité
            scheduleNextActivityChange();
            
        } catch (Exception e) {
            logger.severe("Erreur lors de la génération de l'activité suivante: " + e.getMessage());
            // En cas d'erreur, reprogrammer dans 10 secondes
            currentTask = scheduler.schedule(this::generateNextActivity, 10000, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Sélectionne la prochaine activité selon le mode configuré.
     */
    private Activity selectNextActivity() {
        String sequence = config.getActivities().getSequence().toLowerCase();
        
        switch (sequence) {
            case "scenario":
                return scenarioManager.getNextActivityFromScenario();
            case "sequential":
                return getNextSequentialActivity();
            case "scheduled":
                return getScheduledActivity();
            case "random":
            default:
                return getRandomActivity();
        }
    }
    
    /**
     * Retourne une activité aléatoire.
     */
    private Activity getRandomActivity() {
        return availableActivities[random.nextInt(availableActivities.length)];
    }
    
    /**
     * Retourne la prochaine activité en mode séquentiel.
     */
    private Activity getNextSequentialActivity() {
        Activity activity = availableActivities[sequentialIndex % availableActivities.length];
        sequentialIndex++;
        return activity;
    }
    
    /**
     * Retourne une activité basée sur l'heure de la journée.
     */
    private Activity getScheduledActivity() {
        if (!config.getSchedule().isFollowDailyPattern()) {
            return getRandomActivity();
        }
        
        LocalTime now = LocalTime.now();
        List<String> patterns;
        
        if (now.isBefore(LocalTime.of(12, 0))) {
            patterns = config.getSchedule().getPatterns().get("morning");
        } else if (now.isBefore(LocalTime.of(18, 0))) {
            patterns = config.getSchedule().getPatterns().get("afternoon");
        } else {
            patterns = config.getSchedule().getPatterns().get("evening");
        }
        
        if (patterns != null && !patterns.isEmpty()) {
            String activityName = patterns.get(random.nextInt(patterns.size()));
            try {
                return Activity.valueOf(activityName);
            } catch (IllegalArgumentException e) {
                logger.warning("Activité programmée inconnue: " + activityName);
            }
        }
        
        return getRandomActivity();
    }
    
    /**
     * Calcule l'intervalle jusqu'au prochain changement d'activité.
     */
    private long calculateNextInterval() {
        long baseInterval = config.getSimulation().getInterval();
        double randomness = config.getSimulation().getRandomness();
        
        // Applique un facteur d'aléa à l'intervalle
        double factor = 1.0 + (random.nextDouble() - 0.5) * 2 * randomness;
        return Math.round(baseInterval * factor);
    }
    
    /**
     * Génère un niveau de confiance aléatoire dans les limites configurées.
     */
    private double generateConfidence() {
        double min = config.getActivities().getTransitions().getConfidence().getMin();
        double max = config.getActivities().getTransitions().getConfidence().getMax();
        return min + random.nextDouble() * (max - min);
    }
    
    /**
     * Crée une activité par défaut.
     */
    private TestActivityDTO createDefaultActivity() {
        return new TestActivityDTO(Activity.WAITING, 0.8, System.currentTimeMillis());
    }
    
    /**
     * Nettoie les ressources lors de l'arrêt.
     */
    public void shutdown() {
        stop();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Classe pour les statistiques de simulation.
     */
    public static class SimulationStats {
        private final boolean running;
        private final TestActivityDTO currentActivity;
        private final long nextChangeIn;
        private final String mode;
        private final LocalDateTime timestamp;
        
        public SimulationStats(boolean running, TestActivityDTO currentActivity, long nextChangeIn, String mode) {
            this.running = running;
            this.currentActivity = currentActivity;
            this.nextChangeIn = nextChangeIn;
            this.mode = mode;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public boolean isRunning() { return running; }
        public TestActivityDTO getCurrentActivity() { return currentActivity; }
        public long getNextChangeIn() { return nextChangeIn; }
        public String getMode() { return mode; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
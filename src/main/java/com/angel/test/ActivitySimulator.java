package com.angel.test;

import com.angel.api.dto.ActivityDTO;
import com.angel.model.Activity;
import com.angel.util.LogUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
    private final List<ActivityDTO> activityBuffer = new ArrayList<>();
    
    private ActivityDTO currentActivity;
    private boolean running = false;
    private ScheduledFuture<?> currentTask;
    private int sequentialIndex = 0;
    private double speedMultiplier = 1.0;
    private boolean noiseEnabled = true;
    private int totalActivitiesGenerated = 0;
    
    // Activités disponibles pour le mode aléatoire
    private final Activity[] availableActivities = {
        Activity.EATING, Activity.READING, Activity.WATCHING_TV, Activity.COOKING,
        Activity.CLEANING, Activity.WAITING, Activity.USING_SCREEN, Activity.WASHING,
        Activity.WAKING_UP, Activity.GOING_TO_SLEEP, Activity.LISTENING_MUSIC,
        Activity.CONVERSING, Activity.MOVING, Activity.PUTTING_AWAY
    };
    
    public ActivitySimulator() {
        this.currentActivity = createDefaultActivity();
        logger.info("ActivitySimulator initialisé");
    }
    
    /**
     * Démarre la simulation d'activités.
     */
    public synchronized void startSimulation() {
        if (!running) {
            running = true;
            logger.info("Démarrage de la simulation d'activités");
            scheduleNextActivityChange();
        }
    }
    
    /**
     * Arrête la simulation d'activités.
     */
    public synchronized void stopSimulation() {
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
    public ActivityDTO getCurrentActivity() {
        return currentActivity;
    }
    
    /**
     * Définit manuellement l'activité courante.
     */
    public void setCurrentActivity(ActivityDTO activity) {
        this.currentActivity = activity;
        logger.info("Activité définie manuellement: " + activity.getActivityType());
    }
    
    /**
     * Ajoute une activité manuelle au buffer.
     */
    public void addManualActivity(ActivityDTO activity) {
        synchronized (activityBuffer) {
            activityBuffer.add(activity);
            totalActivitiesGenerated++;
            logger.info("Activité manuelle ajoutée au buffer: " + activity.getActivityType());
        }
    }
    
    /**
     * Récupère les activités pour une période donnée.
     */
    public List<ActivityDTO> getActivitiesForPeriod(LocalDateTime start, LocalDateTime end) {
        synchronized (activityBuffer) {
            long startMillis = start.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endMillis = end.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            return activityBuffer.stream()
                .filter(activity -> activity.getTimestamp() >= startMillis && activity.getTimestamp() <= endMillis)
                .toList();
        }
    }
    
    /**
     * Récupère les dernières activités.
     */
    public List<ActivityDTO> getLatestActivities(int count) {
        synchronized (activityBuffer) {
            int size = activityBuffer.size();
            if (size == 0) return List.of();
            
            int fromIndex = Math.max(0, size - count);
            return new ArrayList<>(activityBuffer.subList(fromIndex, size));
        }
    }
    
    /**
     * Vérifie si la simulation est en cours.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Définit le multiplicateur de vitesse.
     */
    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = Math.max(0.1, Math.min(10.0, speedMultiplier));
        logger.info("Multiplicateur de vitesse défini à: " + this.speedMultiplier);
    }
    
    /**
     * Active/désactive le bruit dans la simulation.
     */
    public void setNoiseEnabled(boolean enabled) {
        this.noiseEnabled = enabled;
        logger.info("Bruit " + (enabled ? "activé" : "désactivé"));
    }
    
    /**
     * Retourne le nombre total d'activités générées.
     */
    public int getTotalActivitiesGenerated() {
        return totalActivitiesGenerated;
    }
    
    /**
     * Retourne la taille du buffer.
     */
    public int getBufferSize() {
        synchronized (activityBuffer) {
            return activityBuffer.size();
        }
    }
    
    /**
     * Calcule la confiance moyenne.
     */
    public double getAverageConfidence() {
        synchronized (activityBuffer) {
            if (activityBuffer.isEmpty()) return 0.0;
            
            return activityBuffer.stream()
                .mapToDouble(ActivityDTO::getConfidence)
                .average()
                .orElse(0.0);
        }
    }
    
    /**
     * Retourne les statistiques de la simulation.
     */
    public SimulationStats getSimulationStats() {
        return new SimulationStats(
            running,
            running ? "RUNNING" : "STOPPED",
            currentActivity != null ? currentActivity.getActivityType() : "NONE",
            calculateNextInterval(),
            System.currentTimeMillis()
        );
    }
    
    /**
     * Programme le prochain changement d'activité.
     */
    private void scheduleNextActivityChange() {
        if (!running) return;
        
        long interval = calculateNextInterval();
        
        currentTask = scheduler.schedule(this::generateNextActivity, interval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Génère la prochaine activité.
     */
    private void generateNextActivity() {
        if (!running) return;
        
        try {
            Activity nextActivity = getRandomActivity();
            double confidence = generateConfidence();
            
            ActivityDTO newActivity = new ActivityDTO(nextActivity, confidence, System.currentTimeMillis());
            this.currentActivity = newActivity;
            
            synchronized (activityBuffer) {
                activityBuffer.add(newActivity);
                totalActivitiesGenerated++;
                
                // Limiter la taille du buffer (garder les 1000 dernières activités)
                if (activityBuffer.size() > 1000) {
                    activityBuffer.remove(0);
                }
            }
            
            logger.fine("Nouvelle activité générée: " + nextActivity.name() + " (confiance: " + confidence + ")");
            
            // Programme la prochaine activité
            scheduleNextActivityChange();
            
        } catch (Exception e) {
            logger.severe("Erreur lors de la génération de l'activité suivante: " + e.getMessage());
            // En cas d'erreur, reprogrammer dans 10 secondes
            currentTask = scheduler.schedule(this::generateNextActivity, 10000, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Retourne une activité aléatoire.
     */
    private Activity getRandomActivity() {
        return availableActivities[random.nextInt(availableActivities.length)];
    }
    
    /**
     * Calcule l'intervalle jusqu'au prochain changement d'activité.
     */
    private long calculateNextInterval() {
        long baseInterval = 30000; // 30 secondes par défaut
        
        // Appliquer le multiplicateur de vitesse
        baseInterval = Math.round(baseInterval / speedMultiplier);
        
        // Ajouter du bruit si activé
        if (noiseEnabled) {
            double randomness = 0.3; // ±30% de variation
            double factor = 1.0 + (random.nextDouble() - 0.5) * 2 * randomness;
            baseInterval = Math.round(baseInterval * factor);
        }
        
        return Math.max(1000, baseInterval); // Minimum 1 seconde
    }
    
    /**
     * Génère un niveau de confiance aléatoire.
     */
    private double generateConfidence() {
        double base = 0.75; // Confiance de base
        double variation = noiseEnabled ? 0.25 : 0.1; // Variation selon le bruit
        
        double confidence = base + (random.nextDouble() - 0.5) * 2 * variation;
        return Math.max(0.5, Math.min(0.98, confidence));
    }
    
    /**
     * Crée une activité par défaut.
     */
    private ActivityDTO createDefaultActivity() {
        return new ActivityDTO(Activity.WAITING, 0.8, System.currentTimeMillis());
    }
    
    /**
     * Nettoie les ressources lors de l'arrêt.
     */
    public void shutdown() {
        stopSimulation();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("ActivitySimulator arrêté");
    }
    
    /**
     * Classe pour les statistiques de simulation.
     */
    public static class SimulationStats {
        private final boolean running;
        private final String mode;
        private final String currentActivity;
        private final long nextChangeIn;
        private final long timestamp;
        
        public SimulationStats(boolean running, String mode, String currentActivity, long nextChangeIn, long timestamp) {
            this.running = running;
            this.mode = mode;
            this.currentActivity = currentActivity;
            this.nextChangeIn = nextChangeIn;
            this.timestamp = timestamp;
        }
        
        // Getters
        public boolean isRunning() { return running; }
        public String getMode() { return mode; }
        public String getCurrentActivity() { return currentActivity; }
        public long getNextChangeIn() { return nextChangeIn; }
        public long getTimestamp() { return timestamp; }
    }
}
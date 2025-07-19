package com.angel.api;

import com.angel.api.dto.ActivityDTO;
import com.angel.test.ActivitySimulator;
import com.angel.test.ScenarioManager;
import com.angel.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Client simulé pour les activités en mode test.
 * Remplace les appels vers l'API Angel-server-capture par des données simulées.
 */
@Component
public class TestActivityClient {
    
    private static final Logger logger = LogUtil.getLogger(TestActivityClient.class);
    
    @Autowired
    private ActivitySimulator activitySimulator;
    
    @Autowired
    private ScenarioManager scenarioManager;
    
    private boolean isActive = false;
    private String currentScenario;
    
    /**
     * Démarre la simulation d'activités.
     */
    public CompletableFuture<Void> startSimulation(String scenarioName) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Démarrage de la simulation avec le scénario: " + scenarioName);
                this.currentScenario = scenarioName;
                this.isActive = true;
                
                scenarioManager.executeScenario(scenarioName);
                
            } catch (Exception e) {
                logger.severe("Erreur lors du démarrage de la simulation: " + e.getMessage());
                this.isActive = false;
                throw new RuntimeException("Impossible de démarrer la simulation", e);
            }
        });
    }
    
    /**
     * Arrête la simulation d'activités.
     */
    public void stopSimulation() {
        logger.info("Arrêt de la simulation");
        this.isActive = false;
        this.currentScenario = null;
        
        if (activitySimulator != null) {
            activitySimulator.stopSimulation();
        }
        
        if (scenarioManager != null) {
            scenarioManager.stopCurrentScenario();
        }
    }
    
    /**
     * Récupère les activités simulées pour une période donnée.
     */
    public List<ActivityDTO> getActivitiesForPeriod(LocalDateTime start, LocalDateTime end) {
        if (!isActive) {
            logger.warning("Tentative de récupération d'activités alors que la simulation n'est pas active");
            return List.of();
        }
        
        try {
            return activitySimulator.getActivitiesForPeriod(start, end);
        } catch (Exception e) {
            logger.severe("Erreur lors de la récupération des activités: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Récupère les dernières activités simulées.
     */
    public List<ActivityDTO> getLatestActivities(int count) {
        if (!isActive) {
            logger.warning("Tentative de récupération d'activités alors que la simulation n'est pas active");
            return List.of();
        }
        
        try {
            return activitySimulator.getLatestActivities(count);
        } catch (Exception e) {
            logger.severe("Erreur lors de la récupération des dernières activités: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Ajoute une activité manuelle à la simulation.
     */
    public void addManualActivity(ActivityDTO activity) {
        if (!isActive) {
            logger.warning("Tentative d'ajout d'activité alors que la simulation n'est pas active");
            return;
        }
        
        try {
            activitySimulator.addManualActivity(activity);
            logger.info("Activité manuelle ajoutée: " + activity.getActivityType());
        } catch (Exception e) {
            logger.severe("Erreur lors de l'ajout de l'activité manuelle: " + e.getMessage());
        }
    }
    
    /**
     * Change le scénario de simulation en cours.
     */
    public void changeScenario(String newScenarioName) {
        if (!isActive) {
            logger.warning("Tentative de changement de scénario alors que la simulation n'est pas active");
            return;
        }
        
        try {
            logger.info("Changement de scénario de '" + currentScenario + "' vers '" + newScenarioName + "'");
            scenarioManager.stopCurrentScenario();
            this.currentScenario = newScenarioName;
            scenarioManager.executeScenario(newScenarioName);
        } catch (Exception e) {
            logger.severe("Erreur lors du changement de scénario: " + e.getMessage());
        }
    }
    
    /**
     * Modifie les paramètres de simulation.
     */
    public void updateSimulationParameters(double speedMultiplier, boolean enableNoise) {
        if (!isActive) {
            logger.warning("Tentative de modification des paramètres alors que la simulation n'est pas active");
            return;
        }
        
        try {
            activitySimulator.setSpeedMultiplier(speedMultiplier);
            activitySimulator.setNoiseEnabled(enableNoise);
            logger.info(String.format("Paramètres de simulation mis à jour: vitesse=%.2f, bruit=%s", 
                speedMultiplier, enableNoise));
        } catch (Exception e) {
            logger.severe("Erreur lors de la mise à jour des paramètres: " + e.getMessage());
        }
    }
    
    /**
     * Vérifie si la simulation est active.
     */
    public boolean isSimulationActive() {
        return isActive;
    }
    
    /**
     * Récupère le nom du scénario actuel.
     */
    public String getCurrentScenario() {
        return currentScenario;
    }
    
    /**
     * Récupère les statistiques de la simulation.
     */
    public SimulationStats getSimulationStats() {
        if (!isActive) {
            return new SimulationStats(false, null, 0, 0, 0.0);
        }
        
        try {
            int totalActivities = activitySimulator.getTotalActivitiesGenerated();
            int activitiesInBuffer = activitySimulator.getBufferSize();
            double averageConfidence = activitySimulator.getAverageConfidence();
            
            return new SimulationStats(true, currentScenario, totalActivities, 
                activitiesInBuffer, averageConfidence);
        } catch (Exception e) {
            logger.severe("Erreur lors de la récupération des statistiques: " + e.getMessage());
            return new SimulationStats(false, null, 0, 0, 0.0);
        }
    }
    
    /**
     * Classe pour les statistiques de simulation.
     */
    public static class SimulationStats {
        private final boolean active;
        private final String scenario;
        private final int totalActivities;
        private final int bufferSize;
        private final double averageConfidence;
        
        public SimulationStats(boolean active, String scenario, int totalActivities, 
                             int bufferSize, double averageConfidence) {
            this.active = active;
            this.scenario = scenario;
            this.totalActivities = totalActivities;
            this.bufferSize = bufferSize;
            this.averageConfidence = averageConfidence;
        }
        
        public boolean isActive() { return active; }
        public String getScenario() { return scenario; }
        public int getTotalActivities() { return totalActivities; }
        public int getBufferSize() { return bufferSize; }
        public double getAverageConfidence() { return averageConfidence; }
    }
}
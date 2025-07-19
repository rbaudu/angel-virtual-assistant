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
     * Démarre la simulation (version simple).
     */
    public boolean startSimulation() {
        try {
            this.isActive = true;
            if (activitySimulator != null) {
                activitySimulator.startSimulation();
            }
            logger.info("Simulation démarrée");
            return true;
        } catch (Exception e) {
            logger.severe("Erreur lors du démarrage de la simulation: " + e.getMessage());
            this.isActive = false;
            return false;
        }
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
     * Arrête la simulation (version simple).
     */
    public boolean stopSimulation() {
        try {
            stopSimulation();
            return true;
        } catch (Exception e) {
            logger.severe("Erreur lors de l'arrêt de la simulation: " + e.getMessage());
            return false;
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
     * Récupère l'activité courante.
     */
    public ActivityDTO getCurrentActivity() {
        if (!isActive || activitySimulator == null) {
            return createDefaultActivity();
        }
        
        try {
            return activitySimulator.getCurrentActivity();
        } catch (Exception e) {
            logger.severe("Erreur lors de la récupération de l'activité courante: " + e.getMessage());
            return createDefaultActivity();
        }
    }
    
    /**
     * Définit l'activité courante manuellement.
     */
    public boolean setCurrentActivity(String activityType, double confidence) {
        if (!isActive) {
            logger.warning("Tentative de définition d'activité alors que la simulation n'est pas active");
            return false;
        }
        
        try {
            ActivityDTO activity = new ActivityDTO(activityType, System.currentTimeMillis(), 
                confidence, "manual", "Manually set activity");
            activitySimulator.setCurrentActivity(activity);
            logger.info("Activité définie manuellement: " + activityType + " (confiance: " + confidence + ")");
            return true;
        } catch (Exception e) {
            logger.severe("Erreur lors de la définition de l'activité: " + e.getMessage());
            return false;
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
     * Charge un scénario spécifique.
     */
    public boolean loadScenario(String scenarioId) {
        try {
            if (scenarioManager != null) {
                scenarioManager.loadScenario(scenarioId);
                this.currentScenario = scenarioId;
                logger.info("Scénario chargé: " + scenarioId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.severe("Erreur lors du chargement du scénario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie si la simulation est en cours.
     */
    public boolean isSimulationRunning() {
        return isActive && (activitySimulator != null ? activitySimulator.isRunning() : false);
    }
    
    /**
     * Récupère la liste des scénarios disponibles.
     */
    public List<ScenarioManager.ScenarioInfo> getAvailableScenarios() {
        if (scenarioManager != null) {
            return scenarioManager.getAvailableScenarios();
        }
        return List.of();
    }
    
    /**
     * Récupère les informations sur le scénario courant.
     */
    public ScenarioManager.CurrentScenarioInfo getCurrentScenarioInfo() {
        if (scenarioManager != null && currentScenario != null) {
            return scenarioManager.getCurrentScenarioInfo();
        }
        return null;
    }
    
    /**
     * Arrête le scénario courant.
     */
    public boolean stopCurrentScenario() {
        try {
            if (scenarioManager != null) {
                scenarioManager.stopCurrentScenario();
                this.currentScenario = null;
                logger.info("Scénario courant arrêté");
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.severe("Erreur lors de l'arrêt du scénario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Vérifie la connexion.
     */
    public boolean checkConnection() {
        return activitySimulator != null && scenarioManager != null;
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
    public ActivitySimulator.SimulationStats getSimulationStats() {
        if (!isActive || activitySimulator == null) {
            return createDefaultStats();
        }
        
        try {
            return activitySimulator.getSimulationStats();
        } catch (Exception e) {
            logger.severe("Erreur lors de la récupération des statistiques: " + e.getMessage());
            return createDefaultStats();
        }
    }
    
    /**
     * Récupère les informations du mode test.
     */
    public TestModeInfo getTestModeInfo() {
        return new TestModeInfo("TEST", "1.0", isActive);
    }
    
    /**
     * Crée une activité par défaut.
     */
    private ActivityDTO createDefaultActivity() {
        return new ActivityDTO("WAITING", System.currentTimeMillis(), 0.5, "test", "Default activity");
    }
    
    /**
     * Crée des statistiques par défaut.
     */
    private ActivitySimulator.SimulationStats createDefaultStats() {
        return new ActivitySimulator.SimulationStats(false, "IDLE", "WAITING", 0, System.currentTimeMillis());
    }
    
    /**
     * Classe pour les informations du mode test.
     */
    public static class TestModeInfo {
        private final String mode;
        private final String version;
        private final boolean simulationRunning;
        
        public TestModeInfo(String mode, String version, boolean simulationRunning) {
            this.mode = mode;
            this.version = version;
            this.simulationRunning = simulationRunning;
        }
        
        public String getMode() { return mode; }
        public String getVersion() { return version; }
        public boolean isSimulationRunning() { return simulationRunning; }
    }
}
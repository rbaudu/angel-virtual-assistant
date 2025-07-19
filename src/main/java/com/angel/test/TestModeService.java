package com.angel.test;

import com.angel.api.TestActivityClient;
import com.angel.config.ConfigManager;
import com.angel.config.TestModeConfig;
import com.angel.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Service principal pour la gestion du mode test.
 * Coordonne l'initialisation et la gestion des composants de test.
 */
@Service
public class TestModeService {
    
    private static final Logger logger = LogUtil.getLogger(TestModeService.class);
    
    @Autowired
    private ConfigManager configManager;
    
    @Autowired
    private ActivitySimulator activitySimulator;
    
    @Autowired
    private ScenarioManager scenarioManager;
    
    @Autowired
    private TestActivityClient testActivityClient;
    
    @Autowired
    private TestDataGenerator testDataGenerator;
    
    private TestModeConfig testConfig;
    private boolean testModeEnabled = false;
    private boolean initialized = false;
    
    /**
     * Initialise le service de mode test.
     */
    @PostConstruct
    public void initialize() {
        try {
            logger.info("Initialisation du service de mode test...");
            
            // Vérifier si le mode test est configuré dans la configuration principale
            if (isTestModeConfigured()) {
                loadTestConfiguration();
                initializeTestComponents();
                
                if (testConfig.isEnabled()) {
                    enableTestMode();
                    
                    if (testConfig.isAutoStart()) {
                        autoStartSimulation();
                    }
                }
            } else {
                logger.info("Mode test non configuré - fonctionnement en mode production");
            }
            
            initialized = true;
            logger.info("Service de mode test initialisé avec succès");
            
        } catch (Exception e) {
            logger.severe("Erreur lors de l'initialisation du mode test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie si le mode test est configuré.
     */
    private boolean isTestModeConfigured() {
        try {
            // Vérifier si le système est en mode test
            String systemMode = configManager.getSystemProperty("mode", "production");
            boolean testModeInConfig = configManager.getBooleanProperty("system.testMode.enabled", false);
            
            return "test".equalsIgnoreCase(systemMode) || testModeInConfig;
        } catch (Exception e) {
            logger.warning("Erreur lors de la vérification de la configuration de test: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Charge la configuration du mode test.
     */
    private void loadTestConfiguration() throws IOException {
        String configPath = configManager.getStringProperty("system.testMode.configFile", 
            "config/test/test-mode-config.json");
        
        logger.info("Chargement de la configuration de test depuis: " + configPath);
        
        try {
            testConfig = TestModeConfig.loadFromFile(configPath);
            logger.info("Configuration de test chargée avec succès");
        } catch (IOException e) {
            logger.warning("Impossible de charger la configuration de test, utilisation de la configuration par défaut");
            testConfig = new TestModeConfig();
            testConfig.setEnabled(true); // Activer par défaut si on est en mode test
        }
    }
    
    /**
     * Initialise les composants de test.
     */
    private void initializeTestComponents() {
        try {
            // Initialiser le gestionnaire de scénarios
            String scenarioFile = testConfig.getSimulation().getScenarioFile();
            scenarioManager.loadScenarios(scenarioFile);
            
            logger.info("Composants de test initialisés");
        } catch (Exception e) {
            logger.severe("Erreur lors de l'initialisation des composants de test: " + e.getMessage());
            throw new RuntimeException("Échec de l'initialisation des composants de test", e);
        }
    }
    
    /**
     * Active le mode test.
     */
    public void enableTestMode() {
        if (!initialized) {
            throw new IllegalStateException("Service non initialisé");
        }
        
        testModeEnabled = true;
        logger.info("Mode test activé");
        
        // Appliquer la configuration de logging si spécifiée
        if (testConfig.getLogging().isEnabled()) {
            applyTestLoggingConfiguration();
        }
    }
    
    /**
     * Désactive le mode test.
     */
    public void disableTestMode() {
        if (testModeEnabled) {
            testActivityClient.stopSimulation();
            testModeEnabled = false;
            logger.info("Mode test désactivé");
        }
    }
    
    /**
     * Démarre automatiquement la simulation si configuré.
     */
    private void autoStartSimulation() {
        if (testConfig.isAutoStart()) {
            logger.info("Démarrage automatique de la simulation...");
            
            // Délai pour permettre l'initialisation complète
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Attendre 2 secondes
                    testActivityClient.startSimulation();
                    logger.info("Simulation démarrée automatiquement");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warning("Démarrage automatique interrompu");
                } catch (Exception e) {
                    logger.severe("Erreur lors du démarrage automatique: " + e.getMessage());
                }
            }).start();
        }
    }
    
    /**
     * Applique la configuration de logging pour le mode test.
     */
    private void applyTestLoggingConfiguration() {
        try {
            String logLevel = testConfig.getLogging().getLevel();
            logger.info("Application du niveau de log de test: " + logLevel);
            
            // Ici, vous pourriez ajuster le niveau de logging global si nécessaire
            // Par exemple, activer des logs plus détaillés en mode test
            
        } catch (Exception e) {
            logger.warning("Erreur lors de l'application de la configuration de logging: " + e.getMessage());
        }
    }
    
    /**
     * Vérifie si le mode test est actif.
     */
    public boolean isTestModeEnabled() {
        return testModeEnabled && initialized;
    }
    
    /**
     * Retourne la configuration du mode test.
     */
    public TestModeConfig getTestConfig() {
        return testConfig;
    }
    
    /**
     * Retourne des informations sur l'état du service.
     */
    public TestModeServiceInfo getServiceInfo() {
        return new TestModeServiceInfo(
            initialized,
            testModeEnabled,
            testConfig != null ? testConfig.isEnabled() : false,
            testActivityClient.isSimulationRunning(),
            scenarioManager.getCurrentScenarioInfo()
        );
    }
    
    /**
     * Force le rechargement de la configuration.
     */
    public void reloadConfiguration() {
        if (!initialized) {
            throw new IllegalStateException("Service non initialisé");
        }
        
        try {
            logger.info("Rechargement de la configuration de test...");
            
            boolean wasRunning = testActivityClient.isSimulationRunning();
            
            if (wasRunning) {
                testActivityClient.stopSimulation();
            }
            
            loadTestConfiguration();
            initializeTestComponents();
            
            if (testConfig.isEnabled() && wasRunning) {
                testActivityClient.startSimulation();
            }
            
            logger.info("Configuration rechargée avec succès");
            
        } catch (Exception e) {
            logger.severe("Erreur lors du rechargement de la configuration: " + e.getMessage());
            throw new RuntimeException("Échec du rechargement de la configuration", e);
        }
    }
    
    /**
     * Démarre la simulation avec un scénario spécifique.
     */
    public boolean startSimulationWithScenario(String scenarioId) {
        if (!isTestModeEnabled()) {
            logger.warning("Tentative de démarrage de simulation alors que le mode test n'est pas activé");
            return false;
        }
        
        try {
            // Charger le scénario
            if (scenarioManager.loadScenario(scenarioId)) {
                // Démarrer la simulation
                return testActivityClient.startSimulation();
            }
            return false;
        } catch (Exception e) {
            logger.severe("Erreur lors du démarrage avec scénario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Génère des données de test pour une période donnée.
     */
    public boolean generateTestData(String type, int count) {
        if (!isTestModeEnabled()) {
            return false;
        }
        
        try {
            switch (type.toLowerCase()) {
                case "daily":
                    testDataGenerator.generateDailySequence(java.time.LocalDateTime.now());
                    break;
                case "weekly":
                    testDataGenerator.generateWeeklySequence(java.time.LocalDateTime.now());
                    break;
                case "random":
                    testDataGenerator.generateRandomSequence(count, 30000);
                    break;
                default:
                    logger.warning("Type de génération de données inconnu: " + type);
                    return false;
            }
            
            logger.info("Données de test générées: " + type);
            return true;
            
        } catch (Exception e) {
            logger.severe("Erreur lors de la génération de données de test: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Nettoie les ressources lors de l'arrêt.
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Nettoyage du service de mode test...");
        
        if (testModeEnabled && testActivityClient != null) {
            testActivityClient.shutdown();
        }
        
        testModeEnabled = false;
        initialized = false;
        
        logger.info("Service de mode test arrêté");
    }
    
    /**
     * Classe d'informations sur l'état du service.
     */
    public static class TestModeServiceInfo {
        private final boolean initialized;
        private final boolean testModeEnabled;
        private final boolean configEnabled;
        private final boolean simulationRunning;
        private final ScenarioManager.CurrentScenarioInfo currentScenario;
        
        public TestModeServiceInfo(boolean initialized, boolean testModeEnabled, 
                                 boolean configEnabled, boolean simulationRunning,
                                 ScenarioManager.CurrentScenarioInfo currentScenario) {
            this.initialized = initialized;
            this.testModeEnabled = testModeEnabled;
            this.configEnabled = configEnabled;
            this.simulationRunning = simulationRunning;
            this.currentScenario = currentScenario;
        }
        
        public boolean isInitialized() { return initialized; }
        public boolean isTestModeEnabled() { return testModeEnabled; }
        public boolean isConfigEnabled() { return configEnabled; }
        public boolean isSimulationRunning() { return simulationRunning; }
        public ScenarioManager.CurrentScenarioInfo getCurrentScenario() { return currentScenario; }
    }
}
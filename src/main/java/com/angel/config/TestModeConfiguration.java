package com.angel.config;

import com.angel.test.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration Spring pour le mode test.
 * Active les beans de test seulement quand le mode test est configuré.
 */
@Configuration
public class TestModeConfiguration {
    
    /**
     * Configuration du mode test - toujours disponible pour permettre la vérification.
     */
    @Bean
    public TestModeConfig testModeConfig() {
        return new TestModeConfig();
    }
    
    /**
     * Simulateur d'activités - toujours disponible.
     */
    @Bean
    public ActivitySimulator activitySimulator() {
        return new ActivitySimulator();
    }
    
    /**
     * Gestionnaire de scénarios - toujours disponible.
     */
    @Bean
    public ScenarioManager scenarioManager() {
        return new ScenarioManager();
    }
    
    /**
     * Générateur de données de test - toujours disponible.
     */
    @Bean
    public TestDataGenerator testDataGenerator() {
        return new TestDataGenerator();
    }
    
    /**
     * Client d'activité de test - toujours disponible mais utilisé seulement en mode test.
     */
    @Bean
    public com.angel.api.TestActivityClient testActivityClient() {
        return new com.angel.api.TestActivityClient();
    }
    
    /**
     * Service de mode test - toujours disponible pour la gestion.
     */
    @Bean
    public TestModeService testModeService() {
        return new TestModeService();
    }
    
    /**
     * Adaptateur pour remplacer AngelServerClient en mode test.
     * Actif seulement quand le mode test est explicitement activé.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(
        prefix = "angel.test", 
        name = "enabled", 
        havingValue = "true"
    )
    public ActivityClientAdapter testActivityClientAdapter(
            com.angel.api.TestActivityClient testActivityClient) {
        return new ActivityClientAdapter(testActivityClient);
    }
    
    /**
     * Contrôleur web pour le dashboard de test.
     */
    @Bean
    public com.angel.api.TestModeController testModeController() {
        return new com.angel.api.TestModeController();
    }
    
    /**
     * Adaptateur pour permettre l'utilisation transparente du client de test
     * à la place du client de production.
     */
    public static class ActivityClientAdapter {
        private final com.angel.api.TestActivityClient testClient;
        
        public ActivityClientAdapter(com.angel.api.TestActivityClient testClient) {
            this.testClient = testClient;
        }
        
        public com.angel.api.dto.ActivityDTO getCurrentActivity() {
            return testClient.getCurrentActivity();
        }
        
        public boolean checkConnection() {
            return testClient.checkConnection();
        }
        
        public String getServiceStatus() {
            if (testClient.isSimulationActive()) {
                return "TEST_MODE_ACTIVE";
            } else {
                return "TEST_MODE_INACTIVE";
            }
        }
        
        // Méthodes spécifiques au mode test
        public boolean startSimulation() {
            return testClient.startSimulation();
        }
        
        public boolean stopSimulation() {
            return testClient.stopSimulation();
        }
        
        public boolean setCurrentActivity(String activity, double confidence) {
            return testClient.setCurrentActivity(activity, confidence);
        }
    }
}
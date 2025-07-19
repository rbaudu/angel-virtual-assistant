package com.angel.api;

import com.angel.api.dto.ActivityDTO;
import com.angel.test.ActivitySimulator;
import com.angel.test.ScenarioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion du mode test.
 * Fournit des endpoints pour contrôler la simulation et les scénarios.
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestModeController {
    
    @Autowired
    private TestActivityClient testActivityClient;
    
    @Autowired
    private ActivitySimulator activitySimulator;
    
    @Autowired
    private ScenarioManager scenarioManager;
    
    /**
     * Retourne l'activité courante.
     */
    @GetMapping("/activity/current")
    public ResponseEntity<ActivityDTO> getCurrentActivity() {
        ActivityDTO activity = testActivityClient.getCurrentActivity();
        return ResponseEntity.ok(activity);
    }
    
    /**
     * Définit manuellement l'activité courante.
     */
    @PostMapping("/activity/set")
    public ResponseEntity<Map<String, Object>> setCurrentActivity(@RequestBody SetActivityRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = testActivityClient.setCurrentActivity(request.getActivity(), request.getConfidence());
        
        response.put("success", success);
        if (success) {
            response.put("message", "Activité définie avec succès");
            response.put("activity", testActivityClient.getCurrentActivity());
        } else {
            response.put("message", "Erreur lors de la définition de l'activité");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Démarre la simulation.
     */
    @PostMapping("/simulation/start")
    public ResponseEntity<Map<String, Object>> startSimulation() {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = testActivityClient.startSimulation();
        
        response.put("success", success);
        response.put("message", success ? "Simulation démarrée" : "Erreur lors du démarrage");
        response.put("running", testActivityClient.isSimulationRunning());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Arrête la simulation.
     */
    @PostMapping("/simulation/stop")
    public ResponseEntity<Map<String, Object>> stopSimulation() {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = testActivityClient.stopSimulation();
        
        response.put("success", success);
        response.put("message", success ? "Simulation arrêtée" : "Erreur lors de l'arrêt");
        response.put("running", testActivityClient.isSimulationRunning());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retourne le statut de la simulation.
     */
    @GetMapping("/simulation/status")
    public ResponseEntity<Map<String, Object>> getSimulationStatus() {
        Map<String, Object> response = new HashMap<>();
        
        ActivitySimulator.SimulationStats stats = testActivityClient.getSimulationStats();
        
        response.put("running", stats.isRunning());
        response.put("mode", stats.getMode());
        response.put("currentActivity", stats.getCurrentActivity());
        response.put("nextChangeIn", stats.getNextChangeIn());
        response.put("timestamp", stats.getTimestamp());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retourne les statistiques détaillées.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDetailedStats() {
        Map<String, Object> response = new HashMap<>();
        
        ActivitySimulator.SimulationStats stats = testActivityClient.getSimulationStats();
        TestActivityClient.TestModeInfo testInfo = testActivityClient.getTestModeInfo();
        
        response.put("simulation", Map.of(
            "running", stats.isRunning(),
            "mode", stats.getMode(),
            "nextChangeIn", stats.getNextChangeIn()
        ));
        
        response.put("testMode", Map.of(
            "mode", testInfo.getMode(),
            "version", testInfo.getVersion(),
            "simulationRunning", testInfo.isSimulationRunning()
        ));
        
        response.put("currentActivity", testActivityClient.getCurrentActivity());
        response.put("currentScenario", testActivityClient.getCurrentScenarioInfo());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retourne la liste des scénarios disponibles.
     */
    @GetMapping("/scenarios")
    public ResponseEntity<List<ScenarioManager.ScenarioInfo>> getAvailableScenarios() {
        List<ScenarioManager.ScenarioInfo> scenarios = testActivityClient.getAvailableScenarios();
        return ResponseEntity.ok(scenarios);
    }
    
    /**
     * Charge un scénario spécifique.
     */
    @PostMapping("/scenario/load/{scenarioId}")
    public ResponseEntity<Map<String, Object>> loadScenario(@PathVariable String scenarioId) {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = testActivityClient.loadScenario(scenarioId);
        
        response.put("success", success);
        response.put("message", success ? "Scénario chargé" : "Erreur lors du chargement");
        response.put("scenarioId", scenarioId);
        
        if (success) {
            response.put("scenarioInfo", testActivityClient.getCurrentScenarioInfo());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retourne les informations sur le scénario en cours.
     */
    @GetMapping("/scenario/current")
    public ResponseEntity<ScenarioManager.CurrentScenarioInfo> getCurrentScenario() {
        ScenarioManager.CurrentScenarioInfo info = testActivityClient.getCurrentScenarioInfo();
        if (info != null) {
            return ResponseEntity.ok(info);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    /**
     * Arrête le scénario en cours.
     */
    @PostMapping("/scenario/stop")
    public ResponseEntity<Map<String, Object>> stopCurrentScenario() {
        Map<String, Object> response = new HashMap<>();
        
        boolean success = testActivityClient.stopCurrentScenario();
        
        response.put("success", success);
        response.put("message", success ? "Scénario arrêté" : "Erreur lors de l'arrêt");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Point de santé pour vérifier que le mode test fonctionne.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "OK");
        response.put("mode", "TEST");
        response.put("service", "Angel Virtual Assistant - Test Mode");
        response.put("timestamp", System.currentTimeMillis());
        response.put("simulation", Map.of(
            "running", testActivityClient.isSimulationRunning(),
            "connection", testActivityClient.checkConnection()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Classe pour les requêtes de définition d'activité.
     */
    public static class SetActivityRequest {
        private String activity;
        private double confidence;
        
        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
}
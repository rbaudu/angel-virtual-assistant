package com.angel.api;

import com.angel.api.dto.ActivityDTO;
import com.angel.config.ConfigManager;
import com.angel.model.Activity;
import com.angel.util.LogUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Client pour communiquer avec l'API du serveur Angel-server-capture.
 * Cette classe est responsable de récupérer les informations d'activité
 * détectées par le système de capture.
 */
public class AngelServerClient {
    
    private static final Logger LOGGER = LogUtil.getLogger(AngelServerClient.class);
    private final ConfigManager configManager;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructeur avec injection du gestionnaire de configuration.
     * 
     * @param configManager Le gestionnaire de configuration qui fournit les paramètres API
     */
    public AngelServerClient(ConfigManager configManager) {
        this.configManager = configManager;
        
        // Récupérer le timeout avec valeur par défaut
        long timeoutMs = configManager.getLong("api.timeout", 5000L);
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        this.objectMapper = new ObjectMapper();
        
        LOGGER.log(Level.INFO, "AngelServerClient initialisé avec timeout: {0}ms", timeoutMs);
    }
    
    /**
     * Récupère l'activité actuelle détectée par le système de capture.
     * 
     * @return Une CompletableFuture qui contiendra l'activité détectée
     */
    public CompletableFuture<Activity> getCurrentActivity() {
        // Utiliser les noms de propriétés exacts définis dans application.properties
        String baseUrl = configManager.getString("api.angel-server-url", "http://localhost:8080/api");
        String apiUrl = baseUrl + "/activity/current";
        long timeoutMs = configManager.getLong("api.timeout", 5000L);
        
        LOGGER.log(Level.FINE, "Récupération de l'activité depuis: {0}", apiUrl);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            LOGGER.log(Level.WARNING, "Erreur lors de la récupération de l'activité: {0}", response.statusCode());
                            return Activity.UNKNOWN;
                        }
                        
                        try {
                            ActivityDTO activityDTO = objectMapper.readValue(response.body(), ActivityDTO.class);
                            return Activity.valueOf(activityDTO.getActivityType());
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Erreur lors du traitement de la réponse de l'API", e);
                            return Activity.UNKNOWN;
                        }
                    })
                    .exceptionally(ex -> {
                        LOGGER.log(Level.SEVERE, "Exception lors de l'appel à l'API", ex);
                        return Activity.UNKNOWN;
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la requête HTTP", e);
            return CompletableFuture.completedFuture(Activity.UNKNOWN);
        }
    }
    
    /**
     * Récupère l'historique des activités sur une période donnée.
     * 
     * @param fromTimestamp Timestamp de début (en millisecondes depuis l'epoch)
     * @param toTimestamp Timestamp de fin (en millisecondes depuis l'epoch)
     * @return Une CompletableFuture qui contiendra la liste des activités avec leur timestamp
     */
    public CompletableFuture<Map<Long, Activity>> getActivityHistory(long fromTimestamp, long toTimestamp) {
        String baseUrl = configManager.getString("api.angel-server-url", "http://localhost:8080/api");
        String apiUrl = baseUrl + "/activity/history";
        long timeoutMs = configManager.getLong("api.timeout", 5000L);
        
        String queryParams = String.format("?from=%d&to=%d", fromTimestamp, toTimestamp);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + queryParams))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            LOGGER.log(Level.WARNING, "Erreur lors de la récupération de l'historique: {0}", response.statusCode());
                            return new HashMap<Long, Activity>();
                        }
                        
                        try {
                            List<ActivityDTO> activitiesDTO = objectMapper.readValue(
                                response.body(),
                                new TypeReference<List<ActivityDTO>>() {}
                            );
                            
                            Map<Long, Activity> activityMap = new HashMap<>();
                            for (ActivityDTO dto : activitiesDTO) {
                                activityMap.put(dto.getTimestamp(), Activity.valueOf(dto.getActivityType()));
                            }
                            
                            return activityMap;
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Erreur lors du traitement de la réponse d'historique", e);
                            return new HashMap<Long, Activity>();
                        }
                    })
                    .exceptionally(ex -> {
                        LOGGER.log(Level.SEVERE, "Exception lors de l'appel à l'API d'historique", ex);
                        return new HashMap<Long, Activity>();
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la requête d'historique", e);
            return CompletableFuture.completedFuture(new HashMap<>());
        }
    }
    
    /**
     * Vérifie que le serveur Angel-capture est accessible.
     * 
     * @return true si le serveur est accessible, false sinon
     */
    public boolean isServerAvailable() {
        String baseUrl = configManager.getString("api.angel-server-url", "http://localhost:8080/api");
        String apiUrl = baseUrl + "/health";
        long timeoutMs = configManager.getLong("api.timeout", 5000L);
        
        LOGGER.log(Level.INFO, "Vérification de la disponibilité du serveur: {0}", apiUrl);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean available = response.statusCode() == 200;
            
            if (available) {
                LOGGER.log(Level.INFO, "Serveur Angel-capture accessible");
            } else {
                LOGGER.log(Level.WARNING, "Serveur Angel-capture retourne le code: {0}", response.statusCode());
            }
            
            return available;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Le serveur Angel-capture est inaccessible: {0}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtient les informations de configuration de l'API.
     * 
     * @return Map contenant les informations de configuration
     */
    public Map<String, Object> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("baseUrl", configManager.getString("api.angel-server-url", "N/A"));
        info.put("timeout", configManager.getLong("api.timeout", 5000L));
        info.put("pollingInterval", configManager.getLong("api.polling-interval", 30000L));
        return info;
    }
}

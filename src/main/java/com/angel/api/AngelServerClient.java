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
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(configManager.getLong("api.timeout")))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Récupère l'activité actuelle détectée par le système de capture.
     * 
     * @return Une CompletableFuture qui contiendra l'activité détectée
     */
    public CompletableFuture<Activity> getCurrentActivity() {
        String apiUrl = configManager.getString("api.angelServerUrl") + "/activity/current";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofMillis(configManager.getLong("api.timeout")))
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
    }
    
    /**
     * Récupère l'historique des activités sur une période donnée.
     * 
     * @param fromTimestamp Timestamp de début (en millisecondes depuis l'epoch)
     * @param toTimestamp Timestamp de fin (en millisecondes depuis l'epoch)
     * @return Une CompletableFuture qui contiendra la liste des activités avec leur timestamp
     */
    public CompletableFuture<Map<Long, Activity>> getActivityHistory(long fromTimestamp, long toTimestamp) {
        String apiUrl = configManager.getString("api.angelServerUrl") + "/activity/history";
        
        String queryParams = String.format("?from=%d&to=%d", fromTimestamp, toTimestamp);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + queryParams))
                .timeout(Duration.ofMillis(configManager.getLong("api.timeout")))
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
    }
    
    /**
     * Vérifie que le serveur Angel-capture est accessible.
     * 
     * @return true si le serveur est accessible, false sinon
     */
    public boolean isServerAvailable() {
        String apiUrl = configManager.getString("api.angelServerUrl") + "/health";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofMillis(configManager.getLong("api.timeout")))
                .GET()
                .build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Le serveur Angel-capture est inaccessible", e);
            return false;
        }
    }
}
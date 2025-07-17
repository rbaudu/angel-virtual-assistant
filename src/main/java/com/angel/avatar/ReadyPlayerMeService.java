package com.angel.avatar;

import com.angel.config.ConfigManager;
import com.angel.util.LogUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service pour l'intégration avec Ready Player Me.
 * Permet de créer, modifier et télécharger des avatars 3D.
 */
@Service
public class ReadyPlayerMeService {
    
    private static final Logger LOGGER = LogUtil.getLogger(ReadyPlayerMeService.class);
    
    private final ConfigManager configManager;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private String apiKey;
    private String baseUrl;
    private boolean enabled;
    
    @Autowired
    public ReadyPlayerMeService(ConfigManager configManager) {
        this.configManager = configManager;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
        
        initialize();
    }
    
    /**
     * Initialise la configuration Ready Player Me.
     */
    private void initialize() {
        this.enabled = configManager.getBoolean("avatar.readyPlayerMe.enabled", false);
        this.apiKey = configManager.getString("avatar.readyPlayerMe.apiKey", "");
        this.baseUrl = configManager.getString("avatar.readyPlayerMe.baseUrl", "https://api.readyplayer.me/v1");
        
        if (enabled && (apiKey.isEmpty() || apiKey.equals("YOUR_READY_PLAYER_ME_API_KEY_HERE"))) {
            LOGGER.log(Level.WARNING, "Ready Player Me activé mais clé API manquante ou invalide");
            this.enabled = false;
        }
        
        LOGGER.log(Level.INFO, "Ready Player Me Service - Enabled: {0}", enabled);
    }
    
    /**
     * Vérifie si Ready Player Me est disponible et configuré.
     * 
     * @return true si le service est prêt à être utilisé
     */
    public boolean isAvailable() {
        return enabled && !apiKey.isEmpty();
    }
    
    /**
     * Créer un nouvel avatar Ready Player Me avec des paramètres spécifiques.
     * 
     * @param gender Genre de l'avatar (male/female)
     * @param age Âge approximatif
     * @param style Style/thème de l'avatar
     * @return CompletableFuture avec l'ID de l'avatar créé
     */
    public CompletableFuture<String> createAvatar(String gender, int age, String style) {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(getDefaultAvatarId());
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOGGER.log(Level.INFO, "Création d'un avatar Ready Player Me: {0}, {1} ans, style {2}", 
                          new Object[]{gender, age, style});
                
                // Préparer les paramètres de création
                String requestBody = buildCreateAvatarRequest(gender, age, style);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/avatars"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    JsonNode responseJson = objectMapper.readTree(response.body());
                    String avatarId = responseJson.get("id").asText();
                    
                    LOGGER.log(Level.INFO, "Avatar Ready Player Me créé avec succès: {0}", avatarId);
                    return avatarId;
                } else {
                    LOGGER.log(Level.WARNING, "Erreur lors de la création de l'avatar: {0} - {1}", 
                              new Object[]{response.statusCode(), response.body()});
                    return getDefaultAvatarId();
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception lors de la création de l'avatar Ready Player Me", e);
                return getDefaultAvatarId();
            }
        });
    }
    
    /**
     * Obtient l'URL de téléchargement du modèle 3D d'un avatar.
     * 
     * @param avatarId ID de l'avatar Ready Player Me
     * @return CompletableFuture avec l'URL du modèle GLB
     */
    public CompletableFuture<String> getAvatarModelUrl(String avatarId) {
        if (!isAvailable() || avatarId == null || avatarId.isEmpty()) {
            return CompletableFuture.completedFuture(getFallbackModelUrl());
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String quality = configManager.getString("avatar.readyPlayerMe.quality", "high");
                String format = configManager.getString("avatar.readyPlayerMe.modelFormat", "glb");
                
                String url = String.format("%s/avatars/%s.%s?quality=%s", 
                                         baseUrl, avatarId, format, quality);
                
                // Vérifier que l'avatar existe
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .HEAD()
                    .build();
                
                HttpResponse<Void> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.discarding());
                
                if (response.statusCode() == 200) {
                    LOGGER.log(Level.INFO, "URL du modèle Ready Player Me obtenue: {0}", url);
                    return url;
                } else {
                    LOGGER.log(Level.WARNING, "Avatar Ready Player Me non trouvé: {0}", avatarId);
                    return getFallbackModelUrl();
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de l'obtention de l'URL du modèle", e);
                return getFallbackModelUrl();
            }
        });
    }
    
    /**
     * Liste les avatars disponibles pour l'utilisateur.
     * 
     * @return CompletableFuture avec la liste des avatars
     */
    public CompletableFuture<JsonNode> listUserAvatars() {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/avatars"))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return objectMapper.readTree(response.body());
                } else {
                    LOGGER.log(Level.WARNING, "Erreur lors de la récupération des avatars: {0}", 
                              response.statusCode());
                    return null;
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception lors de la récupération des avatars", e);
                return null;
            }
        });
    }
    
    /**
     * Supprime un avatar Ready Player Me.
     * 
     * @param avatarId ID de l'avatar à supprimer
     * @return CompletableFuture avec le résultat de l'opération
     */
    public CompletableFuture<Boolean> deleteAvatar(String avatarId) {
        if (!isAvailable() || avatarId == null || avatarId.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/avatars/" + avatarId))
                    .header("Authorization", "Bearer " + apiKey)
                    .DELETE()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                boolean success = response.statusCode() == 200 || response.statusCode() == 204;
                if (success) {
                    LOGGER.log(Level.INFO, "Avatar Ready Player Me supprimé: {0}", avatarId);
                } else {
                    LOGGER.log(Level.WARNING, "Erreur lors de la suppression de l'avatar: {0}", 
                              response.statusCode());
                }
                
                return success;
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception lors de la suppression de l'avatar", e);
                return false;
            }
        });
    }
    
    /**
     * Construit la requête JSON pour créer un avatar.
     */
    private String buildCreateAvatarRequest(String gender, int age, String style) {
        try {
            return String.format("""
                {
                    "bodyType": "%s",
                    "appearancePreset": "%s",
                    "style": "%s",
                    "ageGroup": "%s"
                }
                """, 
                gender,
                determineAppearancePreset(gender, age),
                style,
                determineAgeGroup(age)
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la construction de la requête", e);
            return "{}";
        }
    }
    
    /**
     * Détermine le preset d'apparence selon le genre et l'âge.
     */
    private String determineAppearancePreset(String gender, int age) {
        if ("female".equalsIgnoreCase(gender)) {
            if (age < 25) return "young_woman";
            else if (age < 40) return "adult_woman";
            else return "mature_woman";
        } else {
            if (age < 25) return "young_man";
            else if (age < 40) return "adult_man";
            else return "mature_man";
        }
    }
    
    /**
     * Détermine le groupe d'âge pour Ready Player Me.
     */
    private String determineAgeGroup(int age) {
        if (age < 18) return "teen";
        else if (age < 30) return "young_adult";
        else if (age < 50) return "adult";
        else return "senior";
    }
    
    /**
     * Obtient l'ID de l'avatar par défaut.
     */
    private String getDefaultAvatarId() {
        return configManager.getString("avatar.readyPlayerMe.defaultAvatarId", "default");
    }
    
    /**
     * Obtient l'URL du modèle de fallback local.
     */
    private String getFallbackModelUrl() {
        String gender = configManager.getString("avatar.appearance.gender", "female");
        int age = configManager.getInt("avatar.appearance.age", 30);
        String basePath = configManager.getString("avatar.models.localPath", "/static/models/avatars/");
        
        String ageGroup;
        if (age < 25) ageGroup = "young";
        else if (age < 40) ageGroup = "adult";
        else ageGroup = "mature";
        
        String modelKey = String.format("avatar.models.fallback.%s.%s", gender, ageGroup);
        String modelFile = configManager.getString(modelKey, "default_avatar.glb");
        
        return basePath + modelFile;
    }
}

package com.angel.api;

import com.angel.avatar.AvatarManager;
import com.angel.avatar.ReadyPlayerMeService;
import com.angel.config.ConfigManager;
import com.angel.ui.AvatarController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur REST pour l'API Avatar.
 * Expose les fonctionnalités de l'avatar via des endpoints HTTP.
 */
@RestController
@RequestMapping("/api/avatar")
@CrossOrigin(origins = "*")
public class AvatarApiController {

    private static final Logger LOGGER = Logger.getLogger(AvatarApiController.class.getName());

    private final AvatarController avatarController;
    private final AvatarManager avatarManager;
    private final ReadyPlayerMeService readyPlayerMeService;
    private final ConfigManager configManager;

    @Autowired
    public AvatarApiController(AvatarController avatarController,
                              AvatarManager avatarManager,
                              ReadyPlayerMeService readyPlayerMeService,
                              ConfigManager configManager) {
        this.avatarController = avatarController;
        this.avatarManager = avatarManager;
        this.readyPlayerMeService = readyPlayerMeService;
        this.configManager = configManager;
    }

    /**
     * Obtient la configuration de l'avatar pour le frontend.
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAvatarConfig() {
        try {
            Map<String, Object> config = configManager.getAllConfigAsMap();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la configuration", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Initialise l'avatar.
     */
    @PostMapping("/initialize")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> initializeAvatar() {
        return avatarController.initialize()
            .thenApply(v -> {
                Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Avatar initialisé avec succès",
                    "ready", avatarController.isReady()
                );
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation", throwable);
                Map<String, Object> response = Map.of(
                    "status", "error",
                    "message", "Erreur lors de l'initialisation: " + throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(response);
            });
    }

    /**
     * Fait parler l'avatar.
     */
    @PostMapping("/speak")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> speakText(
            @RequestBody Map<String, Object> request) {
        
        String text = (String) request.get("text");
        String emotion = (String) request.getOrDefault("emotion", "neutral");
        Long durationMs = request.containsKey("duration") ? 
            ((Number) request.get("duration")).longValue() : null;

        if (text == null || text.trim().isEmpty()) {
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Le texte ne peut pas être vide"
            );
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }

        if (durationMs != null) {
            return avatarController.displayMessage(text, emotion, durationMs)
                .thenApply(v -> {
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "message", "Message affiché avec succès"
                    );
                    return ResponseEntity.ok(response);
                });
        } else {
            return avatarManager.speak(text, emotion)
                .thenApply(v -> {
                    Map<String, Object> response = Map.of(
                        "status", "success", 
                        "message", "Parole terminée avec succès"
                    );
                    return ResponseEntity.ok(response);
                });
        }
    }

    /**
     * Change l'émotion de l'avatar.
     */
    @PostMapping("/emotion")
    public ResponseEntity<Map<String, Object>> setEmotion(@RequestBody Map<String, Object> request) {
        try {
            String emotion = (String) request.get("emotion");
            Double intensity = request.containsKey("intensity") ? 
                ((Number) request.get("intensity")).doubleValue() : 0.7;

            if (emotion == null) {
                Map<String, Object> response = Map.of(
                    "status", "error",
                    "message", "L'émotion doit être spécifiée"
                );
                return ResponseEntity.badRequest().body(response);
            }

            avatarManager.setEmotion(emotion, intensity);

            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Émotion changée vers: " + emotion,
                "emotion", emotion,
                "intensity", intensity
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du changement d'émotion", e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Erreur lors du changement d'émotion: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Déclenche un geste de l'avatar.
     */
    @PostMapping("/gesture")
    public ResponseEntity<Map<String, Object>> playGesture(@RequestBody Map<String, Object> request) {
        try {
            String gestureType = (String) request.get("gesture");

            if (gestureType == null) {
                Map<String, Object> response = Map.of(
                    "status", "error",
                    "message", "Le type de geste doit être spécifié"
                );
                return ResponseEntity.badRequest().body(response);
            }

            avatarManager.playGesture(gestureType);

            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Geste déclenché: " + gestureType,
                "gesture", gestureType
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du geste", e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Erreur lors du geste: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Change l'apparence de l'avatar.
     */
    @PostMapping("/appearance")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> changeAppearance(
            @RequestBody Map<String, Object> request) {
        
        try {
            String gender = (String) request.getOrDefault("gender", "female");
            Integer age = request.containsKey("age") ? 
                ((Number) request.get("age")).intValue() : 30;
            String style = (String) request.getOrDefault("style", "casual");

            return avatarController.changeAppearance(gender, age, style)
                .thenApply(v -> {
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "message", "Apparence changée avec succès",
                        "gender", gender,
                        "age", age,
                        "style", style
                    );
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    LOGGER.log(Level.SEVERE, "Erreur lors du changement d'apparence", throwable);
                    Map<String, Object> response = Map.of(
                        "status", "error",
                        "message", "Erreur lors du changement d'apparence: " + throwable.getMessage()
                    );
                    return ResponseEntity.internalServerError().body(response);
                });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur dans la requête de changement d'apparence", e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Requête invalide: " + e.getMessage()
            );
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
    }

    /**
     * Affiche l'avatar.
     */
    @PostMapping("/show")
    public ResponseEntity<Map<String, Object>> showAvatar() {
        try {
            avatarController.showAvatar();
            avatarManager.show();

            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Avatar affiché"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage", e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Erreur lors de l'affichage: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Cache l'avatar.
     */
    @PostMapping("/hide")
    public ResponseEntity<Map<String, Object>> hideAvatar() {
        try {
            avatarController.hideAvatar();
            avatarManager.hide();

            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Avatar masqué"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du masquage", e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Erreur lors du masquage: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Salue l'utilisateur.
     */
    @PostMapping("/greet")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> greetUser() {
        return avatarController.greetUser()
            .thenApply(v -> {
                Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Salutation effectuée"
                );
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                LOGGER.log(Level.SEVERE, "Erreur lors de la salutation", throwable);
                Map<String, Object> response = Map.of(
                    "status", "error",
                    "message", "Erreur lors de la salutation: " + throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(response);
            });
    }

    /**
     * Dit au revoir.
     */
    @PostMapping("/goodbye")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sayGoodbye() {
        return avatarController.sayGoodbye()
            .thenApply(v -> {
                Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Adieux effectués"
                );
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                LOGGER.log(Level.SEVERE, "Erreur lors des adieux", throwable);
                Map<String, Object> response = Map.of(
                    "status", "error",
                    "message", "Erreur lors des adieux: " + throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(response);
            });
    }

    /**
     * Obtient le statut de l'avatar.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAvatarStatus() {
        try {
            Map<String, Object> status = Map.of(
                "ready", avatarController.isReady(),
                "active", avatarManager.isActive(),
                "currentEmotion", avatarManager.getCurrentEmotion(),
                "config", avatarController.getCurrentConfig(),
                "readyPlayerMeAvailable", readyPlayerMeService.isAvailable()
            );
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du statut", e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Erreur lors de la récupération du statut: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API Ready Player Me - Crée un nouvel avatar.
     */
    @PostMapping("/ready-player-me/create")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createReadyPlayerMeAvatar(
            @RequestBody Map<String, Object> request) {
        
        if (!readyPlayerMeService.isAvailable()) {
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Ready Player Me n'est pas disponible"
            );
<<<<<<< HEAD
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)    
            		.body(response));
=======
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
            );
>>>>>>> dacda26b96432273f80e1ad13d2d020599e7e916
        }

        try {
            String gender = (String) request.getOrDefault("gender", "female");
            Integer age = request.containsKey("age") ? 
                ((Number) request.get("age")).intValue() : 30;
            String style = (String) request.getOrDefault("style", "casual");

            return readyPlayerMeService.createAvatar(gender, age, style)
                .thenApply(avatarId -> {
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "message", "Avatar Ready Player Me créé",
                        "avatarId", avatarId
                    );
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    LOGGER.log(Level.SEVERE, "Erreur lors de la création Ready Player Me", throwable);
                    Map<String, Object> response = Map.of(
                        "status", "error",
                        "message", "Erreur lors de la création: " + throwable.getMessage()
                    );
                    return ResponseEntity.internalServerError().body(response);
                });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur dans la requête Ready Player Me", e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Requête invalide: " + e.getMessage()
            );
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)    
            		.body(response));
        }
    }

    /**
     * API Ready Player Me - Obtient l'URL du modèle.
     */
    @GetMapping("/ready-player-me/{avatarId}/model")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getReadyPlayerMeModelUrl(
            @PathVariable String avatarId) {
        
        if (!readyPlayerMeService.isAvailable()) {
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Ready Player Me n'est pas disponible"
            );
<<<<<<< HEAD
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)    
            		.body(response));
=======
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
            );
>>>>>>> dacda26b96432273f80e1ad13d2d020599e7e916
        }

        return readyPlayerMeService.getAvatarModelUrl(avatarId)
            .thenApply(modelUrl -> {
                Map<String, Object> response = Map.of(
                    "status", "success",
                    "avatarId", avatarId,
                    "modelUrl", modelUrl
                );
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                LOGGER.log(Level.SEVERE, "Erreur lors de la récupération du modèle", throwable);
                Map<String, Object> response = Map.of(
                    "status", "error",
                    "message", "Erreur lors de la récupération: " + throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(response);
            });
    }

    /**
     * API Ready Player Me - Liste les avatars.
     */
    @GetMapping("/ready-player-me/avatars")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> listReadyPlayerMeAvatars() {
        if (!readyPlayerMeService.isAvailable()) {
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Ready Player Me n'est pas disponible"
            );
<<<<<<< HEAD
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)    
            		.body(response));
=======
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
            );
>>>>>>> dacda26b96432273f80e1ad13d2d020599e7e916
        }

        return readyPlayerMeService.listUserAvatars()
            .thenApply(avatars -> {
                Map<String, Object> response = Map.of(
                    "status", "success",
                    "avatars", avatars != null ? avatars : "null"
                );
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                LOGGER.log(Level.SEVERE, "Erreur lors de la liste des avatars", throwable);
                Map<String, Object> response = Map.of(
                    "status", "error",
                    "message", "Erreur lors de la récupération: " + throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(response);
            });
    }

    /**
     * API Ready Player Me - Supprime un avatar.
     */
    @DeleteMapping("/ready-player-me/{avatarId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteReadyPlayerMeAvatar(
            @PathVariable String avatarId) {
        
        if (!readyPlayerMeService.isAvailable()) {
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Ready Player Me n'est pas disponible"
            );
<<<<<<< HEAD
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)    
            		.body(response));
=======
            return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
            );
>>>>>>> dacda26b96432273f80e1ad13d2d020599e7e916
        }

        return readyPlayerMeService.deleteAvatar(avatarId)
            .thenApply(success -> {
                if (success) {
                    Map<String, Object> response = Map.of(
                        "status", "success",
                        "message", "Avatar supprimé avec succès",
                        "avatarId", avatarId
                    );
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = Map.of(
                        "status", "error",
                        "message", "Impossible de supprimer l'avatar"
                    );
                    return ResponseEntity.internalServerError().body(response);
                }
            })
            .exceptionally(throwable -> {
                LOGGER.log(Level.SEVERE, "Erreur lors de la suppression", throwable);
                Map<String, Object> response = Map.of(
                    "status", "error",
                    "message", "Erreur lors de la suppression: " + throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(response);
            });
    }

    /**
     * Obtient les informations de debug.
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> getDebugInfo() {
        try {
            Map<String, Object> debug = Map.of(
                "avatarController", Map.of(
                    "ready", avatarController.isReady(),
                    "config", avatarController.getCurrentConfig()
                ),
                "avatarManager", Map.of(
                    "active", avatarManager.isActive(),
                    "currentEmotion", avatarManager.getCurrentEmotion(),
                    "config", avatarManager.getCurrentConfig()
                ),
                "readyPlayerMe", Map.of(
                    "available", readyPlayerMeService.isAvailable()
                ),
                "configManager", configManager.getConfigStats()
            );
            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des infos de debug", e);
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Erreur lors de la récupération des infos de debug: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

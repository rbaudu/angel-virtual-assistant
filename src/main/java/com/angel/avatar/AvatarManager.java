package com.angel.avatar;

import com.angel.config.ConfigManager;
import com.angel.util.LogUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire principal pour l'avatar réaliste 3D.
 * Coordonne le rendu, les animations, et la synthèse vocale.
 */
@Component
public class AvatarManager {
    
    private static final Logger LOGGER = LogUtil.getLogger(AvatarManager.class);
    
    private final ConfigManager configManager;
    private final TextToSpeechService ttsService;
    private final WebSocketService webSocketService;
    private final EmotionAnalyzer emotionAnalyzer;
    
    private AvatarConfig currentConfig;
    private boolean isActive = false;
    private String currentEmotion = "neutral";
    
    public AvatarManager(ConfigManager configManager, 
                        TextToSpeechService ttsService,
                        WebSocketService webSocketService,
                        EmotionAnalyzer emotionAnalyzer) {
        this.configManager = configManager;
        this.ttsService = ttsService;
        this.webSocketService = webSocketService;
        this.emotionAnalyzer = emotionAnalyzer;
        
        // Charger la configuration initiale
        loadAvatarConfig();
    }
    
    /**
     * Charge la configuration de l'avatar depuis le fichier de configuration.
     */
    private void loadAvatarConfig() {
        this.currentConfig = new AvatarConfig();
        
        // Configuration de base
        currentConfig.setEnabled(configManager.getBoolean("avatar.enabled", true));
        currentConfig.setType(configManager.getString("avatar.type", "3d_realistic"));
        currentConfig.setModel(configManager.getString("avatar.model", "female_30_casual"));
        currentConfig.setVoiceType(configManager.getString("avatar.voiceType", "female_french_warm"));
        
        // Configuration d'apparence
        currentConfig.setAge(configManager.getInt("avatar.appearance.age", 30));
        currentConfig.setGender(configManager.getString("avatar.appearance.gender", "female"));
        currentConfig.setStyle(configManager.getString("avatar.appearance.style", "casual_friendly"));
        currentConfig.setHairColor(configManager.getString("avatar.appearance.hairColor", "brown"));
        currentConfig.setEyeColor(configManager.getString("avatar.appearance.eyeColor", "brown"));
        currentConfig.setSkinTone(configManager.getString("avatar.appearance.skinTone", "medium"));
        
        // Configuration des capacités
        currentConfig.setLipSyncEnabled(configManager.getBoolean("avatar.lipSync", true));
        currentConfig.setBlinkingEnabled(configManager.getBoolean("avatar.blinking", true));
        currentConfig.setHeadMovementEnabled(configManager.getBoolean("avatar.headMovement", true));
        currentConfig.setBodyLanguageEnabled(configManager.getBoolean("avatar.bodyLanguage", true));
        
        LOGGER.log(Level.INFO, "Configuration avatar chargée: {0}", currentConfig);
    }
    
    /**
     * Initialise l'avatar avec la configuration actuelle.
     */
    public CompletableFuture<Void> initializeAvatar() {
        if (!currentConfig.isEnabled()) {
            LOGGER.log(Level.INFO, "Avatar désactivé");
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                LOGGER.log(Level.INFO, "Initialisation de l'avatar...");
                
                // Envoyer la configuration initiale au frontend
                AvatarInitMessage initMessage = new AvatarInitMessage(currentConfig);
                webSocketService.sendToAvatar(initMessage);
                
                // Marquer comme actif
                isActive = true;
                
                LOGGER.log(Level.INFO, "Avatar initialisé avec succès");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation de l'avatar", e);
                throw new RuntimeException("Impossible d'initialiser l'avatar", e);
            }
        });
    }
    
    /**
     * Fait parler l'avatar avec un message donné.
     * 
     * @param text Le texte à faire dire à l'avatar
     * @param emotion L'émotion à exprimer (optionnel)
     * @return CompletableFuture qui se termine quand l'avatar a fini de parler
     */
    public CompletableFuture<Void> speak(String text, String emotion) {
        if (!isActive || !currentConfig.isEnabled()) {
            LOGGER.log(Level.WARNING, "Avatar non actif ou désactivé");
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                LOGGER.log(Level.INFO, "Avatar parle: {0} (émotion: {1})", new Object[]{text, emotion});
                
                // 1. Analyser l'émotion du texte si non spécifiée
                String finalEmotion = emotion;
                if (finalEmotion == null || finalEmotion.isEmpty()) {
                    finalEmotion = emotionAnalyzer.analyzeText(text);
                }
                
                // 2. Générer l'audio avec données de synchronisation labiale
                TTSResult ttsResult = ttsService.generateSpeechWithVisemes(
                    text, 
                    currentConfig.getVoiceType()
                );
                
                // 3. Préparer les données d'animation
                AvatarAnimationData animationData = prepareAnimationData(finalEmotion, text);
                
                // 4. Créer le message pour le frontend
                AvatarSpeechMessage speechMessage = new AvatarSpeechMessage(
                    text,
                    ttsResult.getAudioData(),
                    ttsResult.getVisemeData(),
                    finalEmotion,
                    animationData,
                    ttsResult.getDuration()
                );
                
                // 5. Envoyer au frontend
                webSocketService.sendToAvatar(speechMessage);
                
                // 6. Mettre à jour l'état
                currentEmotion = finalEmotion;
                
                // 7. Attendre la fin de la lecture
                Thread.sleep(ttsResult.getDuration());
                
                LOGGER.log(Level.INFO, "Avatar a terminé de parler");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la lecture par l'avatar", e);
            }
        });
    }
    
    /**
     * Change l'apparence de l'avatar.
     * 
     * @param gender Genre (male/female)
     * @param age Âge approximatif
     * @param style Style vestimentaire
     * @return CompletableFuture qui se termine quand le changement est effectué
     */
    public CompletableFuture<Void> changeAppearance(String gender, int age, String style) {
        return CompletableFuture.runAsync(() -> {
            try {
                LOGGER.log(Level.INFO, "Changement d'apparence: {0}, {1} ans, style {2}", 
                          new Object[]{gender, age, style});
                
                // Mettre à jour la configuration
                currentConfig.setGender(gender);
                currentConfig.setAge(age);
                currentConfig.setStyle(style);
                
                // Déterminer le nouveau modèle
                String newModel = determineModelPath(gender, age, style);
                currentConfig.setModel(newModel);
                
                // Envoyer la mise à jour au frontend
                AvatarAppearanceMessage appearanceMessage = new AvatarAppearanceMessage(
                    newModel,
                    gender,
                    age,
                    style
                );
                
                webSocketService.sendToAvatar(appearanceMessage);
                
                LOGGER.log(Level.INFO, "Apparence de l'avatar mise à jour");
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du changement d'apparence", e);
            }
        });
    }
    
    /**
     * Change l'émotion courante de l'avatar.
     * 
     * @param emotion Nouvelle émotion
     * @param intensity Intensité de l'émotion (0.0 à 1.0)
     */
    public void setEmotion(String emotion, double intensity) {
        if (!isActive) return;
        
        try {
            currentEmotion = emotion;
            
            AvatarEmotionMessage emotionMessage = new AvatarEmotionMessage(
                emotion,
                intensity,
                800 // Durée de transition en ms
            );
            
            webSocketService.sendToAvatar(emotionMessage);
            
            LOGGER.log(Level.FINE, "Émotion de l'avatar changée: {0} (intensité: {1})", 
                      new Object[]{emotion, intensity});
                      
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du changement d'émotion", e);
        }
    }
    
    /**
     * Démarre un geste ou animation spécifique.
     * 
     * @param gestureType Type de geste (wave, nod, shrug, etc.)
     */
    public void playGesture(String gestureType) {
        if (!isActive) return;
        
        try {
            AvatarGestureMessage gestureMessage = new AvatarGestureMessage(gestureType);
            webSocketService.sendToAvatar(gestureMessage);
            
            LOGGER.log(Level.FINE, "Geste déclenché: {0}", gestureType);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du geste", e);
        }
    }
    
    /**
     * Cache l'avatar.
     */
    public void hide() {
        if (!isActive) return;
        
        try {
            AvatarVisibilityMessage visibilityMessage = new AvatarVisibilityMessage(false);
            webSocketService.sendToAvatar(visibilityMessage);
            
            LOGGER.log(Level.INFO, "Avatar masqué");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du masquage de l'avatar", e);
        }
    }
    
    /**
     * Affiche l'avatar.
     */
    public void show() {
        if (!isActive) return;
        
        try {
            AvatarVisibilityMessage visibilityMessage = new AvatarVisibilityMessage(true);
            webSocketService.sendToAvatar(visibilityMessage);
            
            LOGGER.log(Level.INFO, "Avatar affiché");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'affichage de l'avatar", e);
        }
    }
    
    /**
     * Prépare les données d'animation en fonction de l'émotion et du contexte.
     */
    private AvatarAnimationData prepareAnimationData(String emotion, String text) {
        AvatarAnimationData data = new AvatarAnimationData();
        
        // Animation de base selon l'émotion
        data.setBaseAnimation(getBaseAnimationForEmotion(emotion));
        
        // Mouvements de tête selon le texte
        data.setHeadMovements(generateHeadMovements(text));
        
        // Gestes selon le contenu
        data.setGestures(generateGestures(text, emotion));
        
        // Clignements naturels
        if (currentConfig.isBlinkingEnabled()) {
            data.setBlinkPattern(generateNaturalBlinks());
        }
        
        return data;
    }
    
    /**
     * Détermine le chemin du modèle 3D selon les paramètres d'apparence.
     */
    private String determineModelPath(String gender, int age, String style) {
        String ageGroup;
        if (age < 25) ageGroup = "young";
        else if (age < 40) ageGroup = "adult";
        else if (age < 60) ageGroup = "mature";
        else ageGroup = "senior";
        
        return String.format("/models/avatars/%s_%s_%s.glb", gender, ageGroup, style);
    }
    
    /**
     * Obtient l'animation de base pour une émotion donnée.
     */
    private String getBaseAnimationForEmotion(String emotion) {
        switch (emotion.toLowerCase()) {
            case "happy": return "smile_idle";
            case "sad": return "sad_idle";
            case "excited": return "excited_idle";
            case "concerned": return "concerned_idle";
            case "thoughtful": return "thinking_idle";
            default: return "neutral_idle";
        }
    }
    
    /**
     * Génère des mouvements de tête naturels selon le texte.
     */
    private HeadMovementData generateHeadMovements(String text) {
        HeadMovementData movements = new HeadMovementData();
        
        // Hochements de tête pour les affirmations
        if (text.contains("oui") || text.contains("certainement") || text.contains("absolument")) {
            movements.addNod(0.5, 1.0);
        }
        
        // Mouvements latéraux pour les négations
        if (text.contains("non") || text.contains("jamais") || text.contains("pas")) {
            movements.addShake(0.3, 0.8);
        }
        
        // Inclinaison pour les questions
        if (text.contains("?")) {
            movements.addTilt(0.2, 0.6);
        }
        
        return movements;
    }
    
    /**
     * Génère des gestes selon le contenu et l'émotion.
     */
    private GestureData generateGestures(String text, String emotion) {
        GestureData gestures = new GestureData();
        
        // Gestes selon le contenu
        if (text.contains("bonjour") || text.contains("salut")) {
            gestures.addGesture("wave", 0.0, 2.0);
        }
        
        if (text.contains("au revoir") || text.contains("à bientôt")) {
            gestures.addGesture("goodbye_wave", 0.0, 2.0);
        }
        
        // Gestes selon l'émotion
        if ("excited".equals(emotion)) {
            gestures.addGesture("enthusiastic_gesture", 1.0, 1.5);
        }
        
        if ("concerned".equals(emotion)) {
            gestures.addGesture("concerned_gesture", 0.5, 1.0);
        }
        
        return gestures;
    }
    
    /**
     * Génère un pattern de clignements naturels.
     */
    private BlinkPattern generateNaturalBlinks() {
        BlinkPattern pattern = new BlinkPattern();
        
        // Clignements aléatoires toutes les 2-4 secondes
        double time = 0;
        while (time < 10) { // Pattern de 10 secondes
            time += 2 + Math.random() * 2; // 2-4 secondes
            pattern.addBlink(time, 0.15); // Clignement de 150ms
        }
        
        return pattern;
    }
    
    // Getters
    public AvatarConfig getCurrentConfig() {
        return currentConfig;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public String getCurrentEmotion() {
        return currentEmotion;
    }
}

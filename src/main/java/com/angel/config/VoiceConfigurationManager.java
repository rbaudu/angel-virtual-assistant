package com.angel.config;

import com.angel.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de configuration spécialisé pour le système vocal.
 * Centralise et optimise l'accès aux paramètres vocaux.
 */
@Component
public class VoiceConfigurationManager {

    private static final Logger LOGGER = LogUtil.getLogger(VoiceConfigurationManager.class);
    
    @Autowired
    private ConfigManager configManager;
    
    // Cache pour les configurations fréquemment utilisées
    private final ConcurrentMap<String, Object> configCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_VALIDITY_MS = 60000; // 1 minute
    
    /**
     * Configuration du mot-clé principal.
     */
    public String getPrimaryWakeWord() {
        return getCachedString("voice.wakeword.primary", "Angèle");
    }
    
    /**
     * Configuration des mots-clés alternatifs.
     */
    public List<String> getAlternativeWakeWords() {
        String alternatives = getCachedString("voice.wakeword.alternative", "Angel,Angele");
        return Arrays.asList(alternatives.split(","));
    }
    
    /**
     * Seuil de confiance minimum pour la détection du mot-clé.
     */
    public float getWakeWordConfidenceThreshold() {
        return getCachedFloat("voice.wakeword.confidence.minimum", 0.7f);
    }
    
    /**
     * Configuration de la langue de reconnaissance.
     */
    public String getRecognitionLanguage() {
        return getCachedString("voice.recognition.language", "fr-FR");
    }
    
    /**
     * Seuil de confiance minimum pour la reconnaissance vocale.
     */
    public float getRecognitionConfidenceThreshold() {
        return getCachedFloat("voice.recognition.confidence.minimum", 0.6f);
    }
    
    /**
     * Configuration de l'écoute continue.
     */
    public boolean isContinuousListeningEnabled() {
        return getCachedBoolean("voice.recognition.continuous", true);
    }
    
    /**
     * Configuration du redémarrage automatique.
     */
    public boolean isAutoRestartEnabled() {
        return getCachedBoolean("voice.recognition.auto.restart", true);
    }
    
    /**
     * Configuration de la synthèse vocale.
     */
    public boolean isSpeechSynthesisEnabled() {
        return getCachedBoolean("voice.speech.enabled", true);
    }
    
    /**
     * Langue de la synthèse vocale.
     */
    public String getSpeechLanguage() {
        return getCachedString("voice.speech.language", "fr-FR");
    }
    
    /**
     * Débit de la synthèse vocale.
     */
    public float getSpeechRate() {
        return getCachedFloat("voice.speech.rate", 0.9f);
    }
    
    /**
     * Hauteur de la synthèse vocale.
     */
    public float getSpeechPitch() {
        return getCachedFloat("voice.speech.pitch", 1.0f);
    }
    
    /**
     * Volume de la synthèse vocale.
     */
    public float getSpeechVolume() {
        return getCachedFloat("voice.speech.volume", 0.8f);
    }
    
    /**
     * Voix préférées pour la synthèse vocale.
     */
    public List<String> getPreferredVoices() {
        String voices = getCachedString("voice.speech.voice.preferred", "Alice,Marie,Hortense,Amélie");
        return Arrays.asList(voices.split(","));
    }
    
    /**
     * Configuration du timeout d'inactivité.
     */
    public int getInactivityTimeoutMinutes() {
        return getCachedInt("voice.inactivity.timeout.minutes", 5);
    }
    
    /**
     * Configuration de l'écran sombre en cas d'inactivité.
     */
    public boolean isDarkModeOnInactivityEnabled() {
        return getCachedBoolean("voice.inactivity.screen.dark", true);
    }
    
    /**
     * Configuration de masquage de l'avatar en cas d'inactivité.
     */
    public boolean isHideAvatarOnInactivityEnabled() {
        return getCachedBoolean("voice.inactivity.avatar.hide", true);
    }
    
    /**
     * Configuration de l'écoute continue pendant l'inactivité.
     */
    public boolean isContinueListeningOnInactivityEnabled() {
        return getCachedBoolean("voice.inactivity.listening.continue", true);
    }
    
    /**
     * Configuration de visibilité des contrôles par défaut.
     */
    public boolean areControlsVisibleByDefault() {
        return getCachedBoolean("voice.ui.controls.visible.default", false);
    }
    
    /**
     * Configuration du mode senior.
     */
    public boolean isSeniorModeEnabled() {
        return getCachedBoolean("voice.ui.senior.mode", true);
    }
    
    /**
     * Configuration des animations.
     */
    public boolean areAnimationsEnabled() {
        return getCachedBoolean("voice.ui.animations.enabled", true);
    }
    
    /**
     * Nom de l'utilisateur.
     */
    public String getUserName() {
        return getCachedString("user.name", "");
    }
    
    /**
     * Catégorie d'âge de l'utilisateur.
     */
    public String getUserAgeCategory() {
        return getCachedString("user.age.category", "senior");
    }
    
    /**
     * Style d'interaction préféré.
     */
    public String getUserInteractionStyle() {
        return getCachedString("user.interaction.style", "gentle");
    }
    
    // === MÉTHODES DE CACHE ===
    
    public String getCachedString(String key, String defaultValue) {
        return (String) getCachedValue(key, () -> configManager.getString(key, defaultValue));
    }
    
    public int getCachedInt(String key, int defaultValue) {
        return (Integer) getCachedValue(key, () -> configManager.getInt(key, defaultValue));
    }
    
    public float getCachedFloat(String key, float defaultValue) {
        return (Float) getCachedValue(key, () -> configManager.getFloat(key, defaultValue));
    }
    
    public boolean getCachedBoolean(String key, boolean defaultValue) {
        return (Boolean) getCachedValue(key, () -> configManager.getBoolean(key, defaultValue));
    }
    
    private Object getCachedValue(String key, java.util.function.Supplier<Object> supplier) {
        long currentTime = System.currentTimeMillis();
        
        // Vérifier si le cache est encore valide
        if (currentTime - lastCacheUpdate > CACHE_VALIDITY_MS) {
            configCache.clear();
            lastCacheUpdate = currentTime;
            LOGGER.log(Level.FINE, "Cache de configuration vocale vidé et mis à jour");
        }
        
        return configCache.computeIfAbsent(key, k -> {
            Object value = supplier.get();
            LOGGER.log(Level.FINEST, "Configuration mise en cache: {0} = {1}", new Object[]{key, value});
            return value;
        });
    }
    
    /**
     * Force la mise à jour du cache.
     */
    public void refreshCache() {
        configCache.clear();
        lastCacheUpdate = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Cache de configuration vocale forcé à se rafraîchir");
    }
    
    /**
     * Obtient toutes les configurations vocales pour le debug.
     */
    public ConcurrentMap<String, Object> getAllVoiceConfigs() {
        ConcurrentMap<String, Object> allConfigs = new ConcurrentHashMap<>();
        
        // Forcer le chargement de toutes les configurations importantes
        allConfigs.put("primaryWakeWord", getPrimaryWakeWord());
        allConfigs.put("alternativeWakeWords", getAlternativeWakeWords());
        allConfigs.put("recognitionLanguage", getRecognitionLanguage());
        allConfigs.put("speechSynthesisEnabled", isSpeechSynthesisEnabled());
        allConfigs.put("continuousListening", isContinuousListeningEnabled());
        allConfigs.put("inactivityTimeout", getInactivityTimeoutMinutes());
        allConfigs.put("seniorMode", isSeniorModeEnabled());
        allConfigs.put("userName", getUserName());
        
        return allConfigs;
    }
}
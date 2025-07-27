package com.angel.voice;

import com.angel.config.ConfigManager;
import com.angel.util.LogUtil;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component  
public class WakeWordDetector {
    private static final Logger LOGGER = LogUtil.getLogger(WakeWordDetector.class);
    
    private final ConfigManager configManager;
    private final String wakeWord;
    private boolean isListening;
    private Consumer<Void> wakeWordCallback;
    
    public WakeWordDetector(ConfigManager configManager) {
        this.configManager = configManager;
        this.wakeWord = configManager.getString("voice.wake-word", "Angel");
        this.isListening = false;
        LOGGER.log(Level.INFO, "WakeWordDetector initialisé avec mot-clé: {0}", wakeWord);
    }
    
    public void startListening(Consumer<Void> callback) {
        this.wakeWordCallback = callback;
        this.isListening = true;
        LOGGER.log(Level.INFO, "Écoute du mot-clé démarrée (côté client)");
    }
    
    public void stopListening() {
        this.isListening = false;
        LOGGER.log(Level.INFO, "Écoute du mot-clé arrêtée");
    }
    
    // Méthode appelée par le WebSocket quand le mot-clé est détecté
    public void onWakeWordDetected() {
        if (isListening && wakeWordCallback != null) {
            LOGGER.log(Level.INFO, "Mot-clé {0} détecté via WebSocket!", wakeWord);
            wakeWordCallback.accept(null);
        }
    }
    
    public void shutdown() {
        stopListening();
    }
    
    public boolean isListening() { return isListening; }
    public String getWakeWord() { return wakeWord; }
}
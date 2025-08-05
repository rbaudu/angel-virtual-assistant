/**
 * Gestionnaire d'écoute vocale continue pour Angel
 * Gère l'écoute automatique, l'inactivité et les modes d'interface
 * 
 * Fichier : src/main/resources/static/js/voice/continuous-voice-manager.js
 */

class ContinuousVoiceManager {
    constructor() {
        this.isListening = false;
        this.isInDarkMode = false;
        this.controlsVisible = false;
        this.lastActivity = Date.now();
        this.inactivityTimeout = 5 * 60 * 1000; // 5 minutes par défaut
        this.inactivityTimer = null;
        
        // Configuration
        this.config = {
            autoStartListening: true,
            continuousMode: true,
            inactivityTimeoutMinutes: 5,
            hideControlsByDefault: true
        };
        
        this.setupEventListeners();
        console.log('🎤 ContinuousVoiceManager initialisé');
    }
    
    /**
     * Démarre l'écoute continue automatique
     */
    async startContinuousListening() {
        console.log('🎤 Démarrage écoute continue...');
        
        try {
            // Vérifier que le détecteur de mot-clé est disponible
            if (!window.wakeWordDetector) {
                console.warn('⚠️ WakeWordDetector non disponible, attente...');
                // Réessayer dans 2 secondes
                setTimeout(() => this.startContinuousListening(), 2000);
                return;
            }
            
            // Démarrer l'écoute
            await window.wakeWordDetector.startListening();
            this.isListening = true;
            
            // Marquer l'activité
            this.markActivity();
            
            // Masquer les contrôles si configuré
            if (this.config.hideControlsByDefault) {
                this.hideControls();
            }
            
            console.log('✅ Écoute continue active');
            
        } catch (error) {
            console.error('❌ Erreur démarrage écoute continue:', error);
        }
    }
    
    /**
     * Configure les écouteurs d'événements
     */
    setupEventListeners() {
        // Écouter les événements d'activité utilisateur
        ['click', 'keydown', 'mousemove', 'touchstart'].forEach(eventType => {
            document.addEventListener(eventType, () => {
                this.markActivity();
            }, { passive: true });
        });
        
        // Écouter les événements de reconnaissance vocale
        document.addEventListener('wakeWordDetected', () => {
            this.handleWakeWordDetected();
        });
        
        document.addEventListener('speechCommandReceived', (event) => {
            this.handleSpeechCommand(event.detail);
        });
        
        // Écouter les messages WebSocket
        document.addEventListener('angelWebSocketMessage', (event) => {
            this.handleWebSocketMessage(event.detail);
        });
    }
    
    /**
     * Gère la détection du mot-clé
     */
    handleWakeWordDetected() {
        console.log('🎯 Mot-clé détecté par ContinuousVoiceManager');
        
        this.markActivity();
        
        // Sortir du mode sombre si nécessaire
        if (this.isInDarkMode) {
            this.exitDarkMode();
        }
        
        // Animation d'activation
        this.showActivationFeedback();
    }
    
    /**
     * Gère les commandes vocales
     */
    handleSpeechCommand(command) {
        console.log('🗣️ Commande vocale reçue:', command);
        
        this.markActivity();
        
        const lowerCommand = command.toLowerCase();
        
        // Commandes d'interface
        if (this.containsKeywords(lowerCommand, ['affiche la configuration', 'montre les contrôles', 'affiche les contrôles'])) {
            this.showControls();
        } else if (this.containsKeywords(lowerCommand, ['cache la configuration', 'masque les contrôles', 'cache les contrôles'])) {
            this.hideControls();
        }
    }
    
    /**
     * Marque une activité utilisateur
     */
    markActivity() {
        this.lastActivity = Date.now();
        
        // Réinitialiser le timer d'inactivité
        if (this.inactivityTimer) {
            clearTimeout(this.inactivityTimer);
        }
        
        // Programmer le prochain check d'inactivité
        this.inactivityTimer = setTimeout(() => {
            this.checkInactivity();
        }, this.inactivityTimeout);
        
        // Sortir du mode sombre si nécessaire
        if (this.isInDarkMode) {
            this.exitDarkMode();
        }
    }
    
    /**
     * Vérifie l'inactivité
     */
    checkInactivity() {
        const timeSinceActivity = Date.now() - this.lastActivity;
        
        if (timeSinceActivity >= this.inactivityTimeout && !this.isInDarkMode) {
            console.log('😴 Inactivité détectée, passage en mode sombre');
            this.enterDarkMode();
        }
    }
    
    /**
     * Entre en mode sombre
     */
    enterDarkMode() {
        console.log('🌙 Passage en mode sombre');
        
        this.isInDarkMode = true;
        
        // Cacher l'avatar si configuré
        this.hideAvatar();
        
        // Assombrir l'écran
        this.createDarkOverlay();
        
        // Cacher tous les contrôles
        this.hideControls();
        
        // Garder l'écoute active
        console.log('🎤 Écoute continue maintenue en mode sombre');
    }
    
    /**
     * Sort du mode sombre
     */
    exitDarkMode() {
        if (!this.isInDarkMode) return;
        
        console.log('☀️ Sortie du mode sombre');
        
        this.isInDarkMode = false;
        
        // Supprimer l'overlay sombre
        this.removeDarkOverlay();
        
        // Réafficher l'avatar
        this.showAvatar();
        
        // Redémarrer l'écoute si nécessaire
        if (this.config.autoStartListening && !this.isListening) {
            this.startContinuousListening();
        }
    }
    
    /**
     * Affiche les contrôles
     */
    showControls() {
        console.log('👁️ Affichage des contrôles');
        
        this.controlsVisible = true;
        
        // Afficher les contrôles avatar
        const avatarControls = document.getElementById('avatar-controls');
        if (avatarControls) {
            avatarControls.style.display = 'flex';
            avatarControls.classList.add('visible');
        }
        
        // Afficher les contrôles vocaux
        const voiceInterface = document.querySelector('.voice-interface');
        if (voiceInterface) {
            voiceInterface.style.display = 'block';
            voiceInterface.classList.add('visible');
        }
        
        this.markActivity();
    }
    
    /**
     * Cache les contrôles
     */
    hideControls() {
        console.log('🙈 Masquage des contrôles');
        
        this.controlsVisible = false;
        
        // Cacher les contrôles avatar
        const avatarControls = document.getElementById('avatar-controls');
        if (avatarControls) {
            avatarControls.classList.remove('visible');
            setTimeout(() => {
                if (!this.controlsVisible) {
                    avatarControls.style.display = 'none';
                }
            }, 300);
        }
        
        // Cacher les contrôles vocaux (garder seulement l'indicateur d'écoute)
        const voiceInterface = document.querySelector('.voice-interface');
        if (voiceInterface) {
            voiceInterface.classList.remove('visible');
            
            // Garder seulement l'indicateur d'écoute visible
            const listeningIndicator = document.getElementById('listening-indicator');
            if (listeningIndicator) {
                listeningIndicator.style.display = 'block';
            }
        }
        
        this.markActivity();
    }
    
    /**
     * Crée l'overlay sombre
     */
    createDarkOverlay() {
        // Supprimer l'overlay existant s'il y en a un
        this.removeDarkOverlay();
        
        const overlay = document.createElement('div');
        overlay.id = 'dark-mode-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: #000;
            z-index: 9998;
            opacity: 0;
            transition: opacity 1s ease;
            pointer-events: none;
        `;
        
        document.body.appendChild(overlay);
        
        // Animation d'apparition
        requestAnimationFrame(() => {
            overlay.style.opacity = '0.95';
        });
        
        // Afficher l'heure au centre
        this.showTimeDisplay();
    }
    
    /**
     * Supprime l'overlay sombre
     */
    removeDarkOverlay() {
        const overlay = document.getElementById('dark-mode-overlay');
        const timeDisplay = document.getElementById('dark-mode-time');
        
        if (overlay) {
            overlay.style.opacity = '0';
            setTimeout(() => {
                if (overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            }, 1000);
        }
        
        if (timeDisplay) {
            timeDisplay.remove();
        }
        
        if (this.timeUpdateInterval) {
            clearInterval(this.timeUpdateInterval);
        }
    }
    
    /**
     * Affiche l'heure en mode sombre
     */
    showTimeDisplay() {
        const timeDisplay = document.createElement('div');
        timeDisplay.id = 'dark-mode-time';
        timeDisplay.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            color: rgba(255, 255, 255, 0.8);
            font-size: 3rem;
            font-weight: 300;
            z-index: 9999;
            text-align: center;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            text-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
        `;
        
        document.body.appendChild(timeDisplay);
        
        // Mettre à jour l'heure
        this.updateTimeDisplay();
        
        // Mettre à jour toutes les minutes
        this.timeUpdateInterval = setInterval(() => {
            this.updateTimeDisplay();
        }, 60000);
    }
    
    /**
     * Met à jour l'affichage de l'heure
     */
    updateTimeDisplay() {
        const timeDisplay = document.getElementById('dark-mode-time');
        if (timeDisplay) {
            const now = new Date();
            const timeString = now.toLocaleTimeString('fr-FR', {
                hour: '2-digit',
                minute: '2-digit'
            });
            timeDisplay.textContent = timeString;
        }
    }
    
    /**
     * Cache l'avatar
     */
    hideAvatar() {
        const avatarContainer = document.getElementById('avatar-container');
        if (avatarContainer) {
            avatarContainer.style.transition = 'opacity 1s ease';
            avatarContainer.style.opacity = '0';
        }
    }
    
    /**
     * Affiche l'avatar
     */
    showAvatar() {
        const avatarContainer = document.getElementById('avatar-container');
        if (avatarContainer) {
            avatarContainer.style.transition = 'opacity 1s ease';
            avatarContainer.style.opacity = '1';
        }
    }
    
    /**
     * Affiche un feedback d'activation
     */
    showActivationFeedback() {
        // Animation de l'indicateur d'écoute
        const indicator = document.getElementById('listening-indicator');
        if (indicator) {
            indicator.classList.add('activated');
            setTimeout(() => {
                indicator.classList.remove('activated');
            }, 2000);
        }
        
        // Pulse de l'avatar
        const avatarContainer = document.getElementById('avatar-container');
        if (avatarContainer) {
            avatarContainer.classList.add('wake-word-detected');
            setTimeout(() => {
                avatarContainer.classList.remove('wake-word-detected');
            }, 2000);
        }
    }
    
    /**
     * Vérifie si le texte contient des mots-clés
     */
    containsKeywords(text, keywords) {
        return keywords.some(keyword => text.includes(keyword.toLowerCase()));
    }
    
    /**
     * Traite les messages WebSocket
     */
    handleWebSocketMessage(message) {
        // Traiter les commandes système si nécessaire
        if (message.type === 'SYSTEM_COMMAND') {
            switch (message.command) {
                case 'SHOW_CONTROLS':
                    this.showControls();
                    break;
                case 'HIDE_CONTROLS':
                    this.hideControls();
                    break;
                case 'ENTER_DARK_MODE':
                    this.enterDarkMode();
                    break;
                case 'EXIT_DARK_MODE':
                    this.exitDarkMode();
                    break;
            }
        }
    }
    
    /**
     * Met à jour la configuration
     */
    updateConfig(newConfig) {
        this.config = { ...this.config, ...newConfig };
        
        // Mettre à jour le timeout d'inactivité
        this.inactivityTimeout = this.config.inactivityTimeoutMinutes * 60 * 1000;
        
        console.log('🔧 Configuration mise à jour:', this.config);
    }
    
    /**
     * Nettoie les ressources
     */
    destroy() {
        if (this.inactivityTimer) {
            clearTimeout(this.inactivityTimer);
        }
        
        if (this.timeUpdateInterval) {
            clearInterval(this.timeUpdateInterval);
        }
        
        this.removeDarkOverlay();
        
        console.log('🧹 ContinuousVoiceManager nettoyé');
    }
}

// Initialisation automatique
let continuousVoiceManager = null;

function initializeContinuousVoiceManager() {
    if (continuousVoiceManager) {
        return continuousVoiceManager;
    }
    
    continuousVoiceManager = new ContinuousVoiceManager();
    window.continuousVoiceManager = continuousVoiceManager;
    
    // Démarrer l'écoute continue automatiquement
    setTimeout(() => {
        continuousVoiceManager.startContinuousListening();
    }, 2000);
    
    console.log('✅ ContinuousVoiceManager initialisé');
    return continuousVoiceManager;
}

// Initialisation au chargement
document.addEventListener('DOMContentLoaded', () => {
    setTimeout(initializeContinuousVoiceManager, 1000);
});

if (document.readyState !== 'loading') {
    setTimeout(initializeContinuousVoiceManager, 1000);
}

// Nettoyage à la fermeture
window.addEventListener('beforeunload', () => {
    if (continuousVoiceManager) {
        continuousVoiceManager.destroy();
    }
});

console.log('📜 continuous-voice-manager.js chargé');
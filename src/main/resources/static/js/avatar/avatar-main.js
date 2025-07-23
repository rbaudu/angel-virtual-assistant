/**
 * Application principale Angel Avatar
 * Gère l'orchestration de tous les composants avatar
 */
class AngelAvatarApp {
    constructor() {
		if (window.angelAppInstance) {
		    console.warn('⚠️ AngelAvatarApp instance déjà existante');
		    return window.angelAppInstance;
		}
		this.avatarRenderer = null;
        this.avatarController = null;
        this.websocketManager = null;
        this.isInitialized = false;
        this.isMuted = false;
		// Marquer cette instance comme active
		window.angelAppInstance = this;
        
        // Configuration initiale compatible avec AvatarRenderer
        this.config = {
            gender: 'female',
            age: 30,
            style: 'casual',
            voice: 'female_french_warm',
            
            // Méthode get() pour compatibilité avec AvatarRenderer
            get: function(key, defaultValue) {
                const keys = key.split('.');
                let value = this;
                
                for (let k of keys) {
                    if (value && typeof value === 'object' && k in value) {
                        value = value[k];
                    } else {
                        return defaultValue;
                    }
                }
                
                return value !== undefined ? value : defaultValue;
            },
            
            // Configuration par défaut pour le rendu
            rendering: {
                antialiasing: true,
                shadows: true,
                quality: 'high'
            },
            
            appearance: {
                gender: 'female',
                age: 30,
                style: 'casual'
            },
            
            // Configuration Ready Player Me
            readyPlayerMe: {
                enabled: true,
                defaultAvatarId: '687f66fafe8107131699bf7b'
            }
        };
        
        console.log('🎭 Création de AngelAvatarApp...');
    }
    
    async init() {
        console.log('🚀 Initialisation de Angel Avatar App...');
        
        try {
            // Attendre que les dépendances soient chargées
            await this.waitForDependencies();
            
            // Initialiser le renderer directement
            await this.initializeRenderer();
            
            // Configurer les gestionnaires d'événements
            this.setupEventHandlers();
            
            // Charger l'avatar par défaut
            await this.loadDefaultAvatar();
            
            // Initialiser WebSocket si disponible
            this.initializeWebSocket();
            
            // Masquer le spinner de chargement
            this.hideLoadingSpinner();
            
            // Afficher l'avatar
            this.showAvatar();
            
            this.isInitialized = true;
            this.updateStatus('Prêt');
            
            console.log('✅ Angel Avatar App initialisé avec succès');
            
        } catch (error) {
            console.error('❌ Erreur lors de l\'initialisation:', error);
            this.handleInitError(error);
        }
    }
    
    /**
     * Attend que les dépendances soient chargées
     */
    async waitForDependencies() {
        return new Promise((resolve, reject) => {
            let attempts = 0;
            const maxAttempts = 100; // 10 secondes max
            
            const checkDependencies = () => {
                attempts++;
                
                const hasThree = typeof THREE !== 'undefined';
                const hasGLTFLoader = window.GLTFLoaderReady || (typeof THREE !== 'undefined' && THREE.GLTFLoader);
                
                console.log(`⏳ Vérification dépendances (${attempts}/${maxAttempts}): THREE=${hasThree}, GLTFLoader=${hasGLTFLoader}`);
                
                if (hasThree && hasGLTFLoader) {
                    console.log('✅ Dépendances chargées');
                    resolve();
                } else if (attempts >= maxAttempts) {
                    reject(new Error('Timeout: dépendances non chargées dans les temps'));
                } else {
                    setTimeout(checkDependencies, 100);
                }
            };
            
            checkDependencies();
        });
    }
    
    /**
     * Initialise le renderer directement
     */
	async initializeRenderer() {
	    const container = document.getElementById('avatar-viewport');
	    if (!container) {
	        throw new Error('Container avatar-viewport non trouvé');
	    }
	    
	    // CORRECTION: Nettoyer le container pour éviter la duplication
	    const existingCanvases = container.querySelectorAll('canvas');
	    existingCanvases.forEach(canvas => {
	        console.log('🧹 Suppression canvas existant');
	        canvas.remove();
	    });
	    
	    if (window.AvatarRenderer) {
	        console.log('📱 Initialisation AvatarRenderer...');
	        try {
	            this.avatarRenderer = new AvatarRenderer(container, this.config);
	            console.log('✅ AvatarRenderer initialisé avec succès');
	        } catch (error) {
	            console.error('❌ Erreur AvatarRenderer:', error);
	            throw error;
	        }
	    } else {
	        console.warn('⚠️ AvatarRenderer non disponible, mode dégradé');
	    }
	    
	    // Initialiser le contrôleur si disponible
	    if (window.AvatarController) {
	        console.log('🎮 Initialisation AvatarController...');
	        try {
	            this.avatarController = new AvatarController();
	            await this.avatarController.initialize();
	            console.log('✅ AvatarController initialisé');
	        } catch (error) {
	            console.warn('⚠️ AvatarController échoué:', error.message);
	        }
	    }
	}    
	
    /**
     * Initialise WebSocket
     */
    initializeWebSocket() {
        if (window.AvatarWebSocket) {
            console.log('🔌 Initialisation WebSocket...');
            try {
                this.websocketManager = new AvatarWebSocket(this.config);
                console.log('✅ WebSocket initialisé');
            } catch (error) {
                console.warn('⚠️ WebSocket non disponible:', error.message);
            }
        }
    }
    
    /**
     * Gère les erreurs d'initialisation
     */
    handleInitError(error) {
        console.error('❌ Erreur critique:', error);
        
        // Afficher l'erreur dans l'interface
        this.showError(`Erreur Avatar: ${error.message}`);
        
        // Essayer un mode de fallback
        this.updateStatus('Mode dégradé');
        this.hideLoadingSpinner();
    }
    
    /**
     * Affiche une erreur à l'utilisateur
     */
    showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #f44336;
            color: white;
            padding: 15px;
            border-radius: 5px;
            z-index: 9999;
            max-width: 300px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        `;
        errorDiv.textContent = message;
        document.body.appendChild(errorDiv);
        
        // Masquer après 10 secondes
        setTimeout(() => {
            if (errorDiv.parentNode) {
                errorDiv.parentNode.removeChild(errorDiv);
            }
        }, 10000);
    }
    
    /**
     * Charge l'avatar par défaut avec Ready Player Me prioritaire
     */
    async loadDefaultAvatar() {
        console.log('📥 Chargement avatar par défaut...');
        
        if (!this.avatarRenderer) {
            console.warn('⚠️ Pas de renderer disponible');
            return;
        }
        
        // Priorité 1: Ready Player Me (direct, sans test)
        const readyPlayerMeId = this.config.get('readyPlayerMe.defaultAvatarId', '687f66fafe8107131699bf7b');
        if (readyPlayerMeId) {
            const readyPlayerMeUrl = `https://models.readyplayer.me/${readyPlayerMeId}.glb`;
            console.log('🎯 Tentative Ready Player Me directe:', readyPlayerMeUrl);
            
            try {
                const success = await this.avatarRenderer.loadAvatar(readyPlayerMeUrl);
                if (success) {
                    console.log('✅ Avatar Ready Player Me chargé:', readyPlayerMeUrl);
                    return;
                }
            } catch (error) {
                console.warn('⚠️ Ready Player Me échoué:', error.message);
            }
        }
        
        // Priorité 2: Modèles locaux
        const localModelPaths = [
            this.getModelPath(this.config.gender, this.config.age, this.config.style),
            '/models/avatars/female_mature_elegant.glb',
            '/models/avatars/female_adult_casual.glb'
        ];
        
        for (const modelPath of localModelPaths) {
            try {
                console.log(`🔄 Tentative modèle local: ${modelPath}`);
                const success = await this.avatarRenderer.loadAvatar(modelPath);
                
                if (success) {
                    console.log(`✅ Avatar local chargé: ${modelPath}`);
                    return;
                }
            } catch (error) {
                console.warn(`⚠️ Échec chargement ${modelPath}:`, error.message);
            }
        }
        
        console.warn('⚠️ Tous les modèles ont échoué');
    }
    
    /**
     * Génère le chemin du modèle
     */
    getModelPath(gender, age, style) {
        const ageGroup = this.getAgeGroup(age);
        return `/models/avatars/${gender}_${ageGroup}_${style}.glb`;
    }
    
    /**
     * Détermine le groupe d'âge
     */
    getAgeGroup(age) {
        if (age < 30) return 'young';
        if (age < 45) return 'adult';
        if (age < 60) return 'mature';
        return 'senior';
    }
    
    /**
     * Configure les gestionnaires d'événements
     */
    setupEventHandlers() {
        console.log('⚙️ Configuration des gestionnaires d\'événements...');
        
        // Éléments de l'interface
        const elements = {
            muteBtn: document.getElementById('mute-btn'),
            settingsBtn: document.getElementById('settings-btn'),
            cancelSettings: document.getElementById('cancel-settings'),
            settingsOverlay: document.getElementById('settings-overlay'),
            applySettings: document.getElementById('apply-settings'),
            genderSelect: document.getElementById('gender-select')
        };
        
        // Bouton muet
        this.safeAddEventListener(elements.muteBtn, 'click', () => this.toggleMute());
        
        // Bouton paramètres
        this.safeAddEventListener(elements.settingsBtn, 'click', () => this.showSettings());
        
        // Fermeture des paramètres
        this.safeAddEventListener(elements.cancelSettings, 'click', () => this.hideSettings());
        this.safeAddEventListener(elements.settingsOverlay, 'click', () => this.hideSettings());
        
        // Application des paramètres
        this.safeAddEventListener(elements.applySettings, 'click', () => this.applySettings());
        
        // Mise à jour de la voix selon le genre
        this.safeAddEventListener(elements.genderSelect, 'change', (e) => this.updateVoiceOptions(e.target.value));
        
        // Gestion du redimensionnement
        window.addEventListener('resize', () => {
            if (this.avatarRenderer && typeof this.avatarRenderer.onWindowResize === 'function') {
                this.avatarRenderer.onWindowResize();
            }
        });
        
        // Gestion des raccourcis clavier
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideSettings();
            }
        });
        
        console.log('✅ Gestionnaires d\'événements configurés');
    }
    
    /**
     * Ajoute un event listener de manière sécurisée
     */
    safeAddEventListener(element, event, handler) {
        if (element && typeof element.addEventListener === 'function') {
            element.addEventListener(event, handler);
        } else {
            console.warn(`⚠️ Élément non trouvé pour l'événement ${event}`);
        }
    }
    
    /**
     * Bascule le mode muet
     */
    toggleMute() {
        this.isMuted = !this.isMuted;
        const muteBtn = document.getElementById('mute-btn');
        if (muteBtn) {
            muteBtn.textContent = this.isMuted ? '🔇' : '🔊';
        }
        console.log(`🔊 Mode muet: ${this.isMuted}`);
    }
    
    /**
     * Affiche les paramètres
     */
	showSettings() {
	    console.log('⚙️ Ouverture panneau paramètres');
	    
	    const settings = document.getElementById('avatar-settings');
	    const overlay = document.getElementById('settings-overlay');
	    
	    if (settings) {
	        settings.classList.remove('hidden');
	        settings.style.display = 'block';
	    }
	    if (overlay) {
	        overlay.classList.remove('hidden');
	        overlay.style.display = 'flex';
	    }
	    
	    // Empêcher le scroll du body
	    document.body.style.overflow = 'hidden';
	}
	    
    /**
     * Masque les paramètres
     */
	hideSettings() {
	    console.log('❌ Fermeture panneau paramètres');
	    
	    const settings = document.getElementById('avatar-settings');
	    const overlay = document.getElementById('settings-overlay');
	    
	    if (settings) {
	        settings.classList.add('hidden');
	        // Délai pour l'animation
	        setTimeout(() => {
	            settings.style.display = 'none';
	        }, 300);
	    }
	    if (overlay) {
	        overlay.classList.add('hidden');
	        setTimeout(() => {
	            overlay.style.display = 'none';
	        }, 300);
	    }
	    
	    // Restaurer le scroll du body
	    document.body.style.overflow = '';
	}
	    
    /**
     * Applique les paramètres
     */
    async applySettings() {
        console.log('⚙️ Application des paramètres...');
        
        const genderSelect = document.getElementById('gender-select');
        const ageSelect = document.getElementById('age-select');
        const styleSelect = document.getElementById('style-select');
        const voiceSelect = document.getElementById('voice-select');
        
        if (genderSelect) this.config.gender = genderSelect.value;
        if (ageSelect) this.config.age = parseInt(ageSelect.value);
        if (styleSelect) this.config.style = styleSelect.value;
        if (voiceSelect) this.config.voice = voiceSelect.value;
        
        console.log('📋 Nouvelle configuration:', this.config);
        
        // Recharger l'avatar avec les nouveaux paramètres
        this.updateStatus('Mise à jour...');
        await this.loadDefaultAvatar();
        this.updateStatus('Prêt');
        
        // Fermer les paramètres
        this.hideSettings();
    }
    
    /**
     * Met à jour les options de voix selon le genre
     */
    updateVoiceOptions(gender) {
        const voiceSelect = document.getElementById('voice-select');
        if (!voiceSelect) return;
        
        voiceSelect.innerHTML = '';
        
        const voices = gender === 'female' 
            ? [
                { value: 'female_french_warm', text: 'Femme - Chaleureuse' },
                { value: 'female_french_professional', text: 'Femme - Professionnelle' }
              ]
            : [
                { value: 'male_french_warm', text: 'Homme - Chaleureux' },
                { value: 'male_french_professional', text: 'Homme - Professionnel' }
              ];
        
        voices.forEach(voice => {
            const option = document.createElement('option');
            option.value = voice.value;
            option.textContent = voice.text;
            voiceSelect.appendChild(option);
        });
        
        this.config.voice = voices[0].value;
    }
    
    /**
     * Fait parler l'avatar
     */
    speak(text, emotion = 'neutral') {
        console.log(`🗣️ Parole: "${text}" (${emotion})`);
        
        if (this.avatarController && typeof this.avatarController.speak === 'function') {
            this.avatarController.speak(text, emotion);
        } else if (this.avatarRenderer) {
            // Mode basique
            this.showMessage(text);
            this.setSpeakingIndicator(true);
            
            setTimeout(() => {
                this.setSpeakingIndicator(false);
                this.hideMessage();
            }, text.length * 100);
        }
    }
    
    /**
     * Définit l'émotion
     */
    setEmotion(emotion, intensity = 0.7) {
        console.log(`😊 Émotion: ${emotion} (${intensity})`);
        
        if (this.avatarController && typeof this.avatarController.setEmotion === 'function') {
            this.avatarController.setEmotion(emotion, intensity);
        } else if (this.avatarRenderer && typeof this.avatarRenderer.setEmotion === 'function') {
            this.avatarRenderer.setEmotion(emotion, intensity);
        }
    }
    
    /**
     * Affiche un message
     */
    showMessage(text) {
        const messageText = document.getElementById('message-text');
        const messageBubble = document.getElementById('message-bubble');
        
        if (messageText) messageText.textContent = text;
        if (messageBubble) messageBubble.classList.remove('hidden');
    }
    
    /**
     * Masque le message
     */
    hideMessage() {
        const messageBubble = document.getElementById('message-bubble');
        if (messageBubble) messageBubble.classList.add('hidden');
    }
    
    /**
     * Contrôle l'indicateur de parole
     */
    setSpeakingIndicator(speaking) {
        const indicator = document.getElementById('speaking-indicator');
        if (indicator) {
            indicator.style.display = speaking ? 'block' : 'none';
        }
    }
    
    /**
     * Met à jour le statut
     */
    updateStatus(status) {
        const statusElement = document.getElementById('avatar-status');
        if (statusElement) {
            statusElement.textContent = status;
        }
        console.log(`📊 Statut: ${status}`);
    }
    
    /**
     * Affiche le spinner de chargement
     */
    showLoadingSpinner() {
        const spinner = document.getElementById('loading-spinner');
        if (spinner) spinner.style.display = 'block';
    }
    
    /**
     * Masque le spinner de chargement
     */
    hideLoadingSpinner() {
        const spinner = document.getElementById('loading-spinner');
        if (spinner) spinner.style.display = 'none';
    }
    
    /**
     * Affiche l'avatar
     */
    showAvatar() {
        const container = document.getElementById('avatar-container');
        if (container) container.classList.add('visible');
        
        if (this.avatarRenderer && typeof this.avatarRenderer.setVisible === 'function') {
            this.avatarRenderer.setVisible(true);
        }
    }
    
    /**
     * Masque l'avatar
     */
    hideAvatar() {
        const container = document.getElementById('avatar-container');
        if (container) container.classList.remove('visible');
        
        if (this.avatarRenderer && typeof this.avatarRenderer.setVisible === 'function') {
            this.avatarRenderer.setVisible(false);
        }
    }
    
    /**
     * Nettoie les ressources
     */
	dispose() {
	    if (this.avatarController && typeof this.avatarController.dispose === 'function') {
	        this.avatarController.dispose();
	    }
	    
	    if (this.avatarRenderer && typeof this.avatarRenderer.dispose === 'function') {
	        this.avatarRenderer.dispose();
	    }
	    
	    if (this.websocketManager && typeof this.websocketManager.disconnect === 'function') {
	        this.websocketManager.disconnect();
	    }
	    
	    // Nettoyer le container
	    const container = document.getElementById('avatar-viewport');
	    if (container) {
	        const canvases = container.querySelectorAll('canvas');
	        canvases.forEach(canvas => canvas.remove());
	    }
	    
	    // Libérer l'instance singleton
	    if (window.angelAppInstance === this) {
	        window.angelAppInstance = null;
	    }
	    
	    console.log('🧹 AngelAvatarApp nettoyé');
	}
}

// Export pour utilisation globale
window.AngelAvatarApp = AngelAvatarApp;

// Initialisation automatique avec gestion d'erreurs améliorée
document.addEventListener('DOMContentLoaded', () => {
    console.log('📄 DOM prêt, initialisation AngelAvatarApp...');
    
    // Protection contre les initialisations multiples
    if (window.angelApp && window.angelApp.isInitialized) {
        console.log('✅ AngelAvatarApp déjà initialisé');
        return;
    }
    
    // Nettoyer toute instance précédente
    if (window.angelApp && typeof window.angelApp.dispose === 'function') {
        window.angelApp.dispose();
    }
    
    // Attendre que tous les scripts soient chargés
    setTimeout(() => {
        try {
            window.angelApp = new AngelAvatarApp();
            window.angelApp.init().catch(error => {
                console.error('❌ Erreur critique lors de l\'initialisation:', error);
            });
        } catch (error) {
            console.error('❌ Erreur création AngelAvatarApp:', error);
        }
    }, 100);
});

window.addEventListener('error', (event) => {
    if (event.error && event.error.message && event.error.message.includes('canvas')) {
        console.warn('⚠️ Erreur canvas détectée, tentative de récupération');
        
        // Essayer de redémarrer l'avatar
        if (window.angelApp && !window.angelApp.isInitialized) {
            setTimeout(() => {
                window.angelApp.init().catch(console.error);
            }, 1000);
        }
    }
});

window.addEventListener('beforeunload', () => {
    if (window.angelApp && typeof window.angelApp.dispose === 'function') {
        window.angelApp.dispose();
    }
});

/**
 * Contrôleur principal de l'avatar 3D
 * Coordonne le rendu, les animations, la communication WebSocket et Ready Player Me
 */

class AvatarController {
    constructor() {
        this.config = null;
        this.renderer = null;
        this.websocket = null;
        this.readyPlayerMe = null;
        
        this.isInitialized = false;
        this.currentEmotion = 'neutral';
        this.currentAvatarId = null;
        
        // Éléments DOM
        this.container = null;
        this.statusElement = null;
        this.speechBubble = null;
        this.controls = null;
        
        this.setupEventListeners();
    }
    
    /**
     * Initialise le contrôleur d'avatar
     */
    async initialize() {
        try {
            console.log('Initialisation du contrôleur d\'avatar...');
            
            // Initialiser la configuration
            if (window.AvatarConfig) {
                this.config = new AvatarConfig();
                await this.waitForConfigLoad();
            } else {
                console.warn('AvatarConfig non disponible, utilisation config par défaut');
                this.config = {
                    get: (key, defaultValue) => defaultValue
                };
            }
            
            // Initialiser les éléments DOM
            this.initializeDOM();
            
            // Initialiser les composants
            this.initializeRenderer();
            this.initializeWebSocket();
            this.initializeReadyPlayerMe();
            
            // Charger l'avatar par défaut
            await this.loadDefaultAvatar();
            
            // Initialiser les contrôles UI
            this.initializeControls();
            
            this.isInitialized = true;
            this.updateStatus('Avatar prêt !', false);
            
            console.log('Contrôleur d\'avatar initialisé avec succès');
            
        } catch (error) {
            console.error('Erreur lors de l\'initialisation:', error);
            this.updateStatus('Erreur lors de l\'initialisation', false);
        }
    }
    
    /**
     * Attend que la configuration soit chargée
     */
    waitForConfigLoad() {
        return new Promise((resolve) => {
            if (this.config.get('enabled') !== null) {
                resolve();
            } else if (this.config.addEventListener) {
                this.config.addEventListener('configLoaded', () => resolve());
            } else {
                resolve(); // Fallback
            }
        });
    }
    
    /**
     * Initialise les éléments DOM
     */
    initializeDOM() {
        this.container = document.getElementById('avatar-viewport');
        this.statusElement = document.getElementById('avatar-status');
        this.speechBubble = document.getElementById('message-bubble');
        this.controls = document.getElementById('avatar-controls');
        
        if (!this.container) {
            throw new Error('Container avatar-viewport non trouvé');
        }
    }
    
    /**
     * Initialise le moteur de rendu 3D
     */
    initializeRenderer() {
        if (window.AvatarRenderer) {
            this.renderer = new AvatarRenderer(this.container, this.config);
            console.log('Moteur de rendu initialisé');
        } else {
            console.warn('AvatarRenderer non disponible');
        }
    }
    
    /**
     * Initialise la connexion WebSocket
     */
    initializeWebSocket() {
        if (!window.AvatarWebSocket) {
            console.warn('AvatarWebSocket non disponible');
            return;
        }
        
        try {
            this.websocket = new AvatarWebSocket(this.config);
            
            // Gestionnaires d'événements WebSocket
            this.websocket.addEventListener('connected', () => {
                console.log('WebSocket connecté');
            });
            
            this.websocket.addEventListener('avatar_speech', (data) => {
                this.handleSpeechMessage(data);
            });
            
            this.websocket.addEventListener('avatar_emotion', (data) => {
                this.handleEmotionMessage(data);
            });
            
            this.websocket.addEventListener('avatar_gesture', (data) => {
                this.handleGestureMessage(data);
            });
            
            this.websocket.addEventListener('avatar_visibility', (data) => {
                this.handleVisibilityMessage(data);
            });
            
            this.websocket.addEventListener('avatar_appearance', (data) => {
                this.handleAppearanceMessage(data);
            });
            
            this.websocket.connect();
            this.websocket.startKeepAlive();
        } catch (error) {
            console.warn('Erreur WebSocket:', error.message);
        }
    }
    
    /**
     * Initialise l'intégration Ready Player Me
     */
    initializeReadyPlayerMe() {
        if (window.ReadyPlayerMeIntegration) {
            this.readyPlayerMe = new ReadyPlayerMeIntegration(this.config);
            console.log('Ready Player Me initialisé, disponible:', this.readyPlayerMe.isAvailable());
        } else {
            console.warn('ReadyPlayerMeIntegration non disponible');
        }
    }
    
    /**
     * Charge l'avatar par défaut
     */
    async loadDefaultAvatar() {
        this.updateStatus('Chargement de l\'avatar...', true);
        
        try {
            const gender = this.config.get('appearance.gender', 'female');
            const age = this.config.get('appearance.age', 30);
            const style = this.config.get('appearance.style', 'casual');
            
            let modelUrl;
            
            if (this.readyPlayerMe && this.readyPlayerMe.isAvailable()) {
                // Utiliser Ready Player Me
                const defaultAvatarId = this.config.get('readyPlayerMe.defaultAvatarId');
                if (defaultAvatarId) {
                    modelUrl = await this.readyPlayerMe.getAvatarModelUrl(defaultAvatarId);
                    this.currentAvatarId = defaultAvatarId;
                }
            }
            
            if (!modelUrl) {
                // Utiliser le modèle de fallback
                modelUrl = this.getAvatarModelPath(gender, age, style);
            }
            
            if (this.renderer) {
                await this.renderer.loadAvatar(modelUrl);
            }
            
            this.updateStatus('Avatar chargé !', false);
            
        } catch (error) {
            console.error('Erreur lors du chargement de l\'avatar:', error);
            this.updateStatus('Erreur de chargement', false);
        }
    }
    
    /**
     * Génère le chemin du modèle d'avatar
     */
    getAvatarModelPath(gender, age, style) {
        const ageGroup = age < 30 ? 'young' : age < 45 ? 'adult' : age < 60 ? 'mature' : 'senior';
        return `/models/avatars/${gender}_${ageGroup}_${style}.glb`;
    }
    
    /**
     * Initialise les contrôles UI
     */
    initializeControls() {
        // Bouton de basculement des contrôles
        const toggleButton = document.getElementById('toggle-controls');
        if (toggleButton) {
            toggleButton.addEventListener('click', () => {
                this.toggleControls();
            });
        }
        
        // Contrôles d'apparence
        this.setupAppearanceControls();
        
        // Contrôles d'émotion
        this.setupEmotionControls();
        
        // Contrôles de gestes
        this.setupGestureControls();
        
        // Contrôle de test vocal
        this.setupSpeechControls();
    }
    
    /**
     * Configure les contrôles d'apparence
     */
    setupAppearanceControls() {
        const genderSelect = document.getElementById('gender-select');
        const ageSelect = document.getElementById('age-select');
        const styleSelect = document.getElementById('style-select');
        
        if (genderSelect) {
            genderSelect.value = this.config.get('appearance.gender', 'female');
            genderSelect.addEventListener('change', () => {
                this.changeAppearance();
            });
        }
        
        if (ageSelect) {
            ageSelect.value = this.config.get('appearance.age', 30);
            ageSelect.addEventListener('change', () => {
                this.changeAppearance();
            });
        }
        
        if (styleSelect) {
            styleSelect.value = this.config.get('appearance.style', 'casual');
            styleSelect.addEventListener('change', () => {
                this.changeAppearance();
            });
        }
    }
    
    /**
     * Configure les contrôles d'émotion
     */
    setupEmotionControls() {
        const emotionSelect = document.getElementById('emotion-select');
        
        if (emotionSelect) {
            emotionSelect.value = this.currentEmotion;
            emotionSelect.addEventListener('change', () => {
                this.setEmotion(emotionSelect.value);
            });
        }
    }
    
    /**
     * Configure les contrôles de gestes
     */
    setupGestureControls() {
        const gestureButtons = document.querySelectorAll('.gesture-btn');
        
        gestureButtons.forEach(button => {
            button.addEventListener('click', () => {
                const gesture = button.dataset.gesture;
                this.playGesture(gesture);
            });
        });
    }
    
    /**
     * Configure les contrôles de test vocal
     */
    setupSpeechControls() {
        const speechTextarea = document.getElementById('speech-text');
        const speakButton = document.getElementById('speak-btn');
        
        if (speakButton && speechTextarea) {
            speakButton.addEventListener('click', () => {
                const text = speechTextarea.value.trim();
                if (text) {
                    this.speak(text, this.currentEmotion);
                }
            });
        }
    }
    
    /**
     * Configure les événements globaux
     */
    setupEventListeners() {
        // Gestion des raccourcis clavier
        document.addEventListener('keydown', (event) => {
            if (event.ctrlKey || event.metaKey) {
                switch (event.key) {
                    case 'h':
                        event.preventDefault();
                        this.toggleControls();
                        break;
                    case 's':
                        event.preventDefault();
                        this.showSpeechInput();
                        break;
                }
            }
        });
    }
    
    /**
     * Gère les messages de parole reçus du backend
     */
    handleSpeechMessage(data) {
        const { text, emotion, duration } = data;
        console.log('Message de parole reçu:', text);
        
        // Afficher la bulle de dialogue
        this.showSpeechBubble(text);
        
        // Changer l'émotion si spécifiée
        if (emotion && emotion !== this.currentEmotion) {
            this.setEmotion(emotion);
        }
        
        // Jouer l'animation de parole
        if (this.renderer && this.renderer.playAnimation) {
            this.renderer.playAnimation('speaking', { loop: true, fadeIn: 0.3 });
        }
        
        // Masquer la bulle après la durée spécifiée
        setTimeout(() => {
            this.hideSpeechBubble();
            if (this.renderer) {
                this.renderer.stopAnimation('speaking', 0.3);
                this.renderer.playAnimation('idle', { loop: true, fadeIn: 0.3 });
            }
        }, duration || 3000);
    }
    
    /**
     * Gère les messages d'émotion reçus du backend
     */
    handleEmotionMessage(data) {
        const { emotion, intensity } = data;
        console.log('Changement d\'émotion reçu:', emotion, intensity);
        
        this.setEmotion(emotion, intensity);
        
        // Mettre à jour l'interface
        const emotionSelect = document.getElementById('emotion-select');
        if (emotionSelect) {
            emotionSelect.value = emotion;
        }
    }
    
    /**
     * Gère les messages de geste reçus du backend
     */
    handleGestureMessage(data) {
        const { gestureType } = data;
        console.log('Geste reçu:', gestureType);
        
        this.playGesture(gestureType);
    }
    
    /**
     * Gère les messages de visibilité reçus du backend
     */
    handleVisibilityMessage(data) {
        const { visible } = data;
        console.log('Changement de visibilité:', visible);
        
        if (visible) {
            this.showAvatar();
        } else {
            this.hideAvatar();
        }
    }
    
    /**
     * Gère les messages de changement d'apparence reçus du backend
     */
    async handleAppearanceMessage(data) {
        const { modelUrl, gender, age, style } = data;
        console.log('Changement d\'apparence:', data);
        
        this.updateStatus('Chargement du nouvel avatar...', true);
        
        try {
            if (this.renderer) {
                await this.renderer.loadAvatar(modelUrl);
            }
            
            // Mettre à jour les contrôles UI
            const genderSelect = document.getElementById('gender-select');
            const ageSelect = document.getElementById('age-select');
            const styleSelect = document.getElementById('style-select');
            
            if (genderSelect) genderSelect.value = gender;
            if (ageSelect) ageSelect.value = age;
            if (styleSelect) styleSelect.value = style;
            
            this.updateStatus('Avatar mis à jour !', false);
            
        } catch (error) {
            console.error('Erreur lors du changement d\'apparence:', error);
            this.updateStatus('Erreur de chargement', false);
        }
    }
    
    /**
     * Change l'apparence de l'avatar
     */
    async changeAppearance() {
        const genderSelect = document.getElementById('gender-select');
        const ageSelect = document.getElementById('age-select');
        const styleSelect = document.getElementById('style-select');
        
        if (!genderSelect || !ageSelect || !styleSelect) return;
        
        const gender = genderSelect.value;
        const age = parseInt(ageSelect.value);
        const style = styleSelect.value;
        
        console.log('Changement d\'apparence:', { gender, age, style });
        
        this.updateStatus('Création de l\'avatar...', true);
        
        try {
            let modelUrl;
            
            if (this.readyPlayerMe && this.readyPlayerMe.isAvailable()) {
                // Créer un nouvel avatar avec Ready Player Me
                const result = await this.readyPlayerMe.createAvatar({
                    gender, age, style
                });
                modelUrl = await this.readyPlayerMe.getAvatarModelUrl(result.id);
                this.currentAvatarId = result.id;
            } else {
                // Utiliser le modèle de fallback
                modelUrl = this.getAvatarModelPath(gender, age, style);
            }
            
            if (this.renderer) {
                await this.renderer.loadAvatar(modelUrl);
            }
            
            // Notifier le backend du changement
            if (this.websocket && this.websocket.isConnectedAndReady()) {
                this.websocket.sendMessage('appearance_changed', {
                    gender, age, style, modelUrl
                });
            }
            
            this.updateStatus('Avatar mis à jour !', false);
            
        } catch (error) {
            console.error('Erreur lors du changement d\'apparence:', error);
            this.updateStatus('Erreur de création', false);
        }
    }
    
    /**
     * Définit l'émotion de l'avatar
     */
    setEmotion(emotion, intensity = 0.7) {
        if (!this.isInitialized) return;
        
        this.currentEmotion = emotion;
        
        if (this.renderer && this.renderer.setEmotion) {
            this.renderer.setEmotion(emotion, intensity);
        }
        
        console.log(`Émotion changée: ${emotion}`);
    }
    
    /**
     * Fait jouer un geste à l'avatar
     */
    playGesture(gestureType) {
        if (!this.isInitialized) return;
        
        if (this.renderer && this.renderer.playGesture) {
            this.renderer.playGesture(gestureType);
        }
        
        console.log(`Geste joué: ${gestureType}`);
    }
    
    /**
     * Fait parler l'avatar
     */
    speak(text, emotion = null) {
        if (!this.isInitialized) return;
        
        const finalEmotion = emotion || this.currentEmotion;
        
        // Envoyer au backend via WebSocket
        if (this.websocket && this.websocket.isConnectedAndReady()) {
            this.websocket.sendMessage('speak_request', {
                text, emotion: finalEmotion
            });
        } else {
            // Mode hors ligne - simulation locale
            this.handleSpeechMessage({
                text, emotion: finalEmotion, duration: text.length * 100
            });
        }
    }
    
    /**
     * Affiche la bulle de dialogue
     */
    showSpeechBubble(text) {
        if (!this.speechBubble) return;
        
        const textDisplay = document.getElementById('message-text');
        if (textDisplay) {
            textDisplay.textContent = text;
        }
        
        this.speechBubble.classList.remove('hidden');
    }
    
    /**
     * Masque la bulle de dialogue
     */
    hideSpeechBubble() {
        if (this.speechBubble) {
            this.speechBubble.classList.add('hidden');
        }
    }
    
    /**
     * Affiche l'avatar
     */
    showAvatar() {
        if (this.container) {
            this.container.style.opacity = '1';
            this.container.style.pointerEvents = 'auto';
        }
    }
    
    /**
     * Masque l'avatar
     */
    hideAvatar() {
        if (this.container) {
            this.container.style.opacity = '0.3';
            this.container.style.pointerEvents = 'none';
        }
    }
    
    /**
     * Bascule l'affichage des contrôles
     */
    toggleControls() {
        if (this.controls) {
            this.controls.classList.toggle('hidden');
        }
    }
    
    /**
     * Ouvre l'interface de création d'avatar Ready Player Me
     */
    async openAvatarCreator() {
        if (!this.readyPlayerMe || !this.readyPlayerMe.isAvailable()) {
            alert('Ready Player Me n\'est pas disponible');
            return;
        }
        
        try {
            const result = await this.readyPlayerMe.openAvatarCreator();
            console.log('Avatar créé:', result);
            
            // Charger le nouvel avatar
            if (this.renderer) {
                await this.renderer.loadAvatar(result.modelUrl);
                this.currentAvatarId = result.avatarId;
            }
            
            this.updateStatus('Nouvel avatar chargé !', false);
            
        } catch (error) {
            console.error('Erreur lors de la création d\'avatar:', error);
        }
    }
    
    /**
     * Affiche l'input de test vocal
     */
    showSpeechInput() {
        const speechTextarea = document.getElementById('speech-text');
        if (speechTextarea) {
            speechTextarea.focus();
            
            // Afficher les contrôles si cachés
            if (this.controls && this.controls.classList.contains('hidden')) {
                this.toggleControls();
            }
        }
    }
    
    /**
     * Met à jour le statut affiché
     */
    updateStatus(message, showSpinner = false) {
        if (this.statusElement) {
            this.statusElement.textContent = message;
        }
        
        const spinner = document.getElementById('loading-spinner');
        if (spinner) {
            spinner.style.display = showSpinner ? 'block' : 'none';
        }
    }
    
    /**
     * Nettoie les ressources
     */
    dispose() {
        if (this.renderer && this.renderer.dispose) {
            this.renderer.dispose();
        }
        
        if (this.websocket) {
            if (this.websocket.stopKeepAlive) this.websocket.stopKeepAlive();
            if (this.websocket.disconnect) this.websocket.disconnect();
        }
        
        if (this.readyPlayerMe && this.readyPlayerMe.clearCache) {
            this.readyPlayerMe.clearCache();
        }
        
        console.log('Contrôleur d\'avatar nettoyé');
    }
    
    /**
     * Obtient les informations de debug
     */
    getDebugInfo() {
        return {
            initialized: this.isInitialized,
            currentEmotion: this.currentEmotion,
            currentAvatarId: this.currentAvatarId,
            websocketConnected: this.websocket?.isConnectedAndReady?.(),
            readyPlayerMeAvailable: this.readyPlayerMe?.isAvailable?.(),
            renderer: this.renderer?.getDebugInfo?.()
        };
    }
}

// Export global
window.AvatarController = AvatarController;

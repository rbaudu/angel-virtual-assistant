/**
 * CORRECTION: Application principale Angel Avatar
 * PROBLÈME IDENTIFIÉ: Initialisation multiple et problèmes de container
 */
class AngelAvatarApp {
    constructor() {
        // CORRECTION: Protection contre instances multiples
        if (window.angelAppInstance) {
            console.warn('⚠️ Instance AngelAvatarApp existante, réutilisation');
            return window.angelAppInstance;
        }
        
        this.avatarRenderer = null;
        this.isInitialized = false;
        this.isMuted = false;
        this.initAttempts = 0;
        this.maxInitAttempts = 3;
        
        // Marquer cette instance comme active
        window.angelAppInstance = this;
        
        // Configuration compatible avec AvatarRenderer
        this.config = {
            gender: 'female',
            age: 30,
            style: 'casual',
            voice: 'female_french_warm',
            
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
            
            readyPlayerMe: {
                enabled: true,
                defaultAvatarId: '687f66fafe8107131699bf7b'
            }
        };
        
        console.log('🎭 AngelAvatarApp créé');
    }
    
    async init() {
        if (this.isInitialized) {
            console.log('✅ Déjà initialisé');
            return;
        }
        
        this.initAttempts++;
        console.log(`🚀 Initialisation Angel Avatar App (tentative ${this.initAttempts}/${this.maxInitAttempts})...`);
        
        try {
            // CORRECTION: Vérifications préliminaires
            if (!this.checkPrerequisites()) {
                throw new Error('Prérequis manquants');
            }
            
            // CORRECTION: Nettoyer d'abord
            this.cleanup();
            
            // Attendre les dépendances
            await this.waitForDependencies();
            
            // Initialiser le renderer
            await this.initializeRenderer();
            
            // Configurer les événements
            this.setupEventHandlers();
            
            // Charger l'avatar
            await this.loadDefaultAvatar();
            
            // Finaliser
            this.finalizeInit();
            
        } catch (error) {
            console.error(`❌ Erreur initialisation (tentative ${this.initAttempts}):`, error);
            await this.handleInitError(error);
        }
    }
    
    /**
     * CORRECTION: Vérifie les prérequis
     */
    checkPrerequisites() {
        const container = document.getElementById('avatar-viewport');
        if (!container) {
            console.error('❌ Container avatar-viewport manquant');
            return false;
        }
        
        console.log('✅ Container trouvé:', {
            width: container.clientWidth,
            height: container.clientHeight,
            children: container.children.length
        });
        
        return true;
    }
    
    /**
     * CORRECTION: Nettoie les instances précédentes
     */
    cleanup() {
        console.log('🧹 Nettoyage...');
        
        // Disposer ancien renderer
        if (this.avatarRenderer && typeof this.avatarRenderer.dispose === 'function') {
            this.avatarRenderer.dispose();
            this.avatarRenderer = null;
        }
        
        // Nettoyer le container
        const container = document.getElementById('avatar-viewport');
        if (container) {
            const canvases = container.querySelectorAll('canvas');
            canvases.forEach(canvas => {
                console.log('🗑️ Suppression canvas existant');
                canvas.remove();
            });
        }
    }
    
    /**
     * CORRECTION: Attendre les dépendances avec timeout plus long
     */
    async waitForDependencies() {
        return new Promise((resolve, reject) => {
            let attempts = 0;
            const maxAttempts = 150; // 15 secondes
            
            const checkDependencies = () => {
                attempts++;
                
                const hasThree = typeof THREE !== 'undefined';
                const hasGLTFLoader = hasThree && (
                    (THREE.GLTFLoader) || 
                    window.GLTFLoaderReady
                );
                
                if (attempts % 10 === 0) { // Log toutes les secondes
                    console.log(`⏳ Dépendances (${attempts}/${maxAttempts}): THREE=${hasThree}, GLTFLoader=${hasGLTFLoader}`);
                }
                
                if (hasThree && hasGLTFLoader) {
                    console.log('✅ Toutes les dépendances chargées');
                    resolve();
                } else if (attempts >= maxAttempts) {
                    reject(new Error(`Timeout: dépendances non chargées après ${maxAttempts/10}s`));
                } else {
                    setTimeout(checkDependencies, 100);
                }
            };
            
            checkDependencies();
        });
    }
    
    /**
     * CORRECTION: Initialise le renderer avec protection
     */
    async initializeRenderer() {
        const container = document.getElementById('avatar-viewport');
        if (!container) {
            throw new Error('Container avatar-viewport non trouvé');
        }
        
        console.log('📱 Initialisation AvatarRenderer...');
        
        try {
            // CORRECTION: Vérifier que AvatarRenderer est disponible
            if (!window.AvatarRenderer) {
                throw new Error('AvatarRenderer non disponible');
            }
            
            this.avatarRenderer = new AvatarRenderer(container, this.config);
            
            // CORRECTION: Vérifier que l'initialisation a réussi
            if (!this.avatarRenderer.renderer) {
                throw new Error('Renderer non créé');
            }
            
            console.log('✅ AvatarRenderer initialisé avec succès');
            
        } catch (error) {
            console.error('❌ Erreur AvatarRenderer:', error);
            throw error;
        }
    }
    
    /**
     * CORRECTION: Charge l'avatar avec stratégie de fallback
     */
	async loadDefaultAvatar() {
	    console.log('📥 Chargement avatar par défaut...');
	    
	    if (!this.avatarRenderer) {
	        console.warn('⚠️ Pas de renderer, création avatar de base');
	        return;
	    }
	    
	    // SUPPRIMER ces lignes de test :
	    // console.log('🎲 Test avec cube...');
	    // this.avatarRenderer.addTestCube();
	    // await new Promise(resolve => setTimeout(resolve, 1000));
	    
	    // Essayer Ready Player Me directement
	    const readyPlayerMeId = this.config.get('readyPlayerMe.defaultAvatarId', '687f66fafe8107131699bf7b');
	    if (readyPlayerMeId) {
	        const readyPlayerMeUrl = `https://models.readyplayer.me/${readyPlayerMeId}.glb`;
	        console.log('🎯 Chargement Ready Player Me:', readyPlayerMeUrl);
	        
	        try {
	            await this.avatarRenderer.loadAvatar(readyPlayerMeUrl);
	            console.log('✅ Avatar Ready Player Me chargé');
	            return;
	        } catch (error) {
	            console.warn('⚠️ Ready Player Me échoué:', error.message);
	        }
	    }
	    
	    // Fallback vers modèles locaux si nécessaire
	    const localModelPaths = [
	        this.getModelPath(this.config.gender, this.config.age, this.config.style),
	        '/models/avatars/female_mature_elegant.glb',
	        '/models/avatars/female_adult_casual.glb'
	    ];
	    
	    for (const modelPath of localModelPaths) {
	        try {
	            console.log(`🔄 Tentative modèle local: ${modelPath}`);
	            await this.avatarRenderer.loadAvatar(modelPath);
	            console.log(`✅ Avatar local chargé: ${modelPath}`);
	            return;
	        } catch (error) {
	            console.warn(`⚠️ Échec chargement ${modelPath}:`, error.message);
	        }
	    }
	    
	    console.warn('⚠️ Tous les modèles ont échoué');
	}
	    
    /**
     * CORRECTION: Finalise l'initialisation
     */
    finalizeInit() {
        this.isInitialized = true;
        this.hideLoadingSpinner();
        this.updateStatus('Prêt');
        this.showAvatar();
        
        console.log('✅ Angel Avatar App initialisé avec succès');
        
        // CORRECTION: Debug info final
        if (this.avatarRenderer && typeof this.avatarRenderer.getDebugInfo === 'function') {
            this.avatarRenderer.getDebugInfo();
        }
    }
    
    /**
     * CORRECTION: Gère les erreurs d'initialisation avec retry
     */
    async handleInitError(error) {
        console.error('❌ Erreur critique:', error);
        
        if (this.initAttempts < this.maxInitAttempts) {
            console.log(`🔄 Nouvelle tentative dans 2 secondes...`);
            setTimeout(() => {
                this.init();
            }, 2000);
        } else {
            console.error('❌ Échec définitif après', this.maxInitAttempts, 'tentatives');
            this.showError(`Erreur Avatar: ${error.message}`);
            this.updateStatus('Erreur');
            this.hideLoadingSpinner();
        }
    }
    
    /**
     * CORRECTION: Configuration événements simplifiée
     */
    setupEventHandlers() {
        console.log('⚙️ Configuration événements...');
        
        // Éléments UI
        const elements = {
            muteBtn: document.getElementById('mute-btn'),
            settingsBtn: document.getElementById('settings-btn'),
            cancelSettings: document.getElementById('cancel-settings'),
            settingsOverlay: document.getElementById('settings-overlay'),
            applySettings: document.getElementById('apply-settings')
        };
        
        // Event listeners avec protection
        this.safeAddEventListener(elements.muteBtn, 'click', () => this.toggleMute());
        this.safeAddEventListener(elements.settingsBtn, 'click', () => this.showSettings());
        this.safeAddEventListener(elements.cancelSettings, 'click', () => this.hideSettings());
        this.safeAddEventListener(elements.settingsOverlay, 'click', () => this.hideSettings());
        this.safeAddEventListener(elements.applySettings, 'click', () => this.applySettings());
        
        // Redimensionnement
        window.addEventListener('resize', () => {
            if (this.avatarRenderer && typeof this.avatarRenderer.onWindowResize === 'function') {
                this.avatarRenderer.onWindowResize();
            }
        });
        
        // Échap pour fermer settings
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideSettings();
            }
        });
        
        console.log('✅ Événements configurés');
    }
    
    safeAddEventListener(element, event, handler) {
        if (element && typeof element.addEventListener === 'function') {
            element.addEventListener(event, handler);
        } else {
            console.warn(`⚠️ Élément manquant pour ${event}`);
        }
    }
    
    /**
     * CORRECTION: Affichage des paramètres avec force
     */
    showSettings() {
        console.log('⚙️ Ouverture paramètres...');
        
        const settings = document.getElementById('avatar-settings');
        const overlay = document.getElementById('settings-overlay');
        
        if (overlay) {
            overlay.classList.remove('hidden');
            overlay.style.display = 'flex';
            overlay.style.opacity = '1';
            overlay.style.visibility = 'visible';
        }
        
        if (settings) {
            settings.classList.remove('hidden');
            settings.style.opacity = '1';
            settings.style.visibility = 'visible';
            settings.style.transform = 'translate(-50%, -50%) scale(1)';
        }
        
        document.body.style.overflow = 'hidden';
        
        console.log('✅ Paramètres ouverts');
    }
    
    hideSettings() {
        console.log('❌ Fermeture paramètres...');
        
        const settings = document.getElementById('avatar-settings');
        const overlay = document.getElementById('settings-overlay');
        
        if (settings) {
            settings.classList.add('hidden');
        }
        if (overlay) {
            overlay.classList.add('hidden');
        }
        
        document.body.style.overflow = '';
    }
    
    async applySettings() {
        console.log('⚙️ Application paramètres...');
        this.hideSettings();
    }
    
    /**
     * Méthodes utilitaires
     */
    toggleMute() {
        this.isMuted = !this.isMuted;
        const muteBtn = document.getElementById('mute-btn');
        if (muteBtn) {
            muteBtn.textContent = this.isMuted ? '🔇' : '🔊';
        }
    }
    
    speak(text, emotion = 'neutral') {
        console.log(`🗣️ Parole: "${text}" (${emotion})`);
        this.showMessage(text);
        this.setSpeakingIndicator(true);
        
        setTimeout(() => {
            this.setSpeakingIndicator(false);
            this.hideMessage();
        }, text.length * 100);
    }
    
    setEmotion(emotion, intensity = 0.7) {
        console.log(`😊 Émotion: ${emotion} (${intensity})`);
        if (this.avatarRenderer && typeof this.avatarRenderer.setEmotion === 'function') {
            this.avatarRenderer.setEmotion(emotion, intensity);
        }
    }
    
    showMessage(text) {
        const messageText = document.getElementById('message-text');
        const messageBubble = document.getElementById('message-bubble');
        
        if (messageText) messageText.textContent = text;
        if (messageBubble) messageBubble.classList.remove('hidden');
    }
    
    hideMessage() {
        const messageBubble = document.getElementById('message-bubble');
        if (messageBubble) messageBubble.classList.add('hidden');
    }
    
    setSpeakingIndicator(speaking) {
        const indicator = document.getElementById('speaking-indicator');
        if (indicator) {
            indicator.style.display = speaking ? 'block' : 'none';
        }
    }
    
    updateStatus(status) {
        const statusElement = document.getElementById('avatar-status');
        if (statusElement) {
            statusElement.textContent = status;
        }
    }
    
    showLoadingSpinner() {
        const spinner = document.getElementById('loading-spinner');
        if (spinner) spinner.style.display = 'block';
    }
    
    hideLoadingSpinner() {
        const spinner = document.getElementById('loading-spinner');
        if (spinner) spinner.style.display = 'none';
    }
    
    showAvatar() {
        const container = document.getElementById('avatar-container');
        if (container) container.classList.add('visible');
    }
    
    hideAvatar() {
        const container = document.getElementById('avatar-container');
        if (container) container.classList.remove('visible');
    }
    
    showError(message) {
        console.error('🚨 Erreur:', message);
        
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
        errorDiv.onclick = () => errorDiv.remove();
        
        document.body.appendChild(errorDiv);
        
        setTimeout(() => {
            if (errorDiv.parentNode) {
                errorDiv.remove();
            }
        }, 10000);
    }
    
    dispose() {
        console.log('🧹 Nettoyage AngelAvatarApp...');
        
        if (this.avatarRenderer && typeof this.avatarRenderer.dispose === 'function') {
            this.avatarRenderer.dispose();
        }
        
        const container = document.getElementById('avatar-viewport');
        if (container) {
            const canvases = container.querySelectorAll('canvas');
            canvases.forEach(canvas => canvas.remove());
        }
        
        if (window.angelAppInstance === this) {
            window.angelAppInstance = null;
        }
        
        this.isInitialized = false;
    }
}

// Export global
window.AngelAvatarApp = AngelAvatarApp;

// CORRECTION: Initialisation sécurisée
document.addEventListener('DOMContentLoaded', () => {
    console.log('📄 DOM prêt, préparation initialisation...');
    
    // Forcer fermeture panneaux
    const settings = document.getElementById('avatar-settings');
    const overlay = document.getElementById('settings-overlay');
    
    if (settings) {
        settings.classList.add('hidden');
        settings.style.display = 'none';
    }
    if (overlay) {
        overlay.classList.add('hidden');
        overlay.style.display = 'none';
    }
    
    // Protection contre initialisations multiples
    if (window.angelApp && window.angelApp.isInitialized) {
        console.log('✅ Déjà initialisé, arrêt');
        return;
    }
    
    // Nettoyer instance précédente
    if (window.angelApp && typeof window.angelApp.dispose === 'function') {
        window.angelApp.dispose();
    }
    
    // CORRECTION: Attendre un peu plus pour les scripts
    setTimeout(() => {
        try {
            console.log('🎭 Création AngelAvatarApp...');
            window.angelApp = new AngelAvatarApp();
            
            // Initialiser avec gestion d'erreur
            window.angelApp.init().catch(error => {
                console.error('❌ Erreur fatale:', error);
            });
            
        } catch (error) {
            console.error('❌ Erreur création AngelAvatarApp:', error);
        }
    }, 200); // Un peu plus de délai
});

// Nettoyage au déchargement
window.addEventListener('beforeunload', () => {
    if (window.angelApp && typeof window.angelApp.dispose === 'function') {
        window.angelApp.dispose();
    }
});
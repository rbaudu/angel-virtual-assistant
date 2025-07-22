/**
 * Système principal d'avatar - Point d'entrée unique
 * Résout le problème "AvatarSystem non disponible"
 */
class AvatarSystem {
    constructor() {
        this.components = new Map();
        this.config = null;
        this.isInitialized = false;
        this.renderer = null;
        this.animationManager = null;
    }
    
    static getInstance() {
        if (!AvatarSystem.instance) {
            AvatarSystem.instance = new AvatarSystem();
        }
        return AvatarSystem.instance;
    }
    
    /**
     * Initialise le système d'avatar
     */
    static async init(containerId) {
        const instance = AvatarSystem.getInstance();
        return await instance.initialize(containerId);
    }
    
    async initialize(containerId) {
        console.log('🚀 Initialisation AvatarSystem...');
        
        try {
            // Attendre que les dépendances soient chargées
            await this.waitForDependencies();
            
            // Charger la configuration
            await this.loadConfig();
            
            // Initialiser le container
            const container = document.getElementById(containerId);
            if (!container) {
                throw new Error(`Container ${containerId} non trouvé`);
            }
            
            // Initialiser le renderer
            if (window.AvatarRenderer) {
                this.renderer = new AvatarRenderer(container, this.config);
                this.components.set('renderer', this.renderer);
                console.log('✅ AvatarRenderer initialisé');
            }
            
            // Initialiser le gestionnaire d'animations
            if (window.AvatarAnimationManager && this.renderer) {
                const model = this.renderer.getAvatarModel();
                const mixer = this.renderer.getMixer();
                
                if (model && mixer) {
                    this.animationManager = new AvatarAnimationManager(model, mixer);
                    this.components.set('animation', this.animationManager);
                    console.log('✅ AvatarAnimationManager initialisé');
                }
            }
            
            this.isInitialized = true;
            console.log('✅ AvatarSystem initialisé avec succès');
            
            return this;
            
        } catch (error) {
            console.error('❌ Erreur initialisation AvatarSystem:', error);
            throw error;
        }
    }
    
    /**
     * Attend que les dépendances soient chargées
     */
    waitForDependencies() {
        return new Promise((resolve) => {
            const checkDependencies = () => {
                const hasThree = typeof THREE !== 'undefined';
                const hasGLTFLoader = window.GLTFLoaderReady || typeof THREE.GLTFLoader !== 'undefined';
                
                if (hasThree && hasGLTFLoader) {
                    console.log('✅ Dépendances chargées');
                    resolve();
                } else {
                    console.log('⏳ Attente des dépendances...');
                    setTimeout(checkDependencies, 100);
                }
            };
            
            checkDependencies();
        });
    }
    
    /**
     * Charge la configuration
     */
    async loadConfig() {
        // Configuration de base - sera enrichie par le chargeur de config
        this.config = {
            enabled: true,
            debug: true,
            appearance: {
                gender: 'female',
                age: 30,
                style: 'casual'
            },
            animations: {
                blinking: {
                    enabled: true,
                    frequency: 3500
                },
                lipSync: {
                    enabled: true
                },
                idleMovement: {
                    enabled: true
                }
            },
            renderer: {
                antialias: true,
                shadows: true
            }
        };
        
        console.log('📋 Configuration chargée');
    }
    
    /**
     * Obtient un composant par son nom
     */
    getComponent(name) {
        return this.components.get(name);
    }
    
    /**
     * Obtient le renderer
     */
    getRenderer() {
        return this.components.get('renderer');
    }
    
    /**
     * Obtient le gestionnaire d'animations
     */
    getAnimationManager() {
        return this.components.get('animation');
    }
    
    /**
     * Vérifie si le système est initialisé
     */
    isReady() {
        return this.isInitialized;
    }
    
    /**
     * Charge un avatar
     */
    async loadAvatar(modelUrl) {
        if (!this.renderer) {
            throw new Error('Renderer non initialisé');
        }
        
        console.log('📥 Chargement avatar:', modelUrl);
        const success = await this.renderer.loadAvatar(modelUrl);
        
        if (success && this.animationManager) {
            // Réinitialiser le gestionnaire d'animations avec le nouveau modèle
            const model = this.renderer.getAvatarModel();
            const mixer = this.renderer.getMixer();
            
            this.animationManager.dispose();
            this.animationManager = new AvatarAnimationManager(model, mixer);
            this.components.set('animation', this.animationManager);
        }
        
        return success;
    }
    
    /**
     * Définit une émotion
     */
    setEmotion(emotion, intensity = 0.7) {
        if (this.animationManager) {
            this.animationManager.setEmotion(emotion, intensity);
        } else {
            console.warn('⚠️ Gestionnaire d\'animations non disponible');
        }
    }
    
    /**
     * Fait parler l'avatar
     */
    speak(text, visemeData = null) {
        if (this.animationManager) {
            this.animationManager.performLipSync(text, visemeData);
        } else {
            console.warn('⚠️ Synchronisation labiale non disponible');
        }
    }
    
    /**
     * Joue une animation
     */
    playAnimation(name, options = {}) {
        if (this.animationManager) {
            return this.animationManager.playAnimation(name, options);
        } else {
            console.warn('⚠️ Gestionnaire d\'animations non disponible');
            return null;
        }
    }
    
    /**
     * Arrête une animation
     */
    stopAnimation(name, fadeOut = 0.5) {
        if (this.animationManager) {
            this.animationManager.stopAnimation(name, fadeOut);
        }
    }
    
    /**
     * Redimensionne le renderer
     */
    resize() {
        if (this.renderer) {
            this.renderer.resize();
        }
    }
    
    /**
     * Met à jour le système (à appeler dans la boucle de rendu)
     */
    update(deltaTime) {
        if (this.renderer) {
            this.renderer.update(deltaTime);
        }
    }
    
    /**
     * Nettoie les ressources
     */
    dispose() {
        if (this.animationManager) {
            this.animationManager.dispose();
        }
        
        if (this.renderer) {
            this.renderer.dispose();
        }
        
        this.components.clear();
        this.isInitialized = false;
        
        console.log('🧹 AvatarSystem nettoyé');
    }
    
    /**
     * Informations de debug
     */
    getDebugInfo() {
        return {
            isInitialized: this.isInitialized,
            hasRenderer: !!this.renderer,
            hasAnimationManager: !!this.animationManager,
            componentsCount: this.components.size,
            config: this.config
        };
    }
}

// Export global
window.AvatarSystem = AvatarSystem;

console.log('🔧 AvatarSystem chargé et prêt');

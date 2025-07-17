/**
 * Configuration centralisée pour le système d'avatar 3D
 * Gère les paramètres par défaut et la communication avec le backend
 */

class AvatarConfig {
    constructor() {
        this.config = {
            // Configuration générale
            enabled: true,
            type: '3d_realistic',
            displayTime: 30000,
            
            // Ready Player Me
            readyPlayerMe: {
                enabled: true,
                baseUrl: 'https://api.readyplayer.me/v1',
                quality: 'high',
                format: 'glb',
                defaultAvatarId: '64bfa9f1e2cde6f24e4b4567'
            },
            
            // Apparence par défaut
            appearance: {
                gender: 'female',
                age: 30,
                style: 'casual_friendly',
                hairColor: 'brown',
                eyeColor: 'brown',
                skinTone: 'medium'
            },
            
            // Capacités d'animation
            capabilities: {
                lipSync: true,
                blinking: true,
                headMovement: true,
                bodyLanguage: true,
                facialExpressions: true,
                gestureRecognition: true
            },
            
            // Configuration vocale
            speech: {
                enabled: true,
                language: 'fr-FR',
                voice: 'fr-FR-Wavenet-C',
                speed: 1.0,
                pitch: 0.0,
                volumeGainDb: 0.0
            },
            
            // WebSocket
            websocket: {
                enabled: true,
                endpoint: '/ws/avatar',
                reconnectInterval: 5000,
                maxReconnectAttempts: 10
            },
            
            // Émotions disponibles
            emotions: {
                available: ['neutral', 'happy', 'sad', 'excited', 'concerned', 'thoughtful', 'friendly', 'surprised', 'confused'],
                transitionDuration: 800,
                defaultIntensity: 0.7
            },
            
            // Gestes disponibles
            gestures: {
                enabled: true,
                available: ['wave', 'nod', 'shake', 'shrug', 'point', 'thumbsup', 'goodbye_wave', 'thinking', 'clap'],
                autoGestures: true
            },
            
            // Configuration du rendu 3D
            rendering: {
                quality: 'high',
                shadows: true,
                antialiasing: true,
                faceSubdivisions: 2,
                bodySubdivisions: 1,
                maxConcurrentAnimations: 5
            },
            
            // Modèles de fallback
            fallbackModels: {
                basePath: '/static/models/avatars/',
                female: {
                    young: 'female_young_casual.glb',
                    adult: 'female_adult_professional.glb',
                    mature: 'female_mature_elegant.glb'
                },
                male: {
                    young: 'male_young_casual.glb',
                    adult: 'male_adult_professional.glb',
                    mature: 'male_mature_distinguished.glb'
                }
            },
            
            // Messages par défaut
            messages: {
                greeting: 'Bonjour ! Je suis votre assistante virtuelle Angel. Comment puis-je vous aider aujourd\'hui ?',
                goodbye: 'Au revoir ! N\'hésitez pas à revenir si vous avez besoin d\'aide.',
                loading: 'Chargement de l\'avatar...',
                error: 'Une erreur est survenue lors du chargement de l\'avatar.',
                ready: 'Avatar prêt !'
            }
        };
        
        this.eventListeners = new Map();
        this.loadFromBackend();
    }
    
    /**
     * Charge la configuration depuis le backend
     */
    async loadFromBackend() {
        try {
            const response = await fetch('/api/avatar/config');
            if (response.ok) {
                const backendConfig = await response.json();
                this.mergeConfig(backendConfig);
                this.notifyConfigLoaded();
            } else {
                console.warn('Impossible de charger la configuration depuis le backend, utilisation des valeurs par défaut');
            }
        } catch (error) {
            console.warn('Erreur lors du chargement de la configuration:', error);
        }
    }
    
    /**
     * Fusionne la configuration du backend avec la configuration par défaut
     */
    mergeConfig(backendConfig) {
        this.config = this.deepMerge(this.config, backendConfig);
    }
    
    /**
     * Fusion profonde de deux objets
     */
    deepMerge(target, source) {
        const result = { ...target };
        
        for (const key in source) {
            if (source[key] !== null && typeof source[key] === 'object' && !Array.isArray(source[key])) {
                result[key] = this.deepMerge(target[key] || {}, source[key]);
            } else {
                result[key] = source[key];
            }
        }
        
        return result;
    }
    
    /**
     * Obtient une valeur de configuration
     */
    get(path, defaultValue = null) {
        const keys = path.split('.');
        let value = this.config;
        
        for (const key of keys) {
            if (value && typeof value === 'object' && key in value) {
                value = value[key];
            } else {
                return defaultValue;
            }
        }
        
        return value;
    }
    
    /**
     * Définit une valeur de configuration
     */
    set(path, value) {
        const keys = path.split('.');
        let current = this.config;
        
        for (let i = 0; i < keys.length - 1; i++) {
            const key = keys[i];
            if (!(key in current) || typeof current[key] !== 'object') {
                current[key] = {};
            }
            current = current[key];
        }
        
        current[keys[keys.length - 1]] = value;
        this.notifyConfigChanged(path, value);
    }
    
    /**
     * Vérifie si une fonctionnalité est activée
     */
    isEnabled(feature) {
        return this.get(feature, false) === true;
    }
    
    /**
     * Obtient la configuration complète
     */
    getAll() {
        return { ...this.config };
    }
    
    /**
     * Détermine le modèle d'avatar approprié selon les paramètres
     */
    getAvatarModelPath(gender, age, style) {
        if (this.isEnabled('readyPlayerMe.enabled')) {
            // Utiliser Ready Player Me
            return this.getReadyPlayerMeUrl(gender, age, style);
        } else {
            // Utiliser les modèles locaux
            return this.getFallbackModelPath(gender, age);
        }
    }
    
    /**
     * Génère l'URL Ready Player Me pour les paramètres donnés
     */
    getReadyPlayerMeUrl(gender, age, style) {
        const baseUrl = this.get('readyPlayerMe.baseUrl');
        const quality = this.get('readyPlayerMe.quality');
        const format = this.get('readyPlayerMe.format');
        const avatarId = this.get('readyPlayerMe.defaultAvatarId');
        
        return `${baseUrl}/avatars/${avatarId}.${format}?quality=${quality}&gender=${gender}&age=${age}&style=${style}`;
    }
    
    /**
     * Obtient le chemin du modèle de fallback
     */
    getFallbackModelPath(gender, age) {
        const basePath = this.get('fallbackModels.basePath');
        const ageGroup = this.determineAgeGroup(age);
        const modelFile = this.get(`fallbackModels.${gender}.${ageGroup}`);
        
        return basePath + modelFile;
    }
    
    /**
     * Détermine le groupe d'âge
     */
    determineAgeGroup(age) {
        if (age < 25) return 'young';
        if (age < 40) return 'adult';
        return 'mature';
    }
    
    /**
     * Ajoute un écouteur d'événement de configuration
     */
    addEventListener(event, callback) {
        if (!this.eventListeners.has(event)) {
            this.eventListeners.set(event, []);
        }
        this.eventListeners.get(event).push(callback);
    }
    
    /**
     * Supprime un écouteur d'événement
     */
    removeEventListener(event, callback) {
        if (this.eventListeners.has(event)) {
            const listeners = this.eventListeners.get(event);
            const index = listeners.indexOf(callback);
            if (index > -1) {
                listeners.splice(index, 1);
            }
        }
    }
    
    /**
     * Notifie que la configuration a été chargée
     */
    notifyConfigLoaded() {
        this.notify('configLoaded', this.config);
    }
    
    /**
     * Notifie qu'une valeur de configuration a changé
     */
    notifyConfigChanged(path, value) {
        this.notify('configChanged', { path, value });
    }
    
    /**
     * Notifie les écouteurs d'un événement
     */
    notify(event, data) {
        if (this.eventListeners.has(event)) {
            this.eventListeners.get(event).forEach(callback => {
                try {
                    callback(data);
                } catch (error) {
                    console.error('Erreur dans l\'écouteur d\'événement:', error);
                }
            });
        }
    }
}

// Instance globale
window.AvatarConfig = window.AvatarConfig || AvatarConfig;
/**
 * Configuration centralisée pour l'avatar Angel
 * Gère le chargement des dépendances et la configuration globale
 */
class AvatarConfigManager {
    constructor() {
        this.config = null;
        this.loadPromise = null;
    }

    /**
     * Charge la configuration depuis le fichier JSON
     */
    async loadConfig() {
        if (this.loadPromise) {
            return this.loadPromise;
        }

        this.loadPromise = this._loadConfigInternal();
        return this.loadPromise;
    }

    async _loadConfigInternal() {
        try {
            const configPath = window.ANGEL_CONFIG?.contextPath || '/';
            const response = await fetch(`${configPath}config/avatar-config.json`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            this.config = await response.json();
            console.log('✅ Configuration avatar chargée:', this.config.app.name, this.config.app.version);
            
            // Fusionner avec la configuration existante
            if (window.ANGEL_CONFIG) {
                Object.assign(window.ANGEL_CONFIG, {
                    ...this.config,
                    contextPath: window.ANGEL_CONFIG.contextPath
                });
            } else {
                window.ANGEL_CONFIG = this.config;
            }

            return this.config;
            
        } catch (error) {
            console.error('❌ Erreur chargement configuration:', error);
            
            // Configuration de fallback
            this.config = this.getDefaultConfig();
            console.log('⚠️ Utilisation configuration par défaut');
            
            return this.config;
        }
    }

    /**
     * Configuration par défaut en cas d'échec
     */
    getDefaultConfig() {
        return {
            app: {
                name: "Angel Virtual Assistant",
                version: "1.0.0",
                mode: "fallback"
            },
            dependencies: {
                threejs: {
                    version: "r128",
                    url: "https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js",
                    timeout: 20000,
                    required: true
                },
                gltfLoader: {
                    url: "https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/loaders/GLTFLoader.js",
                    timeout: 15000,
                    required: true,
                    loadAfter: "threejs"
                }
            },
            avatar: {
                readyPlayerMe: {
                    enabled: true,
                    baseUrl: "https://models.readyplayer.me",
                    defaultAvatarId: "687f66fafe8107131699bf7b"
                }
            },
            voice: {
                wakeWord: {
                    enabled: true,
                    fallbackMode: true
                }
            },
            debug: {
                enabled: true
            }
        };
    }

    /**
     * Charge les dépendances définies dans la configuration
     */
    async loadDependencies() {
        if (!this.config) {
            await this.loadConfig();
        }

        const dependencies = this.config.dependencies;
        const loadedDeps = new Map();

        console.log('📦 Chargement des dépendances...');

        // Charger dans l'ordre défini
        const loadOrder = ['threejs', 'gltfLoader', 'tweenjs'];
        
        for (const depName of loadOrder) {
            const dep = dependencies[depName];
            if (!dep) continue;

            // Vérifier si dépendance nécessaire
            if (dep.loadAfter && !loadedDeps.has(dep.loadAfter)) {
                if (dep.required) {
                    throw new Error(`Dépendance ${dep.loadAfter} requise pour ${depName} non chargée`);
                }
                console.warn(`⚠️ ${depName} ignoré: ${dep.loadAfter} non disponible`);
                continue;
            }

            try {
                await this.loadScript(dep.url, depName, dep.timeout || 10000);
                loadedDeps.set(depName, true);
                console.log(`✅ ${depName} chargé`);

                // Post-traitement spécifique
                if (depName === 'gltfLoader') {
                    await this.setupGLTFLoader();
                }

            } catch (error) {
                console.error(`❌ Erreur chargement ${depName}:`, error);
                
                if (dep.required) {
                    throw new Error(`Dépendance requise ${depName} non chargée: ${error.message}`);
                }
            }
        }

        console.log('✅ Toutes les dépendances chargées');
        return loadedDeps;
    }

    /**
     * Charge un script avec timeout
     */
    loadScript(src, name, timeout = 10000) {
        return new Promise((resolve, reject) => {
            // Vérifier si déjà chargé
            if (this.isScriptLoaded(name)) {
                resolve();
                return;
            }

            const script = document.createElement('script');
            script.src = src;
            script.async = true;

            const timer = setTimeout(() => {
                reject(new Error(`Timeout loading ${name} after ${timeout}ms`));
            }, timeout);

            script.onload = () => {
                clearTimeout(timer);
                console.log(`📦 ${name} loaded from ${src}`);
                resolve();
            };

            script.onerror = () => {
                clearTimeout(timer);
                reject(new Error(`Failed to load ${name} from ${src}`));
            };

            document.head.appendChild(script);
        });
    }

    /**
     * Vérifie si un script est déjà chargé
     */
    isScriptLoaded(name) {
        switch (name) {
            case 'threejs':
                return typeof THREE !== 'undefined';
            case 'gltfLoader':
                return typeof THREE !== 'undefined' && 
                       (THREE.GLTFLoader || window.GLTFLoaderReady);
            case 'tweenjs':
                return typeof TWEEN !== 'undefined';
            default:
                return false;
        }
    }

    /**
     * Configuration post-chargement de GLTFLoader
     */
    async setupGLTFLoader() {
        return new Promise((resolve) => {
            // Attendre que THREE soit disponible
            const checkTHREE = () => {
                if (typeof THREE !== 'undefined') {
                    // Marquer GLTFLoader comme prêt
                    window.GLTFLoaderReady = true;
                    console.log('🎯 GLTFLoader configuré et prêt');
                    resolve();
                } else {
                    setTimeout(checkTHREE, 100);
                }
            };
            checkTHREE();
        });
    }

    /**
     * Utilitaire pour récupérer une valeur de configuration
     */
    get(path, defaultValue = null) {
        if (!this.config) {
            console.warn('⚠️ Configuration non chargée, valeur par défaut utilisée');
            return defaultValue;
        }

        const keys = path.split('.');
        let value = this.config;

        for (const key of keys) {
            if (value && typeof value === 'object' && key in value) {
                value = value[key];
            } else {
                return defaultValue;
            }
        }

        return value !== undefined ? value : defaultValue;
    }

    /**
     * Récupère la configuration complète
     */
    getConfig() {
        return this.config;
    }

    /**
     * Active/Désactive le mode debug
     */
    setDebugMode(enabled) {
        if (this.config) {
            this.config.debug.enabled = enabled;
            console.log(`🐛 Mode debug ${enabled ? 'activé' : 'désactivé'}`);
        }
    }
}

// Instance globale
window.avatarConfigManager = new AvatarConfigManager();

// Export pour compatibilité
window.AvatarConfigManager = AvatarConfigManager;

console.log('⚙️ AvatarConfigManager initialisé');

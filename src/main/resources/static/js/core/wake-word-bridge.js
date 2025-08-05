/**
 * Bridge entre le système Java WakeWordDetector et l'interface JavaScript
 * Gère la détection des mots-clés et la fallback en mode test
 */
class WakeWordBridge {
    constructor() {
        this.isInitialized = false;
        this.isListening = false;
        this.websocket = null;
        this.fallbackMode = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.callbacks = {
            onWakeWordDetected: [],
            onConnectionStatus: [],
            onError: []
        };

        console.log('🔌 WakeWordBridge initialisé');
    }

    /**
     * Initialise la connection WebSocket avec le backend Java
     */
    async init() {
        if (this.isInitialized) {
            console.log('✅ WakeWordBridge déjà initialisé');
            return;
        }

        console.log('🚀 Initialisation WakeWordBridge...');

        try {
            // Récupérer la configuration
            const config = window.avatarConfigManager?.get('voice.wakeWord') || {
                enabled: true,
                fallbackMode: true
            };

            if (!config.enabled) {
                console.log('⚠️ WakeWord désactivé dans la configuration');
                return;
            }

            // Tenter connexion WebSocket
            await this.connectWebSocket();
            
            this.isInitialized = true;
            console.log('✅ WakeWordBridge initialisé avec succès');

        } catch (error) {
            console.warn('⚠️ Erreur initialisation WakeWordBridge:', error.message);
            
            // Basculer en mode fallback
            this.enableFallbackMode();
        }
    }

    /**
     * Établit la connexion WebSocket avec le backend
     */
    async connectWebSocket() {
        return new Promise((resolve, reject) => {
            const contextPath = window.ANGEL_CONFIG?.contextPath || '/';
            const wsUrl = `ws://${window.location.host}${contextPath}ws/wake-word`;

            console.log('🔌 Connexion WebSocket:', wsUrl);

            this.websocket = new WebSocket(wsUrl);

            const timeout = setTimeout(() => {
                reject(new Error('Timeout connexion WebSocket'));
            }, 5000);

            this.websocket.onopen = () => {
                clearTimeout(timeout);
                console.log('✅ WebSocket WakeWordDetector connecté');
                this.reconnectAttempts = 0;
                this.notifyConnectionStatus(true);
                resolve();
            };

            this.websocket.onmessage = (event) => {
                this.handleWebSocketMessage(event);
            };

            this.websocket.onclose = () => {
                clearTimeout(timeout);
                console.log('🔐 WebSocket WakeWordDetector fermé');
                this.notifyConnectionStatus(false);
                this.attemptReconnect();
            };

            this.websocket.onerror = (error) => {
                clearTimeout(timeout);
                console.error('❌ Erreur WebSocket WakeWordDetector:', error);
                this.notifyError(error);
                reject(error);
            };
        });
    }

    /**
     * Gère les messages reçus via WebSocket
     */
    handleWebSocketMessage(event) {
        try {
            const data = JSON.parse(event.data);
            console.log('📨 Message WakeWordDetector:', data);

            switch (data.type) {
                case 'WAKE_WORD_DETECTED':
                    this.handleWakeWordDetected(data);
                    break;
                case 'LISTENING_STATUS':
                    this.handleListeningStatus(data);
                    break;
                case 'ERROR':
                    this.handleError(data);
                    break;
                default:
                    console.warn('⚠️ Type de message inconnu:', data.type);
            }
        } catch (error) {
            console.error('❌ Erreur parsing message WebSocket:', error);
        }
    }

    /**
     * Traite la détection d'un mot-clé
     */
    handleWakeWordDetected(data) {
        console.log('🎯 Mot-clé détecté:', data.word, 'confidence:', data.confidence);
        
        const event = {
            word: data.word,
            confidence: data.confidence,
            timestamp: Date.now(),
            source: 'java-detector'
        };

        this.notifyWakeWordDetected(event);
    }

    /**
     * Gère le statut d'écoute
     */
    handleListeningStatus(data) {
        this.isListening = data.listening;
        console.log(`🎤 WakeWordDetector ${this.isListening ? 'en écoute' : 'arrêté'}`);
    }

    /**
     * Gère les erreurs du backend
     */
    handleError(data) {
        console.error('❌ Erreur backend WakeWordDetector:', data.message);
        this.notifyError(new Error(data.message));
    }

    /**
     * Démarre l'écoute des mots-clés
     */
    async startListening() {
        if (!this.isInitialized) {
            await this.init();
        }

        if (this.fallbackMode) {
            return this.startFallbackListening();
        }

        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            console.log('🎤 Démarrage écoute WakeWordDetector...');
            this.websocket.send(JSON.stringify({
                type: 'START_LISTENING'
            }));
        } else {
            console.warn('⚠️ WebSocket non connecté, mode fallback');
            this.enableFallbackMode();
            this.startFallbackListening();
        }
    }

    /**
     * Arrête l'écoute des mots-clés
     */
    stopListening() {
        if (this.fallbackMode) {
            return this.stopFallbackListening();
        }

        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            console.log('🛑 Arrêt écoute WakeWordDetector...');
            this.websocket.send(JSON.stringify({
                type: 'STOP_LISTENING'
            }));
        }
    }

    /**
     * Active le mode fallback (simulation pour les tests)
     */
    enableFallbackMode() {
        console.log('🔄 Activation mode fallback WakeWordDetector');
        this.fallbackMode = true;
        this.isInitialized = true;
        
        // Simuler une connexion
        this.notifyConnectionStatus(true);
    }

    /**
     * Démarre l'écoute en mode fallback (simulation)
     */
    startFallbackListening() {
        console.log('🎤 Démarrage écoute fallback (simulation)...');
        this.isListening = true;

        // En mode test, simuler la détection occasionnelle
        if (window.ANGEL_CONFIG?.debug?.enabled) {
            this.simulateWakeWordDetection();
        }
    }

    /**
     * Arrête l'écoute en mode fallback
     */
    stopFallbackListening() {
        console.log('🛑 Arrêt écoute fallback');
        this.isListening = false;
    }

    /**
     * Simule la détection de mots-clés pour les tests
     */
    simulateWakeWordDetection() {
        // Simuler détection après 10-30 secondes
        const delay = 10000 + Math.random() * 20000;
        
        setTimeout(() => {
            if (this.isListening && this.fallbackMode) {
                const event = {
                    word: 'angel',
                    confidence: 0.85,
                    timestamp: Date.now(),
                    source: 'fallback-simulator'
                };

                console.log('🎯 Simulation détection mot-clé:', event);
                this.notifyWakeWordDetected(event);

                // Programmer la prochaine simulation
                this.simulateWakeWordDetection();
            }
        }, delay);
    }

    /**
     * Tente de se reconnecter au WebSocket
     */
    attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('⚠️ Nombre max de reconnexions atteint, basculement en mode fallback');
            this.enableFallbackMode();
            return;
        }

        this.reconnectAttempts++;
        const delay = 2000 * this.reconnectAttempts;

        console.log(`🔄 Tentative reconnexion ${this.reconnectAttempts}/${this.maxReconnectAttempts} dans ${delay}ms...`);

        setTimeout(async () => {
            try {
                await this.connectWebSocket();
            } catch (error) {
                console.warn('⚠️ Échec reconnexion:', error.message);
            }
        }, delay);
    }

    /**
     * Enregistre un callback pour les détections
     */
    onWakeWordDetected(callback) {
        this.callbacks.onWakeWordDetected.push(callback);
    }

    /**
     * Enregistre un callback pour le statut de connexion
     */
    onConnectionStatus(callback) {
        this.callbacks.onConnectionStatus.push(callback);
    }

    /**
     * Enregistre un callback pour les erreurs
     */
    onError(callback) {
        this.callbacks.onError.push(callback);
    }

    /**
     * Notifie les callbacks de détection
     */
    notifyWakeWordDetected(event) {
        this.callbacks.onWakeWordDetected.forEach(callback => {
            try {
                callback(event);
            } catch (error) {
                console.error('❌ Erreur callback wake word:', error);
            }
        });
    }

    /**
     * Notifie les callbacks de statut de connexion
     */
    notifyConnectionStatus(connected) {
        this.callbacks.onConnectionStatus.forEach(callback => {
            try {
                callback(connected);
            } catch (error) {
                console.error('❌ Erreur callback connection status:', error);
            }
        });
    }

    /**
     * Notifie les callbacks d'erreur
     */
    notifyError(error) {
        this.callbacks.onError.forEach(callback => {
            try {
                callback(error);
            } catch (error) {
                console.error('❌ Erreur callback error:', error);
            }
        });
    }

    /**
     * Vérifie si le detector est en train d'écouter
     */
    isCurrentlyListening() {
        return this.isListening;
    }

    /**
     * Récupère le statut de connexion
     */
    isConnected() {
        return this.isInitialized && (
            this.fallbackMode || 
            (this.websocket && this.websocket.readyState === WebSocket.OPEN)
        );
    }

    /**
     * Nettoyage des ressources
     */
    dispose() {
        console.log('🧹 Nettoyage WakeWordBridge...');
        
        this.stopListening();
        
        if (this.websocket) {
            this.websocket.close();
            this.websocket = null;
        }

        this.callbacks = {
            onWakeWordDetected: [],
            onConnectionStatus: [],
            onError: []
        };

        this.isInitialized = false;
        this.isListening = false;
    }
}

// Instance globale
window.wakeWordBridge = new WakeWordBridge();

// Compatibilité avec l'ancien système
window.WakeWordDetector = window.wakeWordBridge;

console.log('🔌 WakeWordBridge disponible globalement');

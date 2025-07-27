/**
 * Détecteur de mot-clé côté client qui intègre la reconnaissance vocale
 * avec la communication WebSocket vers le serveur Angel.
 * Version corrigée avec initialisation robuste.
 */
class WakeWordDetector {
    constructor() {
        this.speechService = null;
        this.websocket = null;
        this.isConnected = false;
        this.isInitialized = false;
        this.config = {
            wakeWord: 'Angel',
            language: 'fr-FR',
            continuous: true,
            confidenceThreshold: 0.7
        };
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 2000;
        this.initializationPromise = null;
        
        // Démarrer l'initialisation
        this.initialize();
    }
    
    /**
     * Initialise le détecteur (WebSocket + reconnaissance vocale).
     */
    async initialize() {
        if (this.initializationPromise) {
            return this.initializationPromise;
        }
        
        this.initializationPromise = this._doInitialize();
        return this.initializationPromise;
    }
    
    /**
     * Processus d'initialisation interne.
     */
    async _doInitialize() {
        console.log('🎤 Initialisation du détecteur de mot-clé...');
        
        try {
            // 1. Initialiser d'abord la reconnaissance vocale avec config par défaut
            await this.initializeSpeechRecognition();
            
            // 2. Puis initialiser WebSocket (qui pourra mettre à jour la config)
            await this.initializeWebSocket();
            
            this.isInitialized = true;
            console.log('✅ Détecteur de mot-clé initialisé avec succès');
            
        } catch (error) {
            console.error('❌ Erreur lors de l\'initialisation:', error);
            // Continuer avec la config par défaut même si WebSocket échoue
            if (!this.speechService) {
                await this.initializeSpeechRecognition();
            }
            this.isInitialized = true;
        }
    }
    
    /**
     * Initialise le service de reconnaissance vocale.
     */
    async initializeSpeechRecognition() {
        console.log('🗣️ Initialisation du service de reconnaissance vocale...');
        
        try {
            this.speechService = new SpeechRecognitionService();
            this.speechService.updateConfig(this.config);
            
            // Configurer les événements
            this.setupSpeechEvents();
            
            console.log('✅ Service de reconnaissance vocale initialisé');
            
        } catch (error) {
            console.error('❌ Erreur initialisation reconnaissance vocale:', error);
            throw error;
        }
    }
    
    /**
     * Configure les événements de reconnaissance vocale.
     */
    setupSpeechEvents() {
        if (!this.speechService) return;
        
        // Événements de reconnaissance vocale
        this.speechService.on('wakeWord', (data) => {
            this.onWakeWordDetected(data);
        });
        
        this.speechService.on('command', (data) => {
            this.onCommandReceived(data);
        });
        
        this.speechService.on('error', (error) => {
            this.onSpeechError(error);
        });
        
        this.speechService.on('start', () => {
            this.updateListeningIndicator(true);
            this.sendMessage('speech_status', { status: 'listening' });
        });
        
        this.speechService.on('end', () => {
            this.updateListeningIndicator(false);
            this.sendMessage('speech_status', { status: 'stopped' });
        });
        
        this.speechService.on('interim', (text) => {
            // Affichage optionnel du texte temporaire
            if (this.config.debugMode) {
                console.log('👂 Texte temporaire:', text);
            }
        });
    }
    
    /**
     * Initialise la connexion WebSocket.
     */
    async initializeWebSocket() {
        console.log('🔌 Initialisation WebSocket...');
        
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws/voice`;
        
        return new Promise((resolve, reject) => {
            try {
                this.websocket = new WebSocket(wsUrl);
                
                this.websocket.onopen = () => {
                    console.log('✅ WebSocket connecté pour la détection vocale');
                    this.isConnected = true;
                    this.reconnectAttempts = 0;
                    this.sendMessage('connection', { status: 'connected' });
                    resolve();
                };
                
                this.websocket.onmessage = (event) => {
                    this.handleServerMessage(event);
                };
                
                this.websocket.onerror = (error) => {
                    console.error('❌ Erreur WebSocket:', error);
                    this.isConnected = false;
                    reject(error);
                };
                
                this.websocket.onclose = (event) => {
                    console.log('🔌 WebSocket fermé:', event.code, event.reason);
                    this.isConnected = false;
                    this.handleReconnection();
                };
                
                // Timeout pour la connexion
                setTimeout(() => {
                    if (!this.isConnected) {
                        reject(new Error('Timeout de connexion WebSocket'));
                    }
                }, 5000);
                
            } catch (error) {
                console.error('❌ Erreur création WebSocket:', error);
                reject(error);
            }
        });
    }
    
    /**
     * Démarre l'écoute vocale.
     */
    async startListening() {
        console.log('▶️ Démarrage de l\'écoute...');
        
        // S'assurer que l'initialisation est terminée
        if (!this.isInitialized) {
            console.log('⏳ Attente de l\'initialisation...');
            await this.initialize();
        }
        
        if (!this.speechService) {
            console.error('❌ Service de reconnaissance vocale non disponible');
            this.showError('Service de reconnaissance vocale non disponible');
            return;
        }
        
        try {
            // Demander l'autorisation du microphone
            await navigator.mediaDevices.getUserMedia({ audio: true });
            console.log('✅ Autorisation microphone accordée');
            
            this.speechService.startListening();
            this.showListeningStatus('En écoute du mot-clé...');
            
        } catch (error) {
            console.error('❌ Erreur démarrage écoute:', error);
            this.showMicrophoneError();
        }
    }
    
    /**
     * Arrête l'écoute vocale.
     */
    stopListening() {
        console.log('⏹️ Arrêt de l\'écoute...');
        
        if (this.speechService) {
            this.speechService.stopListening();
        }
        this.updateListeningIndicator(false);
        this.showListeningStatus('Écoute arrêtée');
    }
    
    /**
     * Gère la reconnexion automatique WebSocket.
     */
    handleReconnection() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`🔄 Tentative de reconnexion ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            
            setTimeout(() => {
                this.initializeWebSocket().catch(error => {
                    console.error('❌ Reconnexion échouée:', error);
                });
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('❌ Impossible de se reconnecter au serveur');
            this.showConnectionError();
        }
    }
    
    /**
     * Traite les messages du serveur.
     */
    handleServerMessage(event) {
        try {
            const message = JSON.parse(event.data);
            
            switch (message.type) {
                case 'config':
                case 'configuration':
                    this.handleConfiguration(message);
                    break;
                    
                case 'wake_word_confirmed':
                    this.handleWakeWordConfirmed(message);
                    break;
                    
                case 'ai_response':
                    this.handleAiResponse(message);
                    break;
                    
                case 'error':
                    this.handleServerError(message);
                    break;
                    
                case 'pong':
                    // Réponse au ping
                    break;
                    
                default:
                    console.log('📨 Message serveur non géré:', message);
            }
            
        } catch (error) {
            console.error('❌ Erreur traitement message serveur:', error);
        }
    }
    
    /**
     * Gère la configuration reçue du serveur.
     */
    handleConfiguration(message) {
        console.log('⚙️ Configuration reçue du serveur:', message);
        
        // Mettre à jour la configuration
        if (message.wakeWord) this.config.wakeWord = message.wakeWord;
        if (message.language) this.config.language = message.language;
        if (message.confidenceThreshold !== undefined) this.config.confidenceThreshold = message.confidenceThreshold;
        if (message.continuous !== undefined) this.config.continuous = message.continuous;
        
        // Appliquer à la reconnaissance vocale
        if (this.speechService) {
            this.speechService.updateConfig(this.config);
        }
        
        // Mettre à jour l'interface
        this.updateUIFromConfig();
    }
    
    /**
     * Met à jour l'interface utilisateur avec la configuration.
     */
    updateUIFromConfig() {
        const wakeWordInput = document.getElementById('wake-word-input');
        if (wakeWordInput) wakeWordInput.value = this.config.wakeWord;
        
        const languageSelect = document.getElementById('language-select');
        if (languageSelect) languageSelect.value = this.config.language;
        
        const confidenceSlider = document.getElementById('confidence-slider');
        const confidenceValue = document.getElementById('confidence-value');
        if (confidenceSlider) {
            confidenceSlider.value = this.config.confidenceThreshold;
            if (confidenceValue) confidenceValue.textContent = this.config.confidenceThreshold;
        }
        
        const continuousCheckbox = document.getElementById('continuous-listening');
        if (continuousCheckbox) continuousCheckbox.checked = this.config.continuous;
    }
    
    /**
     * Gère la détection du mot-clé.
     */
    onWakeWordDetected(data) {
        console.log('🎯 Mot-clé détecté:', data);
        
        // Envoyer au serveur
        this.sendMessage('wake_word_detected', {
            word: data.word,
            transcript: data.transcript,
            confidence: data.confidence
        });
        
        // Indication visuelle
        this.showWakeWordDetected();
        
        // Passer en mode commande
        if (this.speechService) {
            this.speechService.startCommandMode();
        }
        this.showListeningStatus('Que puis-je faire pour vous ?');
    }
    
    /**
     * Gère la réception d'une commande vocale.
     */
    onCommandReceived(data) {
        console.log('🗣️ Commande reçue:', data);
        
        // Envoyer au serveur pour traitement
        this.sendMessage('speech_command', {
            command: data.command,
            confidence: data.confidence
        });
        
        // Indication visuelle
        this.showCommandProcessing(data.command);
    }
    
    /**
     * Gère la confirmation du réveil par le serveur.
     */
    handleWakeWordConfirmed(message) {
        console.log('✅ Réveil confirmé par le serveur');
        this.showSystemActivated();
    }
    
    /**
     * Gère la réponse de l'IA.
     */
    handleAiResponse(message) {
        const response = message.data ? message.data.response : message.response;
        console.log('🤖 Réponse IA reçue:', response);
        
        // Afficher la réponse
        this.showAiResponse(response);
        
        // Retourner en mode écoute après un délai
        setTimeout(() => {
            if (this.speechService) {
                this.speechService.returnToWakeWordMode();
                this.showListeningStatus('En écoute du mot-clé...');
            }
        }, 3000);
    }
    
    /**
     * Gère les erreurs de reconnaissance vocale.
     */
    onSpeechError(error) {
        console.error('❌ Erreur reconnaissance vocale:', error);
        
        this.sendMessage('speech_error', {
            error: error,
            timestamp: Date.now()
        });
        
        this.showSpeechError(error);
    }
    
    /**
     * Gère les erreurs du serveur.
     */
    handleServerError(message) {
        console.error('❌ Erreur serveur:', message.message);
        this.showServerError(message.message);
    }
    
    /**
     * Envoie un message au serveur via WebSocket.
     */
    sendMessage(type, data) {
        if (!this.isConnected || !this.websocket) {
            console.warn('⚠️ WebSocket non connecté, message non envoyé:', type);
            return;
        }
        
        const message = {
            type: type,
            timestamp: Date.now(),
            ...data
        };
        
        try {
            this.websocket.send(JSON.stringify(message));
        } catch (error) {
            console.error('❌ Erreur envoi message:', error);
        }
    }
    
    /**
     * Met à jour l'indicateur d'écoute.
     */
    updateListeningIndicator(isListening) {
        const indicator = document.getElementById('listening-indicator');
        if (indicator) {
            indicator.classList.toggle('listening', isListening);
            indicator.classList.toggle('idle', !isListening);
        }
    }
    
    /**
     * Affiche le statut d'écoute.
     */
    showListeningStatus(message) {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = message;
            statusElement.className = 'speech-status listening';
        }
        console.log('📢 Statut:', message);
    }
    
    /**
     * Affiche la détection du mot-clé.
     */
    showWakeWordDetected() {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = '🎯 Angel activé !';
            statusElement.className = 'speech-status activated';
        }
        
        // Animation visuelle
        document.body.classList.add('wake-word-detected');
        setTimeout(() => {
            document.body.classList.remove('wake-word-detected');
        }, 2000);
    }
    
    /**
     * Affiche le traitement d'une commande.
     */
    showCommandProcessing(command) {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = `💭 Traitement: "${command}"`;
            statusElement.className = 'speech-status processing';
        }
    }
    
    /**
     * Affiche la réponse de l'IA.
     */
    showAiResponse(response) {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = `🤖 ${response}`;
            statusElement.className = 'speech-status response';
        }
    }
    
    /**
     * Affiche l'activation du système.
     */
    showSystemActivated() {
        console.log('✅ Système Angel activé');
    }
    
    /**
     * Affiche une erreur de reconnaissance vocale.
     */
    showSpeechError(error) {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = `❌ Erreur: ${error}`;
            statusElement.className = 'speech-status error';
        }
    }
    
    /**
     * Affiche une erreur serveur.
     */
    showServerError(error) {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = `⚠️ Erreur serveur: ${error}`;
            statusElement.className = 'speech-status error';
        }
    }
    
    /**
     * Affiche une erreur de connexion.
     */
    showConnectionError() {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = '🔌 Erreur de connexion au serveur';
            statusElement.className = 'speech-status error';
        }
    }
    
    /**
     * Affiche une erreur de microphone.
     */
    showMicrophoneError() {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = '🎤 Accès au microphone requis';
            statusElement.className = 'speech-status error';
        }
    }
    
    /**
     * Affiche une erreur générale.
     */
    showError(message) {
        const statusElement = document.getElementById('speech-status');
        if (statusElement) {
            statusElement.textContent = `❌ ${message}`;
            statusElement.className = 'speech-status error';
        }
    }
    
    /**
     * Obtient le statut actuel du détecteur.
     */
    getStatus() {
        return {
            isInitialized: this.isInitialized,
            isConnected: this.isConnected,
            speechStatus: this.speechService ? this.speechService.getStatus() : null,
            config: this.config
        };
    }
    
    /**
     * Nettoie les ressources.
     */
    destroy() {
        console.log('🧹 Nettoyage du détecteur de mot-clé...');
        
        if (this.speechService) {
            this.speechService.stopListening();
        }
        
        if (this.websocket) {
            this.websocket.close();
        }
        
        this.isInitialized = false;
        this.isConnected = false;
    }
}
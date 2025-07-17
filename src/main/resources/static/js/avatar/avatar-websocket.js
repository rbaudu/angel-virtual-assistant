/**
 * Service WebSocket pour la communication en temps réel avec le backend avatar
 * Gère la connexion, les messages et la synchronisation
 */

class AvatarWebSocket {
    constructor(config) {
        this.config = config;
        this.socket = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = config.get('websocket.maxReconnectAttempts', 10);
        this.reconnectInterval = config.get('websocket.reconnectInterval', 5000);
        
        this.messageHandlers = new Map();
        this.eventListeners = new Map();
        
        this.setupMessageHandlers();
    }
    
    /**
     * Établit la connexion WebSocket
     */
    connect() {
        if (!this.config.isEnabled('websocket.enabled')) {
            console.log('WebSocket désactivé dans la configuration');
            return;
        }
        
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const endpoint = this.config.get('websocket.endpoint', '/ws/avatar');
        const url = `${protocol}//${window.location.host}${endpoint}`;
        
        console.log('Connexion WebSocket à:', url);
        
        try {
            this.socket = new WebSocket(url);
            this.setupEventHandlers();
        } catch (error) {
            console.error('Erreur lors de la création de la connexion WebSocket:', error);
            this.scheduleReconnect();
        }
    }
    
    /**
     * Configure les gestionnaires d'événements WebSocket
     */
    setupEventHandlers() {
        this.socket.onopen = (event) => {
            console.log('Connexion WebSocket établie');
            this.isConnected = true;
            this.reconnectAttempts = 0;
            this.notify('connected', event);
        };
        
        this.socket.onmessage = (event) => {
            try {
                const message = JSON.parse(event.data);
                this.handleMessage(message);
            } catch (error) {
                console.error('Erreur lors du parsing du message WebSocket:', error);
            }
        };
        
        this.socket.onclose = (event) => {
            console.log('Connexion WebSocket fermée:', event.code, event.reason);
            this.isConnected = false;
            this.notify('disconnected', event);
            
            // Reconnexion automatique si la fermeture n'était pas intentionnelle
            if (event.code !== 1000) {
                this.scheduleReconnect();
            }
        };
        
        this.socket.onerror = (error) => {
            console.error('Erreur WebSocket:', error);
            this.notify('error', error);
        };
    }
    
    /**
     * Configure les gestionnaires de messages
     */
    setupMessageHandlers() {
        // Gestionnaire pour l'initialisation de l'avatar
        this.addMessageHandler('avatar_init', (data) => {
            console.log('Initialisation avatar reçue:', data);
            this.notify('avatar_init', data);
        });
        
        // Gestionnaire pour les messages de parole
        this.addMessageHandler('avatar_speech', (data) => {
            console.log('Message de parole reçu:', data);
            this.notify('avatar_speech', data);
        });
        
        // Gestionnaire pour les changements d'émotion
        this.addMessageHandler('avatar_emotion', (data) => {
            console.log('Changement d\'émotion reçu:', data);
            this.notify('avatar_emotion', data);
        });
        
        // Gestionnaire pour les gestes
        this.addMessageHandler('avatar_gesture', (data) => {
            console.log('Geste reçu:', data);
            this.notify('avatar_gesture', data);
        });
        
        // Gestionnaire pour la visibilité
        this.addMessageHandler('avatar_visibility', (data) => {
            console.log('Changement de visibilité reçu:', data);
            this.notify('avatar_visibility', data);
        });
        
        // Gestionnaire pour les changements d'apparence
        this.addMessageHandler('avatar_appearance', (data) => {
            console.log('Changement d\'apparence reçu:', data);
            this.notify('avatar_appearance', data);
        });
    }
    
    /**
     * Gère les messages reçus
     */
    handleMessage(message) {
        const { type, data } = message;
        
        if (this.messageHandlers.has(type)) {
            const handler = this.messageHandlers.get(type);
            try {
                handler(data);
            } catch (error) {
                console.error(`Erreur dans le gestionnaire de message '${type}':`, error);
            }
        } else {
            console.warn('Type de message non géré:', type);
        }
    }
    
    /**
     * Envoie un message au serveur
     */
    sendMessage(type, data) {
        if (!this.isConnected) {
            console.warn('Tentative d\'envoi de message sans connexion WebSocket');
            return false;
        }
        
        const message = {
            type: type,
            data: data,
            timestamp: Date.now()
        };
        
        try {
            this.socket.send(JSON.stringify(message));
            console.log('Message envoyé:', type, data);
            return true;
        } catch (error) {
            console.error('Erreur lors de l\'envoi du message:', error);
            return false;
        }
    }
    
    /**
     * Ajoute un gestionnaire de message
     */
    addMessageHandler(type, handler) {
        this.messageHandlers.set(type, handler);
    }
    
    /**
     * Supprime un gestionnaire de message
     */
    removeMessageHandler(type) {
        this.messageHandlers.delete(type);
    }
    
    /**
     * Ajoute un écouteur d'événement
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
    
    /**
     * Programme une tentative de reconnexion
     */
    scheduleReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('Nombre maximum de tentatives de reconnexion atteint');
            this.notify('max_reconnect_attempts_reached');
            return;
        }
        
        this.reconnectAttempts++;
        const delay = this.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1); // Backoff exponentiel
        
        console.log(`Tentative de reconnexion ${this.reconnectAttempts}/${this.maxReconnectAttempts} dans ${delay}ms`);
        
        setTimeout(() => {
            if (!this.isConnected) {
                this.connect();
            }
        }, delay);
    }
    
    /**
     * Ferme la connexion WebSocket
     */
    disconnect() {
        if (this.socket) {
            this.socket.close(1000, 'Déconnexion intentionnelle');
            this.socket = null;
        }
        this.isConnected = false;
    }
    
    /**
     * Vérifie si la connexion est active
     */
    isConnectedAndReady() {
        return this.isConnected && this.socket && this.socket.readyState === WebSocket.OPEN;
    }
    
    /**
     * Envoie un ping pour maintenir la connexion
     */
    ping() {
        this.sendMessage('ping', { timestamp: Date.now() });
    }
    
    /**
     * Démarre le maintien de connexion automatique
     */
    startKeepAlive(interval = 30000) {
        if (this.keepAliveInterval) {
            clearInterval(this.keepAliveInterval);
        }
        
        this.keepAliveInterval = setInterval(() => {
            if (this.isConnectedAndReady()) {
                this.ping();
            }
        }, interval);
    }
    
    /**
     * Arrête le maintien de connexion
     */
    stopKeepAlive() {
        if (this.keepAliveInterval) {
            clearInterval(this.keepAliveInterval);
            this.keepAliveInterval = null;
        }
    }
    
    /**
     * Obtient les statistiques de connexion
     */
    getConnectionStats() {
        return {
            connected: this.isConnected,
            reconnectAttempts: this.reconnectAttempts,
            readyState: this.socket ? this.socket.readyState : null,
            url: this.socket ? this.socket.url : null
        };
    }
}

// Export global
window.AvatarWebSocket = AvatarWebSocket;
/**
 * Gestionnaire WebSocket pour la communication avec le backend
 */
class AvatarWebSocketManager {
    constructor(avatarRenderer) {
        this.avatarRenderer = avatarRenderer;
        this.socket = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
        
        this.connect();
    }
    
    connect() {
        try {
            // URL WebSocket - adapter selon votre configuration
            const wsUrl = `ws://${window.location.host}/avatar-ws`;
            this.socket = new WebSocket(wsUrl);
            
            this.socket.onopen = () => {
                console.log('Connexion WebSocket avatar établie');
                this.reconnectAttempts = 0;
            };
            
            this.socket.onmessage = (event) => {
                this.handleMessage(event.data);
            };
            
            this.socket.onclose = () => {
                console.log('Connexion WebSocket avatar fermée');
                this.attemptReconnect();
            };
            
            this.socket.onerror = (error) => {
                console.error('Erreur WebSocket avatar:', error);
            };
            
        } catch (error) {
            console.error('Erreur de connexion WebSocket:', error);
            this.attemptReconnect();
        }
    }
    
    handleMessage(data) {
        try {
            const message = JSON.parse(data);
            
            switch (message.type) {
                case 'AVATAR_INIT':
                    this.handleAvatarInit(message);
                    break;
                case 'AVATAR_SPEAK':
                    this.handleAvatarSpeak(message);
                    break;
                case 'AVATAR_EMOTION':
                    this.handleAvatarEmotion(message);
                    break;
                case 'AVATAR_GESTURE':
                    this.handleAvatarGesture(message);
                    break;
                case 'AVATAR_VISIBILITY':
                    this.handleAvatarVisibility(message);
                    break;
                case 'AVATAR_APPEARANCE':
                    this.handleAvatarAppearance(message);
                    break;
                default:
                    console.warn('Type de message inconnu:', message.type);
            }
            
        } catch (error) {
            console.error('Erreur lors du traitement du message:', error);
        }
    }
    
    async handleAvatarInit(message) {
        const config = message.config;
        const modelPath = `/models/avatars/${config.model}.glb`;
        
        await this.avatarRenderer.loadAvatar(modelPath, config);
        this.avatarRenderer.setVisible(true);
    }
    
    handleAvatarSpeak(message) {
        this.avatarRenderer.speak({
            text: message.text,
            audioData: message.audioData,
            visemeData: message.visemeData,
            emotion: message.emotion,
            animationData: message.animationData
        });
    }
    
    handleAvatarEmotion(message) {
        this.avatarRenderer.setEmotion(
            message.emotion,
            message.intensity,
            message.transitionDuration
        );
    }
    
    handleAvatarGesture(message) {
        this.avatarRenderer.playGesture(message.gestureType);
    }
    
    handleAvatarVisibility(message) {
        this.avatarRenderer.setVisible(message.visible);
    }
    
    async handleAvatarAppearance(message) {
        await this.avatarRenderer.loadAvatar(message.modelPath);
    }
    
    attemptReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Tentative de reconnexion ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            
            setTimeout(() => {
                this.connect();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('Impossible de se reconnecter au WebSocket avatar');
        }
    }
    
    send(message) {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(JSON.stringify(message));
        }
    }
    
    dispose() {
        if (this.socket) {
            this.socket.close();
        }
    }
}

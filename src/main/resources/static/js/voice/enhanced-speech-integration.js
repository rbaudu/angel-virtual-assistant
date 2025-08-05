/**
 * Intégration de synthèse vocale améliorée pour Angel
 * Version optimisée qui corrige le problème de l'avatar qui ne parle pas
 * 
 * Fichier : src/main/resources/static/js/voice/enhanced-speech-integration.js
 */

class EnhancedSpeechIntegration {
    constructor() {
        this.currentUtterance = null;
        this.isSpeaking = false;
        this.isEnabled = true;
        this.speechQueue = [];
        this.isProcessingQueue = false;
        
        // Configuration avancée
        this.config = {
            lang: 'fr-FR',
            rate: 0.9,
            pitch: 1.0,
            volume: 0.8,
            maxRetries: 3,
            retryDelay: 1000
        };
        
        this.selectedVoice = null;
        this.voiceLoadAttempts = 0;
        this.maxVoiceLoadAttempts = 10;
        
        this.initialize();
        console.log('🎤 EnhancedSpeechIntegration initialisé');
    }
    
    /**
     * Initialise le service de synthèse vocale
     */
    async initialize() {
        if (!('speechSynthesis' in window)) {
            console.warn('⚠️ Speech Synthesis API non supportée');
            this.isEnabled = false;
            return;
        }
        
        // Initialiser les voix avec retry
        await this.initializeVoicesWithRetry();
        
        // Configurer les événements
        this.setupEventListeners();
        
        // Démarrer le processeur de queue
        this.startQueueProcessor();
        
        console.log('✅ EnhancedSpeechIntegration prêt');
    }
    
    /**
     * Initialise les voix avec retry automatique
     */
    async initializeVoicesWithRetry() {
        return new Promise((resolve) => {
            const attemptVoiceLoad = () => {
                const voices = speechSynthesis.getVoices();
                
                if (voices.length > 0) {
                    this.selectBestVoice(voices);
                    console.log('✅ Voix chargées avec succès');
                    resolve();
                    return;
                }
                
                this.voiceLoadAttempts++;
                if (this.voiceLoadAttempts >= this.maxVoiceLoadAttempts) {
                    console.warn('⚠️ Impossible de charger les voix, utilisation voix par défaut');
                    resolve();
                    return;
                }
                
                console.log(`🔄 Tentative ${this.voiceLoadAttempts}/${this.maxVoiceLoadAttempts} de chargement des voix`);
                setTimeout(attemptVoiceLoad, 500);
            };
            
            // Événement voiceschanged avec timeout
            const voicesChangedHandler = () => {
                attemptVoiceLoad();
                speechSynthesis.removeEventListener('voiceschanged', voicesChangedHandler);
            };
            
            speechSynthesis.addEventListener('voiceschanged', voicesChangedHandler);
            
            // Première tentative immédiate
            attemptVoiceLoad();
        });
    }
    
    /**
     * Sélectionne la meilleure voix disponible
     */
    selectBestVoice(voices) {
        console.log(`🎵 ${voices.length} voix disponibles`);
        
        // Préférences de voix par ordre de priorité
        const voicePreferences = [
            // Voix françaises féminines spécifiques
            { lang: 'fr-FR', gender: 'female', names: ['Alice', 'Marie', 'Hortense', 'Amélie'] },
            // Voix françaises génériques
            { lang: 'fr-FR', gender: 'female', names: [] },
            { lang: 'fr-CA', gender: 'female', names: [] },
            { lang: 'fr', gender: 'female', names: [] },
            // Fallback vers n'importe quelle voix française
            { lang: 'fr-FR', gender: null, names: [] },
            { lang: 'fr', gender: null, names: [] }
        ];
        
        for (const pref of voicePreferences) {
            let voice = null;
            
            // Chercher d'abord par nom spécifique
            if (pref.names.length > 0) {
                voice = voices.find(v => 
                    v.lang.startsWith(pref.lang) &&
                    pref.names.some(name => v.name.toLowerCase().includes(name.toLowerCase()))
                );
            }
            
            // Puis par genre et langue
            if (!voice && pref.gender) {
                voice = voices.find(v => 
                    v.lang.startsWith(pref.lang) &&
                    (v.name.toLowerCase().includes(pref.gender) ||
                     v.name.toLowerCase().includes('female') ||
                     v.name.toLowerCase().includes('femme'))
                );
            }
            
            // Enfin par langue seulement
            if (!voice) {
                voice = voices.find(v => v.lang.startsWith(pref.lang));
            }
            
            if (voice) {
                this.selectedVoice = voice;
                console.log(`🎤 Voix sélectionnée: ${voice.name} (${voice.lang})`);
                return;
            }
        }
        
        // Fallback vers la première voix disponible
        if (voices.length > 0) {
            this.selectedVoice = voices[0];
            console.log(`🎤 Voix fallback: ${voices[0].name} (${voices[0].lang})`);
        }
    }
    
    /**
     * Configure les écouteurs d'événements
     */
    setupEventListeners() {
        // Intercepter les messages WebSocket pour synthèse vocale
        document.addEventListener('angelWebSocketMessage', (event) => {
            this.handleWebSocketMessage(event.detail);
        });
        
        // Fallback pour interception directe des WebSockets
        setTimeout(() => {
            this.interceptWebSocketMessages();
        }, 2000);
    }
    
    /**
     * Intercepte les messages WebSocket existants
     */
    interceptWebSocketMessages() {
        // Chercher les WebSockets possibles
        const possibleSockets = [
            window.voiceWebSocket,
            window.wakeWordDetector?.websocket,
            window.websocket,
            window.ws
        ];
        
        for (const socket of possibleSockets) {
            if (socket && socket.readyState === WebSocket.OPEN) {
                console.log('🔌 WebSocket trouvé pour interception');
                
                const originalOnMessage = socket.onmessage;
                socket.onmessage = (event) => {
                    const handled = this.handleWebSocketMessage(event.data);
                    if (!handled && originalOnMessage) {
                        originalOnMessage(event);
                    }
                };
                
                return;
            }
        }
        
        // Réessayer plus tard
        setTimeout(() => this.interceptWebSocketMessages(), 2000);
    }
    
    /**
     * Traite les messages WebSocket pour synthèse vocale
     */
    handleWebSocketMessage(data) {
        try {
            let message;
            if (typeof data === 'string') {
                message = JSON.parse(data);
            } else {
                message = data;
            }
            
            console.log('📨 Message WebSocket reçu pour TTS:', message.type);
            
            if (message.type === 'AVATAR_SPEAK' && message.text) {
                console.log('🎯 DÉCLENCHEMENT SYNTHÈSE VOCALE DÉTECTÉ:', message.text);
                this.speakWithQueue(message.text, message.emotion || 'neutral');
                return true;
            }
            
            return false;
            
        } catch (error) {
            console.error('❌ Erreur traitement message WebSocket pour TTS:', error);
            return false;
        }
    }
    
    /**
     * Ajoute un texte à la queue de synthèse vocale
     */
    speakWithQueue(text, emotion = 'neutral', priority = false) {
        if (!this.isEnabled || !text || text.trim() === '') {
            console.log('⏸️ Synthèse vocale désactivée ou texte vide');
            return Promise.resolve();
        }
        
        const speechItem = {
            text: text.trim(),
            emotion,
            timestamp: Date.now(),
            retries: 0,
            id: Math.random().toString(36).substr(2, 9)
        };
        
        if (priority) {
            this.speechQueue.unshift(speechItem);
        } else {
            this.speechQueue.push(speechItem);
        }
        
        console.log(`📝 Ajouté à la queue TTS (${this.speechQueue.length} éléments):`, text);
        
        return new Promise((resolve, reject) => {
            speechItem.resolve = resolve;
            speechItem.reject = reject;
        });
    }
    
    /**
     * Démarre le processeur de queue
     */
    startQueueProcessor() {
        if (this.isProcessingQueue) return;
        
        this.isProcessingQueue = true;
        
        const processQueue = async () => {
            while (this.speechQueue.length > 0 && this.isEnabled) {
                const item = this.speechQueue.shift();
                
                try {
                    await this.speakNow(item.text, item.emotion);
                    if (item.resolve) item.resolve();
                    
                } catch (error) {
                    console.error(`❌ Erreur synthèse vocale pour "${item.text}":`, error);
                    
                    if (item.retries < this.config.maxRetries) {
                        item.retries++;
                        console.log(`🔄 Retry ${item.retries}/${this.config.maxRetries}`);
                        this.speechQueue.unshift(item); // Remettre en début de queue
                        await this.delay(this.config.retryDelay);
                    } else {
                        console.error('❌ Échec définitif de synthèse:', error);
                        if (item.reject) item.reject(error);
                    }
                }
                
                // Petite pause entre les éléments
                await this.delay(200);
            }
            
            this.isProcessingQueue = false;
            
            // Redémarrer le processeur s'il y a de nouveaux éléments
            if (this.speechQueue.length > 0) {
                setTimeout(() => this.startQueueProcessor(), 100);
            }
        };
        
        processQueue();
    }
    
    /**
     * Synthèse vocale immédiate
     */
    async speakNow(text, emotion = 'neutral') {
        return new Promise((resolve, reject) => {
            try {
                // Arrêter la synthèse en cours
                this.stopSpeaking();
                
                console.log(`🗣️ SYNTHÈSE IMMÉDIATE (${emotion}):`, text);
                
                // Créer l'utterance
                this.currentUtterance = new SpeechSynthesisUtterance(text);
                
                // Configuration de la voix
                if (this.selectedVoice) {
                    this.currentUtterance.voice = this.selectedVoice;
                }
                
                this.currentUtterance.lang = this.config.lang;
                this.currentUtterance.rate = this.getEmotionRate(emotion);
                this.currentUtterance.pitch = this.getEmotionPitch(emotion);
                this.currentUtterance.volume = this.config.volume;
                
                // Gestionnaires d'événements
                this.currentUtterance.onstart = () => {
                    this.isSpeaking = true;
                    console.log('▶️ Synthèse vocale démarrée');
                    this.updateUI(true);
                };
                
                this.currentUtterance.onend = () => {
                    this.isSpeaking = false;
                    console.log('⏹️ Synthèse vocale terminée');
                    this.updateUI(false);
                    resolve();
                };
                
                this.currentUtterance.onerror = (event) => {
                    this.isSpeaking = false;
                    console.error('❌ Erreur synthèse vocale:', event.error);
                    this.updateUI(false);
                    reject(new Error(`Erreur TTS: ${event.error}`));
                };
                
                // Démarrer la synthèse
                speechSynthesis.speak(this.currentUtterance);
                
                // Vérification de démarrage
                setTimeout(() => {
                    if (!this.isSpeaking && !speechSynthesis.speaking) {
                        console.warn('⚠️ La synthèse ne semble pas avoir démarré');
                        reject(new Error('Synthèse non démarrée'));
                    }
                }, 500);
                
            } catch (error) {
                console.error('❌ Erreur création utterance:', error);
                reject(error);
            }
        });
    }
    
    /**
     * Arrête la synthèse vocale
     */
    stopSpeaking() {
        if (this.isSpeaking || speechSynthesis.speaking) {
            speechSynthesis.cancel();
            this.isSpeaking = false;
            this.updateUI(false);
            console.log('⏸️ Synthèse vocale arrêtée');
        }
        
        // Vider la queue
        this.speechQueue.length = 0;
    }
    
    /**
     * Adapte le débit selon l'émotion
     */
    getEmotionRate(emotion) {
        const rates = {
            'excited': 1.2,
            'happy': 1.1,
            'friendly': 1.0,
            'neutral': 0.9,
            'informative': 0.85,
            'attentive': 0.85,
            'helpful': 0.9,
            'sad': 0.7,
            'concerned': 0.8,
            'thoughtful': 0.8,
            'apologetic': 0.75
        };
        
        return rates[emotion] || this.config.rate;
    }
    
    /**
     * Adapte la hauteur selon l'émotion
     */
    getEmotionPitch(emotion) {
        const pitches = {
            'excited': 1.3,
            'happy': 1.2,
            'friendly': 1.1,
            'neutral': 1.0,
            'informative': 0.95,
            'attentive': 0.95,
            'helpful': 1.0,
            'sad': 0.8,
            'concerned': 0.85,
            'thoughtful': 0.9,
            'apologetic': 0.85
        };
        
        return pitches[emotion] || this.config.pitch;
    }
    
    /**
     * Met à jour l'interface utilisateur
     */
    updateUI(isSpeaking) {
        // Avatar parlant
        const avatarElements = document.querySelectorAll('#avatar-container, .avatar-container, [data-avatar], #avatar');
        avatarElements.forEach(element => {
            if (isSpeaking) {
                element.classList.add('speaking');
            } else {
                element.classList.remove('speaking');
            }
        });
        
        // Événement personnalisé
        const event = new CustomEvent('angelSpeechStateChanged', {
            detail: { isSpeaking, timestamp: Date.now() }
        });
        document.dispatchEvent(event);
    }
    
    /**
     * Fonction utilitaire de délai
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
    
    /**
     * Test de synthèse vocale
     */
    testSpeech() {
        console.log('🧪 Test de synthèse vocale améliorée...');
        return this.speakWithQueue('Test de synthèse vocale améliorée. Vous m\'entendez bien ?', 'neutral');
    }
    
    /**
     * Diagnostic du système
     */
    getDiagnostics() {
        return {
            isEnabled: this.isEnabled,
            isSpeaking: this.isSpeaking,
            selectedVoice: this.selectedVoice ? {
                name: this.selectedVoice.name,
                lang: this.selectedVoice.lang
            } : null,
            queueLength: this.speechQueue.length,
            isProcessingQueue: this.isProcessingQueue,
            voiceLoadAttempts: this.voiceLoadAttempts,
            speechSynthesisState: {
                speaking: speechSynthesis.speaking,
                pending: speechSynthesis.pending,
                paused: speechSynthesis.paused
            }
        };
    }
    
    /**
     * Nettoie les ressources
     */
    destroy() {
        this.stopSpeaking();
        this.speechQueue.length = 0;
        this.isProcessingQueue = false;
        console.log('🧹 EnhancedSpeechIntegration nettoyé');
    }
}

// Initialisation automatique
let enhancedSpeechIntegration = null;

function initializeEnhancedSpeechIntegration() {
    if (enhancedSpeechIntegration) {
        console.log('🔄 EnhancedSpeechIntegration déjà initialisé');
        return enhancedSpeechIntegration;
    }
    
    enhancedSpeechIntegration = new EnhancedSpeechIntegration();
    window.enhancedSpeechIntegration = enhancedSpeechIntegration;
    
    // Compatibilité avec l'ancienne API
    window.avatarSpeech = {
        speak: (text, emotion) => enhancedSpeechIntegration.speakWithQueue(text, emotion),
        testSpeech: () => enhancedSpeechIntegration.testSpeech(),
        stopSpeaking: () => enhancedSpeechIntegration.stopSpeaking()
    };
    
    console.log('✅ EnhancedSpeechIntegration disponible globalement');
    return enhancedSpeechIntegration;
}

// Initialisation
document.addEventListener('DOMContentLoaded', () => {
    setTimeout(initializeEnhancedSpeechIntegration, 1000);
});

if (document.readyState !== 'loading') {
    setTimeout(initializeEnhancedSpeechIntegration, 1000);
}

// Nettoyage
window.addEventListener('beforeunload', () => {
    if (enhancedSpeechIntegration) {
        enhancedSpeechIntegration.destroy();
    }
});

console.log('📜 enhanced-speech-integration.js chargé');
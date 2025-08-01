/**
 * Extension de synthèse vocale pour Angel - Intégration directe
 * S'intègre via les événements personnalisés du WakeWordDetector
 */

class AvatarSpeech {
    constructor() {
        this.currentUtterance = null;
        this.isSpeaking = false;
        this.isEnabled = true;
        
        // Configuration TTS
        this.voiceConfig = {
            lang: 'fr-FR',
            rate: 0.9,
            pitch: 1.0,
            volume: 0.8
        };
        
        this.selectedVoice = null;
        this.initializeTTS();
        this.setupEventListeners();
        
        console.log('🎤 AvatarSpeech initialisé (intégration directe)');
    }
    
    /**
     * Initialise la synthèse vocale
     */
    initializeTTS() {
        if ('speechSynthesis' in window) {
            // Forcer le chargement des voix
            speechSynthesis.getVoices();
            
            // Attendre que les voix soient chargées
            const voicesChangedHandler = () => {
                this.selectBestVoice();
                speechSynthesis.removeEventListener('voiceschanged', voicesChangedHandler);
            };
            
            if (speechSynthesis.getVoices().length === 0) {
                speechSynthesis.addEventListener('voiceschanged', voicesChangedHandler);
            } else {
                this.selectBestVoice();
            }
            
            console.log('✅ Speech Synthesis API disponible');
        } else {
            console.warn('⚠️ Speech Synthesis API non supportée');
            this.isEnabled = false;
        }
    }
    
    /**
     * Configure les écouteurs d'événements
     */
    setupEventListeners() {
        // Écouter les messages WebSocket via des événements personnalisés
        document.addEventListener('angelWebSocketMessage', (event) => {
            this.handleWebSocketMessage(event.detail);
        });
        
        // Fallback : intercepter directement les messages WebSocket
        setTimeout(() => {
            this.interceptWebSocketMessages();
        }, 2000);
    }
    
    /**
     * Intercepte les messages WebSocket existants
     */
    interceptWebSocketMessages() {
        // Chercher le WebSocket dans différents endroits
        const possibleSockets = [
            window.voiceWebSocket,
            window.wakeWordDetector?.websocket,
            window.websocket,
            window.ws
        ];
        
        for (const socket of possibleSockets) {
            if (socket && socket.readyState === WebSocket.OPEN) {
                console.log('🔌 WebSocket trouvé, interception des messages');
                
                // Sauvegarder l'ancien gestionnaire
                const originalOnMessage = socket.onmessage;
                
                // Remplacer par notre gestionnaire
                socket.onmessage = (event) => {
                    // D'abord traiter pour la synthèse vocale
                    const handled = this.handleWebSocketMessage(event.data);
                    
                    // Si pas traité, utiliser l'ancien gestionnaire
                    if (!handled && originalOnMessage) {
                        originalOnMessage(event);
                    }
                };
                
                console.log('✅ Interception WebSocket configurée');
                return;
            }
        }
        
        console.log('🔍 Aucun WebSocket trouvé, nouvelle tentative dans 2s...');
        setTimeout(() => this.interceptWebSocketMessages(), 2000);
    }
    
    /**
     * Traite les messages WebSocket
     */
    handleWebSocketMessage(data) {
        try {
            let message;
            if (typeof data === 'string') {
                message = JSON.parse(data);
            } else {
                message = data;
            }
            
            console.log('📨 Message reçu:', message.type);
            
            switch (message.type) {
                case 'AVATAR_SPEAK':
                    if (message.text) {
                        console.log('🎯 DÉCLENCHEMENT TTS:', message.text);
                        this.speak(message.text, message.emotion || 'neutral');
                        return true; // Message traité
                    }
                    break;
                    
                case 'AVATAR_STOP_SPEAKING':
                    this.stopSpeaking();
                    return true;
                    
                case 'ai_response':
                    if (message.data && message.data.response) {
                        console.log('🤖 Réponse IA:', message.data.response);
                        this.speak(message.data.response, 'friendly');
                        return true;
                    }
                    break;
            }
            
            return false; // Message non traité
            
        } catch (error) {
            console.error('❌ Erreur traitement message:', error);
            return false;
        }
    }
    
    /**
     * Sélectionne la meilleure voix française disponible
     */
    selectBestVoice() {
        const voices = speechSynthesis.getVoices();
        console.log('🎵 Voix disponibles:', voices.length);
        
        if (voices.length === 0) {
            console.warn('⚠️ Aucune voix disponible');
            return;
        }
        
        // Log des voix françaises pour debug
        const frenchVoices = voices.filter(voice => voice.lang.startsWith('fr'));
        console.log('🇫🇷 Voix françaises:', frenchVoices.map(v => `${v.name} (${v.lang})`));
        
        // Préférer les voix françaises féminines
        let preferredVoice = voices.find(voice => 
            voice.lang.startsWith('fr') && 
            (voice.name.toLowerCase().includes('female') || 
             voice.name.toLowerCase().includes('femme') ||
             voice.name.toLowerCase().includes('marie') ||
             voice.name.toLowerCase().includes('alice') ||
             voice.name.toLowerCase().includes('hortense'))
        );
        
        // Si pas trouvé, chercher n'importe quelle voix française
        if (!preferredVoice) {
            preferredVoice = voices.find(voice => voice.lang.startsWith('fr'));
        }
        
        // Si toujours pas trouvé, utiliser la première voix disponible
        if (!preferredVoice) {
            preferredVoice = voices[0];
        }
        
        this.selectedVoice = preferredVoice;
        console.log('🎤 Voix sélectionnée:', preferredVoice?.name || 'Aucune', preferredVoice?.lang || '');
    }
    
    /**
     * Fait parler l'avatar avec un texte donné
     */
    async speak(text, emotion = 'neutral') {
        if (!this.isEnabled || !text || text.trim() === '') {
            console.log('⏸️ Synthèse vocale désactivée ou texte vide');
            return;
        }
        
        return new Promise((resolve, reject) => {
            try {
                // Arrêter la synthèse en cours
                this.stopSpeaking();
                
                console.log(`🗣️ DÉBUT SYNTHÈSE (${emotion}):`, text);
                
                // Créer l'utterance
                this.currentUtterance = new SpeechSynthesisUtterance(text);
                
                // Configurer la voix et les paramètres
                if (this.selectedVoice) {
                    this.currentUtterance.voice = this.selectedVoice;
                    console.log('🎤 Utilisation de la voix:', this.selectedVoice.name);
                }
                
                this.currentUtterance.lang = this.voiceConfig.lang;
                this.currentUtterance.rate = this.getEmotionRate(emotion);
                this.currentUtterance.pitch = this.getEmotionPitch(emotion);
                this.currentUtterance.volume = this.voiceConfig.volume;
                
                console.log('⚙️ Config TTS:', {
                    lang: this.currentUtterance.lang,
                    rate: this.currentUtterance.rate,
                    pitch: this.currentUtterance.pitch,
                    volume: this.currentUtterance.volume,
                    voice: this.selectedVoice?.name
                });
                
                // Gestionnaires d'événements
                this.currentUtterance.onstart = () => {
                    this.isSpeaking = true;
                    console.log('▶️ DÉBUT SYNTHÈSE VOCALE CONFIRMÉ');
                    this.updateUI(true);
                };
                
                this.currentUtterance.onend = () => {
                    this.isSpeaking = false;
                    console.log('⏹️ FIN SYNTHÈSE VOCALE');
                    this.updateUI(false);
                    resolve();
                };
                
                this.currentUtterance.onerror = (event) => {
                    this.isSpeaking = false;
                    console.error('❌ ERREUR SYNTHÈSE VOCALE:', event.error, event);
                    this.updateUI(false);
                    reject(new Error(`Erreur TTS: ${event.error}`));
                };
                
                // Démarrer la synthèse
                console.log('🚀 Lancement speechSynthesis.speak()...');
                speechSynthesis.speak(this.currentUtterance);
                
                // Vérifier que la synthèse a bien démarré
                setTimeout(() => {
                    if (!this.isSpeaking) {
                        console.warn('⚠️ La synthèse ne semble pas avoir démarré');
                        console.log('📊 État speechSynthesis:', {
                            speaking: speechSynthesis.speaking,
                            pending: speechSynthesis.pending,
                            paused: speechSynthesis.paused
                        });
                    }
                }, 100);
                
            } catch (error) {
                console.error('❌ Erreur lors du démarrage de la synthèse:', error);
                reject(error);
            }
        });
    }
    
    /**
     * Arrête la synthèse vocale en cours
     */
    stopSpeaking() {
        if (this.isSpeaking || speechSynthesis.speaking) {
            speechSynthesis.cancel();
            this.isSpeaking = false;
            this.updateUI(false);
            console.log('⏸️ Synthèse vocale arrêtée');
        }
    }
    
    /**
     * Adapte le débit selon l'émotion
     */
    getEmotionRate(emotion) {
        const rates = {
            'excited': 1.1,
            'happy': 1.0,
            'friendly': 0.95,
            'neutral': 0.9,
            'informative': 0.85,
            'attentive': 0.85,
            'helpful': 0.9,
            'sad': 0.7,
            'concerned': 0.8,
            'thoughtful': 0.8,
            'apologetic': 0.8
        };
        
        return rates[emotion] || this.voiceConfig.rate;
    }
    
    /**
     * Adapte la hauteur selon l'émotion
     */
    getEmotionPitch(emotion) {
        const pitches = {
            'excited': 1.2,
            'happy': 1.1,
            'friendly': 1.05,
            'neutral': 1.0,
            'informative': 0.95,
            'attentive': 0.95,
            'helpful': 1.0,
            'sad': 0.8,
            'concerned': 0.9,
            'thoughtful': 0.95,
            'apologetic': 0.9
        };
        
        return pitches[emotion] || this.voiceConfig.pitch;
    }
    
    /**
     * Met à jour l'interface utilisateur
     */
    updateUI(isSpeaking) {
        // Ajouter/supprimer une classe CSS pour indiquer que l'avatar parle
        const avatarElement = document.querySelector('#avatar-container, .avatar-container, [data-avatar], #avatar');
        if (avatarElement) {
            if (isSpeaking) {
                avatarElement.classList.add('speaking');
            } else {
                avatarElement.classList.remove('speaking');
            }
        }
        
        // Mettre à jour les indicateurs
        const indicators = document.querySelectorAll('.speech-indicator, .avatar-status');
        indicators.forEach(element => {
            if (isSpeaking) {
                element.style.display = 'block';
                element.textContent = 'Angel parle...';
            } else {
                element.style.display = 'none';
            }
        });
        
        // Émettre un événement personnalisé
        const event = new CustomEvent('avatarSpeechStateChanged', {
            detail: { isSpeaking, timestamp: Date.now() }
        });
        document.dispatchEvent(event);
    }
    
    /**
     * Test de synthèse vocale
     */
    testSpeech() {
        console.log('🧪 Test de synthèse vocale...');
        this.speak('Test de synthèse vocale. Est-ce que vous m\'entendez ?', 'neutral');
    }
}

// Initialisation automatique
let avatarSpeech = null;

function initializeAvatarSpeech() {
    if (avatarSpeech) {
        console.log('🔄 AvatarSpeech déjà initialisé');
        return avatarSpeech;
    }
    
    avatarSpeech = new AvatarSpeech();
    window.avatarSpeech = avatarSpeech;
    
    // Ajouter les indicateurs visuels
    addSpeechIndicators();
    
    console.log('✅ AvatarSpeech initialisé et disponible globalement');
    console.log('💡 Utilisez avatarSpeech.testSpeech() pour tester');
    
    return avatarSpeech;
}

/**
 * Ajouter des indicateurs visuels
 */
function addSpeechIndicators() {
    if (document.querySelector('#speech-indicator')) {
        return;
    }
    
    const indicatorHTML = `
        <div id="speech-indicator" class="speech-indicator" style="display:none; position:fixed; bottom:20px; right:20px; background:rgba(76,175,80,0.9); color:white; padding:10px 15px; border-radius:25px; z-index:1000; font-size:14px; box-shadow:0 2px 10px rgba(0,0,0,0.2);">
            <span>🗣️ Angel parle...</span>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', indicatorHTML);
    
    // CSS pour l'avatar qui parle
    if (!document.querySelector('#avatar-speech-css')) {
        const style = document.createElement('style');
        style.id = 'avatar-speech-css';
        style.textContent = `
            .avatar-container.speaking,
            #avatar.speaking {
                border: 2px solid #4CAF50 !important;
                box-shadow: 0 0 20px rgba(76, 175, 80, 0.3) !important;
                transition: all 0.3s ease;
            }
        `;
        document.head.appendChild(style);
    }
}

// Initialisation
document.addEventListener('DOMContentLoaded', () => {
    setTimeout(initializeAvatarSpeech, 500);
});

// Si le DOM est déjà chargé
if (document.readyState !== 'loading') {
    setTimeout(initializeAvatarSpeech, 500);
}

console.log('📜 avatar-speech-direct.js chargé');
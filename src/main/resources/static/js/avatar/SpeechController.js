/**
 * Contrôleur de la synthèse et lecture vocale
 */
class SpeechController {
    constructor() {
        this.audioContext = null;
        this.currentAudio = null;
        this.isPlaying = false;
        
        // Initialiser le contexte audio
        this.initAudioContext();
    }
    
    initAudioContext() {
        try {
            this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
        } catch (error) {
            console.warn('AudioContext non supporté:', error);
        }
    }
    
    async playAudio(audioData) {
        if (!audioData || audioData.length === 0) {
            console.warn('Pas de données audio à jouer');
            return;
        }
        
        try {
            // Arrêter l'audio précédent
            this.stopAudio();
            
            this.isPlaying = true;
            
            // Pour une vraie implémentation, décoder les données audio
            // et les jouer via Web Audio API ou HTML5 Audio
            
            // Simulation de lecture audio
            const duration = this.estimateAudioDuration(audioData);
            
            return new Promise((resolve) => {
                setTimeout(() => {
                    this.isPlaying = false;
                    resolve();
                }, duration);
            });
            
        } catch (error) {
            console.error('Erreur lors de la lecture audio:', error);
            this.isPlaying = false;
            throw error;
        }
    }
    
    estimateAudioDuration(audioData) {
        // Estimation basique - remplacer par la vraie durée
        return Math.max(1000, audioData.length / 100);
    }
    
    stopAudio() {
        if (this.currentAudio) {
            try {
                this.currentAudio.stop();
            } catch (error) {
                // Ignore les erreurs d'arrêt
            }
            this.currentAudio = null;
        }
        this.isPlaying = false;
    }
    
    setVolume(volume) {
        // Ajuster le volume global
        if (this.audioContext && this.audioContext.destination) {
            // Implémentation du contrôle de volume
        }
    }
    
    dispose() {
        this.stopAudio();
        if (this.audioContext) {
            this.audioContext.close();
        }
    }
}

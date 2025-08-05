/**
 * Service de reconnaissance vocale utilisant l'API Web Speech Recognition.
 * Gère la détection du mot-clé et la capture de commandes vocales.
 */
class SpeechRecognitionService {
    constructor() {
        this.recognition = null;
        this.isListening = false;
        this.isWakeWordMode = true;
        this.config = {
            wakeWord: 'Angèle',
            language: 'fr-FR',
            continuous: true,
            confidenceThreshold: 0.7
        };
        this.callbacks = {};
        this.restartAttempts = 0;
        this.maxRestartAttempts = 3;
        this.restartDelay = 1000;
        
        // Statistiques pour calculer la confidence alternative
        this.recognitionStats = {
            totalAttempts: 0,
            successfulDetections: 0,
            lastDetectionTime: null
        };
        
        this.initializeRecognition();
    }
    
    /**
     * Initialise la reconnaissance vocale avec gestion améliorée de la confidence.
     */
    initializeRecognition() {
        if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
            console.error('La reconnaissance vocale n\'est pas supportée par ce navigateur');
            this.triggerCallback('error', 'Reconnaissance vocale non supportée');
            return;
        }
        
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        this.recognition = new SpeechRecognition();
        
        // Configuration optimisée pour Chrome
        this.recognition.continuous = this.config.continuous;
        this.recognition.interimResults = true;
        this.recognition.lang = this.config.language;
        this.recognition.maxAlternatives = 3; // Augmenter pour avoir plus d'alternatives
        
        // Événements
        this.recognition.onstart = () => {
            console.log('🎤 Reconnaissance vocale démarrée');
            this.isListening = true;
            this.triggerCallback('start');
        };
        
        this.recognition.onresult = (event) => {
            this.handleResults(event);
        };
        
        this.recognition.onerror = (event) => {
            console.error('❌ Erreur de reconnaissance vocale:', event.error);
            this.handleError(event);
        };
        
        this.recognition.onend = () => {
            console.log('🔇 Reconnaissance vocale terminée');
            this.isListening = false;
            this.triggerCallback('end');
            
            // Redémarrage automatique en mode wake word
            if (this.isWakeWordMode && this.restartAttempts < this.maxRestartAttempts) {
                setTimeout(() => {
                    this.startListening();
                    this.restartAttempts++;
                }, this.restartDelay);
            }
        };
    }
    
    /**
     * Traite les résultats de reconnaissance avec correction de confidence.
     */
    handleResults(event) {
        let finalTranscript = '';
        let interimTranscript = '';
        let bestConfidence = 0;
        let alternativeResults = [];
        
        for (let i = event.resultIndex; i < event.results.length; i++) {
            const result = event.results[i];
            const transcript = result[0].transcript;
            let confidence = result[0].confidence;
            
            // CORRECTION DU BUG CONFIDENCE = 0
            if (confidence === 0 || confidence === undefined || confidence === null) {
                confidence = this.calculateAlternativeConfidence(result, transcript, i);
                console.log(`🔧 Confidence corrigée: ${confidence} pour "${transcript}"`);
            }
            
            // Collecter les alternatives si disponibles
            if (result.length > 1) {
                for (let j = 0; j < Math.min(result.length, 3); j++) {
                    alternativeResults.push({
                        transcript: result[j].transcript,
                        confidence: result[j].confidence || this.calculateAlternativeConfidence(result, result[j].transcript, i)
                    });
                }
            }
            
            if (result.isFinal) {
                finalTranscript += transcript;
                bestConfidence = Math.max(bestConfidence, confidence);
            } else {
                interimTranscript += transcript;
            }
        }
        
        // Log détaillé pour debug
        if (this.config.debugMode) {
            console.log('🔍 Résultats reconnaissance:', {
                final: finalTranscript,
                interim: interimTranscript,
                confidence: bestConfidence,
                alternatives: alternativeResults
            });
        }
        
        // Traitement du texte final
        if (finalTranscript) {
            this.processTranscript(finalTranscript.trim(), bestConfidence, alternativeResults);
        }
        
        // Traitement du texte temporaire
        if (interimTranscript) {
            this.triggerCallback('interim', interimTranscript.trim());
        }
    }
    
    /**
     * Calcule une confidence alternative quand l'API retourne 0.
     */
    calculateAlternativeConfidence(result, transcript, resultIndex) {
        let confidence = 0.5; // Valeur par défaut raisonnable
        
        try {
            // Méthode 1: Basée sur la longueur et la clarté du transcript
            const transcriptLength = transcript.trim().length;
            if (transcriptLength > 0) {
                // Plus le transcript est long et cohérent, plus on a confiance
                confidence = Math.min(0.4 + (transcriptLength * 0.02), 0.9);
            }
            
            // Méthode 2: Basée sur le nombre d'alternatives
            if (result.length > 1) {
                // S'il y a plusieurs alternatives similaires, augmenter la confidence
                const firstTranscript = result[0].transcript.toLowerCase().trim();
                let similarAlternatives = 0;
                
                for (let i = 1; i < Math.min(result.length, 3); i++) {
                    const altTranscript = result[i].transcript.toLowerCase().trim();
                    if (this.areSimilar(firstTranscript, altTranscript)) {
                        similarAlternatives++;
                    }
                }
                
                if (similarAlternatives > 0) {
                    confidence = Math.min(confidence + (similarAlternatives * 0.1), 0.95);
                }
            }
            
            // Méthode 3: Basée sur l'historique des détections
            if (this.isWakeWordCandidate(transcript)) {
                // Bonus pour les mots-clés attendus
                confidence = Math.min(confidence + 0.2, 0.9);
                
                // Bonus temporel si détection récente
                const now = Date.now();
                if (this.recognitionStats.lastDetectionTime && 
                    (now - this.recognitionStats.lastDetectionTime) < 5000) {
                    confidence = Math.min(confidence + 0.1, 0.95);
                }
            }
            
            // Méthode 4: Basée sur la position du résultat
            if (resultIndex === 0) {
                // Premier résultat = plus fiable
                confidence = Math.min(confidence + 0.1, 0.9);
            }
            
            // Assurer que la confidence reste dans les limites raisonnables
            confidence = Math.max(0.3, Math.min(0.95, confidence));
            
        } catch (error) {
            console.warn('⚠️ Erreur calcul confidence alternative:', error);
            confidence = 0.6; // Valeur de secours
        }
        
        return Math.round(confidence * 100) / 100; // Arrondir à 2 décimales
    }
    
    /**
     * Vérifie si deux transcripts sont similaires.
     */
    areSimilar(text1, text2) {
        const similarity = this.calculateStringSimilarity(text1, text2);
        return similarity > 0.7;
    }
    
    /**
     * Calcule la similarité entre deux chaînes.
     */
    calculateStringSimilarity(str1, str2) {
        const longer = str1.length > str2.length ? str1 : str2;
        const shorter = str1.length > str2.length ? str2 : str1;
        
        if (longer.length === 0) return 1.0;
        
        const distance = this.levenshteinDistance(longer, shorter);
        return (longer.length - distance) / longer.length;
    }
    
    /**
     * Calcule la distance de Levenshtein entre deux chaînes.
     */
    levenshteinDistance(str1, str2) {
        const matrix = [];
        
        for (let i = 0; i <= str2.length; i++) {
            matrix[i] = [i];
        }
        
        for (let j = 0; j <= str1.length; j++) {
            matrix[0][j] = j;
        }
        
        for (let i = 1; i <= str2.length; i++) {
            for (let j = 1; j <= str1.length; j++) {
                if (str2.charAt(i - 1) === str1.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                } else {
                    matrix[i][j] = Math.min(
                        matrix[i - 1][j - 1] + 1,
                        matrix[i][j - 1] + 1,
                        matrix[i - 1][j] + 1
                    );
                }
            }
        }
        
        return matrix[str2.length][str1.length];
    }
    
    /**
     * Vérifie si le transcript est un candidat wake word.
     */
    isWakeWordCandidate(transcript) {
        const normalizedTranscript = transcript.toLowerCase().trim();
        const normalizedWakeWord = this.config.wakeWord.toLowerCase();
        
        // Vérification exacte
        if (normalizedTranscript.includes(normalizedWakeWord)) {
            return true;
        }
        
        // Vérification avec variantes
        const variants = [
            'angel', 'anjel', 'ange', 'angie',
            'angela', 'andel', 'anchel'
        ];
        
        return variants.some(variant => normalizedTranscript.includes(variant));
    }
    
    /**
     * Met à jour la configuration.
     */
    updateConfig(newConfig) {
        this.config = { ...this.config, ...newConfig };
        
        if (this.recognition) {
            this.recognition.lang = this.config.language;
            this.recognition.continuous = this.config.continuous;
        }
        
        console.log('⚙️ Configuration mise à jour:', this.config);
    }
    
    /**
     * Démarre l'écoute.
     */
    startListening() {
        if (!this.recognition) {
            console.error('❌ Reconnaissance vocale non initialisée');
            return;
        }
        
        if (this.isListening) {
            console.log('⚠️ Reconnaissance déjà en cours');
            return;
        }
        
        try {
            this.recognitionStats.totalAttempts++;
            this.recognition.start();
        } catch (error) {
            console.error('❌ Erreur lors du démarrage:', error);
            this.triggerCallback('error', error.message);
        }
    }
    
    /**
     * Arrête l'écoute.
     */
    stopListening() {
        if (this.recognition && this.isListening) {
            this.recognition.stop();
        }
    }
    
    /**
     * Passe en mode commande.
     */
    startCommandMode() {
        this.isWakeWordMode = false;
        this.recognition.continuous = false;
        this.stopListening();
        
        setTimeout(() => {
            this.startListening();
        }, 100);
    }
    
    /**
     * Retourne en mode wake word.
     */
    returnToWakeWordMode() {
        this.isWakeWordMode = true;
        this.recognition.continuous = this.config.continuous;
        this.restartAttempts = 0;
        this.stopListening();
        
        setTimeout(() => {
            this.startListening();
        }, 500);
    }
    
    /**
     * Traite le texte reconnu avec confidence corrigée.
     */
	processTranscript(transcript, confidence, alternatives = []) {
	    console.log(`🎯 Traitement: "${transcript}" (confidence: ${confidence})`);
	    console.log(`📍 Mode actuel: ${this.isWakeWordMode ? 'WAKE_WORD' : 'COMMAND'}`);
	    
	    // Utiliser un seuil adaptatif
	    let effectiveThreshold = this.config.confidenceThreshold;
	    
	    // Pour "Angèle" et autres variantes, seuil plus bas
	    if (this.isWakeWordCandidate(transcript)) {
	        effectiveThreshold = Math.max(0.3, effectiveThreshold - 0.3);
	        console.log(`🔧 Seuil réduit pour wake word candidat: ${effectiveThreshold}`);
	    }
	    
	    // Vérifier le seuil de confiance
	    if (confidence < effectiveThreshold) {
	        console.log(`⚠️ Confidence trop faible: ${confidence} < ${effectiveThreshold}`);
	        return;
	    }
	    
	    if (this.isWakeWordMode) {
	        // TOUJOURS vérifier le wake word en mode wake_word
	        if (this.containsWakeWord(transcript)) {
	            console.log(`🎯 WAKE WORD DÉTECTÉ: ${this.config.wakeWord}`);
	            this.recognitionStats.successfulDetections++;
	            this.recognitionStats.lastDetectionTime = Date.now();
	            
	            this.triggerCallback('wakeWord', {
	                word: this.config.wakeWord,
	                transcript: transcript,
	                confidence: confidence,
	                alternatives: alternatives
	            });
	            return; // Important: sortir ici
	        } else {
	            console.log(`❌ Pas un wake word: "${transcript}"`);
	        }
	    } else {
	        // Mode commande
	        console.log(`🗣️ Commande en mode COMMAND: "${transcript}"`);
	        this.triggerCallback('command', {
	            command: transcript,
	            confidence: confidence,
	            alternatives: alternatives
	        });
	        
	        // Retourner automatiquement en mode wake word
	        setTimeout(() => {
	            console.log('🔄 Retour en mode wake word');
	            this.returnToWakeWordMode();
	        }, 1000);
	    }
	}
	    
    /**
     * Vérifie si le transcript contient le mot-clé.
     */
	containsWakeWord(transcript) {
	    const normalizedTranscript = transcript.toLowerCase().trim();
	    const normalizedWakeWord = this.config.wakeWord.toLowerCase();
	    
	    console.log(`🔍 Vérification wake word: "${normalizedTranscript}" vs "${normalizedWakeWord}"`);
	    
	    // Recherche exacte
	    if (normalizedTranscript.includes(normalizedWakeWord)) {
	        console.log('✅ Correspondance exacte trouvée');
	        return true;
	    }
	    
	    // Recherche approximative
	    const isMatch = this.fuzzyMatch(normalizedTranscript, normalizedWakeWord);
	    if (isMatch) {
	        console.log('✅ Correspondance approximative trouvée');
	    } else {
	        console.log('❌ Aucune correspondance');
	    }
	    
	    return isMatch;
	}    
    /**
     * Correspondance approximative améliorée.
     */
	fuzzyMatch(text, target) {
	    // Variantes connues pour "Angel"
	    const angelVariants = [
	        'angel', 'anjel', 'ange', 'angie',
	        'angela', 'angèle', 'angele', 'andel', 'anchel'
	    ];
	    
	    console.log(`🔍 Test fuzzy match: "${text}" avec variantes:`, angelVariants);
	    
	    // Vérification par inclusion directe
	    for (const variant of angelVariants) {
	        if (text.includes(variant)) {
	            console.log(`✅ Variante trouvée: "${variant}"`);
	            return true;
	        }
	    }
	    
	    // Vérification par similarité pour gérer les accents et erreurs
	    for (const variant of angelVariants) {
	        const similarity = this.calculateStringSimilarity(text, variant);
	        console.log(`📊 Similarité "${text}" vs "${variant}": ${similarity}`);
	        
	        if (similarity > 0.6) { // Seuil plus bas pour "Angèle"
	            console.log(`✅ Similarité suffisante: ${similarity}`);
	            return true;
	        }
	    }
	    
	    // Test spécial pour "Angèle" (gestion des caractères accentués)
	    const textNormalized = text
	        .normalize('NFD')
	        .replace(/[\u0300-\u036f]/g, '') // Supprimer les accents
	        .toLowerCase();
	    
	    console.log(`🔧 Texte normalisé: "${textNormalized}"`);
	    
	    if (textNormalized.includes('angel') || textNormalized.includes('angele')) {
	        console.log('✅ Correspondance après normalisation des accents');
	        return true;
	    }
	    
	    return false;
	}
	
	    
    /**
     * Gère les erreurs de reconnaissance.
     */
    handleError(event) {
        this.restartAttempts++;
        
        switch (event.error) {
            case 'network':
                console.error('❌ Erreur réseau');
                break;
            case 'not-allowed':
                console.error('❌ Microphone non autorisé');
                this.triggerCallback('error', 'Accès au microphone refusé');
                return;
            case 'no-speech':
                console.log('⚠️ Aucune parole détectée');
                break;
            case 'aborted':
                console.log('⚠️ Reconnaissance interrompue');
                break;
            default:
                console.error('❌ Erreur de reconnaissance:', event.error);
        }
        
        this.triggerCallback('error', event.error);
    }
    
    /**
     * Enregistre un callback pour un événement.
     */
    on(event, callback) {
        if (!this.callbacks[event]) {
            this.callbacks[event] = [];
        }
        this.callbacks[event].push(callback);
    }
    
    /**
     * Supprime un callback.
     */
    off(event, callback) {
        if (this.callbacks[event]) {
            this.callbacks[event] = this.callbacks[event].filter(cb => cb !== callback);
        }
    }
    
    /**
     * Déclenche un callback.
     */
    triggerCallback(event, data = null) {
        if (this.callbacks[event]) {
            this.callbacks[event].forEach(callback => {
                try {
                    callback(data);
                } catch (error) {
                    console.error('❌ Erreur dans le callback:', error);
                }
            });
        }
    }
    
    /**
     * Obtient le statut actuel.
     */
    getStatus() {
        return {
            isListening: this.isListening,
            isWakeWordMode: this.isWakeWordMode,
            isSupported: !!this.recognition,
            config: this.config,
            stats: this.recognitionStats
        };
    }
}
/**
 * Gestionnaire d'animations avancées pour l'avatar 3D
 * Gère la synchronisation labiale, les expressions faciales et les animations corporelles
 */

class AvatarAnimationManager {
    constructor(avatarModel, mixer) {
        this.avatarModel = avatarModel;
        this.mixer = mixer;
        
        // Animations et actions
        this.animations = new Map();
        this.activeActions = new Map();
        this.morphTargets = new Map();
        
        // État des animations
        this.currentBaseAnimation = null;
        this.currentEmotion = 'neutral';
        this.isBlinking = false;
        this.isSpeaking = false;
        
        // Timers et intervalles
        this.blinkInterval = null;
        this.idleMovementInterval = null;
        
        // Configuration
        this.config = {
            blinking: {
                enabled: true,
                frequency: 3500, // ms
                duration: 150,   // ms
                randomness: 0.4
            },
            lipSync: {
                enabled: true,
                smoothing: 0.3,
                anticipation: 50 // ms
            },
            idleMovement: {
                enabled: true,
                frequency: 8000, // ms
                intensity: 0.2
            }
        };
        
        this.initialize();
    }
    
    /**
     * Initialise le gestionnaire d'animations
     */
    initialize() {
        this.setupMorphTargets();
        this.startBlinking();
        this.startIdleMovements();
        
        console.log('Gestionnaire d\'animations initialisé');
    }
    
    /**
     * Configure les morph targets pour les expressions faciales
     */
    setupMorphTargets() {
        this.avatarModel.traverse((child) => {
            if (child.isMesh && child.morphTargetInfluences) {
                const morphTargetDict = child.morphTargetDictionary;
                if (morphTargetDict) {
                    // Mappage des visemes pour la synchronisation labiale
                    const visemeMapping = {
                        'A': ['viseme_AA', 'mouthOpen'],
                        'B': ['viseme_PP', 'mouthClosed'],
                        'C': ['viseme_CH', 'mouthNarrow'],
                        'D': ['viseme_DD', 'mouthPress'],
                        'E': ['viseme_E', 'mouthSmile'],
                        'F': ['viseme_FF', 'mouthFrown'],
                        'G': ['viseme_kk', 'mouthWide'],
                        'H': ['viseme_I', 'mouthTight'],
                        'X': ['viseme_sil', 'mouthRest']
                    };
                    
                    // Mappage des émotions
                    const emotionMapping = {
                        'happy': ['browInnerUp', 'eyeSquintLeft', 'eyeSquintRight', 'mouthSmileLeft', 'mouthSmileRight'],
                        'sad': ['browDownLeft', 'browDownRight', 'mouthFrownLeft', 'mouthFrownRight'],
                        'surprised': ['browInnerUp', 'eyeWideLeft', 'eyeWideRight', 'mouthFunnel'],
                        'angry': ['browDownLeft', 'browDownRight', 'eyeSquintLeft', 'eyeSquintRight'],
                        'concerned': ['browInnerUp', 'mouthPress'],
                        'thoughtful': ['browDownLeft', 'browDownRight', 'mouthPucker'],
                        'neutral': []
                    };
                    
                    // Stocker les indices des morph targets
                    for (const [viseme, targets] of Object.entries(visemeMapping)) {
                        for (const target of targets) {
                            if (target in morphTargetDict) {
                                if (!this.morphTargets.has('visemes')) {
                                    this.morphTargets.set('visemes', new Map());
                                }
                                this.morphTargets.get('visemes').set(viseme, {
                                    mesh: child,
                                    index: morphTargetDict[target]
                                });
                            }
                        }
                    }
                    
                    for (const [emotion, targets] of Object.entries(emotionMapping)) {
                        for (const target of targets) {
                            if (target in morphTargetDict) {
                                if (!this.morphTargets.has('emotions')) {
                                    this.morphTargets.set('emotions', new Map());
                                }
                                if (!this.morphTargets.get('emotions').has(emotion)) {
                                    this.morphTargets.get('emotions').set(emotion, []);
                                }
                                this.morphTargets.get('emotions').get(emotion).push({
                                    mesh: child,
                                    index: morphTargetDict[target]
                                });
                            }
                        }
                    }
                    
                    // Clignement des yeux
                    const blinkTargets = ['eyeBlinkLeft', 'eyeBlinkRight'];
                    for (const target of blinkTargets) {
                        if (target in morphTargetDict) {
                            if (!this.morphTargets.has('blink')) {
                                this.morphTargets.set('blink', []);
                            }
                            this.morphTargets.get('blink').push({
                                mesh: child,
                                index: morphTargetDict[target]
                            });
                        }
                    }
                }
            }
        });
        
        console.log('Morph targets configurés:', Array.from(this.morphTargets.keys()));
    }
    
    /**
     * Joue une animation par son nom
     */
    playAnimation(name, options = {}) {
        if (!this.animations.has(name)) {
            console.warn(`Animation '${name}' non trouvée`);
            return null;
        }
        
        const clip = this.animations.get(name);
        const action = this.mixer.clipAction(clip);
        
        // Configuration de l'action
        action.reset();
        action.setEffectiveTimeScale(options.speed || 1);
        action.setEffectiveWeight(options.weight || 1);
        
        if (options.loop) {
            action.setLoop(THREE.LoopRepeat);
        } else {
            action.setLoop(THREE.LoopOnce);
            action.clampWhenFinished = true;
        }
        
        // Transition
        if (options.fadeIn) {
            action.fadeIn(options.fadeIn);
        } else {
            action.play();
        }
        
        // Stocker l'action active
        this.activeActions.set(name, action);
        
        return action;
    }
    
    /**
     * Arrête une animation
     */
    stopAnimation(name, fadeOut = 0.5) {
        if (this.activeActions.has(name)) {
            const action = this.activeActions.get(name);
            
            if (fadeOut > 0) {
                action.fadeOut(fadeOut);
            } else {
                action.stop();
            }
            
            this.activeActions.delete(name);
        }
    }
    
    /**
     * Change l'émotion de l'avatar
     */
    setEmotion(emotion, intensity = 0.7, duration = 0.8) {
        if (emotion === this.currentEmotion) return;
        
        console.log(`Transition vers l'émotion: ${emotion}`);
        
        // Réinitialiser l'émotion précédente
        this.resetEmotionMorphTargets();
        
        // Appliquer la nouvelle émotion
        if (this.morphTargets.has('emotions') && this.morphTargets.get('emotions').has(emotion)) {
            const targets = this.morphTargets.get('emotions').get(emotion);
            
            targets.forEach(target => {
                this.animateMorphTarget(target.mesh, target.index, intensity, duration);
            });
        }
        
        this.currentEmotion = emotion;
    }
    
    /**
     * Synchronisation labiale avec un texte et des données de visemes
     */
    performLipSync(text, visemeData = null) {
        if (!this.config.lipSync.enabled) return;
        
        console.log('Début de la synchronisation labiale:', text);
        this.isSpeaking = true;
        
        if (visemeData && visemeData.length > 0) {
            // Utiliser les données de visemes fournies
            this.playVisemeSequence(visemeData);
        } else {
            // Générer une séquence de visemes basique
            this.generateBasicLipSync(text);
        }
    }
    
    /**
     * Joue une séquence de visemes
     */
    playVisemeSequence(visemeData) {
        let timeOffset = 0;
        
        visemeData.forEach(viseme => {
            setTimeout(() => {
                this.setViseme(viseme.phoneme, viseme.intensity || 0.8);
            }, timeOffset + viseme.time);
            
            timeOffset = viseme.time + viseme.duration;
        });
        
        // Retour au repos après la séquence
        setTimeout(() => {
            this.setViseme('X', 0); // Position de repos
            this.isSpeaking = false;
        }, timeOffset + 200);
    }
    
    /**
     * Génère une synchronisation labiale basique
     */
    generateBasicLipSync(text) {
        const phonemes = this.textToPhonemes(text);
        const duration = text.length * 100; // Durée estimée
        const phonemeDuration = duration / phonemes.length;
        
        phonemes.forEach((phoneme, index) => {
            setTimeout(() => {
                this.setViseme(phoneme, 0.7);
            }, index * phonemeDuration);
        });
        
        // Retour au repos
        setTimeout(() => {
            this.setViseme('X', 0);
            this.isSpeaking = false;
        }, duration);
    }
    
    /**
     * Convertit le texte en phonèmes basiques
     */
    textToPhonemes(text) {
        // Mapping très simplifié français -> visemes
        const phonemeMap = {
            'a': 'A', 'à': 'A', 'â': 'A',
            'e': 'E', 'é': 'E', 'è': 'E', 'ê': 'E',
            'i': 'H', 'î': 'H',
            'o': 'A', 'ô': 'A', 'ö': 'A',
            'u': 'G', 'ù': 'G', 'û': 'G',
            'b': 'B', 'p': 'B', 'm': 'B',
            'f': 'F', 'v': 'F',
            'l': 'D', 'n': 'D', 't': 'D',
            'c': 'C', 'k': 'C', 'g': 'C',
            'r': 'G', 'j': 'C',
            ' ': 'X'
        };
        
        return text.toLowerCase().split('').map(char => 
            phonemeMap[char] || 'X'
        ).filter((phoneme, index, arr) => 
            phoneme !== arr[index - 1] // Éliminer les doublons consécutifs
        );
    }
    
    /**
     * Définit un viseme spécifique
     */
    setViseme(viseme, intensity) {
        // Réinitialiser tous les visemes
        this.resetVisemeMorphTargets();
        
        // Appliquer le nouveau viseme
        if (this.morphTargets.has('visemes') && this.morphTargets.get('visemes').has(viseme)) {
            const target = this.morphTargets.get('visemes').get(viseme);
            this.setMorphTarget(target.mesh, target.index, intensity);
        }
    }
    
    /**
     * Démarre le système de clignement automatique
     */
    startBlinking() {
        if (!this.config.blinking.enabled) return;
        
        const scheduleNextBlink = () => {
            const baseInterval = this.config.blinking.frequency;
            const randomness = this.config.blinking.randomness;
            const nextBlink = baseInterval + (Math.random() - 0.5) * baseInterval * randomness;
            
            this.blinkInterval = setTimeout(() => {
                this.performBlink();
                scheduleNextBlink();
            }, nextBlink);
        };
        
        scheduleNextBlink();
        console.log('Clignement automatique activé');
    }
    
    /**
     * Effectue un clignement
     */
    performBlink() {
        if (this.isBlinking || this.isSpeaking) return;
        
        this.isBlinking = true;
        const duration = this.config.blinking.duration;
        
        // Fermer les yeux
        this.setBlinkTargets(1.0);
        
        // Rouvrir les yeux
        setTimeout(() => {
            this.setBlinkTargets(0.0);
            this.isBlinking = false;
        }, duration);
    }
    
    /**
     * Contrôle les morph targets de clignement
     */
    setBlinkTargets(value) {
        if (this.morphTargets.has('blink')) {
            this.morphTargets.get('blink').forEach(target => {
                this.setMorphTarget(target.mesh, target.index, value);
            });
        }
    }
    
    /**
     * Démarre les mouvements d'attente
     */
    startIdleMovements() {
        if (!this.config.idleMovement.enabled) return;
        
        this.idleMovementInterval = setInterval(() => {
            this.performIdleMovement();
        }, this.config.idleMovement.frequency);
        
        console.log('Mouvements d\'attente activés');
    }
    
    /**
     * Effectue un petit mouvement d'attente
     */
    performIdleMovement() {
        if (this.isSpeaking) return;
        
        const intensity = this.config.idleMovement.intensity;
        const movements = [
            { type: 'head', axis: 'y', amount: (Math.random() - 0.5) * intensity },
            { type: 'head', axis: 'x', amount: (Math.random() - 0.5) * intensity * 0.5 },
            { type: 'eye', direction: Math.random() > 0.5 ? 'left' : 'right' }
        ];
        
        const movement = movements[Math.floor(Math.random() * movements.length)];
        this.applyIdleMovement(movement);
    }
    
    /**
     * Applique un mouvement d'attente
     */
    applyIdleMovement(movement) {
        // Implémentation des mouvements subtils
        // (nécessiterait l'accès aux bones de l'avatar)
        console.log('Mouvement d\'attente:', movement);
    }
    
    /**
     * Anime un morph target vers une valeur cible
     */
    animateMorphTarget(mesh, index, targetValue, duration) {
        const startValue = mesh.morphTargetInfluences[index] || 0;
        const startTime = Date.now();
        
        const animate = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / (duration * 1000), 1);
            
            // Interpolation smooth
            const easeProgress = 0.5 * (1 - Math.cos(progress * Math.PI));
            const currentValue = startValue + (targetValue - startValue) * easeProgress;
            
            mesh.morphTargetInfluences[index] = currentValue;
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        
        animate();
    }
    
    /**
     * Définit directement un morph target
     */
    setMorphTarget(mesh, index, value) {
        if (mesh.morphTargetInfluences && index < mesh.morphTargetInfluences.length) {
            mesh.morphTargetInfluences[index] = Math.max(0, Math.min(1, value));
        }
    }
    
    /**
     * Réinitialise tous les morph targets d'émotion
     */
    resetEmotionMorphTargets() {
        if (this.morphTargets.has('emotions')) {
            this.morphTargets.get('emotions').forEach(targets => {
                targets.forEach(target => {
                    this.setMorphTarget(target.mesh, target.index, 0);
                });
            });
        }
    }
    
    /**
     * Réinitialise tous les morph targets de visemes
     */
    resetVisemeMorphTargets() {
        if (this.morphTargets.has('visemes')) {
            this.morphTargets.get('visemes').forEach(target => {
                this.setMorphTarget(target.mesh, target.index, 0);
            });
        }
    }
    
    /**
     * Arrête le clignement automatique
     */
    stopBlinking() {
        if (this.blinkInterval) {
            clearTimeout(this.blinkInterval);
            this.blinkInterval = null;
        }
    }
    
    /**
     * Arrête les mouvements d'attente
     */
    stopIdleMovements() {
        if (this.idleMovementInterval) {
            clearInterval(this.idleMovementInterval);
            this.idleMovementInterval = null;
        }
    }
    
    /**
     * Nettoie les ressources
     */
    dispose() {
        this.stopBlinking();
        this.stopIdleMovements();
        
        this.activeActions.forEach(action => action.stop());
        this.activeActions.clear();
        this.animations.clear();
        this.morphTargets.clear();
        
        console.log('Gestionnaire d\'animations nettoyé');
    }
}

// Export global
window.AvatarAnimationManager = AvatarAnimationManager;
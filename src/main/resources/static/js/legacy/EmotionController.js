/**
 * Contrôleur des émotions faciales de l'avatar
 */
class EmotionController {
    constructor() {
        this.avatar = null;
        this.faceMesh = null;
        this.eyebrowBones = [];
        this.eyeBones = [];
        this.currentEmotion = 'neutral';
        this.targetValues = {};
        this.currentValues = {};
        
        // Configuration des émotions avec morph targets
        this.emotions = {
            'neutral': {
                eyebrows: 0.0,
                mouth_smile: 0.0,
                mouth_frown: 0.0,
                eyes_squint: 0.0,
                cheek_raise: 0.0
            },
            'happy': {
                eyebrows: 0.2,
                mouth_smile: 0.8,
                mouth_frown: 0.0,
                eyes_squint: 0.3,
                cheek_raise: 0.6
            },
            'sad': {
                eyebrows: -0.4,
                mouth_smile: 0.0,
                mouth_frown: 0.6,
                eyes_squint: 0.0,
                cheek_raise: 0.0
            },
            'surprised': {
                eyebrows: 0.8,
                mouth_smile: 0.0,
                mouth_frown: 0.0,
                eyes_squint: -0.5,
                cheek_raise: 0.0
            },
            'concerned': {
                eyebrows: -0.2,
                mouth_smile: 0.0,
                mouth_frown: 0.3,
                eyes_squint: 0.1,
                cheek_raise: 0.0
            },
            'excited': {
                eyebrows: 0.5,
                mouth_smile: 0.9,
                mouth_frown: 0.0,
                eyes_squint: 0.4,
                cheek_raise: 0.8
            }
        };
        
        // Initialiser les valeurs courantes
        this.currentValues = { ...this.emotions.neutral };
    }
    
    setAvatar(avatar) {
        this.avatar = avatar;
        this.findFacialComponents();
    }
    
    findFacialComponents() {
        if (!this.avatar) return;
        
        this.avatar.traverse((child) => {
            if (child.isMesh && child.name.toLowerCase().includes('face')) {
                this.faceMesh = child;
            }
            
            if (child.isBone) {
                const name = child.name.toLowerCase();
                if (name.includes('eyebrow')) {
                    this.eyebrowBones.push(child);
                } else if (name.includes('eye') && !name.includes('lid')) {
                    this.eyeBones.push(child);
                }
            }
        });
        
        console.log('Composants faciaux trouvés:', {
            face: !!this.faceMesh,
            eyebrows: this.eyebrowBones.length,
            eyes: this.eyeBones.length
        });
    }
    
    setEmotion(emotionName, intensity = 1.0, duration = 800) {
        const emotion = this.emotions[emotionName];
        if (!emotion) {
            console.warn(`Émotion inconnue: ${emotionName}`);
            return;
        }
        
        this.currentEmotion = emotionName;
        
        // Calculer les valeurs cibles avec intensité
        this.targetValues = {};
        Object.keys(emotion).forEach(key => {
            this.targetValues[key] = emotion[key] * intensity;
        });
        
        // Démarrer l'animation vers la nouvelle émotion
        this.animateToEmotion(duration);
    }
    
    animateToEmotion(duration) {
        Object.keys(this.targetValues).forEach(morphTarget => {
            const startValue = this.currentValues[morphTarget] || 0;
            const endValue = this.targetValues[morphTarget];
            
            // Animation fluide avec Tween.js
            new TWEEN.Tween({ value: startValue })
                .to({ value: endValue }, duration)
                .easing(TWEEN.Easing.Cubic.InOut)
                .onUpdate((obj) => {
                    this.currentValues[morphTarget] = obj.value;
                    this.applyMorphTarget(morphTarget, obj.value);
                })
                .start();
        });
    }
    
    applyMorphTarget(morphTarget, value) {
        if (!this.faceMesh || !this.faceMesh.morphTargetInfluences) return;
        
        // Mapper les noms de morph targets vers les indices
        const morphIndex = this.getMorphTargetIndex(morphTarget);
        if (morphIndex >= 0) {
            this.faceMesh.morphTargetInfluences[morphIndex] = Math.max(0, Math.min(1, value));
        }
        
        // Gérer les bones des sourcils et yeux
        this.updateFacialBones(morphTarget, value);
    }
    
    getMorphTargetIndex(morphTargetName) {
        if (!this.faceMesh.morphTargetDictionary) return -1;
        return this.faceMesh.morphTargetDictionary[morphTargetName] || -1;
    }
    
    updateFacialBones(morphTarget, value) {
        switch (morphTarget) {
            case 'eyebrows':
                this.eyebrowBones.forEach(bone => {
                    bone.rotation.z = value * 0.1; // Rotation subtile
                });
                break;
                
            case 'eyes_squint':
                this.eyeBones.forEach(bone => {
                    bone.scale.y = 1 + (value * 0.2); // Plissement des yeux
                });
                break;
        }
    }
    
    update(delta) {
        // Mise à jour continue pour les animations automatiques
        this.updateBlinking();
        this.updateSubtleMovements();
    }
    
    updateBlinking() {
        // Clignements automatiques naturels
        if (Math.random() < 0.001) { // ~1 clignement par seconde
            this.blink();
        }
    }
    
    blink() {
        if (!this.faceMesh) return;
        
        const blinkIndex = this.getMorphTargetIndex('blink');
        if (blinkIndex >= 0) {
            // Animation rapide de clignement
            new TWEEN.Tween({ value: 0 })
                .to({ value: 1 }, 80)
                .easing(TWEEN.Easing.Quad.Out)
                .onUpdate((obj) => {
                    this.faceMesh.morphTargetInfluences[blinkIndex] = obj.value;
                })
                .chain(
                    new TWEEN.Tween({ value: 1 })
                        .to({ value: 0 }, 120)
                        .easing(TWEEN.Easing.Quad.In)
                        .onUpdate((obj) => {
                            this.faceMesh.morphTargetInfluences[blinkIndex] = obj.value;
                        })
                )
                .start();
        }
    }
    
    updateSubtleMovements() {
        // Micro-mouvements pour rendre l'avatar plus vivant
        if (this.avatar && Math.random() < 0.01) {
            const movement = (Math.random() - 0.5) * 0.002;
            this.avatar.rotation.y += movement;
            this.avatar.position.x += movement * 0.5;
        }
    }
    
    dispose() {
        this.avatar = null;
        this.faceMesh = null;
        this.eyebrowBones = [];
        this.eyeBones = [];
    }
}

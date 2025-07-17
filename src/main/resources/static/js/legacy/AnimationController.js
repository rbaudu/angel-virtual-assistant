/**
 * Contrôleur des animations corporelles
 */
class AnimationController {
    constructor() {
        this.avatar = null;
        this.mixer = null;
        this.animations = new Map();
        this.currentAnimation = null;
        this.gestureQueue = [];
        
        // Configuration des animations par défaut
        this.animationConfig = {
            'idle': { loop: true, fadeTime: 0.5 },
            'talking': { loop: true, fadeTime: 0.3 },
            'wave': { loop: false, fadeTime: 0.2 },
            'nod': { loop: false, fadeTime: 0.1 },
            'shake': { loop: false, fadeTime: 0.1 },
            'shrug': { loop: false, fadeTime: 0.3 }
        };
    }
    
    setAvatar(avatar, animationClips) {
        this.avatar = avatar;
        
        if (animationClips && animationClips.length > 0) {
            this.mixer = new THREE.AnimationMixer(avatar);
            
            // Charger toutes les animations
            animationClips.forEach(clip => {
                const action = this.mixer.clipAction(clip);
                this.animations.set(clip.name, action);
            });
            
            console.log(`${animationClips.length} animations chargées`);
        }
    }
    
    playAnimation(name, loop = false, fadeTime = 0.5) {
        const action = this.animations.get(name);
        if (!action) {
            console.warn(`Animation non trouvée: ${name}`);
            return;
        }
        
        // Arrêter l'animation courante
        if (this.currentAnimation && this.currentAnimation !== action) {
            this.currentAnimation.fadeOut(fadeTime);
        }
        
        // Configurer la nouvelle animation
        action.reset();
        action.setLoop(loop ? THREE.LoopRepeat : THREE.LoopOnce);
        action.clampWhenFinished = !loop;
        
        // Démarrer avec fade in
        action.fadeIn(fadeTime);
        action.play();
        
        this.currentAnimation = action;
        
        // Si ce n'est pas en boucle, programmer l'arrêt
        if (!loop) {
            setTimeout(() => {
                if (this.currentAnimation === action) {
                    this.playAnimation('idle', true);
                }
            }, action.getClip().duration * 1000 + fadeTime * 1000);
        }
    }
    
    playGesture(gestureType, intensity = 1.0) {
        // Ajouter le geste à la queue pour éviter les conflits
        this.gestureQueue.push({ type: gestureType, intensity });
        this.processGestureQueue();
    }
    
    processGestureQueue() {
        if (this.gestureQueue.length === 0) return;
        
        const gesture = this.gestureQueue.shift();
        this.executeGesture(gesture.type, gesture.intensity);
        
        // Traiter le geste suivant après un délai
        setTimeout(() => this.processGestureQueue(), 500);
    }
    
    executeGesture(gestureType, intensity) {
        switch (gestureType) {
            case 'wave':
                this.createWaveGesture(intensity);
                break;
            case 'nod':
                this.createNodGesture(intensity);
                break;
            case 'shake':
                this.createShakeGesture(intensity);
                break;
            case 'shrug':
                this.createShrugGesture(intensity);
                break;
            default:
                // Essayer de jouer l'animation correspondante
                this.playAnimation(gestureType, false);
        }
    }
    
    createWaveGesture(intensity) {
        const hand = this.findBone('hand_right') || this.findBone('arm_right');
        if (!hand) return;
        
        const originalRotation = hand.rotation.clone();
        
        // Animation de salut
        new TWEEN.Tween(hand.rotation)
            .to({ z: Math.PI * 0.3 * intensity }, 300)
            .easing(TWEEN.Easing.Back.Out)
            .chain(
                new TWEEN.Tween(hand.rotation)
                    .to(originalRotation, 300)
                    .easing(TWEEN.Easing.Back.In)
            )
            .start();
    }
    
    createNodGesture(intensity) {
        if (!this.avatar) return;
        
        const head = this.findBone('head') || this.avatar;
        const originalRotation = head.rotation.clone();
        
        // Hochement de tête
        new TWEEN.Tween(head.rotation)
            .to({ x: originalRotation.x - 0.2 * intensity }, 200)
            .easing(TWEEN.Easing.Quad.Out)
            .chain(
                new TWEEN.Tween(head.rotation)
                    .to(originalRotation, 300)
                    .easing(TWEEN.Easing.Bounce.Out)
            )
            .start();
    }
    
    createShakeGesture(intensity) {
        if (!this.avatar) return;
        
        const head = this.findBone('head') || this.avatar;
        const originalRotation = head.rotation.clone();
        
        // Mouvement de tête négatif
        new TWEEN.Tween(head.rotation)
            .to({ y: originalRotation.y + 0.3 * intensity }, 150)
            .easing(TWEEN.Easing.Quad.InOut)
            .chain(
                new TWEEN.Tween(head.rotation)
                    .to({ y: originalRotation.y - 0.3 * intensity }, 300)
                    .easing(TWEEN.Easing.Quad.InOut)
                    .chain(
                        new TWEEN.Tween(head.rotation)
                            .to(originalRotation, 150)
                            .easing(TWEEN.Easing.Quad.InOut)
                    )
            )
            .start();
    }
    
    createShrugGesture(intensity) {
        const shoulders = [
            this.findBone('shoulder_left'),
            this.findBone('shoulder_right')
        ].filter(Boolean);
        
        if (shoulders.length === 0) return;
        
        shoulders.forEach(shoulder => {
            const originalRotation = shoulder.rotation.clone();
            
            new TWEEN.Tween(shoulder.rotation)
                .to({ z: originalRotation.z + 0.3 * intensity }, 400)
                .easing(TWEEN.Easing.Back.Out)
                .chain(
                    new TWEEN.Tween(shoulder.rotation)
                        .to(originalRotation, 600)
                        .easing(TWEEN.Easing.Back.In)
                )
                .start();
        });
    }
    
    findBone(boneName) {
        if (!this.avatar) return null;
        
        let foundBone = null;
        this.avatar.traverse((child) => {
            if (child.isBone && child.name.toLowerCase().includes(boneName.toLowerCase())) {
                foundBone = child;
            }
        });
        
        return foundBone;
    }
    
    update(delta) {
        if (this.mixer) {
            this.mixer.update(delta);
        }
    }
    
    dispose() {
        if (this.mixer) {
            this.mixer.stopAllAction();
        }
        this.animations.clear();
        this.gestureQueue = [];
        this.avatar = null;
        this.mixer = null;
    }
}

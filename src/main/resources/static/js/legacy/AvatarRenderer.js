/**
 * Système de rendu d'avatar 3D réaliste pour Angel Virtual Assistant
 * Utilise Three.js pour le rendu WebGL et la gestion des animations
 */

class AvatarRenderer {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.avatar = null;
        this.mixer = null;
        this.clock = new THREE.Clock();
        
        // Contrôleurs spécialisés
        this.lipSyncController = new LipSyncController();
        this.emotionController = new EmotionController();
        this.animationController = new AnimationController();
        this.speechController = new SpeechController();
        
        // État de l'avatar
        this.isInitialized = false;
        this.isVisible = false;
        this.currentEmotion = 'neutral';
        this.isSpeaking = false;
        
        // Configuration
        this.config = {
            enableShadows: true,
            enableAntialiasing: true,
            pixelRatio: Math.min(window.devicePixelRatio, 2),
            cameraDistance: 1.5,
            lightIntensity: 0.8
        };
        
        // Initialiser le rendu
        this.init();
    }
    
    /**
     * Initialise le système de rendu Three.js
     */
    init() {
        console.log('Initialisation du rendu avatar...');
        
        // Créer la scène
        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0xf0f0f0);
        
        // Configurer la caméra
        this.camera = new THREE.PerspectiveCamera(
            50, // FOV
            this.container.clientWidth / this.container.clientHeight, // Aspect ratio
            0.1, // Near
            1000 // Far
        );
        this.camera.position.set(0, 0, this.config.cameraDistance);
        
        // Créer le renderer
        this.renderer = new THREE.WebGLRenderer({ 
            antialias: this.config.enableAntialiasing,
            alpha: true 
        });
        this.renderer.setSize(this.container.clientWidth, this.container.clientHeight);
        this.renderer.setPixelRatio(this.config.pixelRatio);
        this.renderer.shadowMap.enabled = this.config.enableShadows;
        this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        this.renderer.gammaOutput = true;
        this.renderer.gammaFactor = 2.2;
        
        // Ajouter le canvas au container
        this.container.appendChild(this.renderer.domElement);
        
        // Configurer l'éclairage
        this.setupLighting();
        
        // Configurer les contrôles de caméra (optionnel)
        this.setupCameraControls();
        
        // Démarrer la boucle de rendu
        this.animate();
        
        console.log('Rendu avatar initialisé');
    }
    
    /**
     * Configure l'éclairage réaliste de la scène
     */
    setupLighting() {
        // Lumière ambiante douce
        const ambientLight = new THREE.AmbientLight(0x404040, 0.3);
        this.scene.add(ambientLight);
        
        // Lumière principale (key light)
        const keyLight = new THREE.DirectionalLight(0xffffff, this.config.lightIntensity);
        keyLight.position.set(2, 2, 5);
        keyLight.castShadow = this.config.enableShadows;
        keyLight.shadow.mapSize.width = 2048;
        keyLight.shadow.mapSize.height = 2048;
        keyLight.shadow.camera.near = 0.5;
        keyLight.shadow.camera.far = 500;
        this.scene.add(keyLight);
        
        // Lumière de remplissage (fill light)
        const fillLight = new THREE.DirectionalLight(0x8bb7ff, 0.3);
        fillLight.position.set(-2, 1, 3);
        this.scene.add(fillLight);
        
        // Lumière de contour (rim light)
        const rimLight = new THREE.DirectionalLight(0xffffff, 0.2);
        rimLight.position.set(0, 0, -5);
        this.scene.add(rimLight);
    }
    
    /**
     * Configure les contrôles de caméra (optionnel pour débug)
     */
    setupCameraControls() {
        // Dans une version de production, on pourrait permettre à l'utilisateur
        // de faire pivoter légèrement la vue
        // Ici on laisse la caméra fixe pour plus de stabilité
    }
    
    /**
     * Charge un modèle d'avatar 3D
     */
    async loadAvatar(modelPath, config = {}) {
        try {
            console.log(`Chargement de l'avatar: ${modelPath}`);
            
            // Supprimer l'avatar précédent s'il existe
            if (this.avatar) {
                this.scene.remove(this.avatar);
                this.avatar = null;
            }
            
            // Charger le nouveau modèle
            const loader = new THREE.GLTFLoader();
            const gltf = await this.loadGLTF(loader, modelPath);
            
            this.avatar = gltf.scene;
            
            // Configurer l'avatar
            this.setupAvatarModel(gltf);
            
            // Ajouter à la scène
            this.scene.add(this.avatar);
            
            // Initialiser les contrôleurs
            this.lipSyncController.setAvatar(this.avatar);
            this.emotionController.setAvatar(this.avatar);
            this.animationController.setAvatar(this.avatar, gltf.animations);
            
            // Marquer comme initialisé
            this.isInitialized = true;
            
            console.log('Avatar chargé avec succès');
            
            // Démarrer l'animation d'idle
            this.animationController.playAnimation('idle', true);
            
            return true;
            
        } catch (error) {
            console.error('Erreur lors du chargement de l\'avatar:', error);
            return false;
        }
    }
    
    /**
     * Charge un fichier GLTF de manière asynchrone
     */
    loadGLTF(loader, path) {
        return new Promise((resolve, reject) => {
            loader.load(
                path,
                (gltf) => resolve(gltf),
                (progress) => {
                    console.log(`Chargement: ${(progress.loaded / progress.total * 100)}%`);
                },
                (error) => reject(error)
            );
        });
    }
    
    /**
     * Configure le modèle d'avatar après chargement
     */
    setupAvatarModel(gltf) {
        // Configurer les matériaux pour un rendu réaliste
        this.avatar.traverse((child) => {
            if (child.isMesh) {
                // Activer les ombres
                child.castShadow = this.config.enableShadows;
                child.receiveShadow = this.config.enableShadows;
                
                // Améliorer les matériaux
                if (child.material) {
                    if (child.material.map) {
                        child.material.map.anisotropy = this.renderer.capabilities.getMaxAnisotropy();
                    }
                    
                    // Configurer les matériaux de peau
                    if (child.name.includes('skin') || child.name.includes('face')) {
                        child.material.roughness = 0.8;
                        child.material.metalness = 0.0;
                    }
                    
                    // Configurer les matériaux d'yeux
                    if (child.name.includes('eye')) {
                        child.material.roughness = 0.1;
                        child.material.metalness = 0.1;
                    }
                }
            }
        });
        
        // Positionner l'avatar
        this.avatar.position.set(0, -0.8, 0);
        this.avatar.scale.setScalar(1.0);
        
        // Configurer l'animation mixer
        if (gltf.animations && gltf.animations.length > 0) {
            this.mixer = new THREE.AnimationMixer(this.avatar);
        }
    }
    
    /**
     * Fait parler l'avatar avec synchronisation labiale
     */
    async speak(speechData) {
        if (!this.isInitialized) {
            console.warn('Avatar non initialisé');
            return;
        }
        
        try {
            console.log('Avatar commence à parler:', speechData.text);
            
            this.isSpeaking = true;
            
            // Changer l'émotion si spécifiée
            if (speechData.emotion && speechData.emotion !== this.currentEmotion) {
                this.setEmotion(speechData.emotion);
            }
            
            // Démarrer l'animation de parole
            this.animationController.playAnimation('talking', true);
            
            // Démarrer la synchronisation labiale
            if (speechData.visemeData && speechData.visemeData.length > 0) {
                this.lipSyncController.startLipSync(speechData.visemeData);
            }
            
            // Jouer l'audio
            await this.speechController.playAudio(speechData.audioData);
            
            // Arrêter l'animation de parole
            this.animationController.playAnimation('idle', true);
            this.lipSyncController.stopLipSync();
            
            this.isSpeaking = false;
            
            console.log('Avatar a terminé de parler');
            
        } catch (error) {
            console.error('Erreur lors de la parole de l\'avatar:', error);
            this.isSpeaking = false;
        }
    }
    
    /**
     * Change l'émotion de l'avatar
     */
    setEmotion(emotion, intensity = 1.0, duration = 800) {
        if (!this.isInitialized) return;
        
        console.log(`Changement d'émotion: ${emotion} (intensité: ${intensity})`);
        
        this.currentEmotion = emotion;
        this.emotionController.setEmotion(emotion, intensity, duration);
    }
    
    /**
     * Joue un geste spécifique
     */
    playGesture(gestureType, intensity = 1.0) {
        if (!this.isInitialized) return;
        
        console.log(`Geste: ${gestureType}`);
        this.animationController.playGesture(gestureType, intensity);
    }
    
    /**
     * Contrôle la visibilité de l'avatar
     */
    setVisible(visible) {
        if (!this.isInitialized) return;
        
        this.isVisible = visible;
        
        if (this.avatar) {
            this.avatar.visible = visible;
        }
        
        // Animation d'apparition/disparition
        if (visible) {
            this.animateIn();
        } else {
            this.animateOut();
        }
    }
    
    /**
     * Animation d'apparition
     */
    animateIn() {
        if (!this.avatar) return;
        
        // Animation de scale et d'opacité
        this.avatar.scale.setScalar(0.8);
        
        const tween = new TWEEN.Tween(this.avatar.scale)
            .to({ x: 1.0, y: 1.0, z: 1.0 }, 600)
            .easing(TWEEN.Easing.Back.Out)
            .start();
    }
    
    /**
     * Animation de disparition
     */
    animateOut() {
        if (!this.avatar) return;
        
        const tween = new TWEEN.Tween(this.avatar.scale)
            .to({ x: 0.8, y: 0.8, z: 0.8 }, 400)
            .easing(TWEEN.Easing.Back.In)
            .start();
    }
    
    /**
     * Redimensionne le renderer
     */
    resize() {
        if (!this.renderer || !this.camera) return;
        
        const width = this.container.clientWidth;
        const height = this.container.clientHeight;
        
        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        
        this.renderer.setSize(width, height);
    }
    
    /**
     * Boucle de rendu principale
     */
    animate() {
        requestAnimationFrame(() => this.animate());
        
        const delta = this.clock.getDelta();
        
        // Mettre à jour les animations
        if (this.mixer) {
            this.mixer.update(delta);
        }
        
        // Mettre à jour les tweens
        TWEEN.update();
        
        // Mettre à jour les contrôleurs
        this.lipSyncController.update(delta);
        this.emotionController.update(delta);
        this.animationController.update(delta);
        
        // Rendu de la scène
        this.renderer.render(this.scene, this.camera);
    }
    
    /**
     * Nettoyage des ressources
     */
    dispose() {
        if (this.renderer) {
            this.renderer.dispose();
        }
        
        if (this.avatar) {
            this.scene.remove(this.avatar);
        }
        
        // Nettoyer les contrôleurs
        this.lipSyncController.dispose();
        this.emotionController.dispose();
        this.animationController.dispose();
        this.speechController.dispose();
    }
}

/**
 * Contrôleur de synchronisation labiale
 */
class LipSyncController {
    constructor() {
        this.avatar = null;
        this.mouthMesh = null;
        this.jawBone = null;
        this.isActive = false;
        this.currentVisemes = [];
        this.startTime = 0;
        
        // Mapping des visèmes vers les morph targets
        this.visemeMapping = {
            'A': { jaw: 0.7, lips_A: 1.0, lips_E: 0.0, lips_I: 0.0, lips_O: 0.0, lips_U: 0.0 },
            'E': { jaw: 0.3, lips_A: 0.0, lips_E: 1.0, lips_I: 0.0, lips_O: 0.0, lips_U: 0.0 },
            'I': { jaw: 0.1, lips_A: 0.0, lips_E: 0.0, lips_I: 1.0, lips_O: 0.0, lips_U: 0.0 },
            'O': { jaw: 0.8, lips_A: 0.0, lips_E: 0.0, lips_I: 0.0, lips_O: 1.0, lips_U: 0.0 },
            'U': { jaw: 0.2, lips_A: 0.0, lips_E: 0.0, lips_I: 0.0, lips_O: 0.0, lips_U: 1.0 },
            'P': { jaw: 0.0, lips_A: 0.0, lips_E: 0.0, lips_I: 0.0, lips_O: 0.0, lips_U: 0.0 },
            'F': { jaw: 0.2, lips_A: 0.0, lips_E: 0.0, lips_I: 0.0, lips_O: 0.0, lips_U: 0.0 }
        };
    }
    
    setAvatar(avatar) {
        this.avatar = avatar;
        this.findMouthComponents();
    }
    
    findMouthComponents() {
        if (!this.avatar) return;
        
        // Chercher les composants de la bouche
        this.avatar.traverse((child) => {
            if (child.isMesh && child.name.toLowerCase().includes('mouth')) {
                this.mouthMesh = child;
            }
            if (child.isBone && child.name.toLowerCase().includes('jaw')) {
                this.jawBone = child;
            }
        });
        
        console.log('Composants bouche trouvés:', {
            mouth: !!this.mouthMesh,
            jaw: !!this.jawBone
        });
    }
    
    startLipSync(visemeData) {
        if (!this.avatar || !visemeData) return;
        
        this.currentVisemes = visemeData;
        this.startTime = performance.now();
        this.isActive = true;
        
        console.log(`Démarrage lip sync avec ${visemeData.length} visèmes`);
    }
    
    stopLipSync() {
        this.isActive = false;
        this.resetMouth();
    }
    
    update(delta) {
        if (!this.isActive || !this.currentVisemes.length) return;
        
        const currentTime = performance.now() - this.startTime;
        
        // Trouver le visème actuel
        const currentViseme = this.getCurrentViseme(currentTime);
        
        if (currentViseme) {
            this.applyViseme(currentViseme);
        }
    }
    
    getCurrentViseme(currentTime) {
        // Trouver le visème le plus proche du temps actuel
        for (let i = 0; i < this.currentVisemes.length; i++) {
            const viseme = this.currentVisemes[i];
            if (viseme.timestamp <= currentTime && 
                (i === this.currentVisemes.length - 1 || 
                 this.currentVisemes[i + 1].timestamp > currentTime)) {
                return viseme;
            }
        }
        return null;
    }
    
    applyViseme(visemeData) {
        const mapping = this.visemeMapping[visemeData.phoneme] || this.visemeMapping['A'];
        const intensity = visemeData.intensity || 1.0;
        
        // Animer la mâchoire
        if (this.jawBone && mapping.jaw !== undefined) {
            const targetRotation = mapping.jaw * 0.15 * intensity; // Radians
            this.jawBone.rotation.x = THREE.MathUtils.lerp(
                this.jawBone.rotation.x,
                targetRotation,
                0.3
            );
        }
        
        // Animer les morph targets des lèvres
        if (this.mouthMesh && this.mouthMesh.morphTargetInfluences) {
            Object.keys(mapping).forEach((morphTarget, index) => {
                if (morphTarget !== 'jaw' && this.mouthMesh.morphTargetInfluences[index] !== undefined) {
                    const targetValue = mapping[morphTarget] * intensity;
                    this.mouthMesh.morphTargetInfluences[index] = THREE.MathUtils.lerp(
                        this.mouthMesh.morphTargetInfluences[index],
                        targetValue,
                        0.4
                    );
                }
            });
        }
    }
    
    resetMouth() {
        // Remettre la bouche en position neutre
        if (this.jawBone) {
            this.jawBone.rotation.x = 0;
        }
        
        if (this.mouthMesh && this.mouthMesh.morphTargetInfluences) {
            this.mouthMesh.morphTargetInfluences.fill(0);
        }
    }
    
    dispose() {
        this.stopLipSync();
        this.avatar = null;
        this.mouthMesh = null;
        this.jawBone = null;
    }
}

// Gestionnaire global de l'avatar
window.AvatarSystem = {
    renderer: null,
    
    init(containerId) {
        this.renderer = new AvatarRenderer(containerId);
        return this.renderer;
    },
    
    dispose() {
        if (this.renderer) {
            this.renderer.dispose();
            this.renderer = null;
        }
    }
};

// Gestion du redimensionnement
window.addEventListener('resize', () => {
    if (window.AvatarSystem.renderer) {
        window.AvatarSystem.renderer.resize();
    }
});

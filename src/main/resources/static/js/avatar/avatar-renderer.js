/**
 * FINAL: Moteur de rendu 3D pour l'avatar utilisant Three.js
 * Version finale sans cube de test
 */

class AvatarRenderer {
    constructor(container, config) {
        this.container = container;
        this.config = config;
        
        // Composants Three.js
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.controls = null;
        
        // Avatar et animations
        this.avatarModel = null;
        this.mixer = null;
        this.animations = new Map();
        this.activeAnimations = [];
        
        // État du rendu
        this.isRendering = false;
        this.clock = new THREE.Clock();
        
        // Éclairage
        this.lights = [];
        
        console.log('🎬 AvatarRenderer: Début initialisation');
        this.initialize();
    }
    
    /**
     * Initialise le moteur de rendu 3D
     */
    initialize() {
        try {
            // Nettoyer le container d'abord
            this.clearContainer();
            
            this.createScene();
            this.createCamera();
            this.createRenderer();
            this.createLights();
            this.startRenderLoop();
            
            // Gestion du redimensionnement
            window.addEventListener('resize', () => this.onWindowResize());
            
            console.log('✅ AvatarRenderer: Moteur de rendu 3D initialisé');
            
        } catch (error) {
            console.error('❌ AvatarRenderer: Erreur initialisation:', error);
            throw error;
        }
    }
    
    /**
     * Nettoie le container
     */
    clearContainer() {
        while (this.container.firstChild) {
            this.container.removeChild(this.container.firstChild);
        }
        console.log('🧹 Container nettoyé');
    }
    
    /**
     * Crée la scène 3D avec fond transparent pour l'avatar
     */
    createScene() {
        this.scene = new THREE.Scene();
        
        // CORRECTION: Fond transparent pour un rendu propre
        this.scene.background = null;
        
        console.log('📦 Scène créée');
    }
    
    /**
     * Crée la caméra avec position optimale
     */
    createCamera() {
        const width = this.container.clientWidth || 800;
        const height = this.container.clientHeight || 600;
        const aspect = width / height;
        
        this.camera = new THREE.PerspectiveCamera(50, aspect, 0.1, 1000);
        
        // Position caméra optimisée pour voir l'avatar
        this.camera.position.set(0, 1.6, 3);
        this.camera.lookAt(0, 1.6, 0);
        
        console.log('📷 Caméra créée:', {
            position: this.camera.position,
            aspect: aspect,
            size: { width, height }
        });
    }
    
    /**
     * Crée le renderer avec paramètres optimaux
     */
    createRenderer() {
        const width = this.container.clientWidth || 800;
        const height = this.container.clientHeight || 600;
        
        // Configuration renderer pour l'avatar
        this.renderer = new THREE.WebGLRenderer({ 
            antialias: this.config.get('rendering.antialiasing', true),
            alpha: true,  // Transparent pour l'avatar
            preserveDrawingBuffer: true
        });
        
        this.renderer.setSize(width, height);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        
        // Paramètres de rendu optimisés
        this.renderer.outputEncoding = THREE.sRGBEncoding;
        this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
        this.renderer.toneMappingExposure = 1;
        
        // Configuration des ombres si activée
        if (this.config.get('rendering.shadows', true)) {
            this.renderer.shadowMap.enabled = true;
            this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        }
        
        // Ajouter le canvas au container
        this.container.appendChild(this.renderer.domElement);
        
        // Styles du canvas pour l'intégration
        const canvas = this.renderer.domElement;
        canvas.style.display = 'block';
        canvas.style.width = '100%';
        canvas.style.height = '100%';
        
        console.log('🖥️ Renderer créé:', {
            size: { width, height },
            pixelRatio: this.renderer.getPixelRatio()
        });
    }
    
    /**
     * Crée l'éclairage optimisé pour l'avatar
     */
    createLights() {
        // Lumière ambiante douce
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.4);
        this.scene.add(ambientLight);
        this.lights.push(ambientLight);
        
        // Lumière directionnelle principale
        const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
        directionalLight.position.set(5, 10, 5);
        directionalLight.castShadow = true;
        directionalLight.shadow.mapSize.width = 2048;
        directionalLight.shadow.mapSize.height = 2048;
        directionalLight.shadow.camera.near = 0.1;
        directionalLight.shadow.camera.far = 50;
        directionalLight.shadow.camera.left = -5;
        directionalLight.shadow.camera.right = 5;
        directionalLight.shadow.camera.top = 5;
        directionalLight.shadow.camera.bottom = -5;
        this.scene.add(directionalLight);
        this.lights.push(directionalLight);
        
        // Lumière de remplissage
        const fillLight = new THREE.DirectionalLight(0xffffff, 0.3);
        fillLight.position.set(-5, 5, -5);
        this.scene.add(fillLight);
        this.lights.push(fillLight);
        
        // Lumière de contour
        const rimLight = new THREE.DirectionalLight(0xffffff, 0.2);
        rimLight.position.set(0, 5, -10);
        this.scene.add(rimLight);
        this.lights.push(rimLight);
        
        console.log('💡 Éclairage créé (4 sources lumineuses)');
    }
    
    /**
     * Démarre la boucle de rendu
     */
    startRenderLoop() {
        this.isRendering = true;
        console.log('🔄 Démarrage boucle de rendu');
        this.render();
    }
    
    /**
     * Boucle de rendu principale
     */
    render() {
        if (!this.isRendering) return;
        
        requestAnimationFrame(() => this.render());
        
        const delta = this.clock.getDelta();
        
        // Mise à jour des animations
        if (this.mixer) {
            this.mixer.update(delta);
        }
        
        // Mise à jour des contrôles si présents
        if (this.controls) {
            this.controls.update();
        }
        
        // Rendu de la scène
        if (this.renderer && this.scene && this.camera) {
            this.renderer.render(this.scene, this.camera);
        }
    }
    
    /**
     * Charge un modèle d'avatar 3D
     */
    async loadAvatar(modelUrl) {
        return new Promise((resolve, reject) => {
            console.log('📥 Chargement avatar:', modelUrl);
            
            if (!window.THREE || !window.THREE.GLTFLoader) {
                console.error('❌ GLTFLoader non disponible');
                reject(new Error('GLTFLoader non disponible'));
                return;
            }
            
            const loader = new THREE.GLTFLoader();
            
            loader.load(
                modelUrl,
                (gltf) => {
                    console.log('✅ Avatar chargé avec succès');
                    this.onAvatarLoaded(gltf);
                    resolve(gltf);
                },
                (progress) => {
                    const percent = (progress.loaded / progress.total) * 100;
                    console.log(`📊 Chargement: ${percent.toFixed(1)}%`);
                },
                (error) => {
                    console.error('❌ Erreur chargement avatar:', error);
                    reject(error);
                }
            );
        });
    }
    
    /**
     * Callback appelé quand l'avatar est chargé
     */
    onAvatarLoaded(gltf) {
        // Supprimer l'ancien avatar s'il existe
        if (this.avatarModel) {
            this.scene.remove(this.avatarModel);
        }
        
        this.avatarModel = gltf.scene;
        
        // Configuration du modèle
        this.avatarModel.scale.setScalar(1);
        this.avatarModel.position.set(0, 0, 0);
        
        // Activation des ombres et optimisation
        let meshCount = 0;
        this.avatarModel.traverse((child) => {
            if (child.isMesh) {
                meshCount++;
                child.castShadow = true;
                child.receiveShadow = true;
                
                // Optimisation des matériaux
                if (child.material) {
                    child.material.envMapIntensity = 0.5;
                }
            }
        });
        
        // Configuration des animations si présentes
        if (gltf.animations && gltf.animations.length > 0) {
            this.mixer = new THREE.AnimationMixer(this.avatarModel);
            
            gltf.animations.forEach((clip) => {
                const action = this.mixer.clipAction(clip);
                this.animations.set(clip.name, action);
            });
            
            console.log('🎭 Animations disponibles:', Array.from(this.animations.keys()));
        }
        
        this.scene.add(this.avatarModel);
        
        // Positionner la caméra pour voir l'avatar
        this.focusCameraOnAvatar();
        
        console.log(`✅ Avatar ajouté à la scène (${meshCount} mesh)`);
    }
    
    /**
     * Positionne la caméra pour voir l'avatar
     */
    focusCameraOnAvatar() {
        if (!this.avatarModel) return;
        
        // Calculer la bounding box
        const box = new THREE.Box3().setFromObject(this.avatarModel);
        const size = box.getSize(new THREE.Vector3());
        const center = box.getCenter(new THREE.Vector3());
        
        // Ajuster position Y pour poser l'avatar au sol
        this.avatarModel.position.y = -box.min.y;
        
        // Repositionner la caméra
        const maxDim = Math.max(size.x, size.y, size.z);
        const cameraDistance = maxDim * 3;
        
        this.camera.position.set(0, size.y * 0.6, cameraDistance);
        this.camera.lookAt(0, size.y * 0.5, 0);
        
        // Mettre à jour les contrôles si ils existent
        if (this.controls) {
            this.controls.target.set(0, size.y * 0.5, 0);
            this.controls.update();
        }
        
        console.log('📷 Caméra repositionnée pour l\'avatar');
    }
    
    /**
     * Joue une animation
     */
    playAnimation(name, options = {}) {
        if (!this.animations.has(name)) {
            console.warn(`Animation '${name}' non trouvée`);
            return;
        }
        
        const action = this.animations.get(name);
        
        // Configuration de l'animation
        action.reset();
        action.setEffectiveTimeScale(options.speed || 1);
        action.setEffectiveWeight(options.weight || 1);
        
        if (options.loop) {
            action.setLoop(THREE.LoopRepeat);
        } else {
            action.setLoop(THREE.LoopOnce);
            action.clampWhenFinished = true;
        }
        
        // Transition douce si spécifiée
        if (options.fadeIn) {
            action.fadeIn(options.fadeIn);
        } else {
            action.play();
        }
        
        this.activeAnimations.push(action);
        
        console.log(`🎭 Animation '${name}' démarrée`);
        
        return action;
    }
    
    /**
     * Arrête une animation
     */
    stopAnimation(name, fadeOut = 0.5) {
        if (!this.animations.has(name)) {
            return;
        }
        
        const action = this.animations.get(name);
        
        if (fadeOut > 0) {
            action.fadeOut(fadeOut);
        } else {
            action.stop();
        }
        
        // Retirer de la liste des animations actives
        const index = this.activeAnimations.indexOf(action);
        if (index > -1) {
            this.activeAnimations.splice(index, 1);
        }
    }
    
    /**
     * Change l'émotion de l'avatar
     */
    setEmotion(emotion, intensity = 0.7) {
        console.log(`😊 Changement d'émotion: ${emotion} (${intensity})`);
        
        const emotionAnimation = this.getEmotionAnimation(emotion);
        if (emotionAnimation) {
            this.playAnimation(emotionAnimation, { weight: intensity, fadeIn: 0.5 });
        }
    }
    
    /**
     * Obtient l'animation correspondant à une émotion
     */
    getEmotionAnimation(emotion) {
        const emotionMap = {
            'happy': 'smile',
            'sad': 'sad_expression',
            'excited': 'excited_expression',
            'concerned': 'concerned_expression',
            'thoughtful': 'thinking_expression',
            'neutral': 'idle'
        };
        
        return emotionMap[emotion] || 'idle';
    }
    
    /**
     * Exécute un geste
     */
    playGesture(gestureType) {
        console.log(`👋 Geste: ${gestureType}`);
        
        const gestureAnimation = this.getGestureAnimation(gestureType);
        if (gestureAnimation) {
            this.playAnimation(gestureAnimation, { loop: false, fadeIn: 0.2 });
        }
    }
    
    /**
     * Obtient l'animation correspondant à un geste
     */
    getGestureAnimation(gestureType) {
        const gestureMap = {
            'wave': 'wave_animation',
            'nod': 'nod_animation',
            'shake': 'shake_head',
            'shrug': 'shrug_animation',
            'point': 'point_animation',
            'thumbsup': 'thumbs_up',
            'goodbye_wave': 'goodbye_wave',
            'thinking': 'thinking_gesture',
            'clap': 'clap_animation'
        };
        
        return gestureMap[gestureType];
    }
    
    /**
     * Gestion du redimensionnement de la fenêtre
     */
    onWindowResize() {
        if (!this.camera || !this.renderer) return;
        
        const width = this.container.clientWidth;
        const height = this.container.clientHeight;
        
        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        
        this.renderer.setSize(width, height);
        
        console.log('📐 Redimensionnement:', { width, height });
    }
    
    /**
     * Nettoie les ressources
     */
    dispose() {
        this.isRendering = false;
        
        if (this.renderer) {
            this.renderer.dispose();
        }
        
        if (this.controls) {
            this.controls.dispose();
        }
        
        // Nettoyer les animations
        if (this.mixer) {
            this.mixer.stopAllAction();
        }
        
        // Nettoyer la scène
        while (this.scene && this.scene.children.length > 0) {
            this.scene.remove(this.scene.children[0]);
        }
        
        // Nettoyer le container
        while (this.container && this.container.firstChild) {
            this.container.removeChild(this.container.firstChild);
        }
        
        console.log('🧹 AvatarRenderer nettoyé');
    }
    
    /**
     * Obtient les informations de debug
     */
    getDebugInfo() {
        return {
            renderer: this.renderer ? this.renderer.info : null,
            animations: Array.from(this.animations.keys()),
            activeAnimations: this.activeAnimations.length,
            avatarLoaded: !!this.avatarModel,
            isRendering: this.isRendering
        };
    }
}

// Export global
window.AvatarRenderer = AvatarRenderer;
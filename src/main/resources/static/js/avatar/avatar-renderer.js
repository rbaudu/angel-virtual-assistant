/**
 * Moteur de rendu 3D pour l'avatar utilisant Three.js
 * Gère le chargement, l'affichage et les animations de l'avatar 3D
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
        
        this.initialize();
    }
    
    /**
     * Initialise le moteur de rendu 3D
     */
    initialize() {
        this.createScene();
        this.createCamera();
        this.createRenderer();
        this.createLights();
        this.createControls();
        this.startRenderLoop();
        
        // Gestion du redimensionnement
        window.addEventListener('resize', () => this.onWindowResize());
        
        console.log('Moteur de rendu 3D initialisé');
    }
    
    /**
     * Crée la scène 3D
     */
    createScene() {
        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0xf0f0f0);
        
        // Ajout d'un environnement de base
        const geometry = new THREE.PlaneGeometry(10, 10);
        const material = new THREE.MeshLambertMaterial({ color: 0xffffff });
        const floor = new THREE.Mesh(geometry, material);
        floor.rotation.x = -Math.PI / 2;
        floor.position.y = -1;
        this.scene.add(floor);
    }
    
    /**
     * Crée la caméra
     */
    createCamera() {
        const aspect = this.container.clientWidth / this.container.clientHeight;
        this.camera = new THREE.PerspectiveCamera(50, aspect, 0.1, 1000);
        this.camera.position.set(0, 1.6, 3);
        this.camera.lookAt(0, 1.6, 0);
    }
    
    /**
     * Crée le renderer
     */
    createRenderer() {
        this.renderer = new THREE.WebGLRenderer({ 
            antialias: this.config.get('rendering.antialiasing', true),
            alpha: true
        });
        
        this.renderer.setSize(this.container.clientWidth, this.container.clientHeight);
        this.renderer.setPixelRatio(window.devicePixelRatio);
        
        // Configuration des ombres
        if (this.config.get('rendering.shadows', true)) {
            this.renderer.shadowMap.enabled = true;
            this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        }
        
        // Paramètres de rendu
        this.renderer.outputEncoding = THREE.sRGBEncoding;
        this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
        this.renderer.toneMappingExposure = 1;
        
        this.container.appendChild(this.renderer.domElement);
    }
    
    /**
     * Crée l'éclairage de la scène
     */
    createLights() {
        // Lumière ambiante
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
    }
    
    /**
     * Crée les contrôles de caméra
     */
    createControls() {
        if (typeof THREE.OrbitControls !== 'undefined') {
            this.controls = new THREE.OrbitControls(this.camera, this.renderer.domElement);
            this.controls.target.set(0, 1.6, 0);
            this.controls.enableDamping = true;
            this.controls.dampingFactor = 0.1;
            this.controls.enableZoom = true;
            this.controls.enablePan = false;
            this.controls.minDistance = 1;
            this.controls.maxDistance = 10;
            this.controls.minPolarAngle = Math.PI / 6;
            this.controls.maxPolarAngle = Math.PI / 2;
        }
    }
    
    /**
     * Démarre la boucle de rendu
     */
    startRenderLoop() {
        this.isRendering = true;
        this.render();
    }
    
    /**
     * Arrête la boucle de rendu
     */
    stopRenderLoop() {
        this.isRendering = false;
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
        
        // Mise à jour des contrôles
        if (this.controls) {
            this.controls.update();
        }
        
        // Rendu de la scène
        this.renderer.render(this.scene, this.camera);
    }
    
    /**
     * Charge un modèle d'avatar 3D
     */
    async loadAvatar(modelUrl) {
        return new Promise((resolve, reject) => {
            console.log('Chargement du modèle avatar:', modelUrl);
            
            const loader = new THREE.GLTFLoader();
            
            loader.load(
                modelUrl,
                (gltf) => {
                    this.onAvatarLoaded(gltf);
                    resolve(gltf);
                },
                (progress) => {
                    const percent = (progress.loaded / progress.total) * 100;
                    console.log(`Chargement: ${percent.toFixed(1)}%`);
                },
                (error) => {
                    console.error('Erreur lors du chargement du modèle:', error);
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
        
        // Activation des ombres
        this.avatarModel.traverse((child) => {
            if (child.isMesh) {
                child.castShadow = true;
                child.receiveShadow = true;
                
                // Optimisation des matériaux
                if (child.material) {
                    child.material.envMapIntensity = 0.5;
                }
            }
        });
        
        // Configuration des animations
        if (gltf.animations && gltf.animations.length > 0) {
            this.mixer = new THREE.AnimationMixer(this.avatarModel);
            
            gltf.animations.forEach((clip) => {
                const action = this.mixer.clipAction(clip);
                this.animations.set(clip.name, action);
            });
            
            // Animation par défaut
            this.playAnimation('idle', { loop: true });
        }
        
        this.scene.add(this.avatarModel);
        
        console.log('Avatar chargé avec succès');
        console.log('Animations disponibles:', Array.from(this.animations.keys()));
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
        
        console.log(`Animation '${name}' démarrée`);
        
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
     * Arrête toutes les animations
     */
    stopAllAnimations(fadeOut = 0.5) {
        this.activeAnimations.forEach(action => {
            if (fadeOut > 0) {
                action.fadeOut(fadeOut);
            } else {
                action.stop();
            }
        });
        this.activeAnimations = [];
    }
    
    /**
     * Change l'émotion de l'avatar
     */
    setEmotion(emotion, intensity = 0.7) {
        console.log(`Changement d'émotion: ${emotion} (${intensity})`);
        
        // Ici, on pourrait modifier les expressions faciales
        // ou jouer des animations spécifiques à l'émotion
        
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
        console.log(`Geste: ${gestureType}`);
        
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
        const width = this.container.clientWidth;
        const height = this.container.clientHeight;
        
        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        
        this.renderer.setSize(width, height);
    }
    
    /**
     * Nettoie les ressources
     */
    dispose() {
        this.stopRenderLoop();
        
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
        while (this.scene.children.length > 0) {
            this.scene.remove(this.scene.children[0]);
        }
        
        console.log('Moteur de rendu nettoyé');
    }
    
    /**
     * Obtient les informations de debug
     */
    getDebugInfo() {
        return {
            renderer: this.renderer.info,
            animations: Array.from(this.animations.keys()),
            activeAnimations: this.activeAnimations.length,
            avatarLoaded: !!this.avatarModel
        };
    }
}

// Export global
window.AvatarRenderer = AvatarRenderer;
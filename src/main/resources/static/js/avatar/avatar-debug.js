/**
 * DIAGNOSTIC AVANCÉ pour l'avatar - Fichier avatar-debug.js
 * À ajouter dans src/main/resources/static/js/avatar-debug.js
 */

// Fonctions de diagnostic global
window.avatarDiagnostic = {
    
    /**
     * Test complet du système de rendu
     */
    fullDiagnostic() {
        console.log('🔍 === DIAGNOSTIC COMPLET AVATAR ===');
        
        // 1. Vérifier les dépendances
        this.checkDependencies();
        
        // 2. Vérifier les éléments DOM
        this.checkDOM();
        
        // 3. Vérifier l'instance Angel
        this.checkAngelApp();
        
        // 4. Vérifier le renderer
        this.checkRenderer();
        
        // 5. Vérifier la scène 3D
        this.checkScene();
        
        // 6. Test de rendu
        this.testRendering();
        
        console.log('🔍 === FIN DIAGNOSTIC ===');
    },
    
    /**
     * Vérifier les dépendances
     */
    checkDependencies() {
        console.log('📚 Vérification des dépendances:');
        
        const deps = {
            'THREE': typeof THREE !== 'undefined',
            'THREE.WebGLRenderer': typeof THREE !== 'undefined' && !!THREE.WebGLRenderer,
            'THREE.Scene': typeof THREE !== 'undefined' && !!THREE.Scene,
            'THREE.PerspectiveCamera': typeof THREE !== 'undefined' && !!THREE.PerspectiveCamera,
            'THREE.GLTFLoader': typeof THREE !== 'undefined' && !!THREE.GLTFLoader,
            'AngelAvatarApp': typeof window.AngelAvatarApp !== 'undefined',
            'AvatarRenderer': typeof window.AvatarRenderer !== 'undefined'
        };
        
        for (const [name, available] of Object.entries(deps)) {
            console.log(`  ${available ? '✅' : '❌'} ${name}`);
        }
        
        return Object.values(deps).every(d => d);
    },
    
    /**
     * Vérifier les éléments DOM
     */
    checkDOM() {
        console.log('🏗️ Vérification DOM:');
        
        const elements = [
            'avatar-container',
            'avatar-viewport', 
            'avatar-status',
            'message-bubble',
            'loading-spinner'
        ];
        
        elements.forEach(id => {
            const el = document.getElementById(id);
            const rect = el ? el.getBoundingClientRect() : null;
            
            console.log(`  ${el ? '✅' : '❌'} #${id}`, rect ? {
                width: rect.width,
                height: rect.height,
                visible: rect.width > 0 && rect.height > 0
            } : 'Non trouvé');
        });
    },
    
    /**
     * Vérifier l'instance AngelApp
     */
    checkAngelApp() {
        console.log('🎭 Vérification AngelApp:');
        
        if (window.angelApp) {
            console.log('  ✅ Instance trouvée');
            console.log('  📊 État:', {
                isInitialized: window.angelApp.isInitialized,
                hasRenderer: !!window.angelApp.avatarRenderer,
                initAttempts: window.angelApp.initAttempts || 0
            });
        } else {
            console.log('  ❌ Aucune instance AngelApp');
        }
    },
    
    /**
     * Vérifier le renderer
     */
    checkRenderer() {
        console.log('🖥️ Vérification Renderer:');
        
        const renderer = window.angelApp?.avatarRenderer;
        if (!renderer) {
            console.log('  ❌ Pas de renderer');
            return;
        }
        
        console.log('  ✅ Renderer trouvé');
        console.log('  📊 État:', {
            hasScene: !!renderer.scene,
            hasCamera: !!renderer.camera,
            hasRenderer: !!renderer.renderer,
            isRendering: renderer.isRendering,
            containerHasCanvas: !!renderer.container?.querySelector('canvas')
        });
        
        if (renderer.renderer) {
            const size = new THREE.Vector2();
            renderer.renderer.getSize(size);
            console.log('  📐 Taille renderer:', size);
            console.log('  🎨 Clear color:', renderer.renderer.getClearColor().getHex());
        }
    },
    
    /**
     * Vérifier la scène 3D
     */
    checkScene() {
        console.log('🎬 Vérification Scène:');
        
        const renderer = window.angelApp?.avatarRenderer;
        if (!renderer?.scene) {
            console.log('  ❌ Pas de scène');
            return;
        }
        
        const scene = renderer.scene;
        console.log('  ✅ Scène trouvée');
        console.log('  👥 Objets dans la scène:', scene.children.length);
        
        scene.children.forEach((child, index) => {
            console.log(`    ${index}: ${child.type} (${child.name || 'sans nom'})`);
            
            if (child.isMesh) {
                console.log(`      🔹 Mesh visible: ${child.visible}`);
                console.log(`      🔹 Position:`, child.position);
                console.log(`      🔹 Scale:`, child.scale);
            }
        });
        
        // Vérifier avatar spécifiquement
        if (renderer.avatarModel) {
            console.log('  👤 Avatar trouvé:');
            const box = new THREE.Box3().setFromObject(renderer.avatarModel);
            console.log('    📦 Bounding box:', box);
            console.log('    👁️ Visible:', renderer.avatarModel.visible);
            console.log('    📍 Position:', renderer.avatarModel.position);
        }
    },
    
    /**
     * Test de rendu avec objets simples
     */
    testRendering() {
        console.log('🧪 Test de rendu:');
        
        const renderer = window.angelApp?.avatarRenderer;
        if (!renderer) {
            console.log('  ❌ Impossible de tester sans renderer');
            return;
        }
        
        try {
            // Test 1: Ajouter un cube rouge simple
            console.log('  🎲 Test cube rouge...');
            this.addTestCube(renderer);
            
            // Test 2: Forcer un rendu
            console.log('  🖼️ Force rendu...');
            if (renderer.renderer && renderer.scene && renderer.camera) {
                renderer.renderer.render(renderer.scene, renderer.camera);
                console.log('    ✅ Rendu exécuté');
            }
            
            // Test 3: Vérifier le canvas
            const canvas = renderer.container?.querySelector('canvas');
            if (canvas) {
                console.log('    📱 Canvas:', {
                    width: canvas.width,
                    height: canvas.height,
                    style: {
                        display: canvas.style.display,
                        visibility: canvas.style.visibility,
                        opacity: canvas.style.opacity
                    }
                });
            }
            
        } catch (error) {
            console.error('  ❌ Erreur test rendu:', error);
        }
    },
    
    /**
     * Ajouter un cube de test visible
     */
    addTestCube(renderer) {
        // Supprimer ancien cube
        const oldCube = renderer.scene.getObjectByName('diagnosticCube');
        if (oldCube) {
            renderer.scene.remove(oldCube);
        }
        
        // Créer nouveau cube avec matériau très visible
        const geometry = new THREE.BoxGeometry(2, 2, 2);
        const material = new THREE.MeshBasicMaterial({ 
            color: 0xff0000,
            wireframe: false
        });
        const cube = new THREE.Mesh(geometry, material);
        
        cube.position.set(0, 0, 0);
        cube.name = 'diagnosticCube';
        
        renderer.scene.add(cube);
        
        console.log('    ✅ Cube de diagnostic ajouté');
        
        // Animation pour le rendre visible
        let rotation = 0;
        const animate = () => {
            if (cube.parent) {
                rotation += 0.02;
                cube.rotation.x = rotation;
                cube.rotation.y = rotation;
                
                if (rotation < Math.PI * 4) { // 2 tours
                    requestAnimationFrame(animate);
                }
            }
        };
        animate();
    },
    
    /**
     * Forcer la réinitialisation complète
     */
    forceReset() {
        console.log('🔄 RÉINITIALISATION FORCÉE...');
        
        // 1. Nettoyer l'instance actuelle
        if (window.angelApp) {
            if (typeof window.angelApp.dispose === 'function') {
                window.angelApp.dispose();
            }
            window.angelApp = null;
            window.angelAppInstance = null;
        }
        
        // 2. Nettoyer le container
        const container = document.getElementById('avatar-viewport');
        if (container) {
            while (container.firstChild) {
                container.removeChild(container.firstChild);
            }
        }
        
        // 3. Recréer l'instance
        setTimeout(() => {
            try {
                console.log('🎭 Recréation AngelAvatarApp...');
                window.angelApp = new AngelAvatarApp();
                window.angelApp.init().catch(console.error);
            } catch (error) {
                console.error('❌ Erreur recréation:', error);
            }
        }, 1000);
    },
    
    /**
     * Test minimaliste de Three.js
     */
    testThreeJS() {
        console.log('🧪 TEST MINIMALISTE THREE.JS...');
        
        try {
            const container = document.getElementById('avatar-viewport');
            if (!container) {
                console.error('❌ Container manquant');
                return;
            }
            
            // Nettoyer
            while (container.firstChild) {
                container.removeChild(container.firstChild);
            }
            
            // Créer scène simple
            const scene = new THREE.Scene();
            scene.background = new THREE.Color(0x333333);
            
            const camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
            camera.position.z = 5;
            
            const renderer = new THREE.WebGLRenderer();
            renderer.setSize(container.clientWidth, container.clientHeight);
            container.appendChild(renderer.domElement);
            
            // Créer cube
            const geometry = new THREE.BoxGeometry();
            const material = new THREE.MeshBasicMaterial({ color: 0x00ff00 });
            const cube = new THREE.Mesh(geometry, material);
            scene.add(cube);
            
            // Animer
            const animate = () => {
                requestAnimationFrame(animate);
                cube.rotation.x += 0.01;
                cube.rotation.y += 0.01;
                renderer.render(scene, camera);
            };
            
            animate();
            
            console.log('✅ Test Three.js: cube vert qui tourne créé');
            
        } catch (error) {
            console.error('❌ Erreur test Three.js:', error);
        }
    }
};

// Raccourcis pour la console
window.diag = () => window.avatarDiagnostic.fullDiagnostic();
window.reset = () => window.avatarDiagnostic.forceReset();
window.test3d = () => window.avatarDiagnostic.testThreeJS();

console.log('🔧 Avatar Diagnostic chargé');
console.log('💡 Commandes disponibles:');
console.log('  diag() - Diagnostic complet');
console.log('  reset() - Réinitialisation forcée');
console.log('  test3d() - Test Three.js basique');
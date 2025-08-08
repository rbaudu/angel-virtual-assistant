# Guide d'Intégration Avatar - Angel Virtual Assistant

Guide pratique pour intégrer et personnaliser l'avatar 3D dans votre environnement.

## 🎯 Vue d'Ensemble

Ce guide vous accompagne dans l'intégration complète de l'avatar 3D d'Angel Virtual Assistant, de l'installation initiale à la personnalisation avancée.

### Prérequis Techniques
- **Navigateur** : Chrome/Firefox/Safari/Edge récent
- **WebGL** : Support WebGL 1.0 minimum (WebGL 2.0 recommandé)
- **RAM** : 4GB minimum (8GB recommandé)
- **GPU** : Carte graphique dédiée recommandée
- **Connexion** : Accès internet pour Ready Player Me

## 🚀 Installation Rapide

### 1. Vérification des Prérequis

```bash
# Test WebGL support
./angel-launcher.sh test-webgl

# Vérification des ressources système
./angel-launcher.sh system-check

# Test connectivité Ready Player Me
curl -I https://models.readyplayer.me/
```

### 2. Démarrage Simple

```bash
# Mode test (recommandé pour débuter)
./angel-launcher.sh start -p test

# Accès direct à l'avatar
open http://localhost:8081/angel

# Test vocal simple
# Dites "Angel" puis "qui es-tu ?"
```

### 3. Vérification du Fonctionnement

```javascript
// Dans la console du navigateur (F12)

// Vérifier que l'avatar est chargé
console.log('Avatar loaded:', !!window.avatarRenderer?.avatar);

// Test de parole
window.enhancedSpeechIntegration?.testSpeech();

// Test reconnaissance vocale
window.wakeWordDetector?.startListening();

// Changer émotion
window.avatarRenderer?.setEmotion('happy', 0.8);
```

## 🎭 Configuration Avatar

### Configuration de Base

Dans `config/avatar.properties` :

```properties
# ===============================================
# Configuration Avatar - Intégration
# ===============================================

# Avatar Ready Player Me
avatar.ready-player-me.enabled=true
avatar.ready-player-me.default-id=687f66fafe8107131699bf7b
avatar.ready-player-me.fallback-model=/models/avatars/default.glb

# Qualité de rendu (low/medium/high)
avatar.rendering.quality=medium
avatar.rendering.antialiasing=true
avatar.rendering.shadows=true
avatar.rendering.fps-target=60

# Interface utilisateur
avatar.web.controls.enabled=true
avatar.web.controls.mute-button=true
avatar.web.controls.settings-button=true
avatar.web.controls.fullscreen-button=true

# Animations
avatar.animations.speaking.enabled=true
avatar.animations.emotions.enabled=true
avatar.animations.idle.enabled=true
```

### Personnalisation Avancée

```properties
# Personnalisation visuelle
avatar.appearance.lighting.ambient-intensity=0.6
avatar.appearance.lighting.directional-intensity=0.8
avatar.appearance.background.color=#f0f0f0
avatar.appearance.background.enabled=true

# Performance
avatar.performance.lod-enabled=true
avatar.performance.culling-enabled=true
avatar.performance.mobile-optimizations=auto

# Comportement
avatar.behavior.idle-timeout=30000
avatar.behavior.dark-mode-timeout=300000
avatar.behavior.auto-sleep=true
```

## 🎨 Personnalisation Visuelle

### 1. Changement d'Avatar Ready Player Me

```javascript
// Charger un nouvel avatar
const newAvatarId = 'votre-nouveau-id-avatar';
window.avatarRenderer.loadAvatar(`https://models.readyplayer.me/${newAvatarId}.glb`);

// Ou via l'API REST
fetch('/api/avatar/model', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        type: 'ready-player-me',
        id: newAvatarId
    })
});
```

### 2. Personnalisation de l'Éclairage

```javascript
// Modifier l'éclairage via JavaScript
const scene = window.avatarRenderer.scene;

// Lumière ambiante plus chaleureuse
const ambientLight = scene.getObjectByName('ambientLight');
ambientLight.intensity = 0.7;
ambientLight.color.setHex(0xfff4e6);

// Lumière directionnelle
const directionalLight = scene.getObjectByName('directionalLight');
directionalLight.position.set(2, 4, 3);
directionalLight.intensity = 0.9;
```

### 3. Personnalisation du Background

```css
/* Dans avatar.css */
#avatar-container {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 15px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.3);
}

/* Thème sombre */
.dark-theme #avatar-container {
    background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
}
```

## 🎤 Intégration Reconnaissance Vocale

### 1. Configuration Wake Word

```javascript
// Configuration personnalisée du wake word
window.wakeWordConfig = {
    enabled: true,
    words: ['angel', 'angèle', 'hey angel'],
    threshold: 0.7,
    language: 'fr-FR',
    continuous: true
};

// Redémarrer la détection avec nouvelle config
window.wakeWordDetector.updateConfig(window.wakeWordConfig);
```

### 2. Commandes Vocales Personnalisées

```javascript
// Ajouter des commandes personnalisées
window.customVoiceCommands = {
    'angel mode présentation': () => {
        window.avatarRenderer.setEmotion('confident', 1.0);
        window.enhancedSpeechIntegration.speak(
            "Je suis maintenant en mode présentation. Comment puis-je vous aider ?", 
            'professional'
        );
    },
    
    'angel raconte une blague': () => {
        const jokes = [
            "Pourquoi les plongeurs plongent-ils toujours en arrière et jamais en avant ? Parce que sinon, ils tombent dans le bateau !",
            "Que dit un escargot quand il croise une limace ? Regarde, un nudiste !"
        ];
        const joke = jokes[Math.floor(Math.random() * jokes.length)];
        window.avatarRenderer.setEmotion('happy', 0.9);
        window.enhancedSpeechIntegration.speak(joke, 'happy');
    },
    
    'angel change d\'humeur': () => {
        const emotions = ['happy', 'sad', 'surprised', 'neutral', 'excited'];
        const emotion = emotions[Math.floor(Math.random() * emotions.length)];
        window.avatarRenderer.setEmotion(emotion, 0.8);
        window.enhancedSpeechIntegration.speak(
            `Je change d'humeur... Je suis maintenant ${emotion}`, 
            emotion
        );
    }
};

// Enregistrer les commandes
Object.entries(window.customVoiceCommands).forEach(([command, handler]) => {
    window.voiceCommandProcessor.addCommand(command, handler);
});
```

## 🔧 Intégration dans Votre Application

### 1. Intégration iframe

```html
<!-- Intégration simple via iframe -->
<iframe 
    src="http://localhost:8081/angel" 
    width="800" 
    height="600" 
    frameborder="0"
    allow="microphone; camera"
    id="angel-avatar-frame">
</iframe>

<script>
// Communication avec l'iframe
const avatarFrame = document.getElementById('angel-avatar-frame');

// Envoyer une commande à l'avatar
function sendToAvatar(command, data) {
    avatarFrame.contentWindow.postMessage({
        type: 'avatar-command',
        command: command,
        data: data
    }, '*');
}

// Écouter les réponses de l'avatar
window.addEventListener('message', (event) => {
    if (event.data.type === 'avatar-response') {
        console.log('Avatar response:', event.data);
    }
});

// Faire parler l'avatar depuis votre app
sendToAvatar('speak', {
    text: 'Bonjour depuis l\'application parente !',
    emotion: 'friendly'
});

// Changer l'émotion
sendToAvatar('setEmotion', {
    emotion: 'excited',
    intensity: 0.9
});
</script>
```

### 2. Intégration JavaScript Directe

```javascript
// Charger Angel Virtual Assistant dans votre page
class AngelAvatarIntegration {
    constructor(containerId, config = {}) {
        this.container = document.getElementById(containerId);
        this.config = {
            avatarId: '687f66fafe8107131699bf7b',
            quality: 'medium',
            voiceEnabled: true,
            controls: true,
            ...config
        };
        
        this.init();
    }
    
    async init() {
        await this.loadAngelScripts();
        
        this.avatarRenderer = new window.AvatarRenderer({
            container: this.container,
            avatarId: this.config.avatarId,
            quality: this.config.quality
        });
        
        if (this.config.voiceEnabled) {
            this.voiceSystem = new window.WakeWordDetector({
                onWakeWordDetected: () => this.onWakeWord(),
                onCommandProcessed: (result) => this.onCommand(result)
            });
        }
        
        this.setupEventListeners();
    }
    
    async loadAngelScripts() {
        const scripts = [
            '/js/avatar/avatar-renderer.js',
            '/js/voice/wake-word-detector.js',
            '/js/avatar-speech-integration.js'
        ];
        
        for (const script of scripts) {
            await this.loadScript(script);
        }
    }
    
    loadScript(src) {
        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = src;
            script.onload = resolve;
            script.onerror = reject;
            document.head.appendChild(script);
        });
    }
    
    setupEventListeners() {
        // Écouter les événements avatar
        document.addEventListener('avatar-speaking-started', (e) => {
            this.onSpeakingStarted(e.detail);
        });
        
        document.addEventListener('avatar-emotion-changed', (e) => {
            this.onEmotionChanged(e.detail);
        });
    }
    
    onWakeWord() {
        console.log('Wake word détecté !');
        this.setEmotion('attentive', 0.9);
    }
    
    onCommand(result) {
        console.log('Commande traitée:', result);
        this.speak(result.response, result.emotion || 'neutral');
    }
    
    onSpeakingStarted(data) {
        console.log('Avatar commence à parler:', data.text);
    }
    
    onEmotionChanged(data) {
        console.log('Émotion changée:', data.emotion, data.intensity);
    }
    
    // API publique
    speak(text, emotion = 'neutral') {
        return this.avatarRenderer.speak(text, emotion);
    }
    
    setEmotion(emotion, intensity = 0.8) {
        this.avatarRenderer.setEmotion(emotion, intensity);
    }
    
    startListening() {
        if (this.voiceSystem) {
            this.voiceSystem.startListening();
        }
    }
    
    stopListening() {
        if (this.voiceSystem) {
            this.voiceSystem.stopListening();
        }
    }
    
    updateConfig(config) {
        this.config = { ...this.config, ...config };
        if (this.avatarRenderer) {
            this.avatarRenderer.updateConfig(config);
        }
    }
    
    getStatus() {
        return {
            avatarLoaded: !!this.avatarRenderer?.avatar,
            isListening: this.voiceSystem?.isListening || false,
            currentEmotion: this.avatarRenderer?.currentEmotion || 'neutral',
            isSpeaking: this.avatarRenderer?.isSpeaking || false
        };
    }
    
    cleanup() {
        if (this.avatarRenderer) {
            this.avatarRenderer.dispose();
        }
        if (this.voiceSystem) {
            this.voiceSystem.stop();
        }
    }
}

// Utilisation
const angel = new AngelAvatarIntegration('my-avatar-container', {
    quality: 'high',
    voiceEnabled: true,
    controls: false
});

// Tester l'avatar
setTimeout(() => {
    angel.speak('Bonjour ! Je suis Angel, votre assistant virtuel.', 'friendly');
    angel.setEmotion('happy', 0.8);
}, 2000);
```

### 3. Intégration React

```javascript
// Composant React pour Angel Avatar
import React, { useEffect, useRef, useCallback } from 'react';

const AngelAvatar = ({ 
    avatarId = '687f66fafe8107131699bf7b',
    quality = 'medium',
    voiceEnabled = true,
    onWakeWord,
    onCommand,
    onSpeakingStart,
    onEmotionChange
}) => {
    const containerRef = useRef(null);
    const angelRef = useRef(null);
    
    useEffect(() => {
        const containerId = `angel-container-${Date.now()}`;
        containerRef.current.id = containerId;
        
        // Initialiser Angel
        angelRef.current = new AngelAvatarIntegration(containerId, { 
            avatarId, 
            quality,
            voiceEnabled
        });
        
        // Gestionnaires d'événements
        const handleWakeWord = () => onWakeWord?.();
        const handleCommand = (e) => onCommand?.(e.detail);
        const handleSpeakingStart = (e) => onSpeakingStart?.(e.detail);
        const handleEmotionChange = (e) => onEmotionChange?.(e.detail);
        
        document.addEventListener('wake-word-detected', handleWakeWord);
        document.addEventListener('command-processed', handleCommand);
        document.addEventListener('avatar-speaking-started', handleSpeakingStart);
        document.addEventListener('avatar-emotion-changed', handleEmotionChange);
        
        return () => {
            document.removeEventListener('wake-word-detected', handleWakeWord);
            document.removeEventListener('command-processed', handleCommand);
            document.removeEventListener('avatar-speaking-started', handleSpeakingStart);
            document.removeEventListener('avatar-emotion-changed', handleEmotionChange);
            angelRef.current?.cleanup();
        };
    }, [avatarId, quality, voiceEnabled]);
    
    const speak = useCallback((text, emotion) => {
        angelRef.current?.speak(text, emotion);
    }, []);
    
    const setEmotion = useCallback((emotion, intensity) => {
        angelRef.current?.setEmotion(emotion, intensity);
    }, []);
    
    const startListening = useCallback(() => {
        angelRef.current?.startListening();
    }, []);
    
    const stopListening = useCallback(() => {
        angelRef.current?.stopListening();
    }, []);
    
    const getStatus = useCallback(() => {
        return angelRef.current?.getStatus();
    }, []);
    
    // Exposer les méthodes via ref
    React.useImperativeHandle(ref, () => ({
        speak,
        setEmotion,
        startListening,
        stopListening,
        getStatus
    }));
    
    return (
        <div className="angel-avatar-wrapper">
            <div 
                ref={containerRef}
                className="angel-avatar-container"
                style={{ 
                    width: '100%', 
                    height: '400px',
                    position: 'relative'
                }}
            />
        </div>
    );
};

// Hook personnalisé pour Angel Avatar
const useAngelAvatar = () => {
    const angelRef = useRef(null);
    
    const speak = useCallback((text, emotion) => {
        angelRef.current?.speak(text, emotion);
    }, []);
    
    const setEmotion = useCallback((emotion, intensity) => {
        angelRef.current?.setEmotion(emotion, intensity);
    }, []);
    
    return {
        angelRef,
        speak,
        setEmotion,
        startListening: () => angelRef.current?.startListening(),
        stopListening: () => angelRef.current?.stopListening(),
        getStatus: () => angelRef.current?.getStatus()
    };
};

// Utilisation du composant
function App() {
    const { angelRef, speak, setEmotion } = useAngelAvatar();
    
    return (
        <div className="App">
            <h1>Mon Application avec Angel</h1>
            
            <AngelAvatar 
                ref={angelRef}
                avatarId="687f66fafe8107131699bf7b"
                quality="high"
                voiceEnabled={true}
                onWakeWord={() => console.log('Wake word détecté!')}
                onCommand={(command) => console.log('Commande:', command)}
                onSpeakingStart={(data) => console.log('Parole démarrée:', data)}
                onEmotionChange={(data) => console.log('Émotion:', data)}
            />
            
            <div className="avatar-controls">
                <button onClick={() => speak("Bonjour !", "happy")}>
                    Dire Bonjour
                </button>
                <button onClick={() => setEmotion("excited", 0.9)}>
                    Mode Excité
                </button>
                <button onClick={() => setEmotion("calm", 0.7)}>
                    Mode Calme
                </button>
            </div>
        </div>
    );
}

export { AngelAvatar, useAngelAvatar };
```

### 4. Intégration Vue.js

```javascript
// Composant Vue pour Angel Avatar
<template>
  <div class="angel-avatar-wrapper">
    <div 
      ref="avatarContainer" 
      class="angel-avatar-container"
      :style="{ width: '100%', height: height + 'px' }"
    />
    
    <div v-if="showControls" class="avatar-controls">
      <button @click="speak('Bonjour !', 'happy')" class="control-btn">
        💬 Parler
      </button>
      <button @click="setEmotion('excited', 0.9)" class="control-btn">
        😄 Excité
      </button>
      <button @click="toggleListening" class="control-btn">
        {{ isListening ? '🔇 Stop' : '🎤 Écouter' }}
      </button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, watch } from 'vue';

export default {
  name: 'AngelAvatar',
  props: {
    avatarId: {
      type: String,
      default: '687f66fafe8107131699bf7b'
    },
    quality: {
      type: String,
      default: 'medium',
      validator: value => ['low', 'medium', 'high'].includes(value)
    },
    height: {
      type: Number,
      default: 400
    },
    voiceEnabled: {
      type: Boolean,
      default: true
    },
    showControls: {
      type: Boolean,
      default: true
    }
  },
  emits: ['wake-word', 'command', 'speaking-start', 'emotion-change'],
  setup(props, { emit }) {
    const avatarContainer = ref(null);
    const angel = ref(null);
    const isListening = ref(false);
    const currentEmotion = ref('neutral');
    const isSpeaking = ref(false);
    
    const initializeAvatar = async () => {
      if (!avatarContainer.value) return;
      
      const containerId = `angel-container-${Date.now()}`;
      avatarContainer.value.id = containerId;
      
      angel.value = new AngelAvatarIntegration(containerId, {
        avatarId: props.avatarId,
        quality: props.quality,
        voiceEnabled: props.voiceEnabled
      });
      
      setupEventListeners();
    };
    
    const setupEventListeners = () => {
      const handleWakeWord = () => {
        emit('wake-word');
        isListening.value = true;
      };
      
      const handleCommand = (e) => {
        emit('command', e.detail);
      };
      
      const handleSpeakingStart = (e) => {
        emit('speaking-start', e.detail);
        isSpeaking.value = true;
      };
      
      const handleEmotionChange = (e) => {
        emit('emotion-change', e.detail);
        currentEmotion.value = e.detail.emotion;
      };
      
      document.addEventListener('wake-word-detected', handleWakeWord);
      document.addEventListener('command-processed', handleCommand);
      document.addEventListener('avatar-speaking-started', handleSpeakingStart);
      document.addEventListener('avatar-emotion-changed', handleEmotionChange);
    };
    
    const speak = (text, emotion = 'neutral') => {
      angel.value?.speak(text, emotion);
    };
    
    const setEmotion = (emotion, intensity = 0.8) => {
      angel.value?.setEmotion(emotion, intensity);
    };
    
    const startListening = () => {
      angel.value?.startListening();
      isListening.value = true;
    };
    
    const stopListening = () => {
      angel.value?.stopListening();
      isListening.value = false;
    };
    
    const toggleListening = () => {
      if (isListening.value) {
        stopListening();
      } else {
        startListening();
      }
    };
    
    const getStatus = () => {
      return angel.value?.getStatus();
    };
    
    // Watchers pour les props
    watch(() => props.avatarId, (newId) => {
      angel.value?.updateConfig({ avatarId: newId });
    });
    
    watch(() => props.quality, (newQuality) => {
      angel.value?.updateConfig({ quality: newQuality });
    });
    
    onMounted(() => {
      initializeAvatar();
    });
    
    onUnmounted(() => {
      angel.value?.cleanup();
    });
    
    // Exposer les méthodes pour le composant parent
    return {
      avatarContainer,
      isListening,
      currentEmotion,
      isSpeaking,
      speak,
      setEmotion,
      startListening,
      stopListening,
      toggleListening,
      getStatus
    };
  }
};
</script>

<style scoped>
.angel-avatar-wrapper {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
}

.angel-avatar-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.avatar-controls {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.control-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
}
</style>
```

## 📱 Optimisation Mobile

### Configuration Mobile

```javascript
// Détection et optimisation mobile
class MobileOptimizer {
    constructor(angelInstance) {
        this.angel = angelInstance;
        this.isMobile = this.detectMobile();
        
        if (this.isMobile) {
            this.applyMobileOptimizations();
        }
    }
    
    detectMobile() {
        return window.innerWidth <= 768 || 
               /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    }
    
    applyMobileOptimizations() {
        console.log('📱 Application optimisations mobile');
        
        // Réduire la qualité pour préserver les performances
        this.angel.updateConfig({
            quality: 'low',
            fps: 30,
            shadows: false,
            antialiasing: false,
            voiceEnabled: false // Désactivé par défaut sur mobile
        });
        
        // Optimisations tactiles
        this.setupTouchOptimizations();
        
        // Gestion de l'orientation
        this.setupOrientationHandling();
    }
    
    setupTouchOptimizations() {
        const container = this.angel.container;
        
        // Désactiver le zoom par pincement
        container.style.touchAction = 'pan-x pan-y';
        
        // Gestures personnalisées
        let startY = 0;
        
        container.addEventListener('touchstart', (e) => {
            startY = e.touches[0].clientY;
        });
        
        container.addEventListener('touchmove', (e) => {
            const currentY = e.touches[0].clientY;
            const diff = startY - currentY;
            
            // Swipe up pour activer l'écoute
            if (diff > 50) {
                this.angel.startListening();
                startY = currentY;
            }
        });
    }
    
    setupOrientationHandling() {
        window.addEventListener('orientationchange', () => {
            setTimeout(() => {
                // Ajuster la taille du container
                if (this.angel.avatarRenderer) {
                    this.angel.avatarRenderer.handleResize();
                }
            }, 500);
        });
    }
}

// Application automatique sur mobile
if (window.angelAvatar) {
    window.mobileOptimizer = new MobileOptimizer(window.angelAvatar);
}
```

### Interface Tactile

```css
/* Adaptations tactiles */
@media (max-width: 768px) {
    .angel-avatar-wrapper {
        height: 300px;
    }
    
    .avatar-controls {
        position: fixed;
        bottom: 20px;
        right: 20px;
        flex-direction: column;
        background: rgba(0, 0, 0, 0.8);
        border-radius: 30px;
        padding: 10px;
    }
    
    .avatar-controls button {
        width: 50px;
        height: 50px;
        margin: 5px;
        font-size: 20px;
        border-radius: 50%;
    }
    
    .angel-avatar-container {
        touch-action: pan-x pan-y;
        -webkit-touch-callout: none;
        -webkit-user-select: none;
        user-select: none;
    }
}

/* Gestes tactiles indicateurs */
.touch-indicator {
    position: absolute;
    top: 10px;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(255, 255, 255, 0.9);
    padding: 5px 15px;
    border-radius: 15px;
    font-size: 12px;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.touch-indicator.show {
    opacity: 1;
}
```

## 🧪 Tests et Validation

### Tests Automatisés

```javascript
// Suite de tests pour l'intégration avatar
class AvatarIntegrationTests {
    constructor(angelInstance) {
        this.angel = angelInstance;
        this.results = [];
    }
    
    async runAllTests() {
        console.log('🧪 Démarrage des tests d\'intégration Avatar...');
        
        await this.testAvatarLoading();
        await this.testSpeechSynthesis();
        await this.testEmotionChanges();
        await this.testVoiceRecognition();
        await this.testPerformance();
        await this.testEventSystem();
        
        this.displayResults();
    }
    
    async testAvatarLoading() {
        try {
            const avatarLoaded = !!this.angel.avatarRenderer?.avatar;
            const webglSupported = !!window.WebGLRenderingContext;
            const threeJsLoaded = typeof THREE !== 'undefined';
            
            const success = avatarLoaded && webglSupported && threeJsLoaded;
            
            this.addResult('Avatar Loading', success, 
                `Avatar: ${avatarLoaded}, WebGL: ${webglSupported}, Three.js: ${threeJsLoaded}`
            );
        } catch (error) {
            this.addResult('Avatar Loading', false, error.message);
        }
    }
    
    async testSpeechSynthesis() {
        try {
            const speechSupported = 'speechSynthesis' in window;
            
            if (speechSupported) {
                // Test de synthèse vocale
                const testPromise = new Promise((resolve) => {
                    const utterance = new SpeechSynthesisUtterance('Test');
                    utterance.volume = 0.1; // Très faible pour ne pas déranger
                    utterance.onend = () => resolve(true);
                    utterance.onerror = () => resolve(false);
                    speechSynthesis.speak(utterance);
                });
                
                const result = await Promise.race([
                    testPromise,
                    new Promise(resolve => setTimeout(() => resolve(false), 5000))
                ]);
                
                this.addResult('Speech Synthesis', result, 'Test synthèse vocale réussi');
            } else {
                this.addResult('Speech Synthesis', false, 'Non supportée par le navigateur');
            }
        } catch (error) {
            this.addResult('Speech Synthesis', false, error.message);
        }
    }
    
    async testEmotionChanges() {
        try {
            const emotions = ['happy', 'sad', 'surprised', 'neutral'];
            let success = true;
            
            for (const emotion of emotions) {
                this.angel.setEmotion(emotion, 0.8);
                await this.delay(200);
                
                // Vérifier que l'émotion a changé
                const status = this.angel.getStatus();
                if (status.currentEmotion !== emotion) {
                    success = false;
                    break;
                }
            }
            
            this.addResult('Emotion Changes', success, 'Toutes les émotions testées');
        } catch (error) {
            this.addResult('Emotion Changes', false, error.message);
        }
    }
    
    async testVoiceRecognition() {
        try {
            const recognitionSupported = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window;
            
            if (recognitionSupported) {
                // Test basique de reconnaissance
                const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
                const recognition = new SpeechRecognition();
                recognition.lang = 'fr-FR';
                
                const testResult = await new Promise((resolve) => {
                    recognition.onstart = () => resolve(true);
                    recognition.onerror = () => resolve(false);
                    
                    try {
                        recognition.start();
                        setTimeout(() => {
                            recognition.stop();
                            resolve(true);
                        }, 1000);
                    } catch (error) {
                        resolve(false);
                    }
                });
                
                this.addResult('Voice Recognition', testResult, 'API disponible et fonctionnelle');
            } else {
                this.addResult('Voice Recognition', false, 'Non supportée par le navigateur');
            }
        } catch (error) {
            this.addResult('Voice Recognition', false, error.message);
        }
    }
    
    async testPerformance() {
        try {
            const startTime = performance.now();
            
            // Test de charge avec animations multiples
            for (let i = 0; i < 20; i++) {
                this.angel.setEmotion('happy', Math.random());
                await this.delay(50);
            }
            
            const endTime = performance.now();
            const duration = endTime - startTime;
            const success = duration < 3000; // Moins de 3 secondes
            
            this.addResult('Performance', success, `Durée: ${duration.toFixed(0)}ms`);
        } catch (error) {
            this.addResult('Performance', false, error.message);
        }
    }
    
    async testEventSystem() {
        try {
            let eventReceived = false;
            
            // Écouter un événement personnalisé
            const eventHandler = () => {
                eventReceived = true;
            };
            
            document.addEventListener('avatar-test-event', eventHandler);
            
            // Déclencher l'événement
            document.dispatchEvent(new CustomEvent('avatar-test-event'));
            
            await this.delay(100);
            
            document.removeEventListener('avatar-test-event', eventHandler);
            
            this.addResult('Event System', eventReceived, 'Système d\'événements fonctionnel');
        } catch (error) {
            this.addResult('Event System', false, error.message);
        }
    }
    
    addResult(testName, success, message) {
        this.results.push({ testName, success, message });
        const emoji = success ? '✅' : '❌';
        console.log(`${emoji} ${testName}: ${message}`);
    }
    
    displayResults() {
        console.log('\n📊 Résultats des tests:');
        console.table(this.results);
        
        const successCount = this.results.filter(r => r.success).length;
        const totalCount = this.results.length;
        
        console.log(`\n🎯 Score: ${successCount}/${totalCount} tests réussis`);
        
        if (successCount === totalCount) {
            console.log('🎉 Tous les tests sont OK ! Angel est prêt.');
        } else {
            console.log('⚠️ Certains tests ont échoué. Vérifiez la configuration.');
        }
        
        return {
            success: successCount === totalCount,
            score: successCount / totalCount,
            details: this.results
        };
    }
    
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}

// Utilisation des tests
// const tests = new AvatarIntegrationTests(angelInstance);
// tests.runAllTests().then(result => {
//     if (result.success) {
//         console.log('✅ Intégration validée');
//     } else {
//         console.log('❌ Problèmes détectés');
//     }
// });
```

## 🚨 Dépannage Intégration

### Diagnostic Automatique

```javascript
// Outil de diagnostic complet
class AvatarDiagnostic {
    async runFullDiagnostic() {
        console.log('🔍 Diagnostic complet Avatar...');
        
        const results = {
            browser: this.checkBrowserCompatibility(),
            webgl: this.checkWebGLSupport(),
            audio: await this.checkAudioSupport(),
            network: await this.checkNetworkConnectivity(),
            permissions: await this.checkPermissions(),
            performance: this.checkPerformance()
        };
        
        this.displayDiagnostic(results);
        return results;
    }
    
    checkBrowserCompatibility() {
        const userAgent = navigator.userAgent;
        const browser = this.getBrowserInfo(userAgent);
        
        const compatibility = {
            chrome: userAgent.includes('Chrome') && !userAgent.includes('Edge'),
            firefox: userAgent.includes('Firefox'),
            safari: userAgent.includes('Safari') && !userAgent.includes('Chrome'),
            edge: userAgent.includes('Edge')
        };
        
        const supported = compatibility.chrome || compatibility.firefox || compatibility.edge;
        
        return {
            browser: browser,
            supported: supported,
            compatibility: compatibility,
            version: this.getBrowserVersion(userAgent)
        };
    }
    
    checkWebGLSupport() {
        const canvas = document.createElement('canvas');
        const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
        
        if (!gl) {
            return { supported: false, reason: 'WebGL non disponible' };
        }
        
        const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
        
        return {
            supported: true,
            version: gl.getParameter(gl.VERSION),
            vendor: gl.getParameter(gl.VENDOR),
            renderer: debugInfo ? gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL) : 'Inconnu',
            maxTextureSize: gl.getParameter(gl.MAX_TEXTURE_SIZE),
            extensions: gl.getSupportedExtensions()
        };
    }
    
    async checkAudioSupport() {
        try {
            // Test MediaDevices API
            const mediaDevicesSupported = !!navigator.mediaDevices;
            
            // Test Speech Synthesis
            const speechSynthesisSupported = 'speechSynthesis' in window;
            
            // Test Speech Recognition
            const speechRecognitionSupported = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window;
            
            // Test accès microphone
            let microphoneAccess = false;
            try {
                const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                microphoneAccess = true;
                stream.getTracks().forEach(track => track.stop());
            } catch (error) {
                console.warn('Microphone non accessible:', error.message);
            }
            
            return {
                mediaDevicesSupported,
                speechSynthesisSupported,
                speechRecognitionSupported,
                microphoneAccess,
                voicesAvailable: speechSynthesis.getVoices().length
            };
        } catch (error) {
            return {
                error: error.message,
                supported: false
            };
        }
    }
    
    async checkNetworkConnectivity() {
        try {
            // Test connexion Ready Player Me
            const rpmResponse = await fetch('https://models.readyplayer.me/', { method: 'HEAD' });
            const rpmAccessible = rpmResponse.ok;
            
            // Test vitesse de connexion (approximative)
            const startTime = Date.now();
            await fetch('https://models.readyplayer.me/687f66fafe8107131699bf7b.glb', { 
                method: 'HEAD' 
            });
            const loadTime = Date.now() - startTime;
            
            return {
                online: navigator.onLine,
                readyPlayerMeAccessible: rpmAccessible,
                approximateSpeed: this.categorizeSpeed(loadTime),
                loadTime: loadTime
            };
        } catch (error) {
            return {
                online: navigator.onLine,
                error: error.message,
                accessible: false
            };
        }
    }
    
    async checkPermissions() {
        const permissions = {};
        
        try {
            const micPermission = await navigator.permissions.query({ name: 'microphone' });
            permissions.microphone = micPermission.state;
        } catch (error) {
            permissions.microphone = 'unknown';
        }
        
        return permissions;
    }
    
    checkPerformance() {
        const memory = performance.memory || {};
        
        return {
            deviceMemory: navigator.deviceMemory || 'unknown',
            hardwareConcurrency: navigator.hardwareConcurrency || 'unknown',
            usedJSHeapSize: memory.usedJSHeapSize || 'unknown',
            totalJSHeapSize: memory.totalJSHeapSize || 'unknown',
            jsHeapSizeLimit: memory.jsHeapSizeLimit || 'unknown'
        };
    }
    
    getBrowserInfo(userAgent) {
        if (userAgent.includes('Chrome') && !userAgent.includes('Edge')) return 'Chrome';
        if (userAgent.includes('Firefox')) return 'Firefox';
        if (userAgent.includes('Safari') && !userAgent.includes('Chrome')) return 'Safari';
        if (userAgent.includes('Edge')) return 'Edge';
        return 'Unknown';
    }
    
    getBrowserVersion(userAgent) {
        const match = userAgent.match(/(Chrome|Firefox|Safari|Edge)\/(\d+)/);
        return match ? match[2] : 'unknown';
    }
    
    categorizeSpeed(loadTime) {
        if (loadTime < 1000) return 'fast';
        if (loadTime < 3000) return 'medium';
        return 'slow';
    }
    
    displayDiagnostic(results) {
        console.log('\n📋 Résultats du diagnostic:');
        
        // Résumé
        const summary = {
            'Navigateur': results.browser.supported ? '✅' : '❌',
            'WebGL': results.webgl.supported ? '✅' : '❌',
            'Audio': results.audio.speechSynthesisSupported ? '✅' : '❌',
            'Microphone': results.audio.microphoneAccess ? '✅' : '❌',
            'Réseau': results.network.readyPlayerMeAccessible ? '✅' : '❌'
        };
        
        console.table(summary);
        
        // Détails par catégorie
        console.group('🌐 Navigateur');
        console.log('Type:', results.browser.browser);
        console.log('Version:', results.browser.version);
        console.log('Supporté:', results.browser.supported);
        console.groupEnd();
        
        console.group('🎮 WebGL');
        console.log('Supporté:', results.webgl.supported);
        if (results.webgl.supported) {
            console.log('Version:', results.webgl.version);
            console.log('Renderer:', results.webgl.renderer);
        }
        console.groupEnd();
        
        console.group('🎵 Audio');
        console.log('Synthèse vocale:', results.audio.speechSynthesisSupported);
        console.log('Reconnaissance vocale:', results.audio.speechRecognitionSupported);
        console.log('Microphone:', results.audio.microphoneAccess);
        console.groupEnd();
        
        // Recommandations
        this.generateRecommendations(results);
    }
    
    generateRecommendations(results) {
        const recommendations = [];
        
        if (!results.browser.supported) {
            recommendations.push('❌ Utilisez Chrome, Firefox ou Edge pour une meilleure compatibilité');
        }
        
        if (!results.webgl.supported) {
            recommendations.push('❌ Activez WebGL dans votre navigateur ou mettez à jour votre carte graphique');
        }
        
        if (!results.audio.microphoneAccess) {
            recommendations.push('❌ Accordez les permissions microphone pour la reconnaissance vocale');
        }
        
        if (!results.network.readyPlayerMeAccessible) {
            recommendations.push('❌ Vérifiez votre connexion internet et les paramètres proxy');
        }
        
        if (results.network.approximateSpeed === 'slow') {
            recommendations.push('⚠️ Connexion lente détectée - utilisez la qualité "low" pour l\'avatar');
        }
        
        if (recommendations.length > 0) {
            console.log('\n💡 Recommandations:');
            recommendations.forEach(rec => console.log(rec));
        } else {
            console.log('\n🎉 Tous les prérequis sont OK !');
        }
    }
}

// Fonction utilitaire globale
window.diagnoseAvatar = () => {
    const diagnostic = new AvatarDiagnostic();
    return diagnostic.runFullDiagnostic();
};
```

## 📚 API de Référence

### Interface AngelAvatarIntegration

```typescript
interface AngelAvatarIntegrationConfig {
    avatarId?: string;
    quality?: 'low' | 'medium' | 'high';
    voiceEnabled?: boolean;
    controls?: boolean;
    autoStart?: boolean;
}

interface AvatarStatus {
    avatarLoaded: boolean;
    isListening: boolean;
    currentEmotion: string;
    isSpeaking: boolean;
    quality: string;
    uptime: number;
}

class AngelAvatarIntegration {
    // Constructeur
    constructor(containerId: string, config?: AngelAvatarIntegrationConfig);
    
    // Méthodes principales
    speak(text: string, emotion?: string): Promise<void>;
    setEmotion(emotion: string, intensity?: number): void;
    loadAvatar(url: string): Promise<void>;
    
    // Reconnaissance vocale
    startListening(): void;
    stopListening(): void;
    addVoiceCommand(command: string, handler: Function): void;
    removeVoiceCommand(command: string): void;
    
    // Configuration
    updateConfig(config: Partial<AngelAvatarIntegrationConfig>): void;
    getConfig(): AngelAvatarIntegrationConfig;
    
    // État et statistiques
    getStatus(): AvatarStatus;
    getStats(): AvatarStatistics;
    
    // Événements
    on(event: string, handler: Function): void;
    off(event: string, handler: Function): void;
    emit(event: string, data?: any): void;
    
    // Nettoyage
    cleanup(): void;
    dispose(): void;
}
```

### Événements Disponibles

```javascript
// Événements Avatar
document.addEventListener('avatar-loaded', (e) => {
    console.log('Avatar chargé:', e.detail);
});

document.addEventListener('avatar-speaking-started', (e) => {
    console.log('Parole démarrée:', e.detail.text);
});

document.addEventListener('avatar-speaking-ended', (e) => {
    console.log('Parole terminée');
});

document.addEventListener('avatar-emotion-changed', (e) => {
    console.log('Émotion changée:', e.detail.emotion, e.detail.intensity);
});

// Événements Voix
document.addEventListener('wake-word-detected', (e) => {
    console.log('Wake word détecté:', e.detail.transcript);
});

document.addEventListener('command-processed', (e) => {
    console.log('Commande traitée:', e.detail.command, e.detail.response);
});

document.addEventListener('listening-started', () => {
    console.log('Écoute démarrée');
});

document.addEventListener('listening-ended', () => {
    console.log('Écoute terminée');
});
```

---

Ce guide d'intégration vous permet d'intégrer l'avatar Angel Virtual Assistant dans votre environnement avec un contrôle complet sur la personnalisation et les fonctionnalités, que ce soit via iframe, JavaScript direct, ou frameworks modernes comme React et Vue.js.
# Avatar 3D - Angel Virtual Assistant

Documentation complète du système d'avatar 3D avec Ready Player Me et rendu Three.js.

## 🎭 Vue d'Ensemble

### Caractéristiques
- **Avatar photoréaliste** : Modèles Ready Player Me haute qualité
- **Rendu 3D temps réel** : Three.js avec WebGL optimisé
- **Animations contextuelles** : Expressions et gestes adaptés aux émotions  
- **Synchronisation labiale** : Animation bouche avec synthèse vocale
- **Interface responsive** : Compatible desktop, tablette, mobile

### Avatar Par Défaut
- **Type** : Avatar féminin Ready Player Me
- **ID** : `687f66fafe8107131699bf7b`
- **Style** : Casual, mature, élégant
- **Qualité** : Haute résolution avec textures PBR

## 🔧 Configuration

### Configuration Avatar (`config/avatar-config.json`)
```json
{
  "avatar": {
    "rendering": {
      "antialiasing": true,
      "shadows": true,
      "quality": "high",
      "pixelRatio": "auto"
    },
    "readyPlayerMe": {
      "enabled": true,
      "baseUrl": "https://models.readyplayer.me",
      "defaultAvatarId": "687f66fafe8107131699bf7b",
      "timeout": 30000,
      "fallbackModels": [
        "/models/avatars/female_mature_elegant.glb",
        "/models/avatars/female_adult_casual.glb"
      ]
    },
    "animations": {
      "speaking": {
        "enabled": true,
        "intensity": 0.7
      },
      "idle": {
        "enabled": true,
        "variations": 3
      },
      "emotions": {
        "enabled": true,
        "transitions": true,
        "duration": 1000
      }
    }
  }
}
```

### Configuration Backend (`config/application.properties`)
```properties
# Avatar 3D
avatar.enabled=true
avatar.model.default=687f66fafe8107131699bf7b
avatar.animations.enabled=true

# Rendu 3D  
avatar.webgl.antialias=true
avatar.webgl.shadows=true
avatar.webgl.quality=high
```

## 🎨 Rendu 3D

### Moteur de Rendu
- **Three.js** : r128 (version stable)
- **WebGL** : Accélération matérielle GPU
- **Shaders** : PBR (Physically Based Rendering)
- **Post-processing** : Anti-aliasing, ombres

### Qualité Visuelle
- **Résolution adaptative** : Selon performance appareil
- **Ombres temps réel** : PCF Soft Shadow Maps
- **Éclairage** : 4 sources lumineuses (ambiante, directionnelle, remplissage, contour)
- **Matériaux PBR** : Métallique, rugosité, réflexions

### Optimisations Performance
```javascript
// Adaptation qualité selon appareil
if (window.innerWidth < 768) {
  // Mobile : Qualité réduite
  renderer.setPixelRatio(1.5);
  renderer.shadowMap.enabled = false;
} else {
  // Desktop : Qualité maximale
  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.shadowMap.enabled = true;
}
```

## 🎬 Système d'Animations

### Types d'Animations

#### Animation Idle (Repos)
- **Respiration** : Mouvement subtil du torse
- **Clignements** : Yeux naturels (3-5 secondes)
- **Micro-mouvements** : Légers déplacements de tête
- **Posture** : Maintien naturel du corps

#### Animation Speaking (Parole)
- **Synchronisation labiale** : Mouvement bouche avec audio
- **Gestes naturels** : Mouvements subtils de tête
- **Expressions** : Adaptation selon émotion du texte
- **Respiration adaptée** : Rythme synchronisé avec parole

#### Animations Émotionnelles
```javascript
const emotions = {
  'neutral': { expression: 'repos', intensity: 0.0 },
  'happy': { expression: 'sourire', intensity: 0.8 },
  'friendly': { expression: 'sourire_chaleureux', intensity: 0.6 },
  'informative': { expression: 'concentré', intensity: 0.4 },
  'attentive': { expression: 'écoute', intensity: 0.5 },
  'apologetic': { expression: 'désolé', intensity: 0.3 },
  'excited': { expression: 'grand_sourire', intensity: 1.0 }
};
```

### Transitions
- **Durée** : 1000ms (configurable)
- **Interpolation** : Courbes de Bézier pour fluidité
- **Blending** : Mélange progressif entre animations
- **Priority** : Système de priorités pour animations multiples

## 📱 Interface et Contrôles

### Contrôles Utilisateur
- **Bouton Mute** : Couper/activer audio (🔊/🔇)
- **Paramètres** : Configuration avatar (⚙️)  
- **Plein écran** : Mode immersif
- **Masquage automatique** : Contrôles disparaissent après inactivité

### Indicateurs Visuels
- **État écoute** : Indicateur microphone (🎤)
- **État parole** : Animation speaking active
- **Chargement** : Spinner pendant chargement avatar
- **Erreurs** : Messages d'erreur contextuels

### Responsive Design
```css
/* Desktop */
#avatar-container { width: 100%; height: 400px; }

/* Tablette */
@media (max-width: 1024px) {
  #avatar-container { height: 350px; }
}

/* Mobile */  
@media (max-width: 768px) {
  #avatar-container { height: 300px; }
}
```

## 🌙 Modes Adaptatifs

### Mode Sombre
- **Déclenchement** : Après 5 minutes d'inactivité
- **Visuel** : Overlay noir semi-transparent
- **Avatar** : Masqué progressivement
- **Affichage heure** : Grande horloge centrée
- **Réveil** : Détection activité ou commande vocale

### Mode Écoute
- **Visuel** : Indicateur microphone actif
- **Animation** : Avatar en position d'écoute attentive
- **Feedback** : Réaction visuelle sur détection "Angèle"
- **Transition** : Passage fluide vers mode réponse

### Mode Parole
- **Animation** : Synchronisation labiale active
- **Expression** : Émotion adaptée au contenu
- **Gestes** : Mouvements naturels de communication
- **Indicateur** : État "speaking" visible

## 🔄 Intégration Ready Player Me

### Chargement Modèles
```javascript
const avatarUrl = `https://models.readyplayer.me/${avatarId}.glb`;

// Chargement avec suivi progression
loader.load(avatarUrl, 
  (gltf) => {
    // Succès : traitement modèle
    this.processAvatar(gltf);
  },
  (progress) => {
    // Progression : mise à jour UI
    const percent = (progress.loaded / progress.total * 100);
    this.updateLoadingProgress(percent);
  },
  (error) => {
    // Erreur : fallback vers modèle local
    this.loadFallbackModel();
  }
);
```

### Post-Traitement
- **Optimisation matériaux** : Paramètres PBR ajustés
- **Ombres** : Activation cast/receive shadows
- **Positionnement** : Centrage et mise à l'échelle
- **Animations** : Configuration mixer Three.js

### Fallback
- **Modèles locaux** : Sauvegarde si Ready Player Me indisponible
- **Géométrie simple** : Formes de base en cas d'échec total
- **Indication visuelle** : Message utilisateur si fallback activé

## 🎤 Synchronisation Audio-Visuelle

### Détection Parole
```javascript
// Écoute événements synthèse vocale
document.addEventListener('angelSpeechStateChanged', (event) => {
  const { isSpeaking } = event.detail;
  
  if (isSpeaking) {
    avatar.startSpeakingAnimation();
  } else {
    avatar.stopSpeakingAnimation();
  }
});
```

### Animation Bouche
- **Déclenchement** : Début synthèse vocale
- **Mouvement** : Animation procédurale lèvres
- **Synchronisation** : Tempo adapté à la parole
- **Arrêt progressif** : Retour fluide position repos

### Expressions Émotionnelles
- **Analyse texte** : Détection émotion dans réponse
- **Application** : Expression faciale correspondante  
- **Durée** : Maintien pendant toute la réponse
- **Transition** : Retour à l'expression neutre

## 🔧 Performance et Optimisation

### Optimisations Graphiques
- **Frustum culling** : Objets hors caméra non rendus
- **LOD** : Niveaux de détail selon distance
- **Texture compression** : Réduction mémoire GPU
- **Batching** : Regroupement appels de rendu

### Optimisations Mobile
```javascript
if (isMobile) {
  // Réduction qualité
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.5));
  
  // Désactivation effets coûteux
  renderer.shadowMap.enabled = false;
  scene.fog = null;
  
  // Géométrie simplifiée
  avatar.detail = 'low';
}
```

### Monitoring Performance
```javascript
// Stats FPS (mode debug)
if (config.debug.showPerformance) {
  const stats = new Stats();
  document.body.appendChild(stats.dom);
  
  function animate() {
    stats.begin();
    renderer.render(scene, camera);
    stats.end();
    requestAnimationFrame(animate);
  }
}
```

## 🧪 Tests et Diagnostic

### Tests Fonctionnels
```bash
# Test chargement avatar
./angel-launcher.sh test-avatar

# Test avec avatar simple (debug)
./angel-launcher.sh start --simple-avatar

# Test WebGL
# Navigateur → https://get.webgl.org/
```

### Diagnostic Console
```javascript
// État avatar
console.log("Avatar Status:", {
  loaded: !!window.avatarRenderer?.avatar,
  animations: window.avatarRenderer?.mixer?.time,
  speaking: window.avatarRenderer?.isSpeaking
});

// Test animation
window.avatarRenderer?.setEmotion('happy', 0.8);

// Forcer rechargement
window.avatarRenderer?.loadDefaultAvatar();
```

### Métriques Performance
- **FPS** : 60 FPS cible (30 FPS minimum mobile)
- **Mémoire** : <200MB GPU (desktop), <100MB (mobile)  
- **Chargement** : <10 secondes réseau normal
- **Réactivité** : <100ms déclenchement animations

## 🔍 Dépannage

### Avatar ne se charge pas
1. **WebGL Support**
   ```javascript
   // Test support WebGL
   const canvas = document.createElement('canvas');
   const gl = canvas.getContext('webgl');
   console.log('WebGL:', !!gl);
   ```

2. **Connexion réseau**
   - Vérifier accès à `models.readyplayer.me`
   - Tester avec avatar local

3. **Logs**
   ```bash
   grep -i "avatar\|three\|gltf\|ready" logs/angel.log
   ```

### Performances dégradées
1. **Qualité graphiques**
   ```json
   {
     "avatar": {
       "rendering": {
         "quality": "medium",
         "shadows": false,
         "antialiasing": false
       }
     }
   }
   ```

2. **Mode mobile forcé**
   ```javascript
   window.avatarRenderer.setupMobileOptimizations();
   ```

### Animations ne fonctionnent pas
1. **Test manuel**
   ```javascript
   window.avatarRenderer.setEmotion('happy');
   window.avatarRenderer.startSpeakingAnimation();
   ```

2. **Vérifier mixer**
   ```javascript
   console.log('Mixer:', window.avatarRenderer.mixer);
   ```

## 🎯 Personnalisation

### Changement d'Avatar
```javascript
// Charger un autre avatar Ready Player Me
const newAvatarId = 'your-avatar-id';
window.avatarRenderer.loadAvatar(`https://models.readyplayer.me/${newAvatarId}.glb`);
```

### Personnalisation Éclairage
```javascript
// Modifier l'éclairage
const light = scene.getObjectByName('directionalLight');
light.intensity = 0.9;
light.color.setHex(0xffffcc); // Lumière plus chaude
```

### Personnalisation Caméra
```javascript
// Repositionner caméra (gros plan visage)
camera.position.set(0, 1.7, 0.8);
camera.lookAt(0, 1.65, 0);
```

## 🌍 Support Multi-Plateforme

### Navigateurs Desktop
- **Chrome** : Support optimal
- **Firefox** : Support complet  
- **Safari** : Support avec limitations mineures
- **Edge** : Support complet

### Navigateurs Mobile
- **Chrome Mobile** : Bon support
- **Safari Mobile** : Limitations WebGL
- **Firefox Mobile** : Support partiel

### Adaptations Automatiques
```javascript
// Détection capacités
const capabilities = {
  webgl: !!gl,
  webgl2: !!gl?.getExtension('EXT_color_buffer_float'),
  mobile: window.innerWidth < 768,
  lowPower: navigator.deviceMemory < 4
};

// Configuration adaptée
if (capabilities.lowPower) {
  config.avatar.rendering.quality = 'low';
}
```

---

Le système d'avatar 3D d'Angel Virtual Assistant offre une expérience visuelle immersive et interactive, avec un avatar photoréaliste capable d'expressions émotionnelles naturelles et de synchronisation labiale précise.
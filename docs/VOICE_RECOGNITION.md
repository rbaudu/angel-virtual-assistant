# Reconnaissance Vocale - Angel Virtual Assistant

Documentation complète du système de reconnaissance vocale avec détection du mot-clé "Angèle".

## 🎤 Fonctionnement

### Vue d'Ensemble
- **Écoute continue** : L'avatar écoute en permanence le mot-clé "Angèle"
- **Détection intelligente** : Reconnaissance de variantes et correction automatique
- **Mode commande** : Activation automatique après détection du mot-clé
- **Synthèse vocale** : Réponses avec voix française et émotions adaptées

### Flux de Traitement
1. **Écoute** → API Web Speech Recognition active
2. **Détection** → Reconnaissance "Angèle" ou variantes
3. **Activation** → Passage en mode commande
4. **Traitement** → Analyse de la commande utilisateur
5. **Réponse** → Synthèse vocale + retour écoute

## 🎯 Mot-clé "Angèle"

### Variantes Reconnues
```
"Angèle"     # Forme principale
"Angel"      # Variante anglaise
"Ange"       # Forme courte
"Angie"      # Diminutif
"Angela"     # Forme étendue
"Angele"     # Sans accent
```

### Configuration
```json
{
  "voice": {
    "wakeWord": {
      "enabled": true,
      "words": ["angel", "angèle", "angelo"],
      "threshold": 0.7,
      "fallbackMode": true
    }
  }
}
```

### Algorithme de Détection
- **Correspondance exacte** : Recherche directe dans le transcript
- **Correspondance approximative** : Variantes prédéfinies
- **Similarité** : Algorithme Levenshtein (seuil 0.6)
- **Correction d'accents** : Normalisation automatique

## 🗣️ Commandes Vocales

### Informations
```
"Angèle, quelle heure est-il ?"
→ "Il est 14h30."

"Angèle, quel jour sommes-nous ?"
→ "Nous sommes mardi 6 août 2025."

"Angèle, quel temps fait-il ?"
→ "Pour la météo, consultez votre application habituelle."
```

### Contrôles Interface
```
"Angèle, affiche la configuration"
→ Affiche les contrôles de l'avatar

"Angèle, masque la configuration"
→ Masque les contrôles

"Angèle, affiche les paramètres"
→ Ouvre le panneau de paramètres
```

### Interaction Sociale
```
"Angèle, qui es-tu ?"
→ "Je suis Angèle, votre assistante virtuelle..."

"Angèle, bonjour"
→ Salutation adaptée à l'heure (bonjour/bonsoir)

"Angèle, comment allez-vous ?"
→ "Très bien, merci ! Comment puis-je vous aider ?"
```

## 🔧 Configuration

### Fichier Principal (`config/avatar-config.json`)
```json
{
  "voice": {
    "wakeWord": {
      "enabled": true,
      "words": ["angel", "angèle", "angelo"],
      "threshold": 0.7,
      "timeout": 5000,
      "fallbackMode": true
    },
    "speech": {
      "synthesis": {
        "voice": "Microsoft Hortense - French (France) (fr-FR)",
        "rate": 1.0,
        "pitch": 1.0,
        "volume": 0.8
      },
      "recognition": {
        "language": "fr-FR",
        "continuous": true,
        "interimResults": true,
        "maxAlternatives": 1
      }
    }
  }
}
```

### Configuration Backend (`config/application.properties`)
```properties
# Reconnaissance vocale
voice.wake-word=Angèle
voice.language=fr-FR
voice.confidence.threshold=0.7
voice.speech.enabled=true

# WebSocket vocal
websocket.voice.enabled=true
websocket.voice.endpoint=/ws/voice
```

## 🔊 Synthèse Vocale

### Caractéristiques
- **Voix** : Microsoft Hortense (français France)
- **Queue intelligente** : Gestion automatique des messages multiples
- **Émotions** : 7 types d'émotions avec adaptation vocale
- **Interruption** : Possibilité d'interrompre la synthèse

### Types d'Émotions
```javascript
{
  'neutral': { rate: 0.9, pitch: 1.0 },
  'friendly': { rate: 1.0, pitch: 1.1 },
  'informative': { rate: 0.85, pitch: 0.95 },
  'attentive': { rate: 0.85, pitch: 0.95 },
  'apologetic': { rate: 0.75, pitch: 0.85 },
  'happy': { rate: 1.1, pitch: 1.2 },
  'excited': { rate: 1.2, pitch: 1.3 }
}
```

## 🏗️ Architecture Technique

### Backend Java
```
com.angel.voice/
├── WakeWordDetector.java        # Détection serveur
├── VoiceQuestionProcessor.java  # Traitement commandes
└── [Communication WebSocket]
```

### Frontend JavaScript
```
static/js/
├── core/wake-word-bridge.js          # Bridge Java/JavaScript
├── voice/enhanced-speech-integration.js  # Synthèse vocale
├── voice/continuous-voice-manager.js     # Écoute continue
├── voice/wake-word-detector.js           # Détection client
└── speech-recognition.js                 # API Web Speech
```

### Communication
- **WebSocket** : Communication temps réel Java ↔ JavaScript
- **Messages JSON** : Échange de données structurées
- **Fallback** : Mode autonome si connexion échoue

## 🌙 Modes Spéciaux

### Mode Sombre
- **Déclenchement** : Après 5 minutes d'inactivité
- **Comportement** : Interface s'assombrit, avatar se cache
- **Écoute** : Continue en arrière-plan
- **Réveil** : Détection de "Angèle" ou activité utilisateur

### Mode Fallback
- **Activation** : Si connexion WebSocket échoue
- **Fonctionnement** : Reconnaissance vocale locale uniquement
- **Limitations** : Pas de traitement backend des commandes

## 🧪 Tests et Diagnostic

### Tests Manuel Console
```javascript
// Test reconnaissance vocale
window.wakeWordDetector.startListening()

// Test synthèse vocale
window.enhancedSpeechIntegration.testSpeech()

// État du système vocal
console.log("Vocal Status:", {
  wakeWordDetector: !!window.wakeWordDetector,
  speechIntegration: !!window.enhancedSpeechIntegration,
  recognition: !!window.webkitSpeechRecognition,
  synthesis: !!window.speechSynthesis
})
```

### Diagnostic Automatique
```bash
# Test complet du système vocal
./angel-launcher.sh voice-diagnostic

# Test spécifique reconnaissance
./angel-launcher.sh test-wake-word

# Test synthèse vocale
./angel-launcher.sh test-speech
```

## 🔍 Dépannage

### "Angèle" ne répond pas
1. **Permissions microphone**
   - Vérifier autorisation dans le navigateur
   - Chrome: chrome://settings/content/microphone

2. **Test manuel**
   ```javascript
   window.wakeWordDetector.startListening()
   ```

3. **Variantes**
   - Essayer "Angel", "Ange" si "Angèle" ne marche pas
   - Parler clairement et distinctement

4. **Logs**
   ```bash
   grep -i "wake.*word\|angele" logs/angel.log
   ```

### L'avatar ne parle pas
1. **Test synthèse**
   ```javascript
   window.enhancedSpeechIntegration.testSpeech()
   ```

2. **Queue bloquée**
   ```javascript
   // Vérifier la queue
   console.log(window.enhancedSpeechIntegration.speechQueue.length)
   
   // Forcer synthèse
   window.enhancedSpeechIntegration.speakNow("Test", "neutral")
   ```

3. **Volume système**
   - Vérifier haut-parleurs/casque
   - Volume navigateur et système

### Erreurs de reconnaissance
1. **Redémarrer reconnaissance**
   ```javascript
   window.speechSynthesis.cancel()
   location.reload()
   ```

2. **Mode debug**
   ```bash
   ./angel-launcher.sh start -p test -d
   ```

3. **Logs détaillés**
   ```bash
   tail -f logs/angel.log | grep -i "speech\|voice\|recognition"
   ```

## 📊 Métriques et Performance

### Statistiques Reconnaissance
- **Précision** : ~85% sur mot-clé principal
- **Latence** : <500ms détection → réponse
- **Langues** : Français (support principal)
- **Navigateurs** : Chrome (optimal), Firefox (bon), Safari (limité)

### Optimisations
- **Correction automatique** confidence=0 (Chrome)
- **Seuils adaptatifs** selon contexte
- **Redémarrage silencieux** sur erreurs mineures
- **Mode continu** pour réactivité

## 🔐 Sécurité et Confidentialité

### Données Vocales
- **Traitement local** : Reconnaissance dans le navigateur
- **Pas d'enregistrement** : Aucun stockage audio permanent
- **Transcripts temporaires** : Supprimés après traitement

### Permissions Requises
- **Microphone** : Obligatoire pour reconnaissance
- **Géolocalisation** : Non utilisée
- **Cookies** : Configuration uniquement

## 🌍 Support Multi-Navigateur

### Compatibilité Complète
- **Chrome** : Support optimal (recommandé)
- **Firefox** : Support complet
- **Edge** : Support complet

### Compatibilité Partielle
- **Safari** : Limitations Web Speech API
- **Mobile** : Selon support navigateur

### Fallbacks
- Mode texte si vocal indisponible
- Interface alternative pour navigateurs non compatibles

## 🚀 Performances

### Optimisations Client
- **Debounce** : Évite les déclenchements multiples
- **Queue intelligente** : Gestion optimale synthèse vocale
- **Lazy loading** : Chargement différé des ressources
- **Cache vocal** : Réutilisation des configurations

### Optimisations Serveur
- **WebSocket** : Communication temps réel
- **Pool de threads** : Traitement parallèle commandes
- **Cache réponses** : Accélération réponses fréquentes

---

Le système de reconnaissance vocale d'Angel Virtual Assistant offre une interaction naturelle et fluide en français, avec détection continue du mot-clé "Angèle" et traitement intelligent des commandes utilisateur.
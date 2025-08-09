# Reconnaissance Vocale - Angel Virtual Assistant

Documentation complète du système de reconnaissance vocale avec détection du mot-clé "Angèle" et intégration intelligente multi-IA.

## 📖 Vue d'Ensemble

Le système de reconnaissance vocale d'Angel permet de détecter le mot-clé "Angèle" et de traiter les commandes vocales des utilisateurs. L'avatar écoute en permanence et active automatiquement le système de traitement vocal. Une fois détecté, le système analyse la question, sélectionne intelligemment l'IA la plus appropriée selon des critères de complexité et de priorités configurées, puis fournit une réponse vocale naturelle.

Cette implémentation utilise une architecture hybride client-serveur pour optimiser les performances et la compatibilité.

## 🏗️ Architecture

### Architecture Générale

```
┌─────────────────────────┐               ┌──────────────────────────┐
│      Navigateur         │               │    Serveur Spring Boot   │
│  (Client/Frontend)      │               │     (Backend/API)        │
├─────────────────────────┤               ├──────────────────────────┤
│ Web Speech Recognition  │◄─────────────►│ WakeWordWebSocketController│
│ ├─ SpeechRecognitionSvc │   WebSocket   │ ├─ AISelectionService     │
│ ├─ WakeWordDetector     │               │ ├─ AIProviderService      │
│ └─ Interface utilisateur│               │ ├─ VoiceQuestionProcessor │
└─────────────────────────┘               │ └─ Providers IA          │
                                          └──────────────────────────┘
```

### Flux de Données

```
1. Utilisateur dit "Angèle"
   ↓
2. Web Speech Recognition (navigateur)
   ↓
3. Détection du mot-clé
   ↓
4. Envoi WebSocket au serveur
   ↓
5. Activation d'Angel
   ↓
6. Analyse complexité question
   ↓
7. Sélection IA pondérée
   ↓
8. Traitement par IA sélectionnée
   ↓
9. Réponse via Avatar (audio direct ou TTS)
   ↓
10. Retour en mode veille
```

## 🎯 Mot-clé "Angèle"

### Variantes Reconnues
Le système détecte automatiquement plusieurs formes du nom :
```
"Angèle"     # Forme principale
"Angel"      # Variante anglaise
"Ange"       # Forme courte
"Angie"      # Diminutif
"Angela"     # Forme étendue
"Angele"     # Sans accent
```

### Configuration Wake Word
```json
{
  "voice": {
    "wakeWord": {
      "enabled": true,
      "words": ["angel", "angèle", "angelo"],
      "threshold": 0.7,
      "timeout": 5000,
      "fallbackMode": true
    }
  }
}
```

### Algorithme de Détection
- **Correspondance exacte** : Recherche directe dans le transcript
- **Correspondance approximative** : Variantes prédéfinies
- **Similarité phonétique** : Algorithme Levenshtein (seuil 0.6)
- **Correction d'accents** : Normalisation automatique des caractères

## 🤖 Système IA Multi-Providers

### Architecture Intelligente Hybride

Le système offre deux modes d'appel aux IA pour une flexibilité maximale :

#### **Modes de Connexion**
- **Mode Direct** : Appels HTTP directs aux endpoints des IA (HttpClient natif)
- **Mode API** : Utilisation des SDK Spring et services intégrés (RestTemplate)

Le mode est configurable par provider dans `ai-config.json` avec la propriété `"mode": "direct"` ou `"mode": "api"`.

#### **Questions Simples → Réponse Audio Directe**
Distribution par défaut :
- **OpenAI Realtime** : 50% (priorité 1)
- **Gemini Live** : 30% (priorité 2)
- **Copilot Speech** : 20% (priorité 3)

Exemples typiques :
```
"Angèle, quelle heure est-il ?"
"Angèle, quel temps fait-il ?"
"Angèle, bonjour"
"Angèle, qui es-tu ?"
```

#### **Questions Complexes → Texte + Synthèse Vocale**
Distribution par défaut :
- **Claude** : 60% (priorité 1) - Excelle en analyse et raisonnement
- **Mistral** : 40% (priorité 2) - Performant en créativité et technique

Exemples typiques :
```
"Angèle, explique-moi les différences entre l'IA et l'apprentissage automatique"
"Angèle, analyse les avantages et inconvénients du télétravail"
"Angèle, compare les approches philosophiques de Kant et Descartes"
```

### Modes d'Appel aux IA

#### Mode Direct (HttpClient)
- Appels HTTP natifs sans dépendances Spring
- Plus rapide et léger
- Contrôle total sur les headers et timeouts
- Idéal pour les endpoints publics des IA
- Configuration : `"mode": "direct"`

#### Mode API (Spring Services)
- Utilisation des SDK et RestTemplate Spring
- Intégration avec l'écosystème Spring Boot
- Gestion automatique des retry et circuit breakers
- Meilleur pour les services internes
- Configuration : `"mode": "api"`

#### Sélection Automatique du Mode
Le système choisit automatiquement le mode optimal si non spécifié :
- Présence de clé API → Mode Direct privilégié
- Absence de clé API → Mode API avec fallback
- Échec du mode principal → Bascule automatique sur l'autre mode

### Analyse Automatique de Complexité

Le système analyse chaque question selon plusieurs critères :

**Mots-clés Complexes** (Score +2 chacun) :
```
analyse, explique, pourquoi, comment, compare, différence,
avantage, inconvénient, stratégie, philosophie, éthique,
complexe, détaillé, approfondi, nuance, contexte, implication,
développe, argumente, justifie, critique
```

**Mots-clés Simples** (Score -1 chacun) :
```
heure, météo, température, qui, quoi, où, quand,
combien, définition, signifie, ouvre, ferme
```

**Critères Additionnels** :
- Question > 100 caractères : +1 point
- Questions multiples (plusieurs ?) : +1 point
- **Seuil de basculement** : 3 points (configurable)

## 🗣️ Commandes Vocales

### Informations Locales
Traitées instantanément sans appel IA :
```
"Angèle, quelle heure est-il ?" → "Il est 14h30."
"Angèle, quel jour sommes-nous ?" → "Nous sommes mardi 6 août 2025."
"Angèle, quel temps fait-il ?" → "Pour la météo, consultez votre application habituelle."
```

### Questions avec IA
Routées vers le système de sélection intelligente :
```
"Angèle, explique-moi l'intelligence artificielle" → [Claude 60% ou Mistral 40%]
"Angèle, raconte-moi une histoire" → [Favorise Mistral pour créativité]
"Angèle, qui es-tu ?" → [OpenAI/Gemini/Copilot selon poids]
```

### Contrôles Interface
```
"Angèle, affiche la configuration" → Affiche contrôles avatar
"Angèle, masque la configuration" → Masque contrôles
"Angèle, affiche les paramètres" → Ouvre panneau paramètres
"Angèle, stop" ou "Angèle, arrête" → Arrêt système vocal
```

### Interaction Sociale
```
"Angèle, bonjour" → Salutation adaptée à l'heure
"Angèle, comment allez-vous ?" → "Très bien, merci ! Comment puis-je vous aider ?"
"Angèle, bonne nuit" → Salutation appropriée
```

### Types de Commandes Supportées

#### Classification des Commandes
1. **Météo** : "Quel temps fait-il ?", "Météo du jour"
2. **Heure** : "Quelle heure est-il ?", "Il est quelle heure ?"
3. **Propositions** : "Que me proposes-tu ?", "Une suggestion ?"
4. **Questions générales** : Questions ouvertes pour l'IA
5. **Contrôle système** : "Stop", "Silence", "Pause"
6. **Interface** : Affichage/masquage des contrôles

## ⚙️ Configuration

### Configuration IA Principale
Fichier `config/ai-config.json` :

```json
{
  "aiSelectionConfig": {
    "selectionMode": "weighted_random",
    "fallbackOnError": true,
    "maxRetries": 2,
    "timeoutMs": 5000,
    "weightingStrategy": "inverse_priority"
  },
  
  "questionAnalysis": {
    "complexityThreshold": 3,
    "complexityKeywords": [
      "analyse", "explique", "pourquoi", "comment", "compare",
      "différence", "avantage", "inconvénient", "stratégie",
      "philosophie", "éthique", "complexe", "détaillé",
      "approfondi", "nuance", "contexte", "implication"
    ],
    "simpleKeywords": [
      "heure", "météo", "température", "qui", "quoi", "où", "quand",
      "combien", "définition", "signifie", "ouvre", "ferme"
    ]
  },
  
  "audioProviders": {
    "openai_realtime": {
      "enabled": true,
      "priority": 1,
      "weight": 50,
      "mode": "direct",
      "apiKey": "${OPENAI_API_KEY}",
      "model": "gpt-4o-realtime-preview",
      "voice": "nova",
      "responseFormat": "audio",
      "maxTokens": 150,
      "temperature": 0.7
    },
    "gemini_live": {
      "enabled": true,
      "priority": 2,
      "weight": 30,
      "mode": "direct",
      "apiKey": "${GOOGLE_API_KEY}",
      "model": "gemini-pro-live",
      "voice": "fr-FR-Wavenet-C",
      "responseFormat": "audio",
      "maxTokens": 150,
      "temperature": 0.7
    },
    "copilot_speech": {
      "enabled": true,
      "priority": 3,
      "weight": 20,
      "mode": "api",
      "apiKey": "${AZURE_OPENAI_KEY}",
      "endpoint": "${AZURE_OPENAI_ENDPOINT}",
      "voice": "fr-FR-DeniseNeural",
      "responseFormat": "audio",
      "maxTokens": 150,
      "temperature": 0.7
    }
  },
  
  "textProviders": {
    "claude": {
      "enabled": true,
      "priority": 1,
      "weight": 60,
      "mode": "direct",
      "apiKey": "${ANTHROPIC_API_KEY}",
      "model": "claude-3-5-sonnet-20241022",
      "maxTokens": 300,
      "temperature": 0.3,
      "ttsProvider": "azure",
      "voice": "fr-FR-DeniseNeural",
      "endpoint": "https://api.anthropic.com/v1/messages",
      "systemPrompt": "Tu es Angèle, un assistant vocal intelligent et bienveillant..." 
    },
    "mistral": {
      "enabled": true,
      "priority": 2,
      "weight": 40,
      "mode": "api",
      "apiKey": "${MISTRAL_API_KEY}",
      "model": "mistral-large-latest",
      "maxTokens": 300,
      "temperature": 0.3,
      "ttsProvider": "azure",
      "voice": "fr-FR-HenriNeural"
    }
  },
  
  "ttsServices": {
    "azure": {
      "apiKey": "${AZURE_SPEECH_KEY}",
      "region": "${AZURE_REGION}",
      "defaultVoice": "fr-FR-DeniseNeural",
      "speed": 1.0,
      "pitch": "default"
    },
    "google": {
      "apiKey": "${GOOGLE_TTS_KEY}",
      "defaultVoice": "fr-FR-Wavenet-C",
      "speed": 1.0,
      "pitch": 0.0
    }
  },
  
  "statisticsTracking": {
    "enabled": true,
    "logSelections": true,
    "trackPerformance": true,
    "adjustWeightsBasedOnSuccess": false
  }
}
```

### Configuration Vocale
Fichier `config/avatar-config.json` :

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

### Variables d'Environnement
```bash
# OpenAI
OPENAI_API_KEY=sk-...

# Anthropic Claude  
ANTHROPIC_API_KEY=sk-ant-...

# Google/Gemini
GOOGLE_API_KEY=AIza...
GOOGLE_TTS_KEY=AIza...

# Mistral
MISTRAL_API_KEY=...

# Azure/Microsoft
AZURE_OPENAI_KEY=...
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
AZURE_SPEECH_KEY=...
AZURE_REGION=francecentral
```

### Configuration Spring Boot
Fichier `application.properties` :

```properties
# Configuration AI
angel.voice.config.path=config/ai-config.json
angel.voice.config.reload.enabled=true
angel.voice.config.reload.interval=300000

# Configuration de base vocale
voice.wake-word=Angèle
voice.language=fr-FR
voice.continuous=true
voice.confidence-threshold=0.7

# Configuration avancée
voice.recognition-timeout=10000
voice.return-to-sleep-delay=3000
voice.wake-word-variants=angel,anjel,ange,angie
voice.debug=true

# WebSocket vocal
voice.websocket.path=/ws/voice
voice.websocket.connection-timeout=5000
voice.websocket.ping-interval=30000
angel.voice.websocket.endpoint=/voice-websocket
angel.voice.websocket.allowed-origins=*

# Timeouts et performance
angel.voice.ai.timeout=5000
angel.voice.ai.max-retries=2
angel.voice.ai.fallback=true

# Cache
spring.cache.cache-names=ai-responses,tts-cache,config-cache
```

## 🏗️ Architecture Technique

### Backend Java

#### Structure Complète
```
com.angel.voice/
├── service/
│   ├── AISelectionService.java          # Sélection pondérée des IA
│   ├── AIProviderService.java           # Orchestration des appels
│   ├── ConfigurationService.java        # Configuration centralisée
│   ├── TTSService.java                  # Service TTS centralisé
│   └── providers/
│       ├── OpenAIRealtimeService.java   # OpenAI audio direct
│       ├── GeminiLiveService.java       # Gemini + Google TTS
│       ├── CopilotSpeechService.java    # Azure OpenAI + TTS
│       ├── ClaudeService.java           # Claude text only
│       └── MistralService.java          # Mistral text only
├── model/
│   └── AIProvider.java                  # Modèle de configuration IA
├── config/
│   └── WebSocketConfig.java             # Configuration WebSocket
├── controller/
│   └── WakeWordWebSocketController.java # Gestionnaire WebSocket vocal
├── WakeWordDetector.java                # Détection serveur (compatibilité)
└── VoiceQuestionProcessor.java          # Traitement commandes principal
```

#### Composants Clés

**`WakeWordWebSocketController.java`**
- **Rôle** : Gestionnaire WebSocket pour la communication vocale
- **Responsabilités** :
  - Gestion des connexions WebSocket
  - Traitement des messages de détection vocale
  - Communication avec VoiceQuestionProcessor
  - Diffusion des réponses aux clients

**`VoiceQuestionProcessor.java`**
- **Rôle** : Coordinateur principal du traitement vocal
- **Responsabilités** :
  - Classification des commandes (locale/IA)
  - Interface avec AISelectionService
  - Gestion des réponses et du fallback

**`AISelectionService.java`**
- **Rôle** : Sélection intelligente des providers IA
- **Responsabilités** :
  - Analyse de complexité des questions
  - Sélection pondérée aléatoire
  - Gestion des priorités et poids
  - Attribution du mode (direct/api) selon configuration

**`AIProviderService.java`**
- **Rôle** : Orchestrateur des appels IA
- **Responsabilités** :
  - Délégation aux services spécifiques selon le provider
  - Gestion des timeouts adaptés au mode
  - Coordination avec TTSService pour la synthèse vocale
  - Statistiques et monitoring des appels

**`AIProvider.java`**
- **Rôle** : Modèle de données pour les fournisseurs IA
- **Propriétés clés** :
  - `mode` : "direct" ou "api"
  - `endpoint` : URL pour le mode direct
  - `systemPrompt` : Personnalisation du comportement IA
  - Méthodes de validation et fallback

**`WebSocketConfig.java`**
- **Rôle** : Configuration Spring WebSocket
- **Configuration** : Point de terminaison `/ws/voice`

### Frontend JavaScript

#### Structure Complète
```
static/js/
├── core/wake-word-bridge.js              # Bridge Java/JavaScript
├── voice/enhanced-speech-integration.js  # Synthèse vocale avec émotions
├── voice/continuous-voice-manager.js     # Écoute continue  
├── voice/wake-word-detector.js           # Détection client + WebSocket
└── speech-recognition.js                 # Service reconnaissance vocale
```

#### Composants Clés

**`speech-recognition.js`**
- **Rôle** : Service de reconnaissance vocale
- **Fonctionnalités** :
  - Utilisation de l'API Web Speech Recognition
  - Détection du mot-clé avec tolérance aux erreurs
  - Gestion des modes (wake word / commande)
  - Gestion d'erreurs et reconnexion

**`wake-word-detector.js`**
- **Rôle** : Intégration WebSocket + reconnaissance
- **Fonctionnalités** :
  - Communication WebSocket avec le serveur
  - Interface utilisateur (indicateurs, status)
  - Coordination entre reconnaissance et serveur

**`enhanced-speech-integration.js`**
- **Rôle** : Synthèse vocale avancée
- **Fonctionnalités** :
  - Queue intelligente de messages
  - Émotions vocales (7 types)
  - Interruption et contrôle

### Interface Utilisateur

#### Styles CSS
**`wake-word-detector.css`**
- **Éléments stylés** :
  - Indicateur d'écoute (microphone)
  - Statut de reconnaissance  
  - Contrôles vocaux
  - Panneau de configuration

#### Interface HTML
**Modifications dans `avatar.html`** :
- Indicateur de reconnaissance vocale
- Contrôles de démarrage/arrêt
- Panneau de paramètres vocaux
- Messages de statut en temps réel

### Communication WebSocket

Messages échangés entre frontend et backend :

**Question détectée (JS → Java)** :
```json
{
  "type": "VOICE_QUESTION",
  "question": "Angèle, explique-moi l'IA",
  "timestamp": 1691234567890,
  "confidence": 0.95
}
```

**Réponse IA (Java → JS)** :
```json
{
  "type": "AI_RESPONSE",
  "response": "L'intelligence artificielle est...",
  "provider": "claude",
  "questionType": "COMPLEX_TEXT",
  "processingTime": 1245,
  "audioFormat": "text_tts"
}
```

**Statut de connexion** :
```json
{
  "type": "CONNECTION_STATUS",
  "status": "connected",
  "timestamp": 1691234567890
}
```

## 🎯 Avantages de l'Architecture Hybride

### Flexibilité Maximale
- **Choix du mode par provider** : Chaque IA peut utiliser son mode optimal
- **Bascule automatique** : Si un mode échoue, l'autre prend le relais
- **Configuration sans code** : Changement de mode via JSON uniquement

### Performances Optimisées
- **Mode Direct** : Moins de latence, appels HTTP natifs
- **Mode API** : Meilleure intégration Spring, retry automatique
- **Timeouts adaptés** : 30s pour direct, 10s pour API

### Résilience Accrue
- **Double mécanisme** : Si SDK échoue, HTTP direct disponible
- **Fallback intelligent** : Sélection automatique du mode viable
- **Health checks** : Désactivation temporaire des providers défaillants

### Simplicité de Déploiement
- **Pas de dépendances obligatoires** : Mode direct fonctionne sans SDK
- **Configuration centralisée** : Un seul fichier JSON à gérer
- **Variables d'environnement** : Clés API sécurisées et flexibles

## 🔄 Système de Fallback

### Gestion Automatique des Erreurs
1. **Premier provider échoue** → Sélection automatique d'une alternative
2. **Timeout dépassé** → Fallback vers provider plus rapide
3. **API indisponible** → Health check et désactivation temporaire
4. **Toutes les tentatives échouent** → Message d'excuse vocal

### Health Checks
Vérification automatique toutes les 5 minutes :
```java
@Scheduled(fixedRate = 300000)
public void checkProvidersHealth() {
    providers.forEach(provider -> {
        if (!provider.isHealthy()) {
            provider.setEnabled(false);
            log.warn("Provider {} désactivé", provider.getName());
        }
    });
}
```

### Modes Spéciaux

#### Mode Sombre
- **Déclenchement** : Après 5 minutes d'inactivité
- **Comportement** : Interface s'assombrit, avatar se cache
- **Écoute** : Continue en arrière-plan
- **Réveil** : Détection de "Angèle" ou activité utilisateur

#### Mode Fallback
- **Activation** : Si connexion WebSocket échoue
- **Fonctionnement** : Reconnaissance vocale locale uniquement
- **Limitations** : Pas de traitement backend des commandes

## 🔊 Synthèse Vocale

### Caractéristiques
- **Voix principale** : Microsoft Hortense (français France)
- **Queue intelligente** : Gestion automatique des messages multiples
- **Émotions** : 7 types avec adaptation vocale automatique
- **Interruption** : Possibilité d'arrêter la synthèse en cours

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

### Configuration TTS
Optimisation du texte pour la synthèse vocale :
- **Abréviations** : M. → Monsieur, Mme → Madame
- **Nombres** : % → pour cent, € → euros
- **Emoticons** : :) → sourire
- **Pauses naturelles** : Insertion automatique aux ponctuations

## 📊 Monitoring et Statistiques

### Logs Système
```bash
# Sélection IA avec Mode
[AI_SELECTION] Provider: claude, Type: COMPLEX_TEXT, Mode: direct, Time: 1691234567890
[AI_SELECTION] Provider: openai_realtime, Type: SIMPLE_AUDIO, Mode: direct, Time: 1691234567891

# Appels selon le Mode
INFO com.angel.voice.service.providers.ClaudeService : 🔗 Claude mode DIRECT
INFO com.angel.voice.service.providers.MistralService : ⚙️ Mistral mode API (Spring)
INFO com.angel.voice.AIProviderService : Appel IA: claude en mode direct pour question: Explique-moi...

# Fallback avec changement de mode
[FALLBACK] claude (direct) → claude (api), Reason: connection error
[FALLBACK] mistral (api) → mistral (direct), Reason: SDK timeout

# Statistiques par Mode
[STATS] Provider: claude, Mode: direct, Duration: 1245ms, Success: true
[STATS] Provider: mistral, Mode: api, Duration: 2100ms, Success: false

# Wake Word
INFO com.angel.voice.WakeWordWebSocketController : WebSocket connecté
INFO com.angel.voice.WakeWordDetector : WakeWordDetector initialisé
INFO com.angel.core.AngelApplication : Mot-clé détecté, activation du système
```

### Métriques Disponibles
- **Précision reconnaissance** : ~85% sur mot-clé principal
- **Latence totale** : <2s (question → réponse audio)
- **Taux de succès IA** : >95% avec système de fallback
- **Distribution providers** : Selon poids configurés
- **Connexions WebSocket** : Actives en temps réel

### API de Diagnostic
```javascript
// État des providers IA
fetch('/api/ai/providers/health').then(r => r.json())

// Statistiques de sélection
fetch('/api/ai/statistics').then(r => r.json())

// Test de sélection
fetch('/api/ai/test-selection', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({question: "Test question"})
})

// Métriques vocales
fetch('/api/voice/metrics').then(r => r.json())
```

## 🎛️ Personnalisation

### Configuration Dynamique
Les paramètres peuvent être modifiés via l'interface web :
- Mot-clé personnalisé
- Langue de reconnaissance
- Seuil de confiance
- Mode écoute continue
- Mode debug

### Configuration des Modes d'Appel
Exemples de configurations hybrides dans `ai-config.json` :

```json
// Mode Direct pour performance maximale
"claude": {
  "mode": "direct",
  "endpoint": "https://api.anthropic.com/v1/messages",
  "apiKey": "${ANTHROPIC_API_KEY}",
  "systemPrompt": "Tu es Angèle..."
}

// Mode API pour intégration Spring
"mistral": {
  "mode": "api",
  "apiKey": "${MISTRAL_API_KEY}",
  "model": "mistral-large-latest"
}

// Configuration avec fallback automatique
"openai_realtime": {
  "mode": "direct",
  "endpoint": "https://api.openai.com/v1/audio",
  "fallbackMode": "api"  // Si direct échoue
}
```

### Ajustement des Poids
Modification du fichier `ai-config.json` pour changer les probabilités :

```json
// Favoriser OpenAI pour l'audio
"openai_realtime": { "weight": 70, "mode": "direct" },
"gemini_live": { "weight": 20, "mode": "direct" },
"copilot_speech": { "weight": 10, "mode": "api" }

// Équilibrer Claude et Mistral avec modes différents
"claude": { "weight": 50, "mode": "direct" },
"mistral": { "weight": 50, "mode": "api" }
```

### Voix TTS Personnalisées
```json
"ttsServices": {
  "azure": {
    "defaultVoice": "fr-FR-JeromeNeural",  // Voix masculine
    "speed": 0.9,                          // Plus lent
    "pitch": "+5%"                         // Plus aigu
  }
}
```

### Seuils de Complexité
```json
"questionAnalysis": {
  "complexityThreshold": 2,  // Plus de questions vers text+TTS
  "complexityKeywords": ["custom", "keywords"],
  "simpleKeywords": ["custom", "simple"]
}
```

## 🚀 Installation et Déploiement

### Prérequis
- Java 17+
- Spring Boot 3.2+
- Navigateur compatible (Chrome, Edge, Safari)
- Microphone fonctionnel
- Connexion HTTPS (recommandé pour la production)

### Étapes d'Installation
1. **Configuration des services backend** : Créer tous les services Java
2. **Ressources frontend** : Ajouter les fichiers JavaScript et CSS
3. **Configuration** : Paramétrer les fichiers JSON et properties
4. **Variables d'environnement** : Configurer toutes les clés API
5. **Démarrage** : `./angel-launcher.sh start -p test`

### Vérification de l'Installation
1. **Interface** : Accéder à `http://localhost:8080/angel`
2. **Microphone** : Autoriser l'accès dans le navigateur
3. **Indicateur** : Vérifier l'icône microphone verte
4. **Wake word** : Tester "Angèle" → Réaction du système
5. **IA** : Tester une question complexe → Réponse intelligente

## 🧪 Tests et Diagnostic

### Tests Console JavaScript
```javascript
// Test reconnaissance vocale
window.wakeWordDetector.startListening()

// Test synthèse vocale
window.enhancedSpeechIntegration.testSpeech()

// État complet système vocal
console.log("Vocal Status:", {
  wakeWordDetector: !!window.wakeWordDetector,
  speechIntegration: !!window.enhancedSpeechIntegration,
  recognition: !!window.webkitSpeechRecognition,
  synthesis: !!window.speechSynthesis
})

// Test WebSocket
console.log("WebSocket Status:", window.wakeWordDetector.socket.readyState)
```

### Tests Manuels
```javascript
// Test reconnaissance vocale
window.wakeWordDetector.startListening()

// Test synthèse vocale
window.enhancedSpeechIntegration.testSpeech()

// Test WebSocket
window.wakeWordDetector.socket.send(JSON.stringify({
  type: "VOICE_QUESTION",
  question: "test"
}))
```

### Scripts de Diagnostic
```bash
# Test complet système
./angel-launcher.sh ai-diagnostic

# Test vocal complet
./angel-launcher.sh voice-diagnostic

# Test spécifique reconnaissance
./angel-launcher.sh test-wake-word

# Test synthèse vocale
./angel-launcher.sh test-speech

# Test provider spécifique
./angel-launcher.sh test-provider claude

# Rechargement configuration
./angel-launcher.sh reload-config
```

## 🔧 Dépannage

### "Angèle" ne répond pas

#### Symptômes
- Pas de réaction au mot-clé
- Indicateur microphone gris

#### Solutions
1. **Permissions microphone** 
   - Vérifier chrome://settings/content/microphone
   - Autoriser le microphone pour le site
   - Recharger la page après autorisation

2. **Test manuel** 
   ```javascript
   window.wakeWordDetector.startListening()
   ```

3. **Variantes** 
   - Essayer "Angel", "Ange" si "Angèle" échoue
   - Parler clairement et distinctement

4. **Logs** 
   ```bash
   grep -i "wake.*word\|angele" logs/angel.log
   ```

### Problèmes WebSocket

#### Symptômes
- Message "🔌 Erreur de connexion au serveur"
- Pas de réaction aux commandes vocales

#### Solutions
1. **Serveur Spring Boot** : Vérifier qu'il fonctionne
2. **Logs serveur** : Contrôler les erreurs WebSocket
3. **Configuration réseau** : Vérifier proxy/firewall
4. **Reconnexion** : Le système se reconnecte automatiquement

### Problèmes IA

#### Symptômes
- Erreurs de traitement des questions
- Pas de réponse des IA

#### Solutions
1. **Variables environnement** : Vérifier toutes les clés API
   ```bash
   echo $OPENAI_API_KEY
   echo $ANTHROPIC_API_KEY
   # etc.
   ```

2. **Health check** 
   ```bash
   curl http://localhost:8080/api/ai/providers/health
   ```

3. **Configuration** : Valider le JSON
   ```bash
   ./angel-launcher.sh validate-config
   ```

4. **Logs détaillés** 
   ```bash
   tail -f logs/angel.log | grep -i "ai\|provider"
   ```

### L'avatar ne parle pas

#### Symptômes
- Pas de synthèse vocale
- Réponses muettes

#### Solutions
1. **Test synthèse** 
   ```javascript
   window.enhancedSpeechIntegration.testSpeech()
   ```

2. **Queue bloquée** 
   ```javascript
   console.log(window.enhancedSpeechIntegration.speechQueue.length)
   window.enhancedSpeechIntegration.speakNow("Test", "neutral")
   ```

3. **Volume système** : Vérifier haut-parleurs et volume navigateur

4. **TTS service** 
   ```bash
   curl http://localhost:8080/api/tts/test
   ```

### Reconnaissance Vocale Défaillante

#### Symptômes
- Mot-clé non détecté
- Commandes mal comprises

#### Solutions
1. **Navigateur** : Utiliser Chrome ou Edge (meilleure compatibilité)
2. **Élocution** : Parler plus clairement et distinctement
3. **Seuil confiance** : Ajuster dans les paramètres vocaux
4. **Langue** : Vérifier la configuration fr-FR

### Performance Dégradée

#### Symptômes
- Délais de réponse longs
- Reconnexions fréquentes

#### Solutions
1. **Réseau** : Vérifier qualité connexion
2. **Timeouts** : Augmenter dans la configuration
3. **Redémarrage** : Redémarrer l'application
4. **Cache** : Vider le cache navigateur

## 🌍 Compatibilité

### Navigateurs Supportés
- **Chrome** : Support optimal (recommandé)
- **Firefox** : Support complet
- **Edge** : Support complet  
- **Safari** : Limitations Web Speech API

### Navigateurs avec Support Partiel
- **Safari** : Limitations Web Speech API
- **Mobile** : Selon support navigateur

### Fallbacks
- Mode texte si vocal indisponible
- Interface alternative pour navigateurs non compatibles
- Mode dégradé sans WebSocket

### Systèmes d'Exploitation
- **Windows** : Compatible complet
- **macOS** : Compatible complet
- **Linux** : Compatible (selon distribution)

## 🔐 Sécurité et Confidentialité

### Protection des Données
- **Clés API** : Stockage exclusivement en variables d'environnement
- **Logs** : Aucun stockage permanent des questions utilisateur
- **Cache** : Expiration automatique des réponses IA
- **Fallback local** : Commandes critiques sans appel externe

### Données Vocales
- **Traitement local** : Reconnaissance dans le navigateur
- **Pas d'enregistrement** : Aucun stockage audio permanent
- **Transcripts temporaires** : Supprimés après traitement

### Permissions Requises
- **Microphone** : Obligatoire pour reconnaissance
- **Géolocalisation** : Non utilisée
- **Cookies** : Configuration uniquement

### Configuration HTTPS
```properties
# Configuration SSL pour la production
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

## 📈 Performances et Optimisations

### Métriques de Performance
- **Précision reconnaissance** : ~85% sur mot-clé principal
- **Latence totale** : <2s (question → réponse audio)
- **Taux de succès IA** : >95% avec système de fallback
- **Distribution providers** : Selon poids configurés

### Optimisations Client
- **Debounce** : Évite les déclenchements multiples
- **Queue intelligente** : Gestion optimale synthèse vocale
- **Lazy loading** : Chargement différé des ressources
- **Cache vocal** : Réutilisation des configurations

### Optimisations Serveur
- **WebSocket** : Communication temps réel
- **Pool de threads** : Traitement parallèle commandes
- **Cache réponses** : Accélération réponses fréquentes
- **Health checks** : Désactivation automatique des services défaillants

## 🔌 Extensibilité

### Intégration IA
Le système est conçu pour une intégration facile avec de nouveaux services d'IA :

```java
// Nouveau service IA
@Service
public class NewAIService {
    public String getTextResponse(String question, AIProvider provider) {
        // Implémentation du nouveau service
        return response;
    }
    
    public boolean isHealthy() {
        // Vérification de santé
        return true;
    }
}
```

### Nouveaux Types de Commandes
Ajouter de nouveaux types dans l'enum `CommandType` :

```java
private enum CommandType {
    WEATHER_REQUEST,
    TIME_REQUEST,
    NEWS_REQUEST,        // Nouveau
    CALENDAR_REQUEST,    // Nouveau
    SMART_HOME_CONTROL   // Nouveau
}
```

### Support Multilingue
Configuration dans `application.properties` :

```properties
# Support multilingue
voice.languages.fr=fr-FR
voice.languages.en=en-US
voice.languages.es=es-ES
voice.languages.de=de-DE

# Mots-clés par langue
voice.wake-words.fr=Angel,Ange,Angèle
voice.wake-words.en=Angel,Helper
voice.wake-words.es=Angel,Asistente
```

## 📖 Exemples d'Utilisation

### Workflow Utilisateur Typique

1. **Activation** : Dire "Angèle"
   - Réponse : "Oui, comment puis-je vous aider ?"
   - Indicateur passe au bleu

2. **Commande** : Poser une question
   - Exemple : "Explique-moi la blockchain"
   - Statut : "💭 Traitement: Explique-moi la blockchain"

3. **Traitement IA** : Sélection et appel automatique
   - Claude sélectionné (60% chance)
   - Traitement de la question complexe

4. **Réponse** : Angel répond avec TTS
   - Explication détaillée vocalisée
   - Avatar parle et affiche le texte

5. **Retour en veille** : Automatique après réponse
   - Indicateur redevient vert
   - Prêt pour une nouvelle activation

### Scénarios Détaillés

#### Scénario Question Simple (Mode Direct)
```
Utilisateur: "Angèle, bonjour"
→ Détection: wake-word-detector.js
→ WebSocket: Message vers Java
→ Classification: SIMPLE_AUDIO
→ Sélection: OpenAI Realtime (50% chance, mode: direct)
→ Appel: OpenAIRealtimeService.getAudioResponse()
  → HttpClient direct vers OpenAI API
  → Headers et auth gérés manuellement
→ Réponse: Audio direct "Bonjour ! Comment puis-je vous aider ?"
```

#### Scénario Question Complexe (Mode Direct)
```
Utilisateur: "Angèle, explique-moi la blockchain"
→ Détection: wake-word-detector.js
→ WebSocket: Message vers Java
→ Classification: COMPLEX_TEXT (mots: explique, blockchain)
→ Sélection: Claude (60% chance, mode: direct)
→ Appel: ClaudeService.getTextResponseDirect()
  → HttpClient vers api.anthropic.com
  → JSON construit manuellement
→ TTS: Azure Speech → audio
→ Réponse: Explication vocale complète
```

#### Scénario avec Mode API
```
Utilisateur: "Angèle, raconte une histoire"
→ Classification: COMPLEX_TEXT
→ Sélection: Mistral (40% chance, mode: api)
→ Appel: MistralService.getTextResponseAPI()
  → RestTemplate Spring
  → SDK Mistral intégré
→ TTS: Azure Speech → audio
→ Réponse: Histoire créative vocalisée
```

#### Scénario Fallback
```
Utilisateur: "Angèle, raconte une histoire"
→ Sélection: Mistral (créativité)
→ Échec: Timeout après 5s
→ Fallback: Claude automatiquement
→ Succès: Histoire générée + TTS
→ Log: Statistiques de fallback
→ Réponse: Histoire vocalisée
```

#### Scénario Commande Locale
```
Utilisateur: "Angèle, quelle heure est-il ?"
→ Détection: wake-word-detector.js
→ WebSocket: Message vers Java
→ Classification: Commande locale
→ Traitement: VoiceQuestionProcessor directement
→ Réponse: "Il est 14h30" (sans IA externe)
```

---

Le système de reconnaissance vocale d'Angel Virtual Assistant offre une interaction naturelle et intelligente en français, avec détection continue du mot-clé "Angèle", sélection automatique de la meilleure IA selon le contexte, et traitement optimisé des commandes utilisateur.
# Angel Virtual Assistant

Un assistant virtuel intelligent avec avatar 3D et reconnaissance vocale continue.

## Description

Angel Virtual Assistant accompagne les personnes dans leur quotidien en proposant des activités adaptées à leur contexte. Le système combine :

- **Avatar 3D interactif** : Avatar Ready Player Me avec animations et synthèse vocale
- **Reconnaissance vocale continue** : Activation par le mot-clé "Angèle" 
- **Détection d'activités** : Intégration avec Angel-server-capture (27 types d'activités)
- **Interface web** : Dashboard de contrôle accessible via navigateur

## 🚀 Démarrage Rapide

```bash
# 1. Cloner et compiler
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant
mvn clean package

# 2. Mode test (recommandé pour débuter)
./angel-launcher.sh start -p test

# 3. Accéder à l'avatar
open http://localhost:8081/angel

# 4. Tester la reconnaissance vocale
# Dites "Angèle" puis "quelle heure est-il ?"
```

## 🎤 Commandes Vocales

### Activation
- **Mot-clé** : "Angèle" (variantes : "Angel", "Ange")
- **Mode** : Écoute continue en arrière-plan

### Commandes Supportées
```
"Angèle, quelle heure est-il ?"        → Heure actuelle
"Angèle, quel jour sommes-nous ?"      → Date du jour  
"Angèle, quel temps fait-il ?"         → Info météo
"Angèle, qui es-tu ?"                  → Présentation
"Angèle, bonjour"                      → Salutation adaptée
"Angèle, affiche la configuration"     → Afficher contrôles
"Angèle, masque la configuration"      → Masquer contrôles
```

## 🎭 Avatar 3D

- **Modèle** : Ready Player Me (avatar féminin)
- **Rendu** : Three.js avec WebGL
- **Animations** : Parole, émotions, gestes contextuels
- **Synthèse vocale** : Voix française avec émotions adaptées

## 🌐 Accès Web

### Mode Normal (avec Angel-server-capture)
- **Avatar** : http://localhost:8080/angel
- **Dashboard** : http://localhost:8080/angel/test-dashboard

### Mode Test (autonome)
- **Avatar** : http://localhost:8081/angel
- **Dashboard** : http://localhost:8081/test-dashboard

## 📋 Structure du Projet

```
angel-virtual-assistant/
├── config/                         # Configuration centralisée
│   ├── application.properties      # Config normale
│   ├── application-test.properties # Config test
│   └── avatar-config.json         # Config avatar/voix
├── src/main/
│   ├── java/com/angel/
│   │   ├── core/                  # Application Spring Boot
│   │   ├── avatar/                # Gestionnaire avatar
│   │   ├── voice/                 # Reconnaissance vocale
│   │   ├── intelligence/          # Moteur de propositions
│   │   ├── api/                   # API REST
│   │   └── ui/                    # Contrôleurs web
│   └── resources/
│       ├── static/js/
│       │   ├── avatar/            # Scripts avatar 3D
│       │   ├── voice/             # Scripts reconnaissance vocale
│       │   └── core/              # Utilitaires
│       ├── static/css/            # Styles
│       └── templates/             # Pages HTML
└── docs/                          # Documentation
```

## ⚙️ Configuration

### Reconnaissance Vocale (`config/avatar-config.json`)
```json
{
  "voice": {
    "wakeWord": {
      "enabled": true,
      "words": ["angel", "angèle", "angelo"],
      "threshold": 0.7
    },
    "speech": {
      "synthesis": {
        "voice": "Microsoft Hortense - French (France) (fr-FR)",
        "rate": 1.0,
        "volume": 0.8
      },
      "recognition": {
        "language": "fr-FR",
        "continuous": true
      }
    }
  }
}
```

### Avatar (`config/avatar-config.json`)
```json
{
  "avatar": {
    "readyPlayerMe": {
      "enabled": true,
      "defaultAvatarId": "687f66fafe8107131699bf7b"
    },
    "animations": {
      "speaking": { "enabled": true, "intensity": 0.7 },
      "emotions": { "enabled": true, "transitions": true }
    }
  }
}
```

## 🛠️ Installation

### Prérequis
- Java 17+
- Maven 3.6+
- Navigateur moderne (Chrome/Firefox/Safari/Edge)
- Microphone (pour reconnaissance vocale)

### Installation Standard
```bash
# Cloner le projet
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant

# Compiler
mvn clean package

# Démarrer en mode normal
./angel-launcher.sh start

# Ou en mode test
./angel-launcher.sh start -p test
```

### Installation Système
```bash
# Installation avec service systemd
sudo ./install-script.sh install

# Démarrer le service
sudo systemctl start angel-virtual-assistant
```

## 🎮 Mode Test

Le mode test permet d'utiliser l'assistant sans Angel-server-capture :

```bash
# Démarrer en mode test
./angel-launcher.sh start -p test

# Accès web
http://localhost:8081/angel          # Avatar
http://localhost:8081/test-dashboard # Contrôles de test
```

### Fonctionnalités Test
- Simulation d'activités automatique
- Contrôle manuel des activités
- Scénarios prédéfinis (routine matinale, journée type)
- Avatar 3D avec reconnaissance vocale complète

## 🔧 Utilisation

### Démarrage
```bash
./angel-launcher.sh start           # Mode normal
./angel-launcher.sh start -p test   # Mode test
./angel-launcher.sh status          # Voir l'état
./angel-launcher.sh stop            # Arrêter
```

### Paramètres Avancés
```bash
# Avec plus de mémoire
./angel-launcher.sh start -m 2g

# Mode debug
./angel-launcher.sh start -d

# Mode daemon (arrière-plan)
./angel-launcher.sh start -b
```

### Logs
```bash
./angel-launcher.sh logs            # Voir les logs
tail -f logs/angel.log             # Suivi temps réel
```

## 🧪 Tests et Diagnostic

### Tests Fonctionnels
```bash
./angel-launcher.sh test            # Tests unitaires
./angel-launcher.sh test-web        # Test interface web
```

### Diagnostic Vocal
```javascript
// Dans la console du navigateur
window.enhancedSpeechIntegration.testSpeech()  // Test synthèse
window.wakeWordDetector.startListening()       // Test reconnaissance
```

## 🔍 Dépannage

### L'avatar ne se charge pas
```bash
# Vérifier WebGL
# Aller sur https://get.webgl.org/

# Voir les logs
grep -i "avatar\|three\|gltf" logs/angel.log

# Mode simple
./angel-launcher.sh start -p test --simple-avatar
```

### Reconnaissance vocale ne marche pas
```bash
# Vérifier permissions microphone dans le navigateur
# Tester manuellement
window.wakeWordDetector.startListening()

# Logs spécifiques
grep -i "wake\|speech\|voice" logs/angel.log
```

### L'avatar ne parle pas
```javascript
// Tests dans la console du navigateur
window.enhancedSpeechIntegration.testSpeech()
window.enhancedSpeechIntegration.speakNow("Test", "neutral")
```

## 📚 Documentation Technique

- **[VOICE_RECOGNITION.md](docs/VOICE_RECOGNITION.md)** : Système vocal complet
- **[AVATAR_3D.md](docs/AVATAR_3D.md)** : Avatar et rendu 3D
- **[WEB_INTERFACE.md](docs/WEB_INTERFACE.md)** : Interface web
- **[TEST_MODE.md](docs/TEST_MODE.md)** : Mode test
- **[CONFIGURATION.md](docs/CONFIGURATION.md)** : Configuration détaillée

## 🎯 Types d'Activités Détectées

Le système peut détecter 27 types d'activités :
- EATING, COOKING, CLEANING, READING, WATCHING_TV
- LISTENING_MUSIC, PHONING, WRITING, MOVING
- SLEEPING, WAKING_UP, GOING_TO_SLEEP
- Et 15 autres types d'activités quotidiennes

## 🎪 Types de Propositions

L'assistant peut proposer :
- **Informations** : Météo, actualités, heure
- **Divertissement** : Histoires, blagues, musique
- **Rappels** : Rendez-vous, médicaments
- **Social** : Conversations, appels famille
- **Santé** : Exercices, conseils bien-être
- **Cuisine** : Suggestions repas, recettes

## 🏗️ Architecture Technique

### Backend Java (Spring Boot)
- **Core** : Application principale et orchestration
- **Avatar** : Gestion avatar 3D et synthèse vocale
- **Voice** : Reconnaissance vocale et traitement commandes
- **Intelligence** : Moteur de propositions contextuelles
- **API** : Endpoints REST pour contrôle
- **UI** : Contrôleurs web pour interface

### Frontend JavaScript
- **Avatar** : Rendu 3D (Three.js), animations
- **Voice** : Reconnaissance (Web Speech API), synthèse vocale
- **Core** : Configuration, utilitaires, bridges Java/JS

### Base de Données
- **H2 intégrée** : Historique, préférences, activités
- **Console** : http://localhost:8080/angel/h2-console

## 🔒 Sécurité et Permissions

- **Microphone** : Requis pour reconnaissance vocale
- **WebGL** : Requis pour avatar 3D
- **WebSocket** : Communication temps réel Java/JS
- **Stockage local** : Configuration et cache

## 🌍 Support Navigateurs

- **Chrome** : Support complet (recommandé)
- **Firefox** : Support complet
- **Safari** : Support partiel (reconnaissance vocale limitée)
- **Edge** : Support complet

## 📱 Compatibilité Mobile

- Interface responsive adaptée
- Contrôles tactiles optimisés
- Performances réduites pour préserver batterie
- Reconnaissance vocale selon support navigateur

## 🔄 Maintenance

### Mise à Jour
```bash
git pull
./angel-launcher.sh stop
./angel-launcher.sh build
./angel-launcher.sh start
```

### Sauvegarde
```bash
# Sauvegarder la base de données
cp data/angel.mv.db backup/

# Sauvegarder la configuration
cp -r config/ backup/
```

### Nettoyage
```bash
./angel-launcher.sh clean         # Nettoie les compilations
rm -rf logs/*.log               # Supprime les anciens logs
```

## 🎊 Roadmap

### Version Actuelle (1.1.0)
- Avatar 3D avec Ready Player Me
- Reconnaissance vocale continue "Angèle"
- Synthèse vocale émotionnelle
- Interface web complète
- Mode test autonome

### Prochaines Versions
- **1.2.0** : Commandes vocales étendues, multi-langues
- **1.3.0** : IA conversationnelle, reconnaissance faciale
- **2.0.0** : Support VR/AR, écosystème IoT

---

Angel Virtual Assistant offre une expérience d'assistant virtuel complète avec avatar 3D photoréaliste et interaction vocale naturelle en français. 🎭🎤
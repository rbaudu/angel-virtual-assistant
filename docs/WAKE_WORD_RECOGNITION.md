# Wake Word Recognition - Documentation Technique

## 📖 Vue d'ensemble

Le système de reconnaissance vocale d'Angel permet de détecter le mot-clé "Angel" et de traiter les commandes vocales des utilisateurs. Cette implémentation utilise une architecture hybride client-serveur pour optimiser les performances et la compatibilité.

## 🏗️ Architecture

### Architecture Générale

```
┌─────────────────────────┐               ┌──────────────────────────┐
│      Navigateur         │               │    Serveur Spring Boot   │
│  (Client/Frontend)      │               │     (Backend/API)        │
├─────────────────────────┤               ├──────────────────────────┤
│ Web Speech Recognition  │◄─────────────►│ WakeWordWebSocketController│
│ ├─ SpeechRecognitionSvc │   WebSocket   │ ├─ AngelApplication       │
│ ├─ WakeWordDetector     │               │ ├─ ProposalEngine         │
│ └─ Interface utilisateur│               │ └─ Avatar Controller     │
└─────────────────────────┘               └──────────────────────────┘
```

### Flux de données

```
1. Utilisateur dit "Angel"
   ↓
2. Web Speech Recognition (navigateur)
   ↓
3. Détection du mot-clé
   ↓
4. Envoi WebSocket au serveur
   ↓
5. Activation d'Angel
   ↓
6. Mode écoute commande
   ↓
7. Traitement de la commande
   ↓
8. Réponse via Avatar
   ↓
9. Retour en mode veille
```

## 🔧 Composants Techniques

### 1. Backend (Java/Spring Boot)

#### `WakeWordWebSocketController.java`
- **Rôle** : Gestionnaire WebSocket pour la communication vocale
- **Localisation** : `src/main/java/com/angel/voice/`
- **Responsabilités** :
  - Gestion des connexions WebSocket
  - Traitement des messages de détection vocale
  - Communication avec AngelApplication
  - Diffusion des réponses aux clients

```java
// Exemple d'utilisation
@Component
public class WakeWordWebSocketController implements WebSocketHandler {
    // Configuration WebSocket
    // Traitement des messages
    // Gestion des erreurs
}
```

#### `WakeWordDetector.java` (mis à jour)
- **Rôle** : Coordinateur côté serveur
- **Localisation** : `src/main/java/com/angel/voice/`
- **Responsabilités** :
  - Interface de compatibilité avec l'ancien système
  - Surveillance des connexions WebSocket
  - Configuration vocale centralisée

#### `WebSocketConfig.java`
- **Rôle** : Configuration Spring WebSocket
- **Localisation** : `src/main/java/com/angel/config/`
- **Configuration** : Point de terminaison `/ws/voice`

#### Méthodes ajoutées dans `AngelApplication.java`
- `handleWakeWordActivation()` : Gestion de l'activation
- `processVoiceCommand()` : Traitement des commandes vocales
- `analyzeCommandType()` : Classification des commandes
- Gestionnaires spécialisés (météo, heure, etc.)

### 2. Frontend (JavaScript/HTML/CSS)

#### `speech-recognition.js`
- **Rôle** : Service de reconnaissance vocale
- **Localisation** : `src/main/resources/static/js/`
- **Fonctionnalités** :
  - Utilisation de l'API Web Speech Recognition
  - Détection du mot-clé avec tolérance aux erreurs
  - Gestion des modes (wake word / commande)
  - Gestion d'erreurs et reconnexion

```javascript
class SpeechRecognitionService {
    // Configuration de la reconnaissance
    // Détection du wake word
    // Traitement des commandes
    // Gestion des erreurs
}
```

#### `wake-word-detector.js`
- **Rôle** : Intégration WebSocket + reconnaissance
- **Localisation** : `src/main/resources/static/js/`
- **Fonctionnalités** :
  - Communication WebSocket avec le serveur
  - Interface utilisateur (indicateurs, status)
  - Coordination entre reconnaissance et serveur

#### `wake-word-detector.css`
- **Rôle** : Styles pour l'interface vocale
- **Localisation** : `src/main/resources/static/css/`
- **Éléments stylés** :
  - Indicateur d'écoute (microphone)
  - Statut de reconnaissance
  - Contrôles vocaux
  - Panneau de configuration

#### `avatar.html` (modifié)
- **Rôle** : Interface utilisateur principale
- **Localisation** : `src/main/resources/templates/`
- **Nouveaux éléments** :
  - Indicateur de reconnaissance vocale
  - Contrôles de démarrage/arrêt
  - Panneau de paramètres
  - Messages de statut

## ⚙️ Configuration

### Fichier de configuration principal

**Localisation** : `config/application-test.properties`

```properties
# Configuration de base
voice.wake-word=Angel
voice.language=fr-FR
voice.continuous=true
voice.confidence-threshold=0.7

# Configuration avancée
voice.recognition-timeout=10000
voice.return-to-sleep-delay=3000
voice.wake-word-variants=angel,anjel,ange,angie
voice.debug=true

# WebSocket
voice.websocket.path=/ws/voice
voice.websocket.connection-timeout=5000
voice.websocket.ping-interval=30000
```

### Configuration dynamique

Les paramètres peuvent être modifiés via l'interface web :
- Mot-clé personnalisé
- Langue de reconnaissance
- Seuil de confiance
- Mode écoute continue
- Mode debug

## 🚀 Installation et Déploiement

### Prérequis

- Java 17+
- Spring Boot 3.2+
- Navigateur compatible (Chrome, Edge, Safari)
- Microphone fonctionnel
- Connexion HTTPS (recommandé pour la production)

### Étapes d'installation

1. **Créer les fichiers Java**
   ```bash
   mkdir -p src/main/java/com/angel/config
   mkdir -p src/main/java/com/angel/voice
   # Copier WebSocketConfig.java
   # Copier WakeWordWebSocketController.java
   # Remplacer WakeWordDetector.java
   ```

2. **Ajouter les ressources frontend**
   ```bash
   mkdir -p src/main/resources/static/js
   mkdir -p src/main/resources/static/css
   # Copier speech-recognition.js
   # Copier wake-word-detector.js
   # Copier wake-word-detector.css
   ```

3. **Modifier l'interface**
   ```bash
   # Mettre à jour avatar.html
   ```

4. **Configurer l'application**
   ```bash
   # Ajouter les propriétés dans application-test.properties
   ```

5. **Redémarrer le serveur**
   ```bash
   ./angel-launcher.sh start -p test
   ```

### Vérification de l'installation

1. **Accéder à l'interface** : `http://localhost:8080/angel`
2. **Autoriser le microphone** : Le navigateur doit demander l'permission
3. **Vérifier l'indicateur** : L'icône microphone doit être verte
4. **Tester la détection** : Dire "Angel" → Réaction du système
5. **Tester une commande** : "Quelle heure est-il ?" → Réponse

## 🔍 Utilisation

### Interface utilisateur

#### Indicateurs visuels
- **🎤 Vert pulsant** : Écoute active du mot-clé
- **🎤 Bleu** : Mot-clé détecté, en attente de commande
- **🎤 Gris** : Système inactif
- **Message en bas** : Statut détaillé du système

#### Contrôles disponibles
- **▶️** : Démarrer l'écoute manuelle
- **⏹️** : Arrêter l'écoute
- **⚙️** : Ouvrir les paramètres vocaux

### Commandes vocales supportées

#### Mot-clé d'activation
- "Angel" (principal)
- Variantes tolérées : "Anjel", "Ange", "Angie"

#### Types de commandes
1. **Météo** : "Quel temps fait-il ?", "Météo du jour"
2. **Heure** : "Quelle heure est-il ?", "Il est quelle heure ?"
3. **Propositions** : "Que me proposes-tu ?", "Une suggestion ?"
4. **Questions générales** : Questions ouvertes pour l'IA
5. **Contrôle système** : "Stop", "Silence", "Pause"

### Workflow utilisateur typique

1. **Activation** : Dire "Angel"
   - Réponse : "Oui, comment puis-je vous aider ?"
   - Indicateur passe au bleu

2. **Commande** : Poser une question
   - Exemple : "Quelle heure est-il ?"
   - Statut : "💭 Traitement: Quelle heure est-il ?"

3. **Réponse** : Angel répond
   - Exemple : "Il est 14h30."
   - Avatar parle et affiche le texte

4. **Retour en veille** : Automatique après 3 secondes
   - Indicateur redevient vert
   - Prêt pour une nouvelle activation

## 🐛 Dépannage

### Problèmes courants

#### Microphone non autorisé
**Symptômes** :
- Message "🎤 Accès au microphone requis"
- Indicateur reste gris

**Solutions** :
1. Autoriser le microphone dans les paramètres du navigateur
2. Utiliser HTTPS en production
3. Recharger la page après autorisation

#### WebSocket ne se connecte pas
**Symptômes** :
- Message "🔌 Erreur de connexion au serveur"
- Pas de réaction aux commandes vocales

**Solutions** :
1. Vérifier que le serveur Spring Boot fonctionne
2. Contrôler les logs serveur pour erreurs WebSocket
3. Vérifier la configuration réseau/proxy

#### Reconnaissance vocale défaillante
**Symptômes** :
- Mot-clé non détecté
- Commandes mal comprises

**Solutions** :
1. Utiliser Chrome ou Edge (meilleure compatibilité)
2. Parler plus clairement et distinctement
3. Ajuster le seuil de confiance (paramètres vocaux)
4. Vérifier la langue configurée

#### Performance dégradée
**Symptômes** :
- Délais de réponse longs
- Reconnexions fréquentes

**Solutions** :
1. Vérifier la qualité de la connexion réseau
2. Augmenter les timeouts dans la configuration
3. Redémarrer l'application

### Logs de diagnostic

#### Côté serveur
```
INFO com.angel.voice.WakeWordWebSocketController : WebSocket connecté
INFO com.angel.voice.WakeWordDetector : WakeWordDetector initialisé
INFO com.angel.core.AngelApplication : Mot-clé détecté, activation du système
```

#### Côté client (Console navigateur)
```javascript
Initialisation de la détection vocale...
WebSocket connecté pour la détection vocale
Reconnaissance vocale démarrée
Mot-clé détecté: Angel
Commande reçue: quelle heure est-il
```

## 🔌 Extensibilité

### Intégration IA

Le système est conçu pour une intégration facile avec des services d'IA :

```java
// Dans AngelApplication.java
private String generateAIResponse(String question) {
    try {
        // Intégration OpenAI
        String apiKey = configManager.getString("openai.api-key");
        // ... appel API
        
        // Intégration Claude
        String claudeKey = configManager.getString("claude.api-key");
        // ... appel API
        
        return aiResponse;
    } catch (Exception e) {
        return getFallbackResponse(question);
    }
}
```

### Nouveaux types de commandes

Ajouter de nouveaux types dans `CommandType` enum et leurs gestionnaires :

```java
private enum CommandType {
    WEATHER_REQUEST,
    TIME_REQUEST,
    NEWS_REQUEST,        // Nouveau
    CALENDAR_REQUEST,    // Nouveau
    SMART_HOME_CONTROL,  // Nouveau
    // ...
}
```

### Langues supplémentaires

Configuration multilingue dans `application-test.properties` :

```properties
# Support multilingue
voice.languages.fr=fr-FR
voice.languages.en=en-US
voice.languages.es=es-ES
voice.languages.de=de-DE

# Mots-clés par langue
voice.wake-words.fr=Angel,Ange
voice.wake-words.en=Angel,Helper
voice.wake-words.es=Angel,Asistente
```

## 📊 Métriques et Monitoring

### Métriques collectées

- Nombre de détections de wake word
- Taux de succès des commandes vocales
- Temps de réponse moyen
- Erreurs de reconnaissance vocale
- Connexions WebSocket actives

### Logs structurés

```java
// Exemple de logging structuré
LOGGER.log(Level.INFO, "Voice command processed", Map.of(
    "command", command,
    "confidence", confidence,
    "processingTime", processingTime,
    "success", success
));
```

## 🔒 Sécurité

### Considérations de sécurité

1. **Données vocales** : Pas de stockage côté serveur
2. **WebSocket** : Validation des messages entrants
3. **HTTPS** : Obligatoire en production pour l'accès microphone
4. **Rate limiting** : Protection contre le spam de commandes

### Configuration HTTPS

```properties
# Configuration SSL pour la production
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

## 📈 Roadmap

### Améliorations prévues

- [ ] Support de multiple wake words simultanés
- [ ] Reconnaissance vocale offline (WebAssembly)
- [ ] Interface d'administration pour la configuration
- [ ] Analytics avancées des interactions vocales
- [ ] Support des commandes complexes multi-étapes
- [ ] Intégration native avec les services d'IA populaires

### Contributions

Le système est conçu pour être extensible. Les contributions sont les bienvenues pour :
- Nouveaux types de commandes
- Améliorations de la reconnaissance vocale
- Support de nouvelles langues
- Intégrations IA

---

**Version du document** : 1.0  
**Dernière mise à jour** : 24 juillet 2025  
**Auteur** : Angel Development Team
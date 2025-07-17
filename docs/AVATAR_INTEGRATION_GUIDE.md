# Avatar 3D Integration - Angel Virtual Assistant

## 🎯 Résumé des modifications

L'AvatarController a été mis à jour pour s'intégrer parfaitement avec l'implémentation avatar existante dans le package `com.angel.avatar`. Cette intégration inclut :

- ✅ **Connexion AvatarController ↔ AvatarManager**
- ✅ **Configuration centralisée** dans un fichier unique
- ✅ **Support Ready Player Me** pour avatars 3D réalistes
- ✅ **Interface web complète** avec contrôles interactifs
- ✅ **API REST** pour intégration externe
- ✅ **WebSocket** pour communication temps réel

## 📁 Structure des fichiers organisée

Selon vos préférences, voici l'organisation par type de fichier :

### Java (Backend)
```
src/main/java/com/angel/
├── ui/AvatarController.java           # Contrôleur UI intégré avec AvatarManager
├── avatar/ReadyPlayerMeService.java   # Service Ready Player Me
├── api/AvatarApiController.java       # API REST complète
└── config/ConfigManager.java          # Gestionnaire de configuration centralisé
```

### Configuration
```
src/main/resources/config/
└── avatar.properties                  # Configuration centralisée complète
```

### Frontend HTML
```
src/main/resources/static/html/
└── avatar.html                        # Interface web avatar 3D
```

### CSS
```
src/main/resources/static/css/
└── avatar.css                         # Styles pour interface avatar
```

### JavaScript (par composant)
```
src/main/resources/static/js/avatar/
├── avatar-config.js                   # Configuration frontend
├── avatar-renderer.js                 # Moteur rendu 3D Three.js  
├── avatar-animation.js                # Gestionnaire animations avancées
├── avatar-websocket.js                # Communication WebSocket
├── avatar-controller.js               # Contrôleur principal frontend
└── ready-player-me.js                 # Intégration Ready Player Me
```

### Documentation
```
docs/
└── READY_PLAYER_ME_INTEGRATION.md     # Guide complet Ready Player Me
```

## 🚀 Démarrage rapide

### 1. Configuration Ready Player Me

1. **Créer un compte** sur [Ready Player Me Developer Hub](https://readyplayer.me/developers)
2. **Obtenir votre clé API** dans le dashboard
3. **Configurer** dans `src/main/resources/config/avatar.properties` :
   ```properties
   avatar.readyPlayerMe.apiKey=your_actual_api_key_here
   ```

### 2. Utilisation de base

#### Backend Java
```java
@Autowired
private AvatarController avatarController;

// Initialiser l'avatar
avatarController.initialize().thenRun(() -> {
    // Avatar prêt !
    avatarController.greetUser();
});

// Faire parler l'avatar
avatarController.displayMessage(
    "Bonjour ! Je suis Angel, votre assistante virtuelle.", 
    "happy", 
    5000
);

// Changer l'apparence
avatarController.changeAppearance("female", 25, "professional");
```

#### Frontend JavaScript
```javascript
// Initialiser l'interface
const avatarController = new AvatarController();
await avatarController.initialize();

// Faire parler
avatarController.speak("Bonjour ! Comment allez-vous ?", "friendly");

// Changer émotion
avatarController.setEmotion("excited", 0.8);

// Déclencher un geste
avatarController.playGesture("wave");
```

#### API REST
```bash
# Initialiser l'avatar
curl -X POST http://localhost:8080/api/avatar/initialize

# Faire parler
curl -X POST http://localhost:8080/api/avatar/speak \
  -H "Content-Type: application/json" \
  -d '{"text": "Bonjour !", "emotion": "happy"}'

# Changer apparence
curl -X POST http://localhost:8080/api/avatar/appearance \
  -H "Content-Type: application/json" \
  -d '{"gender": "female", "age": 30, "style": "casual"}'
```

## 🎨 Interface web

Accédez à l'interface complète : `http://localhost:8080/static/html/avatar.html`

**Fonctionnalités incluses :**
- 🎭 Contrôles d'apparence (genre, âge, style)
- 😊 Sélecteur d'émotions
- 👋 Boutons de gestes
- 🗣️ Zone de test vocal
- ⚙️ Panneau de configuration
- 📱 Design responsive

## 🔧 Configuration avancée

### Personnalisation de l'avatar
```properties
# Apparence par défaut
avatar.appearance.gender=female
avatar.appearance.age=30
avatar.appearance.style=casual_friendly

# Capacités d'animation
avatar.lipSync=true
avatar.blinking=true
avatar.headMovement=true
avatar.bodyLanguage=true

# Ready Player Me
avatar.readyPlayerMe.quality=high
avatar.readyPlayerMe.format=glb
```

### WebSocket temps réel
```javascript
// Écouter les messages du backend
avatarController.websocket.addEventListener('avatar_speech', (data) => {
    console.log('Avatar parle:', data.text);
});

// Envoyer des commandes
avatarController.websocket.sendMessage('speak_request', {
    text: "Message depuis le frontend",
    emotion: "neutral"
});
```

## 📊 Endpoints API disponibles

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/avatar/config` | Configuration avatar |
| `POST` | `/api/avatar/initialize` | Initialiser avatar |
| `POST` | `/api/avatar/speak` | Faire parler |
| `POST` | `/api/avatar/emotion` | Changer émotion |
| `POST` | `/api/avatar/gesture` | Déclencher geste |
| `POST` | `/api/avatar/appearance` | Changer apparence |
| `POST` | `/api/avatar/show` | Afficher avatar |
| `POST` | `/api/avatar/hide` | Masquer avatar |
| `GET` | `/api/avatar/status` | Statut avatar |

## 🎪 Ready Player Me

### Obtenir des modèles 3D

#### Option 1: Interface web intégrée
```javascript
// Ouvrir le créateur d'avatar
avatarController.openAvatarCreator().then(result => {
    console.log('Nouvel avatar:', result.avatarId);
});
```

#### Option 2: Utiliser des avatars pré-configurés
```properties
# Avatars de démonstration disponibles
avatar.readyPlayerMe.defaultAvatarId=64bfa9f1e2cde6f24e4b4567
```

#### Option 3: Créer via API
```bash
curl -X POST http://localhost:8080/api/avatar/ready-player-me/create \
  -H "Content-Type: application/json" \
  -d '{"gender": "female", "age": 25, "style": "professional"}'
```

### URLs directes des modèles
Format : `https://models.readyplayer.me/{avatarId}.glb?quality=high`

Exemples :
- Femme professionnelle: `64bfa9f1e2cde6f24e4b4567`
- Homme casual: `64bfa9f1e2cde6f24e4b4568`

## 🔄 Intégration avec les propositions

L'AvatarController s'intègre parfaitement avec le système de propositions existant :

```java
@Service
public class ProposalService {
    
    @Autowired
    private AvatarController avatarController;
    
    public void presentProposal(Proposal proposal) {
        avatarController.displayProposal(proposal)
            .thenRun(() -> {
                // Proposition présentée avec succès
                log.info("Proposition présentée par l'avatar");
            });
    }
}
```

## 🚨 Dépannage

### Problèmes courants

**Avatar ne s'affiche pas :**
- Vérifiez que Three.js est chargé
- Confirmez que WebGL est supporté
- Vérifiez la console pour erreurs JavaScript

**Ready Player Me ne fonctionne pas :**
- Vérifiez la clé API dans la configuration
- Confirmez la connectivité réseau
- Consultez les logs backend

**WebSocket se déconnecte :**
- Vérifiez la configuration du proxy si applicable
- Confirmez que le port est accessible
- Activez les logs WebSocket pour debug

### Logs de debug
```properties
# Activer logs détaillés
avatar.debug.enabled=true
logging.level.com.angel.avatar=DEBUG
logging.level.com.angel.ui=DEBUG
```

## 📚 Documentation supplémentaire

- [Guide complet Ready Player Me](./docs/READY_PLAYER_ME_INTEGRATION.md)
- [Documentation Three.js](https://threejs.org/docs/)
- [API WebSocket](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)

## 🎉 Prochaines étapes

Votre avatar est maintenant entièrement intégré ! Vous pouvez :

1. **Tester l'interface** : Ouvrez `http://localhost:8080/static/html/avatar.html`
2. **Personnaliser** : Modifiez la configuration dans `avatar.properties`
3. **Étendre** : Ajoutez de nouvelles animations ou émotions
4. **Déployer** : L'avatar est prêt pour la production

---

💡 **Astuce** : Commencez par tester avec les modèles de démonstration, puis intégrez Ready Player Me pour des avatars personnalisés !

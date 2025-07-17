# Guide d'intégration Ready Player Me pour Angel Virtual Assistant

## Vue d'ensemble

Ready Player Me est une plateforme qui permet de créer des avatars 3D personnalisés pour les applications et jeux. Ce guide explique comment intégrer et utiliser Ready Player Me avec votre assistant virtuel Angel.

## 1. Configuration initiale

### Étape 1: Créer un compte Ready Player Me

1. Allez sur [Ready Player Me Developer Hub](https://readyplayer.me/developers)
2. Créez un compte développeur
3. Créez une nouvelle application dans le dashboard
4. Notez votre **API Key** et **Application ID**

### Étape 2: Configurer l'application Angel

1. Ouvrez le fichier `src/main/resources/config/avatar.properties`
2. Remplacez `YOUR_READY_PLAYER_ME_API_KEY_HERE` par votre vraie clé API :
   ```properties
   avatar.readyPlayerMe.apiKey=your_actual_api_key_here
   ```
3. Redémarrez l'application

## 2. Comment obtenir des modèles 3D

### Option A: Utiliser l'interface web Ready Player Me

1. **Création manuelle via le site web:**
   - Allez sur [readyplayer.me](https://readyplayer.me)
   - Cliquez sur "Create Avatar"
   - Personnalisez votre avatar avec les options disponibles
   - Une fois terminé, notez l'ID de l'avatar dans l'URL (format: `64bfa9f1e2cde6f24e4b4567`)
   - Utilisez cet ID dans votre configuration

2. **Télécharger le modèle GLB:**
   - Format de l'URL: `https://models.readyplayer.me/64bfa9f1e2cde6f24e4b4567.glb`
   - Remplacez l'ID par votre avatar ID
   - Vous pouvez télécharger et stocker localement ou utiliser l'URL directement

### Option B: Utiliser l'API Ready Player Me (Recommandé)

1. **Via l'interface Angel intégrée:**
   ```javascript
   // Dans le frontend
   avatarController.openAvatarCreator().then(result => {
       console.log('Nouvel avatar créé:', result.avatarId);
   });
   ```

2. **Via l'API REST Angel:**
   ```bash
   # Créer un nouvel avatar
   curl -X POST http://localhost:8080/api/avatar/ready-player-me/create \
     -H "Content-Type: application/json" \
     -d '{"gender": "female", "age": 25, "style": "professional"}'
   ```

3. **Programmatiquement avec le service Java:**
   ```java
   @Autowired
   ReadyPlayerMeService readyPlayerMeService;
   
   CompletableFuture<String> avatarId = readyPlayerMeService.createAvatar("female", 30, "casual");
   ```

### Option C: Utiliser des modèles pré-configurés

Des avatars de démonstration sont disponibles avec ces IDs:
- Femme professionnelle: `64bfa9f1e2cde6f24e4b4567`
- Homme casual: `64bfa9f1e2cde6f24e4b4568`
- Femme jeune: `64bfa9f1e2cde6f24e4b4569`

## 3. Structure des modèles Ready Player Me

### Formats supportés
- **GLB** (recommandé): Format binaire optimisé
- **GLTF**: Format JSON avec fichiers séparés

### Qualités disponibles
- `low`: Optimisé pour mobile (2K-5K triangles)
- `medium`: Qualité équilibrée (5K-8K triangles)
- `high`: Haute qualité (8K-15K triangles)

### Fonctionnalités incluses
- **Animations faciales**: Morph targets pour expressions
- **Synchronisation labiale**: Visemes pour la parole
- **Bones**: Squelette pour animations corporelles
- **Textures PBR**: Matériaux réalistes

## 4. Configuration avancée

### Personnalisation de l'apparence
```properties
# Dans avatar.properties
avatar.readyPlayerMe.quality=high
avatar.readyPlayerMe.format=glb

# Options de personnalisation par défaut
avatar.appearance.hairColor=brown
avatar.appearance.eyeColor=blue
avatar.appearance.skinTone=medium
```

### Paramètres de requête disponibles
```
https://models.readyplayer.me/{avatarId}.glb?
quality=high&
textureAtlas=1024&
morphTargets=ARKit&
lod=0
```

## 5. Utilisation dans l'application

### Chargement dynamique d'un avatar
```java
// Backend - Service
public CompletableFuture<Void> loadCustomAvatar(String avatarId) {
    return readyPlayerMeService.getAvatarModelUrl(avatarId)
        .thenCompose(modelUrl -> avatarManager.loadAvatar(modelUrl));
}
```

```javascript
// Frontend - JavaScript
async function loadCustomAvatar(avatarId) {
    const response = await fetch(`/api/avatar/ready-player-me/${avatarId}/model`);
    const data = await response.json();
    
    if (data.status === 'success') {
        await avatarRenderer.loadAvatar(data.modelUrl);
    }
}
```

### Changement d'apparence en temps réel
```java
// Via l'API REST
@PostMapping("/avatar/appearance")
public CompletableFuture<ResponseEntity<Object>> changeAppearance(@RequestBody AppearanceRequest request) {
    return readyPlayerMeService.createAvatar(request.getGender(), request.getAge(), request.getStyle())
        .thenCompose(avatarId -> readyPlayerMeService.getAvatarModelUrl(avatarId))
        .thenCompose(modelUrl -> avatarController.loadAvatar(modelUrl));
}
```

## 6. Optimisation et cache

### Cache local des modèles
```java
// Configuration du cache
@Component
public class AvatarCacheManager {
    private final Map<String, String> modelCache = new ConcurrentHashMap<>();
    
    public String getCachedModelUrl(String avatarId) {
        return modelCache.get(avatarId);
    }
    
    public void cacheModelUrl(String avatarId, String url) {
        modelCache.put(avatarId, url);
    }
}
```

### Préchargement des avatars populaires
```properties
# Avatars à précharger au démarrage
avatar.preload.avatars=64bfa9f1e2cde6f24e4b4567,64bfa9f1e2cde6f24e4b4568
```

## 7. Fallback et gestion d'erreurs

### Modèles de fallback locaux
Placez vos modèles de secours dans : `src/main/resources/static/models/avatars/`

Structure recommandée:
```
static/models/avatars/
├── female_young_casual.glb
├── female_adult_professional.glb
├── female_mature_elegant.glb
├── male_young_casual.glb
├── male_adult_professional.glb
└── male_mature_distinguished.glb
```

### Configuration des fallbacks
```properties
# Modèles de fallback si Ready Player Me indisponible
avatar.models.fallback.female.young=female_young_casual.glb
avatar.models.fallback.female.adult=female_adult_professional.glb
avatar.models.fallback.male.young=male_young_casual.glb
```

## 8. Bonnes pratiques

### Sécurité
- Ne jamais exposer votre clé API dans le frontend
- Utiliser HTTPS pour toutes les requêtes
- Implémenter une limitation de taux pour les créations d'avatars

### Performance
- Utiliser le cache pour éviter les téléchargements répétés
- Précharger les avatars les plus utilisés
- Optimiser la qualité selon l'utilisation (mobile vs desktop)

### UX
- Afficher un indicateur de chargement pendant la création
- Permettre la prévisualisation avant application
- Sauvegarder les préférences utilisateur

## 9. Dépannage

### Problèmes courants

**Avatar ne se charge pas:**
- Vérifiez la clé API dans la configuration
- Confirmez que l'ID d'avatar est valide
- Vérifiez la connectivité réseau

**Erreur 401 Unauthorized:**
- Vérifiez que la clé API est correcte
- Assurez-vous que l'application est bien configurée dans Ready Player Me

**Modèle 3D déformé:**
- Vérifiez que les animations et morph targets sont supportés
- Utilisez la qualité 'high' pour les détails importants

### Logs de debug
```properties
# Activer les logs détaillés
avatar.debug.enabled=true
logging.level.com.angel.avatar=DEBUG
```

## 10. Ressources supplémentaires

- [Documentation API Ready Player Me](https://docs.readyplayer.me/)
- [Exemples Three.js pour avatars](https://threejs.org/examples/)
- [Guide des morph targets](https://threejs.org/docs/#api/en/objects/Mesh.morphTargetInfluences)
- [Spécifications GLTF](https://www.khronos.org/gltf/)

---

Pour plus d'assistance, consultez la documentation Ready Player Me ou contactez leur support technique.

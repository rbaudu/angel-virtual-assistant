# Ready Player Me - Angel Virtual Assistant

Guide d'intégration spécifique pour les avatars Ready Player Me.

## 🎭 Vue d'Ensemble

Ready Player Me est une plateforme qui permet de créer des avatars 3D personnalisés pour les applications et jeux. Ce guide explique comment utiliser Ready Player Me avec Angel Virtual Assistant.

### Avantages Ready Player Me
- **Avatars photoréalistes** : Qualité professionnelle
- **Personnalisation poussée** : Visage, vêtements, accessoires depuis photo
- **Optimisation web** : Modèles GLB optimisés pour le navigateur
- **Cross-platform** : Compatible tous navigateurs modernes
- **Bibliothèque étendue** : Milliers d'avatars disponibles

## 1. Configuration Initiale

### Qu'est-ce que Ready Player Me ?

Ready Player Me est une plateforme accessible sur :
- **Site principal** : https://readyplayer.me/
- **Hub des avatars** : https://readyplayer.me/hub
- **Documentation** : https://docs.readyplayer.me/

### Configuration dans Angel

Dans `config/avatar.properties`, Angel utilise cette configuration :

```properties
# Ready Player Me
avatar.ready-player-me.enabled=true
avatar.ready-player-me.default-id=687f66fafe8107131699bf7b
avatar.ready-player-me.base-url=https://models.readyplayer.me
avatar.ready-player-me.timeout=30000
```

**Note** : Angel utilise actuellement Ready Player Me en mode public (sans clé API requise) pour les modèles existants.

### Avatar par Défaut

L'avatar par défaut d'Angel est :
- **ID** : `687f66fafe8107131699bf7b`
- **Type** : Avatar féminin professionnel
- **URL modèle** : https://models.readyplayer.me/687f66fafe8107131699bf7b.glb
- **Aperçu** : https://models.readyplayer.me/687f66fafe8107131699bf7b.png

## 2. Comment Obtenir des Modèles 3D

### Option A : Utiliser l'Hub Ready Player Me

1. **Parcourir les avatars existants :**
   - Allez sur https://readyplayer.me/hub
   - Explorez les avatars créés par la communauté
   - Filtrez par style : réaliste, cartoon, etc.
   - Notez l'ID dans l'URL (format: `64bfa9f1e2cde6f24e4b4567`)

2. **Utiliser l'ID dans Angel :**
   - Modifiez `avatar.ready-player-me.default-id` dans `config/avatar.properties`
   - Ou utilisez l'ID via l'interface web d'Angel si disponible

### Option B : Créer un Avatar Personnalisé

1. **Création depuis photo :**
   - Allez sur https://readyplayer.me/
   - Téléchargez une photo de face bien éclairée
   - L'IA génère automatiquement l'avatar
   - Personnalisez les vêtements, accessoires
   - Récupérez l'ID depuis l'URL finale

2. **Éditeur manuel :**
   - Utilisez l'éditeur https://readyplayer.me/avatar
   - Personnalisez chaque aspect (visage, corps, style)
   - Sauvegardez et récupérez l'ID

### Option C : Avatars de Démonstration

Quelques avatars testés compatibles avec Angel :
- `687f66fafe8107131699bf7b` : Avatar féminin (défaut)
- `64bfa9f2c678a5b8c1e5e123` : Avatar alternatif
- `65cfa8e3d789b6c9d2f6f234` : Avatar masculin

## 3. Structure des Modèles Ready Player Me

### Formats Supportés

**GLB (recommandé)** : Format binaire optimisé
- Géométrie + Textures + Matériaux dans un seul fichier
- Taille typique : 2-8 MB
- Compatible Three.js (utilisé par Angel)

### Qualités Disponibles

Angel peut utiliser différentes qualités via les paramètres URL :
- `?quality=low` : Mobile/performances (2K-5K triangles)
- `?quality=medium` : Équilibré (5K-8K triangles) - **utilisé par Angel**
- `?quality=high` : Haute qualité (8K-15K triangles)

### Fonctionnalités Incluses

**Structure typique d'un modèle Ready Player Me :**
- **Géométrie optimisée** : Maillage adapté temps réel
- **Matériaux PBR** : Compatibles avec l'éclairage d'Angel
- **Textures compressées** : Diffuse, Normal, Roughness maps
- **Rigging standard** : Skeleton humanoïde
- **Morph targets** : Pour expressions faciales (si supporté)

### URLs des Ressources

**Modèle 3D :**
```
https://models.readyplayer.me/{avatarId}.glb
```

**Image d'aperçu :**
```
https://models.readyplayer.me/{avatarId}.png
```

**Avec paramètres de qualité :**
```
https://models.readyplayer.me/{avatarId}.glb?quality=medium
```

## 4. Configuration Avancée

### Paramètres de Configuration

Dans `config/avatar.properties`, vous pouvez ajuster :

```properties
# Configuration Ready Player Me
avatar.ready-player-me.enabled=true
avatar.ready-player-me.default-id=votre-avatar-id
avatar.ready-player-me.fallback-id=id-avatar-secours
avatar.ready-player-me.timeout=30000
avatar.ready-player-me.base-url=https://models.readyplayer.me

# Cache (si implémenté)
avatar.ready-player-me.cache.enabled=true
avatar.ready-player-me.cache.duration=86400000
```

### Variables d'Environnement

Pour la production, vous pouvez utiliser :
```bash
# Optionnel si clé API nécessaire
export READY_PLAYER_ME_API_KEY=your_api_key

# Configuration de qualité
export RPM_QUALITY=medium
```

## 5. Utilisation dans l'Application

### Chargement d'Avatar

Le chargement se fait automatiquement au démarrage d'Angel selon la configuration. Les fichiers concernés :

**Backend :**
- `src/main/java/com/angel/avatar/` : Services de gestion avatar
- Configuration chargée depuis `config/avatar.properties`

**Frontend :**
- `src/main/resources/static/js/avatar/` : Scripts Three.js pour le rendu
- Utilise Three.js GLTFLoader pour charger les modèles Ready Player Me

### Changement d'Avatar

**Via configuration :**
1. Modifiez `avatar.ready-player-me.default-id` dans `config/avatar.properties`
2. Redémarrez Angel

**Via interface web :**
- Utilisez les contrôles disponibles dans l'interface Angel
- Les modifications peuvent être temporaires selon l'implémentation

## 6. Optimisation et Cache

### Performance

Angel optimise l'utilisation de Ready Player Me :
- **Chargement asynchrone** : Évite le blocage de l'interface
- **Réutilisation de modèles** : Cache navigateur pour éviter re-téléchargements
- **Qualité adaptative** : Peut ajuster selon les performances

### Gestion Mémoire

**Côté navigateur :**
- Three.js gère automatiquement la mémoire GPU
- Les modèles sont optimisés pour le temps réel
- Déchargement automatique si changement d'avatar

## 7. Fallback et Gestion d'Erreurs

### Modèles de Secours

En cas de problème avec Ready Player Me :

1. **Avatar de fallback configuré :**
   - `avatar.ready-player-me.fallback-id` dans la config
   - Utilisé si l'avatar principal échoue

2. **Modèles locaux (si disponibles) :**
   - Stockés dans `src/main/resources/static/models/avatars/`
   - Chargés si Ready Player Me indisponible

### Gestion d'Erreurs

**Cas d'erreur courants :**
- Réseau indisponible → Utilise fallback
- Avatar ID invalide → Utilise avatar par défaut
- Modèle corrompu → Logs d'erreur et fallback

**Vérification dans les logs :**
```bash
# Voir les erreurs avatar
grep -i "avatar\|ready.*player" logs/angel.log
```

## 8. Bonnes Pratiques

### Choix des Avatars

**Pour Angel Virtual Assistant :**
- Privilégier les avatars **réalistes** plutôt que cartoon
- Éviter les avatars avec accessoires trop volumineux
- Tester la qualité du rendu dans l'environnement Angel

**Qualité recommandée :**
- Desktop : `medium` ou `high`
- Mobile : `low` ou `medium`
- Test : `low` pour développement

### Performance

**Optimisations recommandées :**
- Tester la vitesse de chargement de l'avatar choisi
- Vérifier la compatibilité avec l'éclairage d'Angel
- S'assurer que les animations fonctionnent correctement

### Configuration Production

```properties
# Configuration optimisée pour production
avatar.ready-player-me.timeout=15000
avatar.ready-player-me.cache.enabled=true
avatar.ready-player-me.fallback-id=687f66fafe8107131699bf7b
```

## 9. Dépannage

### Problèmes Courants

**Avatar ne se charge pas :**
1. Vérifiez l'ID dans `config/avatar.properties`
2. Testez l'URL : `https://models.readyplayer.me/{id}.glb`
3. Vérifiez les logs : `logs/angel.log`
4. Testez la connectivité réseau

**Avatar déformé ou incorrect :**
1. Vérifiez la compatibilité avec Three.js
2. Testez avec un avatar de référence connu
3. Vérifiez la qualité : essayez `?quality=medium`

**Performance dégradée :**
1. Réduisez la qualité : `?quality=low`
2. Vérifiez les spécifications système
3. Testez avec l'avatar par défaut

### Debug

**Activer les logs détaillés :**
```properties
# Dans config/application-dev.properties
logging.level.com.angel.avatar=DEBUG
logging.level.org.springframework.web=DEBUG
```

**Test manuel d'un avatar :**
1. Ouvrir https://models.readyplayer.me/687f66fafe8107131699bf7b.glb
2. Vérifier que le modèle se télécharge (2-8 MB)
3. Tester dans un viewer GLB en ligne

### Diagnostic WebGL

```javascript
// Dans la console du navigateur (F12)
// Vérifier WebGL
const canvas = document.createElement('canvas');
const gl = canvas.getContext('webgl');
console.log('WebGL supporté:', !!gl);

// Vérifier Three.js
console.log('Three.js chargé:', typeof THREE !== 'undefined');
```

## 10. Ressources Supplémentaires

### Documentation Ready Player Me
- **API Documentation** : https://docs.readyplayer.me/
- **Hub d'avatars** : https://readyplayer.me/hub
- **Support technique** : https://docs.readyplayer.me/overview/frequently-asked-questions

### Documentation Technique
- **Three.js GLTFLoader** : https://threejs.org/docs/#examples/en/loaders/GLTFLoader
- **Spécifications GLB** : https://www.khronos.org/gltf/
- **WebGL Compatibility** : https://caniuse.com/webgl

### Outils de Test
- **GLB Viewer** : https://gltf-viewer.donmccurdy.com/
- **Three.js Editor** : https://threejs.org/editor/
- **WebGL Report** : https://webglreport.com/

---

Cette documentation couvre l'intégration Ready Player Me telle qu'implémentée dans Angel Virtual Assistant. Pour des fonctionnalités avancées non encore implémentées, consultez la documentation Ready Player Me officielle.
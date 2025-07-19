# Configuration Angel Virtual Assistant

Ce document décrit l'organisation et l'utilisation du système de configuration d'Angel Virtual Assistant.

## Structure de Configuration

### Fichiers de Configuration Principaux

```
config/
├── application.properties           # Configuration par défaut
├── application-test.properties      # Configuration pour mode test
├── application-prod.properties      # Configuration pour production (à créer)
└── test/                           # Configuration spécifique aux tests
    ├── scenarios.json              # Scénarios de test
    ├── activity-scenarios.json     # Scénarios d'activités
    └── test-mode-config.json       # Configuration mode test (JSON)
```

### Fichiers de Configuration Techniques

```
src/main/resources/config/
├── avatar.properties               # Configuration technique de l'avatar
└── phoneme-viseme-mapping.properties # Mappings phonèmes-visèmes
```

## Ordre de Priorité de Chargement

Le système charge les configurations dans cet ordre (du plus faible au plus fort) :

1. **Classpath** : `src/main/resources/config/*.properties` (priorité basse)
2. **Configuration externe de base** : `config/application.properties`
3. **Configuration profil externe** : `config/application-{profile}.properties` (priorité haute)
4. **Propriétés système** : `-Dproperty=value` (priorité maximale)

## Profils Supportés

### Default (par défaut)
- Configuration standard pour développement local
- Base de données H2 sur fichier
- Logs niveau INFO

### Test (`-p test`)
- Base de données H2 en mémoire
- Simulation accélérée
- Logs niveau DEBUG/TRACE
- Mode développement activé

### Prod (`-p prod`) - À configurer
- Configuration optimisée pour production
- Sécurité activée
- Logs optimisés

## Utilisation

### Lancement avec Profil

```bash
# Mode par défaut
./angel-launcher.sh start

# Mode test
./angel-launcher.sh start -p test

# Mode production (après création du fichier config)
./angel-launcher.sh start -p prod
```

### Variables d'Environnement

```bash
# Définir le profil via variable d'environnement
export SPRING_PROFILES_ACTIVE=test
./angel-launcher.sh start

# Ou via propriété système
./angel-launcher.sh start -Dspring.profiles.active=test
```

### Arguments du Programme

Le ConfigManager détecte automatiquement l'argument `-p` passé au programme :

```bash
java -jar angel.jar -p test
```

## Configuration des Propriétés

### Format

Toutes les configurations utilisent le format `properties` :

```properties
# Système
system.name=Angel Companion
system.language=fr
system.wake-word=Angel

# Base de données
database.url=jdbc:h2:file:./angel-db
database.driver=org.h2.Driver
database.username=angel
database.password=angel123

# API
api.angel-server-url=http://localhost:8080/api
api.polling-interval=30000
api.timeout=5000
```

### Propriétés Principales

| Catégorie | Préfixe | Description |
|-----------|---------|-------------|
| Système | `system.*` | Configuration générale |
| Base de données | `database.*` | Paramètres BDD |
| API | `api.*` | Configuration API externe |
| Avatar | `avatar.*` | Configuration avatar |
| Propositions | `proposals.*` | Moteur de propositions |
| Logging | `logging.*` | Configuration des logs |
| Test | `angel.test.*` | Mode test |
| Sécurité | `angel.security.*` | Paramètres sécurité |

## Migration depuis JSON

L'ancien fichier `config/angel-config.json` a été migré vers `config/application.properties`. 

### Correspondances

```json
// JSON (ancien)
{
  "system": {
    "name": "Angel Companion",
    "language": "fr"
  }
}
```

```properties
# Properties (nouveau)
system.name=Angel Companion
system.language=fr
```

## Dépannage

### Erreur de Chargement de Configuration

Si vous obtenez des erreurs de configuration au démarrage :

1. **Vérifiez l'existence des fichiers** :
   ```bash
   ls -la config/application*.properties
   ```

2. **Vérifiez le profil actif** :
   ```bash
   ./angel-launcher.sh status
   ```

3. **Vérifiez les logs de démarrage** pour voir quels fichiers sont chargés

### Mode Debug

Pour diagnostiquer les problèmes de configuration :

```bash
# Activer le mode verbose
./angel-launcher.sh start -p test -v

# Mode debug complet
./angel-launcher.sh start -p test -d -v
```

### Reconstruction de la Configuration

Si la configuration est corrompue :

```bash
# Nettoyer et recréer
./angel-launcher.sh clean
./angel-launcher.sh build
./angel-launcher.sh start -p test
```

## Exemple : Création d'un Profil Production

1. **Créer le fichier** `config/application-prod.properties` :

```properties
# Configuration Production Angel Virtual Assistant

# Application
system.name=Angel Companion Production
system.language=fr

# Base de données (PostgreSQL par exemple)
database.url=jdbc:postgresql://localhost:5432/angel_prod
database.driver=org.postgresql.Driver
database.username=angel_prod
database.password=${DB_PASSWORD}

# Sécurité activée
angel.security.enabled=true
angel.security.encryption.enabled=true

# Logging optimisé
logging.level.root=WARN
logging.level.com.angel=INFO
logging.file.name=logs/angel-prod.log

# Performance
angel.dev.debug-mode=false
angel.dev.mock-data=false
```

2. **Lancer en mode production** :

```bash
DB_PASSWORD=motdepasse ./angel-launcher.sh start -p prod -m 2g
```

## API de Configuration

Le ConfigManager expose une API pour accéder à la configuration :

```java
// Obtenir une propriété
String value = configManager.getString("system.name", "Default");

// Obtenir le profil actif
String profile = configManager.getActiveProfile();

// Statistiques de configuration
Map<String, Object> stats = configManager.getConfigStats();
```

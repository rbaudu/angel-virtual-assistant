# Configuration Angel Virtual Assistant

Guide complet de la configuration avec Spring Boot et organisation centralisée.

## Vue d'ensemble

Angel Virtual Assistant utilise maintenant une **configuration centralisée** dans le dossier `config/` avec Spring Boot. Cette approche permet :

- 🎯 **Configuration unique** : Tous les paramètres dans un seul endroit
- 🔧 **Profils Spring Boot** : Configuration différente par environnement
- ⚙️ **Hot reload** : Rechargement automatique en développement
- 🔒 **Sécurité** : Séparation des configurations sensibles

## Structure de Configuration

```
config/
├── application.properties          # Configuration principale (mode normal)
├── application-test.properties     # Configuration mode test
├── application-dev.properties      # Configuration développement (optionnel)
├── application-prod.properties     # Configuration production (optionnel)
└── test/
    ├── test-mode-config.json      # Configuration détaillée mode test
    └── activity-scenarios.json    # Scénarios d'activités
```

## Fichiers de Configuration

### `config/application.properties` (Mode Normal)

Configuration principale pour le mode normal de fonctionnement :

```properties
# ===============================================
# Configuration centrale Angel Virtual Assistant
# ===============================================

# Application
app.name=Angel Companion
app.version=1.0.0
app.description=Assistant virtuel intelligent pour surveillance d'activités

# Système
system.name=Angel Companion
system.version=1.0.0
system.language=fr
system.wake-word=Angel

# ===============================================
# Configuration Spring Boot
# ===============================================

# Application Spring Boot
spring.application.name=Angel Virtual Assistant
spring.main.allow-bean-definition-overriding=true

# Serveur (mode normal)
server.port=8080
server.servlet.context-path=/angel

# Configuration Web Spring
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html

# Configuration des ressources statiques
spring.web.resources.static-locations=classpath:/static/

# Configuration Templates (Thymeleaf)
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# ===============================================
# Configuration API Angel-server-capture
# ===============================================

# API externe
api.angel-server-url=http://localhost:8080/api
api.polling-interval=30000
api.timeout=5000

# ===============================================
# Configuration Base de données
# ===============================================

# Base de données H2
database.url=jdbc:h2:file:./angel-db
database.driver=org.h2.Driver
database.username=angel
database.password=angel123

# Spring JPA/Hibernate
spring.datasource.url=jdbc:h2:file:./angel-db
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=angel
spring.datasource.password=angel123
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# ===============================================
# Configuration Logging
# ===============================================

# Logging
logging.level=INFO
logging.file-path=./logs/angel.log
logging.rotation-size=10MB
logging.max-files=5

# Spring Logging
logging.level.root=INFO
logging.level.com.angel=DEBUG
logging.file.name=logs/angel.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# ===============================================
# Configuration Avatar
# ===============================================

# Avatar général
avatar.enabled=true
avatar.display-time=30000
avatar.transition-effect=fade
avatar.default-mood=neutral
avatar.type=3d_realistic
avatar.model=female_50_casual
avatar.voice-type=female_french_warm

# Apparence avatar
avatar.appearance.age=50
avatar.appearance.gender=female
avatar.appearance.style=casual_friendly

# Avatar Web
avatar.web.enabled=true
avatar.web.websocket.path=/ws/avatar
avatar.web.3d.quality=medium
avatar.web.voice.enabled=true

# ===============================================
# Configuration Propositions
# ===============================================

# Propositions quotidiennes - News
proposals.daily.news.max-per-day=5
proposals.daily.news.min-time-between=7200000
proposals.daily.news.sources=local,national,international
proposals.daily.news.preferred-categories=general,health,science

# Propositions quotidiennes - Météo
proposals.daily.weather.max-per-day=3
proposals.daily.weather.min-time-between=14400000
proposals.daily.weather.include-today=true
proposals.daily.weather.include-tomorrow=true

# Mapping des activités
proposals.activity-mapping.cleaning=recommendations,stories,media.music,media.radio
proposals.activity-mapping.eating=news,weather,reminders.medications,conversations
proposals.activity-mapping.waiting=news,weather,stories,conversations,games,media
proposals.activity-mapping.waking-up=weather,reminders,news

# ===============================================
# Configuration Mode Test
# ===============================================

# Mode test (désactivé par défaut en mode normal)
angel.test.enabled=false
angel.test.auto-start=false
angel.test.dashboard.enabled=true

# ===============================================
# Configuration Monitoring
# ===============================================

# Métriques et santé
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

### `config/application-test.properties` (Mode Test)

Configuration spécifique pour le mode test :

```properties
# ===============================================
# Configuration Angel Virtual Assistant - Mode Test
# ===============================================

# Application (mode test)
system.name=Angel Companion Test
system.version=1.0.0-TEST
system.language=fr
system.wake-word=Angel

# ===============================================
# Configuration Spring Boot Test
# ===============================================

# Application Spring Boot (test)
spring.application.name=Angel Virtual Assistant Test
spring.main.allow-bean-definition-overriding=true

# Serveur (port différent pour test)
server.port=8081
server.servlet.context-path=/

# Configuration Web Spring (test)
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html

# Configuration des ressources statiques (test)
spring.web.resources.static-locations=classpath:/static/

# Configuration Templates (test)
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# ===============================================
# Configuration Base de données Test
# ===============================================

# Base de données (test avec H2 en mémoire)
database.url=jdbc:h2:mem:angel-test-db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
database.driver=org.h2.Driver
database.username=angel_test
database.password=test123

# Spring JPA/Hibernate (test)
spring.datasource.url=jdbc:h2:mem:angel-test-db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=angel_test
spring.datasource.password=test123
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# ===============================================
# Configuration Mode Test spécifique
# ===============================================

# Mode test activé
angel.test.enabled=true
angel.test.auto-start=true
angel.test.dashboard.enabled=true
angel.test.config-file=config/test/test-mode-config.json
angel.test.scenarios-file=config/test/scenarios.json

# Simulation accélérée pour tests
angel.test.simulation.interval=5000
angel.test.simulation.randomness=0.5
angel.test.simulation.speed-multiplier=5.0
angel.test.simulation.noise-enabled=true

# Dashboard test
angel.test.dashboard.refresh-interval=5000
angel.test.dashboard.max-log-entries=1000
angel.test.dashboard.stats.enabled=true

# ===============================================
# Configuration API Test
# ===============================================

# API externe (mock en mode test)
api.angel-server-url=http://localhost:8082/api
api.polling-interval=10000
api.timeout=2000

# ===============================================
# Configuration Avatar Test
# ===============================================

# Avatar (simplifié en test)
avatar.enabled=true
avatar.display-time=5000
avatar.transition-effect=none
avatar.default-mood=neutral

# Avatar Web (test)
avatar.web.enabled=true
avatar.web.websocket.path=/ws/avatar
avatar.web.3d.quality=low
avatar.web.voice.enabled=false

# ===============================================
# Configuration Propositions Test
# ===============================================

# Propositions (accélérées pour test)
proposals.daily.news.max-per-day=10
proposals.daily.news.min-time-between=60000
proposals.daily.weather.max-per-day=5
proposals.daily.weather.min-time-between=120000

# ===============================================
# Configuration Logging Test
# ===============================================

# Logging (plus verbeux en test)
logging.level.root=DEBUG
logging.level.com.angel=TRACE
logging.file.name=logs/angel-test.log
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# ===============================================
# Configuration développement/debug (mode test)
# ===============================================

# Mode développement activé
angel.dev.debug-mode=true
angel.dev.mock-data=true
angel.dev.performance-monitoring=true

# Sécurité désactivée en test
angel.security.enabled=false

# Monitoring simplifié en test
management.endpoints.web.exposure.include=health,info
```

## Configuration par Profil

### Activation des Profils

```bash
# Profil par défaut (application.properties)
./angel-launcher.sh start

# Profil test (application-test.properties)
./angel-launcher.sh start -p test

# Profil développement (application-dev.properties)
./angel-launcher.sh start -p dev

# Profil production (application-prod.properties)
./angel-launcher.sh start -p prod
```

### Hiérarchie de Configuration

Spring Boot charge les fichiers dans cet ordre (le dernier écrase le précédent) :

1. `config/application.properties` (base)
2. `config/application-{profile}.properties` (profil spécifique)
3. Variables d'environnement
4. Arguments de ligne de commande

### Exemple avec Profil Dev

Créer `config/application-dev.properties` :

```properties
# Configuration développement

# Port développement
server.port=8090

# Hot reload activé
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true

# Cache désactivé
spring.thymeleaf.cache=false
spring.web.resources.cache.period=0

# Logs détaillés
logging.level.com.angel=DEBUG
logging.level.org.springframework=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# Base de données développement
spring.datasource.url=jdbc:h2:file:./angel-dev-db
spring.jpa.show-sql=true

# Mode développement
angel.dev.debug-mode=true
angel.dev.mock-data=false
angel.dev.performance-monitoring=true
```

## Variables d'Environnement

### Variables Support

```bash
# Port du serveur
export SERVER_PORT=8080

# Base de données
export DB_URL=jdbc:h2:file:./angel-db
export DB_USERNAME=angel
export DB_PASSWORD=secret123

# API externe
export ANGEL_SERVER_URL=http://production-server:8080/api

# Profil actif
export SPRING_PROFILES_ACTIVE=prod

# Démarrer avec les variables
./angel-launcher.sh start
```

### Fichier `.env` (optionnel)

Créer un fichier `.env` pour les variables locales :

```bash
# Variables d'environnement Angel
SERVER_PORT=8080
DB_PASSWORD=mySecretPassword
ANGEL_SERVER_URL=http://localhost:8080/api
LOG_LEVEL=INFO
```

Charger avant démarrage :

```bash
source .env
./angel-launcher.sh start
```

## Configuration Avancée

### SSL/TLS (Production)

Ajouter dans `config/application-prod.properties` :

```properties
# Configuration HTTPS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=angel

# Redirection HTTP vers HTTPS
server.ssl.require-ssl=true
```

### Base de Données Externe

Pour utiliser PostgreSQL en production :

```properties
# Configuration PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/angel_db
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Configuration JPA pour PostgreSQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

### Cache Redis

Configuration pour cache distribué :

```properties
# Configuration Redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=${REDIS_PASSWORD}
spring.redis.timeout=2000ms

# Cache propositions
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000
```

### Monitoring Prometheus

```properties
# Métriques Prometheus
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true

# Métriques personnalisées
management.metrics.tags.application=angel-virtual-assistant
management.metrics.tags.environment=${SPRING_PROFILES_ACTIVE}
```

## Configuration de Sécurité

### Authentification Basique

```properties
# Sécurité Spring Boot
spring.security.user.name=admin
spring.security.user.password=${ADMIN_PASSWORD}
spring.security.user.roles=ADMIN

# Configuration CORS
angel.web.cors.allowed-origins=http://localhost:3000,https://angel.example.com
angel.web.cors.allowed-methods=GET,POST,PUT,DELETE
angel.web.cors.allow-credentials=true
```

### JWT (à venir)

```properties
# Configuration JWT
angel.security.jwt.secret=${JWT_SECRET}
angel.security.jwt.expiration=86400000
angel.security.jwt.refresh-expiration=604800000
```

## Configuration du Mode Test

### `config/test/test-mode-config.json`

Configuration détaillée du mode test :

```json
{
  "simulation": {
    "enabled": true,
    "interval": 30000,
    "randomness": 0.3,
    "speedMultiplier": 1.0,
    "noiseEnabled": true,
    "scenarios": {
      "autoLoad": true,
      "defaultScenario": "normal_day",
      "scenariosPath": "config/test/activity-scenarios.json"
    }
  },
  "dashboard": {
    "enabled": true,
    "refreshInterval": 5000,
    "maxLogEntries": 1000,
    "autoStart": true,
    "features": {
      "statistics": true,
      "realTimeUpdates": true,
      "scenarioManager": true,
      "activityControl": true
    }
  },
  "api": {
    "enabled": true,
    "basePath": "/api/test",
    "authentication": false,
    "rateLimit": {
      "enabled": false,
      "requests": 100,
      "window": 60000
    }
  }
}
```

### `config/test/activity-scenarios.json`

Scénarios d'activités pour les tests :

```json
{
  "scenarios": {
    "morning_routine": {
      "name": "Routine Matinale",
      "description": "Séquence typique du matin",
      "activities": [
        {
          "activity": "WAKING_UP",
          "duration": 300000,
          "confidence": 0.9,
          "description": "Se réveiller"
        },
        {
          "activity": "WASHING",
          "duration": 600000,
          "confidence": 0.85,
          "description": "Se laver"
        },
        {
          "activity": "EATING",
          "duration": 900000,
          "confidence": 0.8,
          "description": "Petit déjeuner"
        }
      ]
    },
    "evening_routine": {
      "name": "Routine du Soir",
      "description": "Séquence typique du soir",
      "activities": [
        {
          "activity": "COOKING",
          "duration": 1800000,
          "confidence": 0.85,
          "description": "Préparer le dîner"
        },
        {
          "activity": "EATING",
          "duration": 1200000,
          "confidence": 0.9,
          "description": "Dîner"
        },
        {
          "activity": "WATCHING_TV",
          "duration": 3600000,
          "confidence": 0.8,
          "description": "Regarder la télévision"
        },
        {
          "activity": "GOING_TO_SLEEP",
          "duration": 600000,
          "confidence": 0.95,
          "description": "Se coucher"
        }
      ]
    }
  }
}
```

## Validation de Configuration

### Script de Validation

Créer `scripts/validate-config.sh` :

```bash
#!/bin/bash

echo "Validation de la configuration Angel Virtual Assistant"

# Vérifier les fichiers de configuration
echo "Vérification des fichiers de configuration..."

CONFIG_FILES=(
    "config/application.properties"
    "config/application-test.properties"
    "config/test/test-mode-config.json"
    "config/test/activity-scenarios.json"
)

for file in "${CONFIG_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        echo "✅ $file"
    else
        echo "❌ $file (manquant)"
    fi
done

# Vérifier la syntaxe JSON
echo "Vérification de la syntaxe JSON..."

JSON_FILES=(
    "config/test/test-mode-config.json"
    "config/test/activity-scenarios.json"
)

for file in "${JSON_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        if jq empty "$file" 2>/dev/null; then
            echo "✅ $file (JSON valide)"
        else
            echo "❌ $file (JSON invalide)"
        fi
    fi
done

# Vérifier les propriétés obligatoires
echo "Vérification des propriétés obligatoires..."

REQUIRED_PROPS=(
    "spring.application.name"
    "server.port"
    "spring.datasource.url"
    "avatar.enabled"
)

for prop in "${REQUIRED_PROPS[@]}"; do
    if grep -q "^$prop=" config/application.properties; then
        echo "✅ $prop"
    else
        echo "❌ $prop (manquant)"
    fi
done

echo "Validation terminée."
```

### Commandes de Validation

```bash
# Rendre le script exécutable
chmod +x scripts/validate-config.sh

# Exécuter la validation
./scripts/validate-config.sh

# Validation avec Spring Boot
./angel-launcher.sh start --dry-run

# Test de la configuration
./angel-launcher.sh test-config
```

## Dépannage Configuration

### Problèmes Courants

#### 1. **Configuration non trouvée**

```
Error: Could not resolve placeholder 'server.port'
```

**Solution** : Vérifier que les fichiers de configuration sont dans `config/` et que le script de lancement configure correctement `spring.config.location`.

#### 2. **Profil non reconnu**

```
Warning: No configuration found for profile 'myprofile'
```

**Solution** : Créer `config/application-myprofile.properties` ou vérifier l'orthographe du profil.

#### 3. **Port déjà utilisé**

```
Error: Port 8080 is already in use
```

**Solution** : Changer le port dans la configuration ou utiliser une variable d'environnement :

```bash
export SERVER_PORT=8090
./angel-launcher.sh start
```

#### 4. **Base de données inaccessible**

```
Error: Could not create connection to database
```

**Solution** : Vérifier les paramètres de base de données dans la configuration.

### Logs de Configuration

```bash
# Voir la configuration chargée
./angel-launcher.sh start -v | grep "spring.config"

# Debug de la configuration
./angel-launcher.sh start --debug

# Logs spécifiques à la configuration
tail -f logs/angel.log | grep -i "config\|property"
```

## Migration de Configuration

### Depuis l'Ancien Format JSON

Si vous avez un ancien fichier `angel-config.json`, voici comment migrer :

```bash
# Script de migration (exemple)
#!/bin/bash

OLD_CONFIG="config/angel-config.json"
NEW_CONFIG="config/application.properties"

if [[ -f "$OLD_CONFIG" ]]; then
    echo "Migration de $OLD_CONFIG vers $NEW_CONFIG"
    
    # Extraire les valeurs avec jq et convertir
    echo "# Configuration migrée depuis $OLD_CONFIG" >> "$NEW_CONFIG"
    echo "server.port=$(jq -r '.server.port // 8080' "$OLD_CONFIG")" >> "$NEW_CONFIG"
    echo "avatar.enabled=$(jq -r '.avatar.enabled // true' "$OLD_CONFIG")" >> "$NEW_CONFIG"
    
    echo "Migration terminée. Vérifiez $NEW_CONFIG"
fi
```

## Conclusion

La configuration d'Angel Virtual Assistant est maintenant centralisée et flexible grâce à Spring Boot. Les points clés :

1. **Configuration centralisée** dans le dossier `config/`
2. **Profils Spring Boot** pour différents environnements
3. **Variables d'environnement** pour les valeurs sensibles
4. **Validation automatique** des propriétés
5. **Hot reload** en développement

Cette approche permet une gestion propre et professionnelle de la configuration tout en préservant la flexibilité nécessaire pour différents environnements d'exécution.

Pour plus d'informations :
- [README.md](../README.md) : Documentation principale
- [WEB_INTERFACE.md](WEB_INTERFACE.md) : Guide de l'interface web
- [SPRING_BOOT_MIGRATION.md](SPRING_BOOT_MIGRATION.md) : Guide de migration
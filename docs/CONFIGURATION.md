# Configuration - Angel Virtual Assistant

Guide de la configuration centralisée avec Spring Boot et profils d'environnement.

## 🎯 Vue d'Ensemble

Angel Virtual Assistant utilise une **configuration centralisée** dans le dossier `config/` avec Spring Boot :

- **Configuration unique** : Tous les paramètres dans `config/`
- **Profils Spring Boot** : Configuration par environnement
- **Variables d'environnement** : Valeurs sensibles externalisées
- **Hot reload** : Rechargement automatique en développement
- **Validation** : Vérification automatique des propriétés

## 📁 Structure de Configuration

```
config/
├── application.properties              # Configuration normale (port 8080)
├── application-test.properties         # Configuration test (port 8081)
├── application-dev.properties          # Configuration développement
├── application-prod.properties         # Configuration production
└── avatar.properties                   # Configuration avatar par défaut
```

## ⚙️ Configuration Principale

### `config/application.properties` (Mode Normal)

Configuration pour le mode normal avec Angel-server-capture :

```properties
# ===============================================
# Angel Virtual Assistant - Configuration Normale
# ===============================================

# Application
spring.application.name=Angel Virtual Assistant
app.name=Angel Companion
app.version=1.1.0
app.description=Assistant virtuel avec avatar 3D et reconnaissance vocale

# Serveur principal (mode normal)
server.port=8080
server.servlet.context-path=/angel

# ===============================================
# Configuration Base de Données
# ===============================================

# Base de données H2 persistante
spring.datasource.url=jdbc:h2:file:./data/angel-db
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=angel
spring.datasource.password=${DB_PASSWORD:angel123}
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# ===============================================
# Configuration Web
# ===============================================

# Templates Thymeleaf
spring.thymeleaf.cache=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Ressources statiques
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.cache.cachecontrol.max-age=3600

# ===============================================
# Configuration API Angel-server-capture
# ===============================================

# API externe pour capture d'activités
api.angel-server.url=${ANGEL_SERVER_URL:http://localhost:8080/api}
api.angel-server.polling-interval=30000
api.angel-server.timeout=5000
api.angel-server.enabled=true

# ===============================================
# Configuration Avatar
# ===============================================

# Avatar général
avatar.enabled=true
avatar.model.ready-player-me.default-id=687f66fafe8107131699bf7b
avatar.web.enabled=true
avatar.web.websocket.path=/ws/avatar
avatar.web.quality=medium

# ===============================================
# Configuration Reconnaissance Vocale
# ===============================================

# Wake word "Angel"
voice.wake-word.enabled=true
voice.wake-word.words=angel,angèle,ange
voice.wake-word.threshold=0.7

# Synthèse vocale
voice.speech.synthesis.voice=Microsoft Hortense - French (France) (fr-FR)
voice.speech.synthesis.rate=1.0
voice.speech.synthesis.volume=0.8

# ===============================================
# Configuration Propositions
# ===============================================

# Propositions quotidiennes
proposals.enabled=true
proposals.daily.news.max-per-day=5
proposals.daily.weather.max-per-day=3
proposals.activity-mapping.enabled=true

# ===============================================
# Configuration Logging
# ===============================================

# Logs application
logging.level.root=INFO
logging.level.com.angel=DEBUG
logging.file.name=logs/angel.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.file.max-size=10MB
logging.file.max-history=5

# ===============================================
# Configuration Monitoring
# ===============================================

# Actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=false

# Mode test désactivé en normal
angel.test.enabled=false
```

### `config/application-test.properties` (Mode Test)

Configuration pour le mode test autonome :

```properties
# ===============================================
# Angel Virtual Assistant - Configuration Test
# ===============================================

# Application test
spring.application.name=Angel Virtual Assistant Test
app.name=Angel Companion Test
app.version=1.1.0-TEST

# Serveur test (port différent)
server.port=8081
server.servlet.context-path=/

# ===============================================
# Configuration Base de Données Test
# ===============================================

# Base de données H2 en mémoire pour tests
spring.datasource.url=jdbc:h2:mem:angel-test;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=angel_test
spring.datasource.password=test123
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# ===============================================
# Configuration Web Test
# ===============================================

# Templates sans cache en test
spring.thymeleaf.cache=false
spring.web.resources.cache.period=0

# ===============================================
# Configuration Mode Test
# ===============================================

# Mode test activé
angel.test.enabled=true
angel.test.auto-start=true
angel.test.dashboard.enabled=true

# Simulation accélérée
angel.test.simulation.interval=5000
angel.test.simulation.randomness=0.5
angel.test.simulation.speed-multiplier=5.0

# Dashboard test
angel.test.dashboard.refresh-interval=2000
angel.test.dashboard.max-log-entries=500
angel.test.dashboard.auto-clear-logs=true

# ===============================================
# Configuration API Test (Mock)
# ===============================================

# API mock pour tests
api.angel-server.enabled=false
api.angel-server.mock.enabled=true
api.angel-server.url=http://localhost:8082/api/mock

# ===============================================
# Configuration Avatar Test
# ===============================================

# Avatar simplifié en test
avatar.enabled=true
avatar.web.quality=low
avatar.web.shadows=false
avatar.web.antialiasing=false

# ===============================================
# Configuration Reconnaissance Vocale Test
# ===============================================

# Wake word avec seuil réduit pour tests
voice.wake-word.threshold=0.5
voice.wake-word.fallback-mode=true

# Synthèse vocale désactivée par défaut en test
voice.speech.synthesis.enabled=false
voice.speech.synthesis.volume=0.5

# ===============================================
# Configuration Logging Test
# ===============================================

# Logs détaillés en test
logging.level.root=DEBUG
logging.level.com.angel=TRACE
logging.level.org.springframework=DEBUG
logging.file.name=logs/angel-test.log
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n


# ===============================================
# Configuration Développement
# ===============================================

# Mode développement activé
angel.dev.debug-mode=true
angel.dev.mock-data=true
angel.dev.performance-monitoring=true

# Sécurité désactivée en test
spring.security.enabled=false
```

## 🔧 Profils et Environnements

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

# Profils multiples
./angel-launcher.sh start -p "test,dev"
```

### Profil Développement (`application-dev.properties`)

```properties
# Configuration développement

# Port dédié développement
server.port=8090

# Hot reload activé
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
spring.devtools.restart.additional-paths=config/

# Cache désactivé
spring.thymeleaf.cache=false
spring.web.resources.cache.period=0

# Base de données développement
spring.datasource.url=jdbc:h2:file:./data/angel-dev-db
spring.jpa.show-sql=true

# Logs détaillés
logging.level.com.angel=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Debug mode
angel.dev.debug-mode=true
angel.dev.performance-monitoring=true
angel.dev.mock-external-apis=true

# Avatar qualité réduite pour développement
avatar.web.quality=medium
avatar.web.fps-target=30
```

### Profil Production (`application-prod.properties`)

```properties
# Configuration production

# Port standard production
server.port=8080
server.servlet.context-path=/angel

# SSL/HTTPS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12

# Base de données production
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Cache activé
spring.thymeleaf.cache=true
spring.web.resources.cache.cachecontrol.max-age=31536000
spring.web.resources.chain.strategy.content.enabled=true

# Logs production
logging.level.root=WARN
logging.level.com.angel=INFO
logging.file.name=logs/angel-prod.log

# Sécurité renforcée
spring.security.enabled=true
angel.web.security.api-key-required=true
angel.web.cors.allowed-origins=${ALLOWED_ORIGINS}

# Monitoring complet
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true

# Avatar qualité maximale
avatar.web.quality=high
avatar.web.shadows=true
avatar.web.antialiasing=true
avatar.web.fps-target=60
```

## 🌍 Variables d'Environnement

### Variables Principales

```bash
# Variables de base
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod

# Base de données
export DATABASE_URL=jdbc:postgresql://localhost:5432/angel_prod
export DATABASE_USERNAME=angel_user
export DATABASE_PASSWORD=secure_password_here
export DB_PASSWORD=local_dev_password

# API externe
export ANGEL_SERVER_URL=http://production-server:8080/api

# Sécurité
export SSL_KEYSTORE_PASSWORD=keystore_password
export ALLOWED_ORIGINS=https://angel.mycompany.com,https://dashboard.mycompany.com

# Monitoring
export PROMETHEUS_ENABLED=true

# Avatar
export READY_PLAYER_ME_API_KEY=your_api_key_here

# Démarrage avec variables
./angel-launcher.sh start
```

### Fichier `.env` Local

Créer `.env` pour le développement local :

```bash
# .env - Variables locales (ne pas commiter)
SERVER_PORT=8090
DB_PASSWORD=dev_password_123
ANGEL_SERVER_URL=http://localhost:8080/api
SPRING_PROFILES_ACTIVE=dev
DEBUG_MODE=true
LOG_LEVEL=DEBUG
```

Charger avant démarrage :

```bash
# Charger les variables et démarrer
source .env
./angel-launcher.sh start

# Ou directement
./angel-launcher.sh start --env-file .env
```

## 🎯 Configuration Avatar

### `config/avatar.properties`

Configuration dédiée avatar (chargée par défaut) :

```properties
# ===============================================
# Configuration Avatar par Défaut
# ===============================================

# Ready Player Me
avatar.ready-player-me.enabled=true
avatar.ready-player-me.default-id=687f66fafe8107131699bf7b
avatar.ready-player-me.base-url=https://models.readyplayer.me
avatar.ready-player-me.timeout=30000

# Rendu 3D
avatar.rendering.quality=medium
avatar.rendering.antialiasing=true
avatar.rendering.shadows=true
avatar.rendering.pixel-ratio=auto
avatar.rendering.fps-target=60

# Animations
avatar.animations.speaking.enabled=true
avatar.animations.speaking.intensity=0.7
avatar.animations.emotions.enabled=true
avatar.animations.emotions.transitions=true
avatar.animations.idle.enabled=true
avatar.animations.idle.variations=3

# Interface Web
avatar.web.controls.mute-button=true
avatar.web.controls.settings-button=true
avatar.web.controls.fullscreen-button=true
avatar.web.controls.auto-hide=true
avatar.web.controls.auto-hide-delay=5000

# Mode sombre
avatar.dark-mode.enabled=true
avatar.dark-mode.trigger-delay=300000
avatar.dark-mode.show-clock=true
```

## 🔍 Validation de Configuration

### Script de Validation

Créer `scripts/validate-config.sh` :

```bash
#!/bin/bash

echo "🔍 Validation de la configuration Angel Virtual Assistant"
echo

# Vérifier les fichiers de configuration
echo "📁 Vérification des fichiers..."
CONFIG_FILES=(
    "config/application.properties"
    "config/application-test.properties"
    "config/avatar.properties"
)

for file in "${CONFIG_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        echo "✅ $file"
    else
        echo "❌ $file (manquant)"
    fi
done
echo

# Vérifier les propriétés obligatoires
echo "🔑 Vérification des propriétés obligatoires..."
REQUIRED_PROPS=(
    "spring.application.name"
    "server.port"
    "spring.datasource.url"
    "avatar.enabled"
    "voice.wake-word.enabled"
)

for prop in "${REQUIRED_PROPS[@]}"; do
    if grep -q "^$prop=" config/application.properties; then
        value=$(grep "^$prop=" config/application.properties | cut -d'=' -f2-)
        echo "✅ $prop = $value"
    else
        echo "❌ $prop (manquant)"
    fi
done
echo

# Vérifier les ports
echo "🌐 Vérification des ports..."
NORMAL_PORT=$(grep "^server.port=" config/application.properties | cut -d'=' -f2)
TEST_PORT=$(grep "^server.port=" config/application-test.properties | cut -d'=' -f2)

if [[ "$NORMAL_PORT" != "$TEST_PORT" ]]; then
    echo "✅ Ports différents: Normal($NORMAL_PORT) ≠ Test($TEST_PORT)"
else
    echo "⚠️  Ports identiques: Normal($NORMAL_PORT) = Test($TEST_PORT)"
fi
echo

# Vérifier les variables d'environnement requises
echo "🌍 Variables d'environnement optionnelles..."
OPTIONAL_VARS=(
    "DATABASE_URL"
    "ANGEL_SERVER_URL"
    "SSL_KEYSTORE_PASSWORD"
)

for var in "${OPTIONAL_VARS[@]}"; do
    if [[ -n "${!var}" ]]; then
        echo "✅ $var définie"
    else
        echo "ℹ️  $var non définie (utilise la valeur par défaut)"
    fi
done
echo

echo "✅ Validation terminée."
```

### Commandes de Validation

```bash
# Rendre le script exécutable
chmod +x scripts/validate-config.sh

# Exécuter la validation
./scripts/validate-config.sh

# Validation avec Spring Boot (dry-run)
./angel-launcher.sh start --dry-run

# Test de la configuration test
./angel-launcher.sh start -p test --validate-only

# Afficher la configuration effective
./angel-launcher.sh show-config
```

## 🔄 Hiérarchie de Configuration

Spring Boot charge les configurations dans cet ordre (le dernier écrase le précédent) :

1. **`config/application.properties`** (base)
2. **`config/application-{profile}.properties`** (profil)
3. **Variables d'environnement** (`DATABASE_URL`, etc.)
4. **Arguments ligne de commande** (`--server.port=8090`)
5. **Configuration Java** (`@ConfigurationProperties`)

### Exemple Complet

```bash
# 1. Base: server.port=8080 (application.properties)
# 2. Profil: server.port=8081 (application-test.properties)
# 3. Variable: SERVER_PORT=8090
# 4. Argument: --server.port=8095
# → Résultat final: 8095

export SERVER_PORT=8090
./angel-launcher.sh start -p test --server.port=8095
```

## 🚀 Configuration Avancée

### Base de Données Externe (PostgreSQL)

```properties
# Configuration PostgreSQL production
spring.datasource.url=jdbc:postgresql://localhost:5432/angel_db
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USERNAME:angel}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Configuration JPA pour PostgreSQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.jdbc.batch_size=50
```

### Cache Redis

```properties
# Configuration Redis pour cache distribué
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.password=${REDIS_PASSWORD}
spring.redis.timeout=2000ms
spring.redis.jedis.pool.max-active=20
spring.redis.jedis.pool.max-idle=10

# Cache propositions
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000
spring.cache.cache-names=proposals,activities,avatar-states
```

### Monitoring Avancé

```properties
# Prometheus et métriques
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
management.metrics.export.prometheus.step=30s

# Tags personnalisés
management.metrics.tags.application=angel-virtual-assistant
management.metrics.tags.environment=${SPRING_PROFILES_ACTIVE:default}
management.metrics.tags.instance=${HOSTNAME:unknown}

# Métriques personnalisées
angel.metrics.proposals.enabled=true
angel.metrics.avatar.performance.enabled=true
angel.metrics.voice.recognition.enabled=true
```

## 🔍 Dépannage Configuration

### Problèmes Courants

#### Configuration non trouvée
```bash
# Erreur: Could not resolve placeholder 'server.port'
# Solution: Vérifier spring.config.location
echo "Configuration location: $(java -jar target/*.jar --spring.config.location=config/)" 
```

#### Profil non reconnu
```bash
# Erreur: No configuration found for profile 'myprofile'
# Solution: Créer config/application-myprofile.properties
touch config/application-myprofile.properties
```

#### Port déjà utilisé
```bash
# Erreur: Port 8080 is already in use
# Solution 1: Changer le port
export SERVER_PORT=8090
./angel-launcher.sh start

# Solution 2: Arrêter le processus existant
lsof -ti:8080 | xargs kill -9
```

#### Variables d'environnement
```bash
# Debug des variables
printenv | grep -E "(SERVER|DATABASE|ANGEL)" | sort

# Test avec variables temporaires
SERVER_PORT=8090 ./angel-launcher.sh start -p dev
```

### Logs de Configuration

```bash
# Voir la configuration chargée au démarrage
./angel-launcher.sh start --debug | grep -E "(spring.config|PropertySource)"

# Logs spécifiques configuration
tail -f logs/angel.log | grep -i "config\|property\|profile"

# Debug complet configuration Spring
./angel-launcher.sh start --debug --logging.level.org.springframework.boot.context.config=TRACE
```

## 📊 Configuration par Composant

### Reconnaissance Vocale
```properties
# Wake word detection
voice.wake-word.enabled=true
voice.wake-word.words=angel,angèle,ange
voice.wake-word.threshold=0.7
voice.wake-word.timeout=5000
voice.wake-word.fallback-mode=true

# Speech synthesis
voice.speech.synthesis.voice=Microsoft Hortense - French (France) (fr-FR)
voice.speech.synthesis.rate=1.0
voice.speech.synthesis.pitch=1.0
voice.speech.synthesis.volume=0.8
voice.speech.synthesis.queue-size=10

# Speech recognition
voice.speech.recognition.language=fr-FR
voice.speech.recognition.continuous=true
voice.speech.recognition.interim-results=true
```

### Propositions d'Activités
```properties
# Propositions générales
proposals.enabled=true
proposals.max-per-day=20
proposals.min-interval-between=1800000
proposals.priority-threshold=0.7

# Propositions par type
proposals.daily.news.enabled=true
proposals.daily.news.max-per-day=5
proposals.daily.weather.enabled=true
proposals.daily.weather.max-per-day=3
proposals.reminders.enabled=true
proposals.conversations.enabled=true

# Mapping activités → propositions
proposals.activity-mapping.cleaning=recommendations,stories,music
proposals.activity-mapping.eating=news,weather,reminders,conversations
proposals.activity-mapping.waiting=news,weather,stories,games,media
```

### Mode Test Avancé
```properties
# Test simulation
angel.test.simulation.enabled=true
angel.test.simulation.interval=30000
angel.test.simulation.randomness=0.3
angel.test.simulation.speed-multiplier=1.0
angel.test.simulation.noise-enabled=false

# Test scenarios
angel.test.scenarios.enabled=true
angel.test.scenarios.auto-load=true
angel.test.scenarios.default=normal_day
angel.test.scenarios.path=config/test/scenarios/

# Test dashboard
angel.test.dashboard.enabled=true
angel.test.dashboard.refresh-interval=5000
angel.test.dashboard.max-log-entries=1000
angel.test.dashboard.auto-start=false
```

---

La configuration d'Angel Virtual Assistant est maintenant centralisée, flexible et adaptée à tous les environnements grâce aux profils Spring Boot et à la gestion des variables d'environnement.
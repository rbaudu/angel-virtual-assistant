# Mode Test - Angel Virtual Assistant

Ce document décrit le mode test implémenté pour Angel Virtual Assistant, qui permet de simuler des activités sans dépendre du serveur dl4j-server-capture.

## Vue d'ensemble

Le mode test fournit un environnement de simulation complet qui :
- **Simule des activités** en continu selon différents patterns
- **Remplace le serveur dl4j-server-capture** pour les tests et le développement
- **Offre une interface web** pour contrôler la simulation
- **Supporte des scénarios** prédéfinis d'activités
- **Génère des données réalistes** avec variations temporelles

## Architecture

### Composants principaux

```
com.angel.test/
├── ActivitySimulator.java          # Simulateur d'activités
├── ScenarioManager.java             # Gestionnaire de scénarios
├── TestDataGenerator.java           # Générateur de données
└── TestModeService.java             # Service principal

com.angel.api/
├── TestActivityClient.java          # Client de test (remplace AngelServerClient)
└── TestModeController.java          # API REST pour le contrôle

com.angel.config/
├── TestModeConfig.java              # Configuration du mode test
└── TestModeConfiguration.java       # Configuration Spring

com.angel.ui/
└── TestDashboardController.java     # Contrôleur web du dashboard

resources/
├── static/
│   ├── css/test-mode.css           # Styles du dashboard
│   └── js/test-control.js          # Logique JavaScript
└── templates/
    └── test-dashboard.html         # Interface web

config/test/
├── test-mode-config.json           # Configuration du mode test
└── activity-scenarios.json         # Scénarios d'activités
```

### Flux de données

```
[Application] ←→ [TestModeService] ←→ [ActivitySimulator]
                        ↓
               [ScenarioManager] ←→ [TestDataGenerator]
                        ↓
               [TestActivityClient] ←→ [TestModeController]
                        ↓
                [Dashboard Web]
```

## Configuration

### Activation du mode test

#### Option 1: Via la configuration principale

```json
// config/angel-config.json
{
  "system": {
    "mode": "test",
    "testMode": {
      "enabled": true,
      "configFile": "config/test/test-mode-config.json"
    }
  }
}
```

#### Option 2: Via les propriétés système

```bash
# Démarrage avec mode test
java -Dangel.test.enabled=true -jar angel-virtual-assistant.jar

# Ou via script
./angel.sh start -p test
```

#### Option 3: Variable d'environnement

```bash
export ANGEL_TEST_ENABLED=true
java -jar angel-virtual-assistant.jar
```

### Configuration détaillée

#### config/test/test-mode-config.json

```json
{
  "testMode": {
    "enabled": true,
    "autoStart": true,
    "simulation": {
      "interval": 30000,        // Intervalle entre activités (ms)
      "randomness": 0.2,        // Facteur d'aléa (0-1)
      "scenarioFile": "config/test/activity-scenarios.json"
    },
    "activities": {
      "sequence": "random",    // random, sequential, scenario, scheduled
      "duration": {
        "min": 60000,          // Durée minimum (ms)
        "max": 300000          // Durée maximum (ms)
      },
      "transitions": {
        "gradual": true,       // Transitions graduelles
        "confidence": {
          "min": 0.6,
          "max": 0.95
        }
      }
    },
    "schedule": {
      "followDailyPattern": true,
      "patterns": {
        "morning": ["WAKING_UP", "WASHING", "EATING", "READING"],
        "afternoon": ["COOKING", "EATING", "WATCHING_TV", "READING"],
        "evening": ["EATING", "WATCHING_TV", "READING", "GOING_TO_SLEEP"]
      }
    },
    "web": {
      "dashboardEnabled": true,
      "dashboardPort": 8081,
      "dashboardPath": "/test-dashboard"
    },
    "logging": {
      "enabled": true,
      "level": "DEBUG",
      "logActivities": true,
      "logTransitions": true
    }
  }
}
```

## Modes de simulation

### 1. Mode Aléatoire (random)

```json
{
  "activities": {
    "sequence": "random"
  }
}
```

- Sélectionne des activités au hasard
- Durées variables selon l'activité
- Confiance réaliste selon le type d'activité

### 2. Mode Séquentiel (sequential)

```json
{
  "activities": {
    "sequence": "sequential"
  }
}
```

- Parcourt toutes les activités dans l'ordre
- Utile pour tester toutes les activités
- Durées et confiances variables

### 3. Mode Programmé (scheduled)

```json
{
  "activities": {
    "sequence": "scheduled"
  },
  "schedule": {
    "followDailyPattern": true,
    "patterns": {
      "morning": ["WAKING_UP", "WASHING", "EATING"],
      "afternoon": ["COOKING", "EATING", "WATCHING_TV"],
      "evening": ["WATCHING_TV", "READING", "GOING_TO_SLEEP"]
    }
  }
}
```

- Suit des patterns d'activités selon l'heure
- Plus réaliste pour les tests d'intégration
- Adapte les activités au moment de la journée

### 4. Mode Scénario (scenario)

```json
{
  "activities": {
    "sequence": "scenario"
  }
}
```

- Utilise des scénarios prédéfinis
- Séquences d'activités avec durées et descriptions
- Idéal pour tester des cas d'usage spécifiques

## Scénarios prédéfinis

### config/test/activity-scenarios.json

```json
{
  "scenarios": {
    "morning_routine": {
      "name": "Routine matinale",
      "description": "Séquence typique d'activités du matin",
      "activities": [
        {
          "activity": "WAKING_UP",
          "duration": 120000,
          "confidence": 0.9,
          "description": "Se réveiller"
        },
        {
          "activity": "WASHING",
          "duration": 180000,
          "confidence": 0.85,
          "description": "Se laver, aller aux toilettes"
        }
      ]
    },
    "evening_routine": {
      "name": "Routine du soir",
      "activities": [
        // Séquence du soir...
      ]
    },
    "random_day": {
      "name": "Journée aléatoire",
      "activities": "RANDOM_FROM_ALL"
    }
  }
}
```

### Scénarios disponibles

- **morning_routine** : Routine matinale typique
- **afternoon_routine** : Activités d'après-midi
- **evening_routine** : Routine du soir
- **lazy_day** : Journée tranquille avec activités relaxantes
- **busy_day** : Journée chargée avec beaucoup d'activités
- **random_day** : Activités aléatoires

## Interface web (Dashboard)

### Accès

```
http://localhost:8080/test-dashboard
```

### Fonctionnalités

#### 1. Contrôles de simulation
- **Démarrer/Arrêter** la simulation
- **Statut en temps réel** (en cours, arrêtée)
- **Mode actuel** (random, scenario, etc.)
- **Temps jusqu'au prochain changement**

#### 2. Contrôle manuel
- **Sélection d'activité** : Choisir manuellement une activité
- **Niveau de confiance** : Ajuster avec un slider (50%-100%)
- **Application immédiate** : Changement instantané

#### 3. Gestionnaire de scénarios
- **Liste des scénarios** disponibles
- **Chargement** d'un scénario spécifique
- **Arrêt** du scénario en cours
- **Informations** sur le scénario actuel

#### 4. Statistiques
- **Activités simulées** : Nombre total
- **Confiance moyenne** : Niveau moyen de confiance
- **Temps d'activité** : Durée de fonctionnement
- **Dernière mise à jour** : Horodatage

#### 5. Journal d'activité
- **Logs en temps réel** : Affichage des événements
- **Niveaux de log** : INFO, WARNING, ERROR
- **Historique limité** : 100 dernières entrées
- **Auto-scroll** : Défilement automatique

## API REST

### Endpoints de base

#### Activité courante
```http
GET /api/test/activity/current
```

```json
{
  "activity": "EATING",
  "confidence": 0.85,
  "timestamp": 1625123456789
}
```

#### Définir une activité
```http
POST /api/test/activity/set
Content-Type: application/json

{
  "activity": "READING",
  "confidence": 0.8
}
```

### Endpoints de simulation

#### Démarrer la simulation
```http
POST /api/test/simulation/start
```

#### Arrêter la simulation
```http
POST /api/test/simulation/stop
```

#### Statut de la simulation
```http
GET /api/test/simulation/status
```

```json
{
  "running": true,
  "mode": "random",
  "currentActivity": {
    "activity": "COOKING",
    "confidence": 0.78,
    "timestamp": 1625123456789
  },
  "nextChangeIn": 25000,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Endpoints de scénarios

#### Liste des scénarios
```http
GET /api/test/scenarios
```

```json
[
  {
    "id": "morning_routine",
    "name": "Routine matinale",
    "description": "Séquence typique d'activités du matin"
  },
  {
    "id": "evening_routine",
    "name": "Routine du soir",
    "description": "Séquence d'activités du soir"
  }
]
```

#### Charger un scénario
```http
POST /api/test/scenario/load/{scenarioId}
```

#### Scénario courant
```http
GET /api/test/scenario/current
```

#### Arrêter le scénario
```http
POST /api/test/scenario/stop
```

### Endpoints utilitaires

#### Santé du service
```http
GET /api/test/health
```

```json
{
  "status": "OK",
  "mode": "TEST",
  "service": "Angel Virtual Assistant - Test Mode",
  "timestamp": 1625123456789,
  "simulation": {
    "running": true,
    "connection": true
  }
}
```

#### Statistiques détaillées
```http
GET /api/test/stats
```

## Utilisation

### Démarrage en mode test

#### Via script
```bash
# Démarrage avec configuration de test
./angel.sh start -p test

# Démarrage avec mode test forcé
./angel.sh start --test-mode

# Démarrage avec fichier de configuration custom
./angel.sh start -c config/test/custom-test-config.json
```

#### Via Java
```bash
# Mode test avec propriétés système
java -Dangel.test.enabled=true \
     -Dangel.test.config=config/test/test-mode-config.json \
     -jar angel-virtual-assistant.jar

# Mode test avec profil Spring
java -Dspring.profiles.active=test \
     -jar angel-virtual-assistant.jar
```

### Intégration avec l'application principale

Le mode test s'intègre transparemment :

```java
// L'application utilise automatiquement le client de test
// quand le mode test est activé
@Autowired
private AngelServerClient angelServerClient; // Sera TestActivityClient en mode test

// Récupération d'activité (identique en production et test)
ActivityDTO currentActivity = angelServerClient.getCurrentActivity();
```

### Basculement automatique

En cas d'indisponibilité du serveur dl4j-server-capture :

```json
{
  "api": {
    "testMode": {
      "fallbackToTest": true
    }
  }
}
```

L'application bascule automatiquement en mode test.

## Développement et extension

### Ajouter un nouveau scénario

1. **Éditer le fichier de scénarios** :

```json
// config/test/activity-scenarios.json
{
  "scenarios": {
    "my_custom_scenario": {
      "name": "Mon Scénario",
      "description": "Description de mon scénario",
      "activities": [
        {
          "activity": "READING",
          "duration": 600000,
          "confidence": 0.9,
          "description": "Lecture prolongée"
        }
      ]
    }
  }
}
```

2. **Recharger la configuration** :

```bash
curl -X POST http://localhost:8080/api/test/scenarios/reload
```

### Ajouter un mode de simulation

1. **Étendre ActivitySimulator** :

```java
private Activity selectNextActivity() {
    String sequence = config.getActivities().getSequence().toLowerCase();
    
    switch (sequence) {
        case "my_custom_mode":
            return getMyCustomActivity();
        // ... autres modes
    }
}

private Activity getMyCustomActivity() {
    // Logique personnalisée
    return Activity.READING;
}
```

2. **Mettre à jour la configuration** :

```json
{
  "activities": {
    "sequence": "my_custom_mode"
  }
}
```

### Personnaliser les patterns temporels

```java
// TestDataGenerator.java
private Activity selectActivityForTime(LocalDateTime time) {
    int hour = time.getHour();
    
    // Ajouter vos propres règles temporelles
    if (hour >= 14 && hour < 16) {
        return Activity.MY_CUSTOM_ACTIVITY;
    }
    
    // Logique existante...
}
```

## Dépannage

### Problèmes courants

#### Mode test ne démarre pas

```bash
# Vérifier la configuration
curl http://localhost:8080/api/test/health

# Vérifier les logs
tail -f logs/angel.log | grep -i test

# Vérifier les propriétés système
java -Dangel.test.enabled=true -Dangel.test.debug=true -jar app.jar
```

#### Dashboard inaccessible

```bash
# Vérifier le port configuré
grep -r "dashboardPort" config/

# Tester l'accès direct
curl http://localhost:8081/test-dashboard

# Vérifier les logs du serveur web
```

#### Scénarios ne se chargent pas

```bash
# Vérifier la syntaxe JSON
jsonlint config/test/activity-scenarios.json

# Vérifier les permissions
ls -la config/test/

# Tester le chargement manuel
curl -X POST http://localhost:8080/api/test/scenario/load/morning_routine
```

#### Simulation ne s'arrête pas

```bash
# Forcer l'arrêt
curl -X POST http://localhost:8080/api/test/simulation/stop

# Redémarrer le service
./angel.sh restart

# Vérifier les threads
jstack <pid> | grep -i simulator
```

### Logs de débogage

Activer les logs détaillés :

```json
{
  "logging": {
    "level": "DEBUG",
    "logActivities": true,
    "logTransitions": true
  }
}
```

Ou via propriétés système :

```bash
java -Dlogging.level.com.angel.test=DEBUG -jar app.jar
```

### Métriques et monitoring

```bash
# Statistiques en temps réel
watch -n 1 "curl -s http://localhost:8080/api/test/stats | jq"

# Monitoring de l'activité
curl -s http://localhost:8080/api/test/activity/current | jq '.activity'

# Logs structurés
tail -f logs/angel.log | jq 'select(.logger | contains("test"))'
```

## Performances

### Optimisation

- **Intervalle de simulation** : Ajuster selon les besoins (minimum 1 seconde)
- **Complexité des scénarios** : Éviter les scénarios trop longs
- **Logging** : Désactiver les logs détaillés en production
- **Mémoire** : Limiter l'historique des logs

### Limites

- **Maximum 1000 activités** par scénario
- **Intervalle minimum** : 1000ms entre activités
- **Logs** : Maximum 100 entrées en mémoire
- **Scénarios** : Maximum 50 scénarios simultanés

## Sécurité

### Considérations

- **Mode test uniquement** en développement/test
- **Pas de données sensibles** dans les scénarios
- **Désactivation** obligatoire en production
- **Accès restreint** au dashboard de test

### Configuration de production

```json
{
  "system": {
    "mode": "production",
    "testMode": {
      "enabled": false
    }
  }
}
```

## Conclusion

Le mode test d'Angel Virtual Assistant offre une solution complète pour :

- **Développement autonome** sans dépendance externe
- **Tests automatisés** avec scénarios reproductibles
- **Débogage facilité** avec interface graphique
- **Simulation réaliste** d'activités temporelles

Cette approche permet un cycle de développement plus rapide et des tests plus fiables, tout en maintenant la compatibilité avec le système de production.
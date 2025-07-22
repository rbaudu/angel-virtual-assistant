# Mode Test Angel Virtual Assistant

Guide complet du mode test intégré avec interface web Spring Boot.

## Vue d'ensemble

Le mode test d'Angel Virtual Assistant permet de **développer et tester l'application sans dépendre du serveur angel-server-capture**. Il simule des activités en continu et propose une interface web complète pour le contrôle et le monitoring.

### 🆕 Nouveautés avec Spring Boot

- 🌐 **Interface web intégrée** : Dashboard accessible via navigateur
- 🔄 **API REST complète** : Contrôle programmatique via HTTP
- 📊 **Monitoring temps réel** : WebSocket pour mises à jour instantanées
- ⚙️ **Configuration centralisée** : Fichiers dans `config/` pour tous les paramètres
- 📱 **Interface responsive** : Compatible desktop et mobile

## URLs d'Accès Mode Test

Quand l'application est démarrée en mode test :

- **Port** : 8081 (différent du mode normal)
- **Context-path** : `/` (racine)

### Interfaces Disponibles

- **Dashboard principal** : http://localhost:8081/test-dashboard
- **Interface Avatar** : http://localhost:8081/angel et http://localhost:8081/
- **Console H2** : http://localhost:8081/h2-console
- **API REST** : http://localhost:8081/api/test/
- **Métriques** : http://localhost:8081/actuator/health

## Démarrage du Mode Test

### Via Script de Lancement

```bash
# Démarrage simple en mode test
./angel-launcher.sh start -p test

# Avec options avancées
./angel-launcher.sh start -p test -m 1g -v

# En mode daemon (arrière-plan)
./angel-launcher.sh start -p test -b

# Avec debug activé
./angel-launcher.sh start -p test -d
```

### Via Java Direct

```bash
# Avec profil Spring Boot
java -Dspring.profiles.active=test -jar target/angel-virtual-assistant-1.0.0-SNAPSHOT.jar

# Avec configuration externe
java -Dspring.config.location=file:./config/ -Dspring.profiles.active=test -jar target/angel-virtual-assistant-1.0.0-SNAPSHOT.jar
```

### Variables d'Environnement

```bash
export SPRING_PROFILES_ACTIVE=test
export SERVER_PORT=8081
./angel-launcher.sh start
```

## Interface Web du Mode Test

### Dashboard Principal

Le dashboard est accessible à http://localhost:8081/test-dashboard et comprend :

#### 1. **Panneau de Contrôle de la Simulation**

- **État de la simulation** : Démarrée/Arrêtée avec indicateur visuel
- **Boutons de contrôle** :
  - ▶️ Start : Démarrer la simulation automatique
  - ⏸️ Stop : Arrêter la simulation
  - ⏭️ Next : Passer à l'activité suivante
  - 🔀 Random : Activité aléatoire

- **Paramètres de simulation** :
  - Vitesse (1x à 10x)
  - Niveau d'aléatoire (0% à 100%)
  - Intervalle entre activités
  - Activation du bruit (variations)

#### 2. **Sélection Manuelle d'Activité**

- **Liste déroulante** avec toutes les 27 activités supportées
- **Niveau de confiance** : Curseur de 0% à 100%
- **Durée personnalisée** : En minutes ou secondes
- **Bouton "Appliquer"** : Application immédiate de l'activité

#### 3. **Gestionnaire de Scénarios**

- **Scénarios prédéfinis** :
  - 🌅 `morning_routine` : Routine matinale
  - 🌆 `evening_routine` : Routine du soir
  - 🎯 `focused_work` : Journée de travail concentré
  - 🏠 `relaxed_day` : Journée tranquille à la maison

- **Actions disponibles** :
  - 📥 Charger un scénario
  - ▶️ Démarrer l'exécution
  - ⏸️ Mettre en pause
  - 📊 Voir les détails du scénario

- **Création de scénarios** :
  - ➕ Nouveau scénario
  - ✏️ Édition visuelle
  - 💾 Sauvegarde locale
  - 📤 Export/Import JSON

#### 4. **Statistiques en Temps Réel**

**Graphique d'Activités** :
- Histogramme des dernières 24 heures
- Répartition par type d'activité
- Tendances et patterns

**Métriques Actuelles** :
- Activité courante avec niveau de confiance
- Nombre total d'activités simulées
- Temps moyen par activité
- Propositions générées

**Performance** :
- Temps de réponse API
- Utilisation mémoire
- Statistiques CPU
- Trafic réseau

#### 5. **Journal d'Activité Temps Réel**

- **Log en direct** : Scroll automatique des événements
- **Filtrage par type** :
  - 🎯 Activités
  - 💡 Propositions
  - ⚠️ Erreurs
  - ℹ️ Informations

- **Fonctionnalités** :
  - 🔍 Recherche dans les logs
  - 📥 Export en CSV/JSON
  - 🗑️ Nettoyage du journal
  - ⏸️ Pause du défilement

### Interface Avatar Web

Accessible à http://localhost:8081/angel :

#### Fonctionnalités Avatar
- **Rendu 3D** : Avatar animé en temps réel
- **Synchronisation labiale** : Mouvements de bouche pendant la parole
- **Expressions émotionnelles** : Changement d'humeur selon le contexte
- **Gestures** : Mouvements de mains et du corps

#### Contrôles Utilisateur
- 🔊 **Volume** : Contrôle du volume audio
- 🎤 **Voix** : Activation/désactivation de la synthèse vocale
- 🎭 **Émotion** : Sélection manuelle de l'humeur
- 🖼️ **Mode d'affichage** : Plein écran, fenêtré, compact

## Configuration du Mode Test

### Configuration Principal

Dans `config/application-test.properties` :

```properties
# ===============================================
# Configuration Spring Boot Mode Test
# ===============================================

# Serveur (port dédié test)
server.port=8081
server.servlet.context-path=/

# Mode test activé
angel.test.enabled=true
angel.test.auto-start=true
angel.test.dashboard.enabled=true

# Base de données en mémoire
spring.datasource.url=jdbc:h2:mem:angel-test-db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=angel_test
spring.datasource.password=test123
spring.h2.console.enabled=true

# Simulation
angel.test.simulation.interval=5000
angel.test.simulation.randomness=0.5
angel.test.simulation.speed-multiplier=5.0
angel.test.simulation.noise-enabled=true

# Dashboard
angel.test.dashboard.refresh-interval=5000
angel.test.dashboard.max-log-entries=1000
angel.test.dashboard.stats.enabled=true

# Avatar simplifié
avatar.enabled=true
avatar.display-time=5000
avatar.web.enabled=true
avatar.web.3d.quality=low

# Logging verbeux
logging.level.com.angel=TRACE
logging.level.root=DEBUG
logging.file.name=logs/angel-test.log
```

### Configuration Détaillée

Dans `config/test/test-mode-config.json` :

```json
{
  "simulation": {
    "enabled": true,
    "interval": 30000,
    "randomness": 0.3,
    "speedMultiplier": 1.0,
    "noiseEnabled": true,
    "activities": {
      "weights": {
        "EATING": 0.15,
        "SLEEPING": 0.20,
        "WATCHING_TV": 0.10,
        "READING": 0.08,
        "CLEANING": 0.05,
        "COOKING": 0.08,
        "WORKING": 0.12,
        "WAITING": 0.10,
        "OTHER": 0.12
      },
      "durations": {
        "min": 300000,
        "max": 3600000,
        "average": 1200000
      }
    },
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
    "theme": "light",
    "features": {
      "statistics": true,
      "realTimeUpdates": true,
      "scenarioManager": true,
      "activityControl": true,
      "exportData": true
    }
  },
  "api": {
    "enabled": true,
    "basePath": "/api/test",
    "authentication": false,
    "cors": {
      "enabled": true,
      "allowedOrigins": ["http://localhost:3000", "http://localhost:8081"]
    },
    "rateLimit": {
      "enabled": false,
      "requests": 100,
      "window": 60000
    }
  },
  "notifications": {
    "enabled": true,
    "channels": ["dashboard", "websocket", "logs"],
    "levels": ["info", "warning", "error"]
  }
}
```

## API REST Mode Test

### Endpoints de Simulation

#### Contrôle de la Simulation

```bash
# Obtenir le statut de la simulation
curl http://localhost:8081/api/test/simulation/status

# Démarrer la simulation
curl -X POST http://localhost:8081/api/test/simulation/start

# Arrêter la simulation
curl -X POST http://localhost:8081/api/test/simulation/stop

# Mettre en pause/reprendre
curl -X POST http://localhost:8081/api/test/simulation/pause
curl -X POST http://localhost:8081/api/test/simulation/resume

# Configurer la simulation
curl -X PUT http://localhost:8081/api/test/simulation/config \
     -H "Content-Type: application/json" \
     -d '{
       "interval": 10000,
       "randomness": 0.4,
       "speedMultiplier": 2.0,
       "noiseEnabled": true
     }'
```

#### Gestion des Activités

```bash
# Obtenir l'activité courante
curl http://localhost:8081/api/test/activity/current

# Définir une activité manuellement
curl -X POST http://localhost:8081/api/test/activity/set \
     -H "Content-Type: application/json" \
     -d '{
       "activity": "READING",
       "confidence": 0.85,
       "duration": 300000,
       "description": "Lecture d'un livre"
     }'

# Passer à l'activité suivante
curl -X POST http://localhost:8081/api/test/activity/next

# Activité aléatoire
curl -X POST http://localhost:8081/api/test/activity/random

# Historique des activités
curl http://localhost:8081/api/test/activity/history?limit=50

# Statistiques des activités
curl http://localhost:8081/api/test/activity/stats
```

### Endpoints de Scénarios

```bash
# Lister tous les scénarios disponibles
curl http://localhost:8081/api/test/scenarios

# Obtenir les détails d'un scénario
curl http://localhost:8081/api/test/scenarios/morning_routine

# Charger un scénario
curl -X POST http://localhost:8081/api/test/scenario/load \
     -H "Content-Type: application/json" \
     -d '{"name": "morning_routine"}'

# Créer un nouveau scénario
curl -X POST http://localhost:8081/api/test/scenarios \
     -H "Content-Type: application/json" \
     -d '{
       "name": "custom_scenario",
       "description": "Mon scénario personnalisé",
       "activities": [
         {
           "activity": "WAKING_UP",
           "duration": 300000,
           "confidence": 0.9
         },
         {
           "activity": "EATING",
           "duration": 600000,
           "confidence": 0.8
         }
       ]
     }'

# Supprimer un scénario
curl -X DELETE http://localhost:8081/api/test/scenarios/custom_scenario
```

### Endpoints de Monitoring

```bash
# Santé du système test
curl http://localhost:8081/api/test/health

# Statistiques complètes
curl http://localhost:8081/api/test/stats

# Statistiques en temps réel (stream)
curl http://localhost:8081/api/test/stats/stream

# Export des données
curl http://localhost:8081/api/test/export/csv
curl http://localhost:8081/api/test/export/json

# Reset des statistiques
curl -X POST http://localhost:8081/api/test/stats/reset
```

### Endpoints Dashboard

```bash
# Configuration du dashboard
curl http://localhost:8081/api/test/dashboard/config

# État du dashboard
curl http://localhost:8081/api/test/dashboard/status

# Logs récents
curl http://localhost:8081/api/test/dashboard/logs?limit=100

# Métriques dashboard
curl http://localhost:8081/api/test/dashboard/metrics
```

## Communication WebSocket

### Connexion WebSocket

Le dashboard utilise WebSocket pour les mises à jour temps réel :

```javascript
// Connexion au WebSocket de test
const testWs = new WebSocket('ws://localhost:8081/ws/test');

testWs.onopen = function() {
    console.log('Connexion WebSocket établie');
    
    // S'abonner aux mises à jour
    testWs.send(JSON.stringify({
        type: 'subscribe',
        channels: ['activities', 'proposals', 'stats']
    }));
};

testWs.onmessage = function(event) {
    const message = JSON.parse(event.data);
    
    switch(message.type) {
        case 'activity_change':
            updateActivityDisplay(message.data);
            break;
            
        case 'proposal_generated':
            addProposalToLog(message.data);
            break;
            
        case 'stats_update':
            updateStatisticsCharts(message.data);
            break;
            
        case 'simulation_status':
            updateSimulationControls(message.data);
            break;
    }
};

// Envoi de commandes via WebSocket
function sendTestCommand(command, data) {
    testWs.send(JSON.stringify({
        type: 'command',
        command: command,
        data: data,
        timestamp: Date.now()
    }));
}
```

### Messages WebSocket Supportés

#### Messages Entrants (du serveur)

```json
{
  "type": "activity_change",
  "data": {
    "activity": "READING",
    "confidence": 0.85,
    "timestamp": 1625097600000,
    "duration": 300000
  }
}

{
  "type": "proposal_generated",
  "data": {
    "id": "prop_123",
    "title": "Suggestion météo",
    "content": "Il fait beau aujourd'hui...",
    "type": "weather",
    "timestamp": 1625097600000
  }
}

{
  "type": "stats_update",
  "data": {
    "totalActivities": 42,
    "currentActivity": "READING",
    "averageDuration": 1800000,
    "proposalsGenerated": 8
  }
}
```

#### Messages Sortants (vers le serveur)

```json
{
  "type": "command",
  "command": "set_activity",
  "data": {
    "activity": "COOKING",
    "confidence": 0.9
  }
}

{
  "type": "subscribe",
  "channels": ["activities", "proposals", "stats"]
}

{
  "type": "unsubscribe",
  "channels": ["stats"]
}
```

## Scénarios d'Activités

### Scénarios Prédéfinis

Dans `config/test/activity-scenarios.json` :

```json
{
  "scenarios": {
    "morning_routine": {
      "name": "Routine Matinale",
      "description": "Séquence typique du matin d'une personne",
      "duration": 10800000,
      "activities": [
        {
          "activity": "WAKING_UP",
          "duration": 300000,
          "confidence": 0.95,
          "description": "Se réveiller et sortir du lit"
        },
        {
          "activity": "WASHING",
          "duration": 900000,
          "confidence": 0.90,
          "description": "Toilette matinale"
        },
        {
          "activity": "COOKING",
          "duration": 1200000,
          "confidence": 0.85,
          "description": "Préparation du petit déjeuner"
        },
        {
          "activity": "EATING",
          "duration": 1800000,
          "confidence": 0.90,
          "description": "Petit déjeuner"
        },
        {
          "activity": "READING",
          "duration": 1200000,
          "confidence": 0.75,
          "description": "Lecture du journal"
        }
      ]
    },
    
    "evening_routine": {
      "name": "Routine du Soir",
      "description": "Séquence typique du soir avant le coucher",
      "duration": 14400000,
      "activities": [
        {
          "activity": "COOKING",
          "duration": 2400000,
          "confidence": 0.85,
          "description": "Préparation du dîner"
        },
        {
          "activity": "EATING",
          "duration": 2700000,
          "confidence": 0.90,
          "description": "Dîner en famille"
        },
        {
          "activity": "CLEANING",
          "duration": 1800000,
          "confidence": 0.80,
          "description": "Rangement après le repas"
        },
        {
          "activity": "WATCHING_TV",
          "duration": 5400000,
          "confidence": 0.85,
          "description": "Détente devant la télévision"
        },
        {
          "activity": "READING",
          "duration": 1800000,
          "confidence": 0.75,
          "description": "Lecture avant de dormir"
        },
        {
          "activity": "GOING_TO_SLEEP",
          "duration": 300000,
          "confidence": 0.95,
          "description": "Se coucher"
        }
      ]
    },
    
    "busy_day": {
      "name": "Journée Chargée",
      "description": "Journée avec beaucoup d'activités variées",
      "duration": 43200000,
      "activities": [
        {
          "activity": "WAKING_UP",
          "duration": 300000,
          "confidence": 0.95
        },
        {
          "activity": "EATING",
          "duration": 1200000,
          "confidence": 0.85
        },
        {
          "activity": "CLEANING",
          "duration": 3600000,
          "confidence": 0.80
        },
        {
          "activity": "COOKING",
          "duration": 2400000,
          "confidence": 0.85
        },
        {
          "activity": "EATING",
          "duration": 1800000,
          "confidence": 0.90
        },
        {
          "activity": "PHONING",
          "duration": 1800000,
          "confidence": 0.85
        },
        {
          "activity": "USING_SCREEN",
          "duration": 3600000,
          "confidence": 0.80
        },
        {
          "activity": "MOVING",
          "duration": 1800000,
          "confidence": 0.75
        },
        {
          "activity": "COOKING",
          "duration": 2400000,
          "confidence": 0.85
        },
        {
          "activity": "EATING",
          "duration": 2700000,
          "confidence": 0.90
        },
        {
          "activity": "WATCHING_TV",
          "duration": 7200000,
          "confidence": 0.85
        }
      ]
    },
    
    "relaxed_weekend": {
      "name": "Weekend Tranquille",
      "description": "Journée détendue de weekend",
      "duration": 36000000,
      "activities": [
        {
          "activity": "SLEEPING",
          "duration": 3600000,
          "confidence": 0.95
        },
        {
          "activity": "WAKING_UP",
          "duration": 600000,
          "confidence": 0.90
        },
        {
          "activity": "EATING",
          "duration": 2400000,
          "confidence": 0.85
        },
        {
          "activity": "READING",
          "duration": 5400000,
          "confidence": 0.80
        },
        {
          "activity": "LISTENING_MUSIC",
          "duration": 3600000,
          "confidence": 0.75
        },
        {
          "activity": "COOKING",
          "duration": 2400000,
          "confidence": 0.85
        },
        {
          "activity": "EATING",
          "duration": 2700000,
          "confidence": 0.90
        },
        {
          "activity": "WATCHING_TV",
          "duration": 10800000,
          "confidence": 0.85
        },
        {
          "activity": "GOING_TO_SLEEP",
          "duration": 300000,
          "confidence": 0.95
        }
      ]
    }
  }
}
```

### Création de Scénarios Personnalisés

#### Via Interface Web

1. Accéder au dashboard : http://localhost:8081/test-dashboard
2. Onglet "Scénarios"
3. Cliquer sur "➕ Nouveau Scénario"
4. Remplir les informations :
   - Nom du scénario
   - Description
   - Durée totale estimée
5. Ajouter les activités une par une :
   - Sélectionner l'activité
   - Définir la durée
   - Ajuster la confiance
   - Ajouter une description
6. Sauvegarder le scénario
7. Tester immédiatement ou programmer pour plus tard

#### Via API REST

```bash
# Créer un scénario personnalisé
curl -X POST http://localhost:8081/api/test/scenarios \
     -H "Content-Type: application/json" \
     -d '{
       "name": "my_custom_scenario",
       "description": "Mon scénario personnalisé pour tester des cas spécifiques",
       "activities": [
         {
           "activity": "WAKING_UP",
           "duration": 300000,
           "confidence": 0.9,
           "description": "Réveil matinal"
         },
         {
           "activity": "COOKING",
           "duration": 1800000,
           "confidence": 0.85,
           "description": "Préparation du petit déjeuner"
         },
         {
           "activity": "EATING",
           "duration": 1200000,
           "confidence": 0.9,
           "description": "Petit déjeuner tranquille"
         },
         {
           "activity": "READING",
           "duration": 2400000,
           "confidence": 0.8,
           "description": "Lecture du journal et livres"
         }
       ]
     }'
```

#### Via Fichier JSON

Éditer directement `config/test/activity-scenarios.json` :

```json
{
  "scenarios": {
    "existing_scenarios": "...",
    
    "my_test_scenario": {
      "name": "Scénario de Test Personnalisé",
      "description": "Scénario créé pour tester des propositions spécifiques",
      "duration": 7200000,
      "tags": ["test", "développement", "propositions"],
      "metadata": {
        "author": "Développeur",
        "created": "2025-01-15",
        "version": "1.0"
      },
      "activities": [
        {
          "activity": "WAITING",
          "duration": 1800000,
          "confidence": 0.85,
          "description": "Attente pour déclencher des propositions",
          "expectedProposals": ["news", "weather", "stories"]
        },
        {
          "activity": "EATING",
          "duration": 1200000,
          "confidence": 0.9,
          "description": "Repas pour tester les propositions contextuelles",
          "expectedProposals": ["reminders.medications", "conversations"]
        },
        {
          "activity": "USING_SCREEN",
          "duration": 3600000,
          "confidence": 0.8,
          "description": "Utilisation écran pour propositions",
          "expectedProposals": ["news", "reminders"]
        },
        {
          "activity": "GOING_TO_SLEEP",
          "duration": 600000,
          "confidence": 0.95,
          "description": "Coucher pour propositions du soir",
          "expectedProposals": ["weather.tomorrow", "reminders"]
        }
      ]
    }
  }
}
```

## Monitoring et Analytics

### Dashboard de Monitoring

Le dashboard fournit plusieurs vues de monitoring :

#### 1. **Vue d'Ensemble**
- 📊 Statut global du système
- ⚡ Performance en temps réel
- 📈 Tendances des dernières 24h
- 🎯 Activité courante avec détails

#### 2. **Graphiques d'Activités**

**Histogramme Temporel** :
- Activités par heure sur 24h
- Code couleur par type d'activité
- Zoom et filtrage interactif

**Répartition Circulaire** :
- Pourcentage par type d'activité
- Temps total par catégorie
- Comparaison avec moyennes

**Timeline Détaillée** :
- Chronologie précise des activités
- Durées et transitions
- Événements et propositions associées

#### 3. **Métriques de Propositions**

- 💡 **Propositions générées** : Nombre total et par type
- 🎯 **Taux de pertinence** : Propositions adaptées au contexte
- ⏱️ **Délai de génération** : Temps de calcul des propositions
- 🔄 **Fréquence d'apparition** : Éviter les répétitions

#### 4. **Performance Système**

**Utilisation Ressources** :
- 🧠 CPU : Utilisation processeur
- 💾 RAM : Consommation mémoire
- 💿 Disque : Espace utilisé pour logs/DB
- 🌐 Réseau : Trafic API et WebSocket

**Temps de Réponse** :
- 🔄 API REST : Latence des endpoints
- 🔌 WebSocket : Délai des mises à jour
- 🗄️ Base de données : Performance des requêtes
- 🎭 Avatar : Temps de rendu 3D

### Export des Données

#### Export CSV

```bash
# Export complet
curl http://localhost:8081/api/test/export/csv > test_data.csv

# Export filtré par date
curl "http://localhost:8081/api/test/export/csv?from=2025-01-15&to=2025-01-16" > daily_data.csv

# Export par type d'activité
curl "http://localhost:8081/api/test/export/csv?activities=EATING,COOKING,CLEANING" > activities.csv
```

Format CSV généré :

```csv
timestamp,activity,confidence,duration,proposals_generated,scenario
2025-01-15T08:00:00Z,WAKING_UP,0.95,300000,1,morning_routine
2025-01-15T08:05:00Z,WASHING,0.90,900000,0,morning_routine
2025-01-15T08:20:00Z,COOKING,0.85,1200000,2,morning_routine
```

#### Export JSON

```bash
# Export JSON détaillé
curl http://localhost:8081/api/test/export/json > test_data.json

# Export avec métadonnées
curl "http://localhost:8081/api/test/export/json?include_metadata=true" > full_export.json
```

Format JSON généré :

```json
{
  "metadata": {
    "export_date": "2025-01-15T14:30:00Z",
    "total_entries": 1247,
    "duration_hours": 24,
    "test_session_id": "test_20250115"
  },
  "activities": [
    {
      "timestamp": "2025-01-15T08:00:00Z",
      "activity": "WAKING_UP",
      "confidence": 0.95,
      "duration": 300000,
      "scenario": "morning_routine",
      "proposals": [
        {
          "type": "weather",
          "generated_at": "2025-01-15T08:01:30Z",
          "content": "Proposition météo du matin"
        }
      ]
    }
  ],
  "statistics": {
    "total_activities": 42,
    "unique_activities": 15,
    "average_confidence": 0.847,
    "total_proposals": 28,
    "most_common_activity": "EATING"
  }
}
```

### Analyse des Patterns

#### Détection de Tendances

Le système analyse automatiquement :

```javascript
// Exemple d'analyse de patterns
{
  "daily_patterns": {
    "morning_peak": {
      "time_range": "07:00-09:00",
      "common_activities": ["WAKING_UP", "EATING", "WASHING"],
      "proposal_effectiveness": 0.85
    },
    "afternoon_lull": {
      "time_range": "14:00-16:00", 
      "common_activities": ["WAITING", "READING"],
      "proposal_effectiveness": 0.72
    },
    "evening_active": {
      "time_range": "18:00-21:00",
      "common_activities": ["COOKING", "EATING", "WATCHING_TV"],
      "proposal_effectiveness": 0.91
    }
  },
  "activity_transitions": {
    "EATING_to_CLEANING": {
      "frequency": 0.78,
      "average_delay": 1800000,
      "proposal_opportunity": "high"
    },
    "WAKING_UP_to_WASHING": {
      "frequency": 0.92,
      "average_delay": 600000,
      "proposal_opportunity": "medium"
    }
  },
  "proposal_success": {
    "weather_proposals": {
      "generated": 45,
      "appropriate": 41,
      "success_rate": 0.91
    },
    "news_proposals": {
      "generated": 38,
      "appropriate": 32,
      "success_rate": 0.84
    }
  }
}
```

## Tests Automatisés

### Tests d'Intégration

Le mode test permet de lancer des tests automatisés :

```bash
# Lancer tous les tests du mode test
./angel-launcher.sh test -p test

# Tests spécifiques
mvn test -Dtest="TestModeIntegrationTest" -Dspring.profiles.active=test

# Tests avec scénarios
mvn test -Dtest="ScenarioExecutionTest" -Dangel.test.scenario=morning_routine
```

### Tests de Charge

```bash
# Test de charge sur l'API
curl -X POST http://localhost:8081/api/test/load-test \
     -H "Content-Type: application/json" \
     -d '{
       "concurrent_users": 10,
       "duration_minutes": 5,
       "requests_per_second": 100
     }'

# Monitoring pendant le test de charge
curl http://localhost:8081/api/test/load-test/status
```

### Tests de Régression

```java
@Test
@ActiveProfiles("test")
public class ProposalRegressionTest {
    
    @Test
    public void testWeatherProposalGeneration() {
        // Simuler activité EATING
        testService.setActivity(Activity.EATING, 0.9);
        
        // Attendre les propositions
        await().atMost(30, SECONDS)
               .until(() -> proposalService.getLastProposals().size() > 0);
        
        // Vérifier qu'une proposition météo est générée
        List<Proposal> proposals = proposalService.getLastProposals();
        assertThat(proposals)
            .extracting(Proposal::getType)
            .contains("weather");
    }
}
```

## Intégration Continue

### Tests Automatisés en CI/CD

Configuration GitHub Actions (`.github/workflows/test.yml`) :

```yaml
name: Angel Test Suite

on: [push, pull_request]

jobs:
  test-mode:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Java 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests in test mode
      run: |
        chmod +x angel-launcher.sh
        ./angel-launcher.sh build
        ./angel-launcher.sh start -p test -b
        sleep 30  # Attendre que l'application démarre
        ./angel-launcher.sh test-web
        ./angel-launcher.sh stop
    
    - name: Run unit tests
      run: mvn test -Dspring.profiles.active=test
    
    - name: Generate test report
      run: mvn surefire-report:report
    
    - name: Upload test results
      uses: actions/upload-artifact@v3
      with:
        name: test-results
        path: target/surefire-reports/
```

### Tests de Performance

Script de test automatisé (`scripts/performance-test.sh`) :

```bash
#!/bin/bash

echo "🚀 Démarrage des tests de performance mode test"

# Démarrer en mode test
./angel-launcher.sh start -p test -b -m 2g

sleep 30

# Test de charge API
echo "📊 Test de charge API..."
for i in {1..100}; do
    curl -s http://localhost:8081/api/test/activity/current > /dev/null &
done
wait

# Test WebSocket
echo "🔌 Test WebSocket..."
node scripts/websocket-load-test.js

# Test dashboard
echo "🖥️ Test chargement dashboard..."
curl -w "@curl-format.txt" -s http://localhost:8081/test-dashboard > /dev/null

# Test avatar
echo "🎭 Test interface avatar..."
curl -w "@curl-format.txt" -s http://localhost:8081/angel > /dev/null

# Collecter les métriques
echo "📈 Collecte des métriques..."
curl http://localhost:8081/actuator/metrics/jvm.memory.used
curl http://localhost:8081/actuator/metrics/http.server.requests

./angel-launcher.sh stop

echo "✅ Tests de performance terminés"
```

## Dépannage Mode Test

### Problèmes Courants

#### 1. **Dashboard non accessible**

**Symptômes** :
```
404 Not Found - http://localhost:8081/test-dashboard
```

**Solutions** :
```bash
# Vérifier que le mode test est activé
grep "angel.test.enabled=true" config/application-test.properties

# Vérifier le port
grep "server.port" config/application-test.properties

# Vérifier les logs
tail -f logs/angel-test.log | grep -i "dashboard\|thymeleaf"

# Redémarrer en mode test
./angel-launcher.sh stop
./angel-launcher.sh start -p test
```

#### 2. **WebSocket connection failed**

**Symptômes** :
```javascript
WebSocket connection to 'ws://localhost:8081/ws/test' failed
```

**Solutions** :
```bash
# Vérifier la configuration WebSocket
grep -r "websocket" config/

# Tester la connexion manuellement
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" \
     http://localhost:8081/ws/test

# Vérifier les logs WebSocket
tail -f logs/angel-test.log | grep -i websocket
```

#### 3. **Simulation ne démarre pas**

**Symptômes** :
- Bouton "Start" ne réagit pas
- Pas de changement d'activité
- Erreurs dans les logs

**Solutions** :
```bash
# Vérifier la configuration de simulation
cat config/test/test-mode-config.json | jq .simulation

# Tester via API
curl -X POST http://localhost:8081/api/test/simulation/start

# Vérifier les logs de simulation
tail -f logs/angel-test.log | grep -i "simulation\|activity"

# Reset du mode test
curl -X POST http://localhost:8081/api/test/simulation/reset
```

#### 4. **Base de données H2 inaccessible**

**Symptômes** :
```
Database connection error
```

**Solutions** :
```bash
# Vérifier la configuration H2
grep "h2" config/application-test.properties

# Accéder à la console H2
open http://localhost:8081/h2-console
# URL: jdbc:h2:mem:angel-test-db
# User: angel_test
# Password: test123

# Vérifier les logs de base de données
tail -f logs/angel-test.log | grep -i "h2\|database\|sql"
```

#### 5. **Performances dégradées**

**Symptômes** :
- Dashboard lent à charger
- WebSocket messages en retard
- CPU élevé

**Solutions** :
```bash
# Augmenter la mémoire allouée
./angel-launcher.sh stop
./angel-launcher.sh start -p test -m 2g

# Réduire la fréquence de mise à jour
# Éditer config/application-test.properties
angel.test.dashboard.refresh-interval=10000

# Désactiver certaines fonctionnalités
angel.test.dashboard.stats.enabled=false
avatar.web.enabled=false

# Vérifier les métriques
curl http://localhost:8081/actuator/metrics/jvm.memory.used
```

### Logs de Debug

#### Logs Utiles pour le Dépannage

```bash
# Logs généraux mode test
tail -f logs/angel-test.log

# Logs Spring Boot
tail -f logs/angel-test.log | grep -i "spring\|tomcat"

# Logs simulation
tail -f logs/angel-test.log | grep -i "simulation\|scenario"

# Logs WebSocket
tail -f logs/angel-test.log | grep -i "websocket\|ws"

# Logs dashboard
tail -f logs/angel-test.log | grep -i "dashboard\|thymeleaf"

# Logs API
tail -f logs/angel-test.log | grep -i "api\|rest\|controller"

# Logs base de données
tail -f logs/angel-test.log | grep -i "h2\|jpa\|hibernate"
```

#### Augmentation des Logs pour Debug

Temporairement augmenter le niveau de logs dans `config/application-test.properties` :

```properties
# Logs très verbeux pour debug
logging.level.com.angel=TRACE
logging.level.com.angel.test=TRACE
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.websocket=DEBUG
logging.level.org.thymeleaf=DEBUG

# Logs SQL
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Bonnes Pratiques

### 1. **Développement avec le Mode Test**

```bash
# Workflow recommandé pour le développement
./angel-launcher.sh start -p test -d  # Démarrage avec debug
# Développer les fonctionnalités
# Tester via dashboard: http://localhost:8081/test-dashboard
./angel-launcher.sh test-web         # Vérifier les endpoints
./angel-launcher.sh logs             # Surveiller les logs
```

### 2. **Tests de Régression**

```bash
# Avant chaque release
./angel-launcher.sh start -p test -b
sleep 30

# Exécuter tous les scénarios
curl -X POST http://localhost:8081/api/test/scenario/load/morning_routine
sleep 300  # 5 minutes
curl -X POST http://localhost:8081/api/test/scenario/load/evening_routine
sleep 600  # 10 minutes

# Vérifier les métriques
curl http://localhost:8081/api/test/stats > regression_test_stats.json

./angel-launcher.sh stop
```

### 3. **Configuration pour CI/CD**

Configuration optimisée pour les tests automatisés :

```properties
# config/application-ci.properties (pour CI/CD)
server.port=8081
angel.test.enabled=true
angel.test.auto-start=true
angel.test.dashboard.enabled=false  # Pas d'interface web en CI
avatar.web.enabled=false           # Pas d'avatar en CI
logging.level.root=WARN            # Logs moins verbeux
spring.jpa.show-sql=false         # Pas de logs SQL
angel.test.simulation.speed-multiplier=10.0  # Tests plus rapides
```

### 4. **Monitoring en Production (Mode Test)**

Pour utiliser le mode test en production pour des démonstrations :

```properties
# Configuration production-demo
server.port=8081
angel.test.enabled=true
angel.test.dashboard.enabled=true
avatar.web.enabled=true
angel.test.simulation.interval=60000    # Plus réaliste
angel.test.simulation.randomness=0.2    # Moins chaotique
logging.level.com.angel=INFO           # Logs production
```

## Conclusion

Le mode test d'Angel Virtual Assistant avec Spring Boot offre une plateforme complète pour :

1. **Développement sans dépendances** : Tester sans angel-server-capture
2. **Interface web moderne** : Dashboard et avatar accessibles via navigateur
3. **API REST complète** : Intégration et automatisation faciles
4. **Monitoring temps réel** : WebSocket et métriques avancées
5. **Scénarios personnalisables** : Tests répétables et configurables

### Points Clés

- ✅ **URL d'accès** : http://localhost:8081/test-dashboard
- ✅ **Configuration centralisée** : Fichiers dans `config/`
- ✅ **API REST** : Endpoints `/api/test/*` complets
- ✅ **WebSocket** : Mises à jour temps réel
- ✅ **Export de données** : CSV et JSON
- ✅ **Tests automatisés** : Intégration CI/CD

Le mode test constitue un environnement de développement et de démonstration robuste qui facilite grandement le travail sur Angel Virtual Assistant tout en offrant des fonctionnalités avancées de monitoring et d'analyse.

Pour plus d'informations :
- [README.md](../README.md) : Documentation principale
- [WEB_INTERFACE.md](WEB_INTERFACE.md) : Guide de l'interface web
- [SPRING_BOOT_MIGRATION.md](SPRING_BOOT_MIGRATION.md) : Guide de migration
- [CONFIGURATION.md](CONFIGURATION.md) : Configuration détaillée
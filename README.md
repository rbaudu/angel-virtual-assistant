# Angel Virtual Assistant

Un assistant virtuel intelligent qui propose des activités en fonction de la détection d'activités de l'utilisateur.

## Description

Angel Virtual Assistant est un système conçu pour accompagner les personnes dans leur quotidien en proposant des activités adaptées à leur contexte actuel. Le système utilise la détection d'activités fournie par Angel-server-capture pour comprendre ce que fait la personne à un moment donné, puis propose des activités appropriées via un avatar visuel.

## ✨ Nouveauté : Mode Test Intégré

**🎯 Développement et tests simplifiés !**

Le mode test permet de développer et tester l'assistant virtuel **sans dépendre du serveur dl4j-server-capture**. Il simule des activités en continu avec :

- 🎮 **Interface de contrôle web** : Dashboard complet accessible sur `http://localhost:8080/test-dashboard`
- 🎭 **Scénarios personnalisables** : Routines matinales, journées chargées, activités aléatoires
- ⚡ **Contrôle en temps réel** : Démarrage/arrêt, changement d'activité manuel
- 📊 **Statistiques détaillées** : Monitoring et logs en direct
- 🔄 **Basculement automatique** : Passe en mode test si le serveur principal est indisponible

### Démarrage rapide en mode test

```bash
# Cloner le projet
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant

# Démarrer en mode test (sans dl4j-server-capture)
./angel.sh start -p test

# Ou directement avec Java
java -Dangel.test.enabled=true -jar target/angel-virtual-assistant.jar

# Accéder au dashboard de test
open http://localhost:8080/test-dashboard
```

📚 **Documentation complète** : [docs/TEST_MODE.md](docs/TEST_MODE.md)

---

## Fonctionnalités principales

- **Détection d'activités** : Intégration avec Angel-server-capture pour détecter 27 types d'activités différentes
- **Propositions contextuelles** : Suggestions d'activités adaptées à l'activité actuelle de l'utilisateur
- **Avatar visuel** : Interface utilisateur basée sur un avatar avec visage humain
- **Activation vocale** : Système d'activation via le mot-clé "Angel"
- **Configuration flexible** : Paramétrage des fréquences et types de propositions
- **Historique intelligent** : Mémorisation des propositions pour éviter les répétitions
- **Préférences utilisateur** : Système de préférences personnalisables
- **🆕 Mode test complet** : Simulation d'activités pour développement et tests

## Types d'activités détectées

Le système peut détecter et réagir à 27 types d'activités différentes :

- CLEANING (Nettoyer)
- CONVERSING (Converser, parler)
- COOKING (Préparer à manger)
- DANCING (Danser)
- EATING (Manger)
- FEEDING (Nourrir les animaux de compagnie)
- GOING_TO_SLEEP (Se coucher)
- KNITTING (Tricoter/coudre)
- IRONING (Repasser)
- LISTENING_MUSIC (Écouter de la musique/radio)
- MOVING (Se déplacer)
- NEEDING_HELP (Avoir besoin d'assistance)
- PHONING (Téléphoner)
- PLAYING (Jouer)
- PLAYING_MUSIC (Jouer de la musique)
- PUTTING_AWAY (Ranger)
- READING (Lire)
- RECEIVING (Recevoir quelqu'un)
- SINGING (Chanter)
- SLEEPING (Dormir)
- UNKNOWN (Autre)
- USING_SCREEN (Utiliser un écran)
- WAITING (Ne rien faire, s'ennuyer)
- WAKING_UP (Se lever)
- WASHING (Se laver, passer aux toilettes)
- WATCHING_TV (Regarder la télévision)
- WRITING (Écrire)

## Types de propositions

Le système peut proposer différents types d'activités :

- **Informations** : Nouvelles locales/nationales/internationales, météo
- **Divertissement** : Histoires courtes, blagues, légendes, poésies, anecdotes
- **Rappels** : Rendez-vous, anniversaires, médicaments
- **Social** : Conversations, appels WhatsApp avec la famille
- **Recommandations** : Conseils contextuels (médicaments, promenades, etc.)
- **Jeux** : Jeux de mémoire, devinettes
- **Médias** : Photos, vidéos, musique, radio, télévision
- **Santé** : Exercices, assouplissements, mouvements de gym
- **Cuisine** : Suggestions de repas avec recettes simples
- **Récapitulatifs** : Résumé des activités de la journée/semaine

## Architecture technique

L'architecture du système est modulaire et principalement implémentée en Java :

### Composants principaux

1. **Module Core** (`com.angel.core`)
   - `AngelApplication.java` : Point d'entrée et orchestration générale

2. **Module Modèles** (`com.angel.model`)
   - `Activity.java` : Énumération des activités détectables
   - `ProposalHistory.java` : Historique des propositions
   - `UserProfile.java` : Profil et préférences utilisateur

3. **Module Configuration** (`com.angel.config`)
   - `ConfigManager.java` : Gestion de la configuration centralisée
   - `TestModeConfig.java` : Configuration du mode test

4. **Module API** (`com.angel.api`)
   - `AngelServerClient.java` : Communication avec Angel-server-capture
   - `TestActivityClient.java` : Client de simulation pour le mode test
   - `TestModeController.java` : API REST pour contrôle des tests
   - `dto/ActivityDTO.java` : Objets de transfert de données

5. **🆕 Module Test** (`com.angel.test`)
   - `ActivitySimulator.java` : Simulateur d'activités
   - `ScenarioManager.java` : Gestionnaire de scénarios
   - `TestDataGenerator.java` : Générateur de données de test
   - `TestModeService.java` : Service principal du mode test

6. **Module Intelligence** (`com.angel.intelligence`)
   - `ProposalEngine.java` : Moteur de décision pour les propositions
   - `proposals/Proposal.java` : Interface des propositions
   - `proposals/WeatherProposal.java` : Exemple de proposition météo

7. **Module Persistance** (`com.angel.persistence`)
   - `DatabaseManager.java` : Gestion de la base de données H2
   - `dao/ProposalDAO.java` : Accès aux données des propositions
   - `dao/UserPreferenceDAO.java` : Accès aux préférences utilisateur

8. **Module Interface Utilisateur** (`com.angel.ui`)
   - `AvatarController.java` : Contrôle de l'avatar visuel
   - `TestDashboardController.java` : Contrôleur web du dashboard de test

9. **Module Reconnaissance Vocale** (`com.angel.voice`)
   - `WakeWordDetector.java` : Détection du mot-clé "Angel"

10. **Module Utilitaires** (`com.angel.util`)
    - `LogUtil.java` : Gestion des logs
    - `DateTimeUtil.java` : Utilitaires de date/heure

## Structure des fichiers

```
angel-virtual-assistant/
├── README.md
├── pom.xml
├── angel.sh                    # Script de lancement Linux/macOS
├── angel.bat                   # Script de lancement Windows
├── install.sh                  # Script d'installation système
├── config/
│   ├── angel-config.json       # Configuration principale
│   └── test/
│       ├── test-mode-config.json     # 🆕 Configuration mode test
│       └── activity-scenarios.json   # 🆕 Scénarios d'activités
├── docs/
│   └── TEST_MODE.md           # 🆕 Documentation du mode test
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── angel/
│   │   │           ├── core/
│   │   │           │   └── AngelApplication.java
│   │   │           ├── model/
│   │   │           │   ├── Activity.java
│   │   │           │   ├── ProposalHistory.java
│   │   │           │   └── UserProfile.java
│   │   │           ├── config/
│   │   │           │   ├── ConfigManager.java
│   │   │           │   ├── TestModeConfig.java          # 🆕
│   │   │           │   └── TestModeConfiguration.java   # 🆕
│   │   │           ├── api/
│   │   │           │   ├── AngelServerClient.java
│   │   │           │   ├── TestActivityClient.java      # 🆕
│   │   │           │   ├── TestModeController.java      # 🆕
│   │   │           │   └── dto/
│   │   │           │       └── ActivityDTO.java
│   │   │           ├── test/                            # 🆕 Module complet
│   │   │           │   ├── ActivitySimulator.java
│   │   │           │   ├── ScenarioManager.java
│   │   │           │   ├── TestDataGenerator.java
│   │   │           │   └── TestModeService.java
│   │   │           ├── intelligence/
│   │   │           │   ├── ProposalEngine.java
│   │   │           │   └── proposals/
│   │   │           │       ├── Proposal.java
│   │   │           │       └── WeatherProposal.java
│   │   │           ├── persistence/
│   │   │           │   ├── DatabaseManager.java
│   │   │           │   └── dao/
│   │   │           │       ├── ProposalDAO.java
│   │   │           │       └── UserPreferenceDAO.java
│   │   │           ├── ui/
│   │   │           │   ├── AvatarController.java
│   │   │           │   └── TestDashboardController.java # 🆕
│   │   │           ├── voice/
│   │   │           │   └── WakeWordDetector.java
│   │   │           └── util/
│   │   │               ├── LogUtil.java
│   │   │               └── DateTimeUtil.java
│   │   └── resources/
│   │       ├── static/                                  # 🆕 Ressources web
│   │       │   ├── css/
│   │       │   │   └── test-mode.css
│   │       │   └── js/
│   │       │       └── test-control.js
│   │       └── templates/                               # 🆕 Templates web
│   │           └── test-dashboard.html
│   └── test/
│       └── java/
└── logs/
```

## Prérequis

- Java 17 ou supérieur
- Maven 3.6 ou supérieur
- **En mode production** : Angel-server-capture en fonctionnement
- **En mode test** : Aucune dépendance externe 🎉

## Installation

### Installation rapide (Recommandée)

1. Cloner le dépôt :
```bash
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant
```

2. Rendre les scripts exécutables :
```bash
chmod +x angel.sh install.sh
```

3. **Installation système** (avec service systemd) :
```bash
sudo ./install.sh install
```

4. **Installation utilisateur** (sans service système) :
```bash
./install.sh install --user
```

### Installation manuelle

1. Cloner le dépôt :
```bash
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant
```

2. Compiler le projet :
```bash
mvn clean compile
```

3. Configurer le système :
   - Éditer `config/angel-config.json` pour adapter les paramètres
   - **Mode production** : Configurer l'URL du serveur Angel-server-capture
   - **Mode test** : Activer le mode test dans la configuration

4. Exécuter l'application :
```bash
mvn exec:java -Dexec.mainClass="com.angel.core.AngelApplication"
```

Ou créer un JAR exécutable :
```bash
mvn clean package
java -jar target/angel-virtual-assistant-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

## Utilisation

### Mode Production (avec dl4j-server-capture)

#### Linux/macOS
```bash
# Démarrer l'application en mode production
./angel.sh start

# Démarrer en mode développement avec 1GB de RAM
./angel.sh start -p dev -m 1g

# Voir le statut
./angel.sh status

# Voir les logs en temps réel
./angel.sh logs

# Arrêter l'application
./angel.sh stop
```

### 🆕 Mode Test (sans dl4j-server-capture)

#### Démarrage rapide
```bash
# Démarrer en mode test
./angel.sh start -p test

# Ou avec activation explicite
./angel.sh start --test-mode

# Avec configuration personnalisée
java -Dangel.test.enabled=true \
     -Dangel.test.config=config/test/custom-config.json \
     -jar angel-virtual-assistant.jar
```

#### Interface web de test

1. **Accéder au dashboard** :
   ```
   http://localhost:8080/test-dashboard
   ```

2. **Fonctionnalités disponibles** :
   - 🎮 Contrôles de simulation (start/stop)
   - 🎯 Définition manuelle d'activités
   - 🎭 Chargement de scénarios prédéfinis
   - 📊 Statistiques en temps réel
   - 📝 Journal d'activité en direct

#### API de test

```bash
# Vérifier l'état du mode test
curl http://localhost:8080/api/test/health

# Obtenir l'activité courante
curl http://localhost:8080/api/test/activity/current

# Définir une activité manuellement
curl -X POST http://localhost:8080/api/test/activity/set \
     -H "Content-Type: application/json" \
     -d '{"activity": "READING", "confidence": 0.85}'

# Démarrer la simulation
curl -X POST http://localhost:8080/api/test/simulation/start

# Charger un scénario
curl -X POST http://localhost:8080/api/test/scenario/load/morning_routine
```

### Configuration du mode test

#### Activation dans angel-config.json

```json
{
  "system": {
    "mode": "test",
    "testMode": {
      "enabled": true,
      "configFile": "config/test/test-mode-config.json"
    }
  },
  "api": {
    "testMode": {
      "fallbackToTest": true
    }
  }
}
```

#### Scénarios personnalisés

Éditer `config/test/activity-scenarios.json` :

```json
{
  "scenarios": {
    "my_scenario": {
      "name": "Mon Scénario Personnalisé",
      "description": "Description de mon scénario",
      "activities": [
        {
          "activity": "WAKING_UP",
          "duration": 120000,
          "confidence": 0.9,
          "description": "Se réveiller"
        },
        {
          "activity": "EATING",
          "duration": 300000,
          "confidence": 0.85,
          "description": "Petit déjeuner"
        }
      ]
    }
  }
}
```

### Profils d'exécution

Le système supporte plusieurs profils :

- **prod** : Mode production (avec dl4j-server-capture)
- **test** : Mode test (simulation intégrée) 🆕
- **dev** : Mode développement (logs détaillés, hot reload)
- **default** : Mode par défaut

### Gestion du service système

Si vous avez utilisé l'installation système :

```bash
# Démarrer le service
sudo systemctl start angel-virtual-assistant

# Activer au démarrage
sudo systemctl enable angel-virtual-assistant

# Voir le statut
sudo systemctl status angel-virtual-assistant

# Arrêter le service
sudo systemctl stop angel-virtual-assistant

# Ou via le script d'installation
sudo ./install.sh service start
sudo ./install.sh service enable
sudo ./install.sh service status
```

## Configuration

Le fichier `config/angel-config.json` permet de configurer :

- **Système** : Langue, mot-clé d'activation, mode de fonctionnement
- **API** : URL du serveur Angel-capture, timeouts, mode test
- **Avatar** : Paramètres d'affichage et d'animation
- **Propositions** : Fréquences, types autorisés par activité
- **Base de données** : Configuration H2
- **Logging** : Niveaux et fichiers de log
- **🆕 Mode test** : Configuration de la simulation

### Exemple de configuration des propositions :

```json
{
  "proposals": {
    "daily": {
      "weather": {
        "maxPerDay": 3,
        "minTimeBetween": 14400000,
        "preferredHours": [7, 8, 13, 14, 19]
      }
    },
    "activityMapping": {
      "EATING": ["news", "weather", "reminders.medications", "conversations"],
      "WAITING": ["news", "weather", "stories", "conversations", "games", "media"]
    }
  }
}
```

## Maintenance

### Mise à jour du système

```bash
# Mise à jour automatique (garde la configuration)
sudo ./install.sh update

# Ou manuellement
git pull
./angel.sh stop
./angel.sh build
./angel.sh start
```

### Basculement entre modes

```bash
# Passer en mode test
./angel.sh stop
./angel.sh start -p test

# Retour en mode production
./angel.sh stop
./angel.sh start -p prod

# Basculement automatique si dl4j-server-capture indisponible
# (si fallbackToTest: true dans la configuration)
```

### Désinstallation

```bash
# Désinstallation complète
sudo ./install.sh uninstall

# Ou arrêt simple
./angel.sh stop
```

### Surveillance et logs

```bash
# Voir les logs en temps réel
./angel.sh logs

# Voir les logs système (si installé en service)
sudo journalctl -u angel-virtual-assistant -f

# Vérifier l'état des processus
./angel.sh status

# 🆕 Logs spécifiques au mode test
tail -f logs/angel.log | grep -i test
```

## Extensibilité

Le système est conçu pour être facilement extensible :

### Ajouter un nouveau type de proposition :

1. Créer une classe implémentant `Proposal`
2. L'ajouter dans `createAvailableProposals()` de `AngelApplication`
3. Configurer les paramètres dans `angel-config.json`

### 🆕 Ajouter un nouveau scénario de test :

1. Éditer `config/test/activity-scenarios.json`
2. Ajouter votre scénario avec les activités désirées
3. Recharger via l'API ou redémarrer l'application

### Exemple d'implémentation :

```java
public class NewsProposal implements Proposal {
    @Override
    public String getId() { return "news"; }
    
    @Override
    public boolean isAppropriate(Activity currentActivity, ...) {
        // Logique de pertinence
    }
    
    @Override
    public int getPriority(Activity currentActivity, ...) {
        // Calcul de priorité
    }
    
    // Autres méthodes...
}
```

## Base de données

Le système utilise une base de données H2 intégrée avec les tables :

- `proposal_history` : Historique des propositions faites
- `user_preferences` : Préférences utilisateur
- `activities` : Cache local des activités détectées
- **🆕** `test_sessions` : Sessions de test et statistiques

## Logging

Les logs sont configurés avec plusieurs niveaux :
- **INFO** : Informations générales de fonctionnement
- **WARNING** : Avertissements et erreurs récupérables
- **SEVERE** : Erreurs critiques
- **🆕 DEBUG** : Logs détaillés du mode test

Fichiers de log dans `./logs/angel.log` avec rotation automatique.

## Dépannage

### Problèmes courants

#### L'application ne démarre pas
```bash
# Vérifier les prérequis
java -version
mvn -version

# Vérifier les logs
./angel.sh logs

# Recompiler si nécessaire
./angel.sh clean
./angel.sh build
```

#### Impossible de se connecter à Angel-server-capture
```bash
# Vérifier que le serveur est démarré
curl http://localhost:8080/api/health

# 🆕 Basculer en mode test temporairement
./angel.sh stop
./angel.sh start -p test

# Ou modifier temporairement la configuration
# dans config/angel-config.json
```

#### 🆕 Problèmes avec le mode test
```bash
# Vérifier l'état du mode test
curl http://localhost:8080/api/test/health

# Vérifier la configuration
cat config/test/test-mode-config.json | jq

# Redémarrer la simulation
curl -X POST http://localhost:8080/api/test/simulation/stop
curl -X POST http://localhost:8080/api/test/simulation/start

# Vérifier les scénarios
curl http://localhost:8080/api/test/scenarios
```

#### Dashboard de test inaccessible
```bash
# Vérifier que le mode test est activé
grep -r "testMode" config/

# Tester l'accès direct
curl http://localhost:8080/test-dashboard

# Vérifier les logs du serveur web
tail -f logs/angel.log | grep -i dashboard
```

## Tests

Exécuter les tests unitaires :
```bash
# Avec le script
./angel.sh test

# Ou avec Maven
mvn test

# 🆕 Tests spécifiques au mode test
mvn test -Dtest="*Test*"
```

## Contribution

1. Fork le projet
2. Créer une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Committer vos changements (`git commit -m 'Add AmazingFeature'`)
4. Pousser vers la branche (`git push origin feature/AmazingFeature`)
5. Créer une Pull Request

## Licence

À définir.

## Roadmap

- [x] **🆕 Mode test intégré** avec simulation d'activités
- [x] **🆕 Interface web** de contrôle des tests
- [x] **🆕 Scénarios personnalisables** d'activités
- [x] **🆕 API REST** pour contrôle programmatique
- [ ] Implémentation des propositions manquantes (News, Stories, Games, etc.)
- [ ] Interface web pour l'avatar
- [ ] Intégration reconnaissance vocale avancée
- [ ] API REST pour contrôle externe
- [ ] Support multi-utilisateurs
- [ ] Intégration services externes (météo, actualités)
- [ ] Application mobile companion
- [ ] Interface de configuration graphique
- [ ] Système de plugins
- [ ] Support Docker/containerisation
- [ ] Monitoring et métriques avancées
- [ ] **🔄 Mode hybride** : basculement automatique production/test
- [ ] **📊 Analytics** : statistiques d'usage et patterns d'activités
- [ ] **🎯 IA améliorée** : apprentissage des préférences utilisateur

---

## 🚀 Démarrage Rapide

**Pour commencer immédiatement avec le mode test :**

```bash
# 1. Cloner et compiler
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant
mvn clean package

# 2. Démarrer en mode test
./angel.sh start -p test

# 3. Ouvrir le dashboard
open http://localhost:8080/test-dashboard

# 4. Commencer à tester ! 🎉
```

**Le mode test vous permet de développer et tester Angel Virtual Assistant sans aucune dépendance externe !**
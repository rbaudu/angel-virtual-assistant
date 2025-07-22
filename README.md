# Angel Virtual Assistant

Un assistant virtuel intelligent qui propose des activités en fonction de la détection d'activités de l'utilisateur.

## Description

Angel Virtual Assistant est un système conçu pour accompagner les personnes dans leur quotidien en proposant des activités adaptées à leur contexte actuel. Le système utilise la détection d'activités fournie par Angel-server-capture pour comprendre ce que fait la personne à un moment donné, puis propose des activités appropriées via un avatar visuel.

## ⚡ Nouveautés v1.1.0

### 🌐 Interface Web Intégrée (Nouveau !)

**🎯 Accès web complet avec Spring Boot !**

L'application est maintenant dotée d'une interface web complète avec :

- 🖥️ **Dashboard de test interactif** : Interface complète de contrôle en mode test
- 🎭 **Interface Avatar Web** : Avatar accessible via navigateur web
- ⚙️ **Configuration centralisée** : Gestion unifiée des paramètres via fichiers `config/`
- 🔧 **Serveur Spring Boot intégré** : Plus besoin de serveur externe pour l'interface
- 📱 **Interface responsive** : Compatible desktop et mobile

### 🎮 Mode Test Intégré

**🎯 Développement et tests simplifiés !**

Le mode test permet de développer et tester l'assistant virtuel **sans dépendre du serveur dl4j-server-capture**. Il simule des activités en continu avec :

- 🎮 **Interface de contrôle web** : Dashboard complet accessible via navigateur
- 🎭 **Scénarios personnalisables** : Routines matinales, journées chargées, activités aléatoires
- ⚡ **Contrôle en temps réel** : Démarrage/arrêt, changement d'activité manuel
- 📊 **Statistiques détaillées** : Monitoring et logs en direct
- 🔄 **Basculement automatique** : Passe en mode test si le serveur principal est indisponible

### Démarrage rapide

```bash
# Cloner le projet
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant

# Démarrer en mode normal (avec interface web)
./angel-launcher.sh start

# Démarrer en mode test (sans dl4j-server-capture)
./angel-launcher.sh start -p test

# Accéder aux interfaces web
# Mode normal : http://localhost:8080/angel/test-dashboard
# Mode normal : http://localhost:8080/angel/
# Mode test :   http://localhost:8081/test-dashboard
# Mode test :   http://localhost:8081/angel
```

📚 **Documentation complète** : [docs/WEB_INTERFACE.md](docs/WEB_INTERFACE.md) | [docs/TEST_MODE.md](docs/TEST_MODE.md)

---

## Fonctionnalités principales

### Core
- **Détection d'activités** : Intégration avec Angel-server-capture pour détecter 27 types d'activités différentes
- **Propositions contextuelles** : Suggestions d'activités adaptées à l'activité actuelle de l'utilisateur
- **Avatar visuel** : Interface utilisateur basée sur un avatar avec visage humain
- **Activation vocale** : Système d'activation via le mot-clé \"Angel\"
- **Configuration flexible** : Paramétrage des fréquences et types de propositions
- **Historique intelligent** : Mémorisation des propositions pour éviter les répétitions
- **Préférences utilisateur** : Système de préférences personnalisables

### 🆕 Interface Web
- **🌐 Dashboard de test interactif** : Interface complète pour le mode test
- **🎭 Avatar Web** : Interface avatar accessible via navigateur
- **📊 Monitoring en temps réel** : Statistiques et logs en direct
- **⚙️ Configuration web** : Interface de configuration (à venir)
- **📱 Interface responsive** : Compatible tous écrans

### 🆕 Mode Test
- **🎮 Simulation intégrée** : Test sans dépendances externes
- **🎯 Scénarios personnalisables** : Routines et activités programmées
- **⚡ Contrôle temps réel** : Start/stop, changement d'activité manuel
- **📈 Analytics** : Statistiques d'usage et patterns

## URLs et Accès Web

### Mode Normal (Production)
- **Dashboard test** : http://localhost:8080/angel/test-dashboard
- **Avatar** : http://localhost:8080/angel/
- **Console H2** : http://localhost:8080/angel/h2-console
- **API** : http://localhost:8080/angel/api/

### Mode Test
- **Dashboard test** : http://localhost:8081/test-dashboard
- **Avatar** : http://localhost:8081/angel et http://localhost:8081/
- **Console H2** : http://localhost:8081/h2-console
- **API Test** : http://localhost:8081/api/test/

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

L'architecture du système est modulaire et implémentée avec **Spring Boot** :

### Composants principaux

1. **🆕 Module Core Spring Boot** (`com.angel.core`)
   - `SpringBootAngelApplication.java` : Point d'entrée Spring Boot
   - `AngelApplication.java` : Orchestration générale (composant Spring)

2. **Module Modèles** (`com.angel.model`)
   - `Activity.java` : Énumération des activités détectables
   - `ProposalHistory.java` : Historique des propositions
   - `UserProfile.java` : Profil et préférences utilisateur

3. **🆕 Module Configuration Spring** (`com.angel.config`)
   - `ConfigManager.java` : Gestion de la configuration centralisée
   - `WebConfig.java` : Configuration Spring MVC et ressources web
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

8. **🆕 Module Interface Web** (`com.angel.ui`)
   - `AvatarController.java` : Contrôle de l'avatar (service)
   - `AvatarWebController.java` : Contrôleur web pour l'avatar
   - `TestDashboardController.java` : Contrôleur web du dashboard de test

9. **Module Reconnaissance Vocale** (`com.angel.voice`)
   - `WakeWordDetector.java` : Détection du mot-clé \"Angel\"

10. **Module Utilitaires** (`com.angel.util`)
    - `LogUtil.java` : Gestion des logs
    - `DateTimeUtil.java` : Utilitaires de date/heure

11. **🆕 Module Avatar** (`com.angel.avatar`)
    - `AvatarManager.java` : Gestionnaire principal de l'avatar
    - `TextToSpeechService.java` : Service de synthèse vocale
    - `WebSocketService.java` : Communication WebSocket pour l'avatar
    - `EmotionAnalyzer.java` : Analyseur d'émotions

## Structure des fichiers

```
angel-virtual-assistant/
├── README.md
├── pom.xml
├── angel-launcher.sh               # Script de lancement Linux/macOS (Spring Boot)
├── angel-launcher.bat              # Script de lancement Windows (Spring Boot)
├── install-script.sh               # Script d'installation système
├── config/                         # 🆕 Configuration centralisée
│   ├── application.properties      # Configuration principale (mode normal)
│   ├── application-test.properties # Configuration mode test
│   └── test/
│       ├── test-mode-config.json   # Configuration détaillée mode test
│       └── activity-scenarios.json # Scénarios d'activités
├── docs/
│   ├── WEB_INTERFACE.md           # 🆕 Documentation interface web
│   ├── SPRING_BOOT_MIGRATION.md   # 🆕 Guide migration Spring Boot
│   └── TEST_MODE.md               # Documentation du mode test
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── angel/
│   │   │           ├── core/
│   │   │           │   ├── SpringBootAngelApplication.java  # 🆕 Point d'entrée Spring Boot
│   │   │           │   └── AngelApplication.java             # 🆕 Composant Spring
│   │   │           ├── config/
│   │   │           │   ├── ConfigManager.java
│   │   │           │   ├── WebConfig.java                   # 🆕 Configuration web
│   │   │           │   └── TestModeConfig.java
│   │   │           ├── ui/
│   │   │           │   ├── AvatarController.java            # Service avatar
│   │   │           │   ├── AvatarWebController.java         # 🆕 Contrôleur web avatar
│   │   │           │   └── TestDashboardController.java     # 🆕 Contrôleur web test
│   │   │           └── [autres modules...]
│   │   └── resources/
│   │       ├── static/                                      # 🆕 Ressources web statiques
│   │       │   ├── css/
│   │       │   │   ├── avatar.css
│   │       │   │   └── test-dashboard.css
│   │       │   ├── js/
│   │       │   │   ├── avatar.js
│   │       │   │   └── test-control.js
│   │       │   └── images/
│   │       │       └── [avatars et icônes]
│   │       └── templates/                                   # 🆕 Templates HTML
│   │           ├── avatar.html
│   │           ├── test-dashboard.html
│   │           └── test-help.html
│   └── test/
│       └── java/
└── logs/
```

## Prérequis

- Java 17 ou supérieur
- Maven 3.6 ou supérieur
- **En mode production** : Angel-server-capture en fonctionnement
- **En mode test** : Aucune dépendance externe 🎉
- **🆕 Pour l'interface web** : Navigateur moderne (Chrome, Firefox, Safari, Edge)

## Installation

### Installation rapide (Recommandée)

1. Cloner le dépôt :
```bash
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant
```

2. Rendre les scripts exécutables :
```bash
chmod +x angel-launcher.sh install-script.sh
```

3. **Installation système** (avec service systemd) :
```bash
sudo ./install-script.sh install
```

4. **Installation utilisateur** (sans service système) :
```bash
./install-script.sh install --user
```

### Installation manuelle

1. Cloner le dépôt :
```bash
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant
```

2. **🆕 Mettre à jour la configuration** :
   - Les fichiers `config/application.properties` et `config/application-test.properties` sont automatiquement mis à jour avec les propriétés Spring Boot nécessaires

3. **🆕 Ajouter la dépendance Thymeleaf** dans `pom.xml` :
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
    <version>${spring-boot.version}</version>
</dependency>
```

4. Compiler le projet :
```bash
mvn clean package
```

5. Exécuter l'application :
```bash
# Avec Spring Boot
java -jar target/angel-virtual-assistant-1.0.0-SNAPSHOT.jar

# Ou via le script
./angel-launcher.sh start
```

## Utilisation

### Mode Production (avec dl4j-server-capture)

#### Linux/macOS
```bash
# Démarrer l'application en mode production
./angel-launcher.sh start

# Démarrer en mode développement avec 1GB de RAM
./angel-launcher.sh start -p dev -m 1g

# Voir le statut avec URLs d'accès
./angel-launcher.sh status

# Tester l'interface web
./angel-launcher.sh test-web

# Voir les logs en temps réel
./angel-launcher.sh logs

# Arrêter l'application
./angel-launcher.sh stop
```

#### Windows
```batch
# Démarrer l'application
angel-launcher.bat start

# Démarrer en mode développement
angel-launcher.bat start -p dev -m 1g

# Voir le statut
angel-launcher.bat status

# Arrêter l'application
angel-launcher.bat stop
```

#### 🆕 Accès Web (Mode Normal)
Une fois démarré, accédez aux interfaces :
- **Dashboard test** : http://localhost:8080/angel/test-dashboard
- **Avatar** : http://localhost:8080/angel/
- **Console H2** : http://localhost:8080/angel/h2-console

### 🆕 Mode Test (sans dl4j-server-capture)

#### Démarrage rapide
```bash
# Démarrer en mode test
./angel-launcher.sh start -p test

# Avec mode daemon
./angel-launcher.sh start -p test -b

# Avec debug
./angel-launcher.sh start -p test -d

# Avec mémoire personnalisée
./angel-launcher.sh start -p test -m 1g
```

#### Windows
```batch
# Démarrer en mode test
angel-launcher.bat start -p test

# Voir le statut
angel-launcher.bat status

# Arrêter
angel-launcher.bat stop
```

#### 🆕 Interface web de test

1. **Accéder au dashboard** :
   ```
   http://localhost:8081/test-dashboard
   ```

2. **Accéder à l'avatar** :
   ```
   http://localhost:8081/angel
   http://localhost:8081/          # Route alternative
   ```

3. **Fonctionnalités disponibles** :
   - 🎮 Contrôles de simulation (start/stop)
   - 🎯 Définition manuelle d'activités
   - 🎭 Chargement de scénarios prédéfinis
   - 📊 Statistiques en temps réel
   - 📝 Journal d'activité en direct
   - 🎭 Avatar interactif

#### API de test

```bash
# Vérifier l'état du mode test
curl http://localhost:8081/api/test/health

# Obtenir l'activité courante
curl http://localhost:8081/api/test/activity/current

# Définir une activité manuellement
curl -X POST http://localhost:8081/api/test/activity/set \
     -H \"Content-Type: application/json\" \
     -d '{\"activity\": \"READING\", \"confidence\": 0.85}'

# Démarrer la simulation
curl -X POST http://localhost:8081/api/test/simulation/start

# Charger un scénario
curl -X POST http://localhost:8081/api/test/scenario/load/morning_routine
```

### Configuration du mode test

#### Configuration centralisée

Les configurations sont dans le dossier `config/` :

**`config/application.properties`** (mode normal) :
```properties
# Configuration serveur
server.port=8080
server.servlet.context-path=/angel

# Configuration Spring Boot (ajout automatique)
spring.application.name=Angel Virtual Assistant
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html
angel.test.dashboard.enabled=true
```

**`config/application-test.properties`** (mode test) :
```properties
# Configuration serveur test
server.port=8081
server.servlet.context-path=/

# Mode test activé
angel.test.enabled=true
angel.test.auto-start=true
```

#### Scénarios personnalisés

Éditer `config/test/activity-scenarios.json` :

```json
{
  \"scenarios\": {
    \"my_scenario\": {
      \"name\": \"Mon Scénario Personnalisé\",
      \"description\": \"Description de mon scénario\",
      \"activities\": [
        {
          \"activity\": \"WAKING_UP\",
          \"duration\": 120000,
          \"confidence\": 0.9,
          \"description\": \"Se réveiller\"
        },
        {
          \"activity\": \"EATING\",
          \"duration\": 300000,
          \"confidence\": 0.85,
          \"description\": \"Petit déjeuner\"
        }
      ]
    }
  }
}
```

### Profils d'exécution

Le système supporte plusieurs profils avec configuration centralisée :

- **prod** : Mode production (avec dl4j-server-capture) - port 8080, context /angel
- **test** : Mode test (simulation intégrée) - port 8081, context /
- **dev** : Mode développement (logs détaillés, hot reload)
- **default** : Mode par défaut

### 🆕 Interface Web - Guide d'utilisation

#### Dashboard de Test
1. **Navigation** : Menu latéral avec sections organisées
2. **Contrôles de simulation** : Start/stop, vitesse, randomisation
3. **Sélection d'activité** : Liste déroulante avec toutes les activités
4. **Scénarios** : Chargement de routines prédéfinies
5. **Statistiques** : Graphiques en temps réel
6. **Logs** : Affichage en direct des événements

#### Interface Avatar
1. **Avatar 3D** : Rendu en temps réel avec animations
2. **Contrôles** : Play/pause, volume, mode d'affichage
3. **Chat** : Interface de conversation (à venir)
4. **Paramètres** : Configuration de l'apparence

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
sudo ./install-script.sh service start
sudo ./install-script.sh service enable
sudo ./install-script.sh service status
```

### Options avancées

```bash
# Démarrer avec un profil spécifique
./angel-launcher.sh start -p test

# Démarrer avec plus de mémoire
./angel-launcher.sh start -m 2g

# Démarrer en mode debug sur un port spécifique
./angel-launcher.sh start -d -D 8000

# Mode verbose pour le débogage
./angel-launcher.sh start -v

# Compilation et tests
./angel-launcher.sh build
./angel-launcher.sh test
./angel-launcher.sh clean

# 🆕 Test de l'interface web
./angel-launcher.sh test-web
```

## Configuration

### 🆕 Configuration centralisée

La configuration utilise maintenant des fichiers dans le dossier `config/` :

- **`config/application.properties`** : Configuration principale (mode normal)
- **`config/application-test.properties`** : Configuration mode test
- **`config/test/test-mode-config.json`** : Configuration détaillée du mode test
- **`config/test/activity-scenarios.json`** : Scénarios d'activités

### Configuration Spring Boot

Les propriétés Spring Boot sont automatiquement ajoutées aux fichiers de configuration :

```properties
# Configuration Spring Boot (ajoutée automatiquement)
spring.application.name=Angel Virtual Assistant
spring.main.allow-bean-definition-overriding=true
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html
spring.thymeleaf.cache=false
angel.test.dashboard.enabled=true
```

### Exemple de configuration des propositions :

```properties
# Propositions quotidiennes - News
proposals.daily.news.max-per-day=5
proposals.daily.news.min-time-between=7200000
proposals.daily.news.sources=local,national,international

# Propositions quotidiennes - Météo
proposals.daily.weather.max-per-day=3
proposals.daily.weather.min-time-between=14400000
proposals.daily.weather.include-today=true

# Mapping des activités
proposals.activity-mapping.eating=news,weather,reminders.medications,conversations
proposals.activity-mapping.waiting=news,weather,stories,conversations,games,media
```

## Maintenance

### Mise à jour du système

```bash
# Mise à jour automatique (garde la configuration)
sudo ./install-script.sh update

# Ou manuellement
git pull
./angel-launcher.sh stop
./angel-launcher.sh build
./angel-launcher.sh start
```

### Basculement entre modes

```bash
# Passer en mode test
./angel-launcher.sh stop
./angel-launcher.sh start -p test

# Retour en mode production
./angel-launcher.sh stop
./angel-launcher.sh start

# Vérifier les URLs actuelles
./angel-launcher.sh status
```

### Désinstallation

```bash
# Désinstallation complète
sudo ./install-script.sh uninstall

# Ou arrêt simple
./angel-launcher.sh stop
```

### Surveillance et logs

```bash
# Voir les logs en temps réel
./angel-launcher.sh logs

# Voir les logs système (si installé en service)
sudo journalctl -u angel-virtual-assistant -f

# Vérifier l'état des processus et URLs
./angel-launcher.sh status

# 🆕 Tester l'interface web
./angel-launcher.sh test-web

# 🆕 Logs spécifiques au mode test
tail -f logs/angel.log | grep -i test
```

## Extensibilité

Le système est conçu pour être facilement extensible :

### Ajouter un nouveau type de proposition :

1. Créer une classe implémentant `Proposal`
2. L'ajouter dans `createAvailableProposals()` de `AngelApplication`
3. Configurer les paramètres dans `config/application.properties`

### 🆕 Ajouter une nouvelle page web :

1. Créer un contrôleur Spring dans `com.angel.ui`
2. Ajouter le template HTML dans `src/main/resources/templates/`
3. Ajouter les ressources CSS/JS dans `src/main/resources/static/`

### 🆕 Ajouter un nouveau scénario de test :

1. Éditer `config/test/activity-scenarios.json`
2. Ajouter votre scénario avec les activités désirées
3. Recharger via l'API ou redémarrer l'application

### Exemple d'implémentation :

```java
@Controller
@RequestMapping(\"/my-page\")
public class MyPageController {
    
    @GetMapping
    public String showMyPage(Model model) {
        model.addAttribute(\"title\", \"Ma Page\");
        return \"my-page\";
    }
}
```

## Base de données

Le système utilise une base de données H2 intégrée avec les tables :

- `proposal_history` : Historique des propositions faites
- `user_preferences` : Préférences utilisateur
- `activities` : Cache local des activités détectées
- **🆕** `test_sessions` : Sessions de test et statistiques

**🆕 Console H2 accessible via web** :
- Mode normal : http://localhost:8080/angel/h2-console
- Mode test : http://localhost:8081/h2-console

## Logging

Les logs sont configurés avec plusieurs niveaux :
- **INFO** : Informations générales de fonctionnement
- **WARNING** : Avertissements et erreurs récupérables
- **SEVERE** : Erreurs critiques
- **🆕 DEBUG** : Logs détaillés du mode test et Spring Boot

Fichiers de log dans `./logs/angel.log` avec rotation automatique.

## Dépannage

### Problèmes courants

#### L'application ne démarre pas
```bash
# Vérifier les prérequis
java -version
mvn -version

# Vérifier les logs
./angel-launcher.sh logs

# Recompiler si nécessaire
./angel-launcher.sh clean
./angel-launcher.sh build
```

#### 🆕 Interface web inaccessible
```bash
# Vérifier que l'application est démarrée
./angel-launcher.sh status

# Tester l'accès web
./angel-launcher.sh test-web

# Vérifier la configuration
grep -E \"server.port|server.servlet.context-path\" config/application*.properties

# Vérifier les logs Spring Boot
tail -f logs/angel.log | grep -i \"tomcat\\|spring\"
```

#### Impossible de se connecter à Angel-server-capture
```bash
# Vérifier que le serveur est démarré
curl http://localhost:8080/api/health

# 🆕 Basculer en mode test temporairement
./angel-launcher.sh stop
./angel-launcher.sh start -p test

# Vérifier la nouvelle URL : http://localhost:8081/test-dashboard
```

#### 🆕 Problèmes avec le mode test
```bash
# Vérifier l'état du mode test
curl http://localhost:8081/api/test/health

# Vérifier la configuration
cat config/application-test.properties | grep test

# Redémarrer la simulation
curl -X POST http://localhost:8081/api/test/simulation/stop
curl -X POST http://localhost:8081/api/test/simulation/start

# Vérifier les scénarios
curl http://localhost:8081/api/test/scenarios
```

#### Dashboard de test inaccessible
```bash
# Vérifier que le mode test est activé
grep \"angel.test.enabled=true\" config/application-test.properties

# Tester l'accès direct selon le mode
# Mode normal : curl http://localhost:8080/angel/test-dashboard
# Mode test :   curl http://localhost:8081/test-dashboard

# Vérifier les logs du serveur web
tail -f logs/angel.log | grep -i \"dashboard\\|thymeleaf\"
```

#### 🆕 Problèmes Spring Boot
```bash
# Vérifier les propriétés Spring Boot dans la configuration
grep -E \"spring\\.|server\\.\" config/application*.properties

# Vérifier les dépendances
mvn dependency:tree | grep spring

# Forcer la recompilation
./angel-launcher.sh clean
./angel-launcher.sh build
```

### Support et logs

En cas de problème, consultez les logs :
- Application : `./logs/angel.log`
- Service système : `sudo journalctl -u angel-virtual-assistant`
- Sortie daemon : `./logs/angel.out` et `./logs/angel.err`
- **🆕 Logs Spring Boot** : Inclus dans `./logs/angel.log` avec préfixe `[SPRING]`

## Tests

Exécuter les tests unitaires :
```bash
# Avec le script
./angel-launcher.sh test

# Ou avec Maven
mvn test

# 🆕 Tests spécifiques au mode test
mvn test -Dtest=\"*Test*\"

# 🆕 Tests d'intégration Spring Boot
mvn test -Dtest=\"*ControllerTest\"
```

## Contribution

1. Fork le projet
2. Créer une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`)
3. Committer vos changements (`git commit -m 'Add AmazingFeature'`)
4. Pousser vers la branche (`git push origin feature/AmazingFeature`)
5. Créer une Pull Request

### 🆕 Guidelines pour l'interface web
- Utiliser les conventions Spring Boot
- Templates HTML dans `src/main/resources/templates/`
- CSS/JS dans `src/main/resources/static/`
- Suivre le pattern MVC avec contrôleurs dans `com.angel.ui`

## Licence

À définir.

## Roadmap

### Version 1.1.0 (Actuelle)
- [x] **🆕 Interface web intégrée** avec Spring Boot
- [x] **🆕 Dashboard de test interactif**
- [x] **🆕 Avatar accessible via web**
- [x] **🆕 Configuration centralisée** dans dossier `config/`
- [x] **🆕 Serveur Spring Boot intégré**
- [x] **🆕 Mode test** avec simulation d'activités
- [x] **🆕 Scénarios personnalisables** d'activités
- [x] **🆕 API REST** pour contrôle programmatique

### Version 1.2.0 (Prochaine)
- [ ] **🔧 Interface de configuration web** : Édition des paramètres via web
- [ ] **💬 Chat interactif** : Interface de conversation avec l'avatar
- [ ] **📱 Interface mobile** : Optimisation pour smartphones/tablettes
- [ ] **🔐 Authentification** : Système de connexion et profils utilisateurs
- [ ] **🎨 Thèmes personnalisables** : Interface adaptable

### Version 1.3.0 et plus
- [ ] Implémentation des propositions manquantes (News, Stories, Games, etc.)
- [ ] Intégration reconnaissance vocale avancée via web
- [ ] Support multi-utilisateurs
- [ ] Intégration services externes (météo, actualités)
- [ ] Application mobile companion
- [ ] Système de plugins
- [ ] Support Docker/containerisation
- [ ] Monitoring et métriques avancées
- [ ] **🔄 Mode hybride** : basculement automatique production/test
- [ ] **📊 Analytics** : statistiques d'usage et patterns d'activités
- [ ] **🎯 IA améliorée** : apprentissage des préférences utilisateur

---

## 🚀 Démarrage Rapide

**Pour commencer immédiatement avec l'interface web :**

```bash
# 1. Cloner et compiler
git clone https://github.com/rbaudu/angel-virtual-assistant.git
cd angel-virtual-assistant
mvn clean package

# 2. Démarrer en mode test (recommandé pour débuter)
./angel-launcher.sh start -p test

# 3. Ouvrir les interfaces web
open http://localhost:8081/test-dashboard  # Dashboard de test
open http://localhost:8081/angel          # Interface avatar

# 4. Ou démarrer en mode normal (nécessite angel-server-capture)
./angel-launcher.sh start
open http://localhost:8080/angel/test-dashboard
open http://localhost:8080/angel/

# 5. Commencer à explorer ! 🎉
```

**L'interface web vous permet maintenant d'interagir complètement avec Angel Virtual Assistant via votre navigateur !**
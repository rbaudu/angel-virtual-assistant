# Angel Virtual Assistant

Un assistant virtuel intelligent qui propose des activités en fonction de la détection d'activités de l'utilisateur.

## Description

Angel Virtual Assistant est un système conçu pour accompagner les personnes dans leur quotidien en proposant des activités adaptées à leur contexte actuel. Le système utilise la détection d'activités fournie par Angel-server-capture pour comprendre ce que fait la personne à un moment donné, puis propose des activités appropriées via un avatar visuel.

## Fonctionnalités principales

- **Détection d'activités** : Intégration avec Angel-server-capture pour détecter 27 types d'activités différentes
- **Propositions contextuelles** : Suggestions d'activités adaptées à l'activité actuelle de l'utilisateur
- **Avatar visuel** : Interface utilisateur basée sur un avatar avec visage humain
- **Activation vocale** : Système d'activation via le mot-clé "Angel"
- **Configuration flexible** : Paramétrage des fréquences et types de propositions
- **Historique intelligent** : Mémorisation des propositions pour éviter les répétitions
- **Préférences utilisateur** : Système de préférences personnalisables

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

4. **Module API** (`com.angel.api`)
   - `AngelServerClient.java` : Communication avec Angel-server-capture
   - `dto/ActivityDTO.java` : Objets de transfert de données

5. **Module Intelligence** (`com.angel.intelligence`)
   - `ProposalEngine.java` : Moteur de décision pour les propositions
   - `proposals/Proposal.java` : Interface des propositions
   - `proposals/WeatherProposal.java` : Exemple de proposition météo

6. **Module Persistance** (`com.angel.persistence`)
   - `DatabaseManager.java` : Gestion de la base de données H2
   - `dao/ProposalDAO.java` : Accès aux données des propositions
   - `dao/UserPreferenceDAO.java` : Accès aux préférences utilisateur

7. **Module Interface Utilisateur** (`com.angel.ui`)
   - `AvatarController.java` : Contrôle de l'avatar visuel

8. **Module Reconnaissance Vocale** (`com.angel.voice`)
   - `WakeWordDetector.java` : Détection du mot-clé "Angel"

9. **Module Utilitaires** (`com.angel.util`)
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
│   └── angel-config.json
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
│   │   │           │   └── ConfigManager.java
│   │   │           ├── api/
│   │   │           │   ├── AngelServerClient.java
│   │   │           │   └── dto/
│   │   │           │       └── ActivityDTO.java
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
│   │   │           │   └── AvatarController.java
│   │   │           ├── voice/
│   │   │           │   └── WakeWordDetector.java
│   │   │           └── util/
│   │   │               ├── LogUtil.java
│   │   │               └── DateTimeUtil.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── assets/
│   │       └── templates/
│   └── test/
│       └── java/
└── logs/
```

## Prérequis

- Java 17 ou supérieur
- Maven 3.6 ou supérieur
- Angel-server-capture en fonctionnement (pour la détection d'activités)

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
   - Configurer l'URL du serveur Angel-server-capture

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

### Avec les scripts de lancement (Recommandé)

#### Linux/macOS
```bash
# Démarrer l'application
./angel.sh start

# Démarrer en mode développement avec 1GB de RAM
./angel.sh start -p dev -m 1g

# Démarrer en mode debug
./angel.sh start -d

# Démarrer en mode daemon (arrière-plan)
./angel.sh start -b

# Voir le statut
./angel.sh status

# Voir les logs en temps réel
./angel.sh logs

# Arrêter l'application
./angel.sh stop

# Redémarrer avec configuration production
./angel.sh restart -p prod -m 2g

# Voir l'aide complète
./angel.sh help
```

#### Windows
```batch
# Démarrer l'application
angel.bat start

# Démarrer en mode développement
angel.bat start -p dev -m 1g

# Voir le statut
angel.bat status

# Arrêter l'application
angel.bat stop

# Voir l'aide
angel.bat help
```

### Profils d'exécution

Le système supporte plusieurs profils :

- **dev** : Mode développement (logs détaillés, hot reload)
- **prod** : Mode production (logs optimisés, performances)
- **test** : Mode test (base de données en mémoire)
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

### Options avancées

```bash
# Démarrer avec un fichier de configuration personnalisé
./angel.sh start -c /path/to/custom-config.json

# Démarrer avec plus de mémoire
./angel.sh start -m 2g

# Démarrer en mode debug sur un port spécifique
./angel.sh start -d -D 8000

# Mode verbose pour le débogage
./angel.sh start -v

# Compilation et tests
./angel.sh build
./angel.sh test
./angel.sh clean
```

## Configuration

Le fichier `config/angel-config.json` permet de configurer :

- **Système** : Langue, mot-clé d'activation
- **API** : URL du serveur Angel-capture, timeouts
- **Avatar** : Paramètres d'affichage et d'animation
- **Propositions** : Fréquences, types autorisés par activité
- **Base de données** : Configuration H2
- **Logging** : Niveaux et fichiers de log

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
```

## Extensibilité

Le système est conçu pour être facilement extensible :

### Ajouter un nouveau type de proposition :

1. Créer une classe implémentant `Proposal`
2. L'ajouter dans `createAvailableProposals()` de `AngelApplication`
3. Configurer les paramètres dans `angel-config.json`

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

## Logging

Les logs sont configurés avec plusieurs niveaux :
- **INFO** : Informations générales de fonctionnement
- **WARNING** : Avertissements et erreurs récupérables
- **SEVERE** : Erreurs critiques

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

# Ou modifier temporairement la configuration
# dans config/angel-config.json
```

#### Problème de permissions
```bash
# Réinstaller avec les bonnes permissions
sudo ./install.sh uninstall
sudo ./install.sh install
```

#### Problème de mémoire
```bash
# Augmenter la mémoire allouée
./angel.sh start -m 2g
```

### Support et logs

En cas de problème, consultez les logs :
- Application : `./logs/angel.log`
- Service système : `sudo journalctl -u angel-virtual-assistant`
- Sortie daemon : `./logs/angel.out` et `./logs/angel.err`

## Tests

Exécuter les tests unitaires :
```bash
# Avec le script
./angel.sh test

# Ou avec Maven
mvn test
```

## Contribution

1. Fork le projet
2. Créer une branche pour votre fonctionnalité
3. Committer vos changements
4. Pousser vers la branche
5. Créer une Pull Request

## Licence

À définir.

## Roadmap

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
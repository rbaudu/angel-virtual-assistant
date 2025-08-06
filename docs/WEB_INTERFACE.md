# Interface Web - Angel Virtual Assistant

Guide de l'interface web basée sur Spring Boot pour l'accès navigateur à l'assistant virtuel.

## 🌐 Vue d'Ensemble

L'interface web permet d'accéder à Angel Virtual Assistant via navigateur avec :

- **Interface Avatar** : Page principale d'interaction avec l'avatar
- **Dashboard Test** : Contrôles complets pour le mode test
- **API REST** : Endpoints pour intégration et contrôle programmatique
- **WebSocket** : Communication temps réel avec l'avatar
- **Design Responsive** : Compatible desktop, tablette, mobile

## 🏗️ Architecture Web

### Stack Technique
- **Backend** : Spring Boot 3.2+ avec Spring MVC
- **Templates** : Thymeleaf pour rendu HTML
- **Frontend** : HTML5, CSS3, JavaScript ES6+
- **Communication** : WebSocket + REST API
- **Ressources** : Organisation modulaire par fonctionnalité

### Structure des Ressources
```
src/main/resources/
├── templates/
│   ├── avatar.html              # Page principale avatar
│   ├── test-dashboard.html      # Dashboard mode test  
│   └── fragments/               # Composants réutilisables
├── static/
│   ├── css/
│   │   ├── avatar.css          # Styles interface avatar
│   │   ├── test-dashboard.css  # Styles dashboard
│   │   └── common.css          # Styles partagés
│   ├── js/
│   │   ├── avatar/             # Scripts avatar 3D
│   │   ├── voice/              # Scripts reconnaissance vocale
│   │   ├── test-control.js     # Contrôles mode test
│   │   └── speech-recognition.js
│   └── assets/                 # Images, icônes, modèles
└── config/
    └── avatar.properties       # Config avatar par défaut
```

## 🌍 URLs et Accès

### Mode Normal (avec Angel-server-capture)
- **Port** : 8080
- **Context** : `/angel`
- **Avatar** : http://localhost:8080/angel
- **Dashboard Test** : http://localhost:8080/angel/test-dashboard
- **API** : http://localhost:8080/angel/api/
- **Console H2** : http://localhost:8080/angel/h2-console

### Mode Test (autonome)
- **Port** : 8081  
- **Context** : `/` (racine)
- **Avatar** : http://localhost:8081/angel
- **Dashboard Test** : http://localhost:8081/test-dashboard
- **API Test** : http://localhost:8081/api/test/
- **Console H2** : http://localhost:8081/h2-console

## 📱 Interface Avatar

### Page Principale (`/angel`)
Page d'interaction principale avec l'avatar :

```html
<!-- Structure simplifiée -->
<div id="avatar-container">
  <!-- Rendu 3D de l'avatar -->
  <canvas id="avatar-canvas"></canvas>
  
  <!-- Contrôles utilisateur -->
  <div class="avatar-controls">
    <button id="mute-btn">🔊</button>
    <button id="settings-btn">⚙️</button>
    <button id="fullscreen-btn">⛶</button>
  </div>
  
  <!-- Indicateurs d'état -->
  <div class="status-indicators">
    <div id="listening-indicator">🎤</div>
    <div id="speaking-indicator">💬</div>
  </div>
</div>
```

### Fonctionnalités Interface
- **Affichage avatar 3D** : Rendu Three.js temps réel
- **Contrôles audio** : Volume, mute, configuration voix
- **Mode plein écran** : Expérience immersive
- **Indicateurs visuels** : États écoute/parole
- **Masquage automatique** : Contrôles disparaissent après inactivité

## 🎮 Dashboard de Test

### Sections Principales

#### 1. Contrôles de Simulation
```html
<div class="simulation-controls">
  <button id="start-sim">▶️ Start</button>
  <button id="stop-sim">⏹️ Stop</button>
  <input type="range" id="speed-slider" min="1" max="10" value="1">
  <input type="range" id="randomness-slider" min="0" max="100" value="30">
</div>
```

#### 2. Sélection d'Activité
```html
<div class="activity-selector">
  <select id="activity-select">
    <option value="EATING">Eating</option>
    <option value="READING">Reading</option>
    <option value="WATCHING_TV">Watching TV</option>
    <!-- ... 24 autres activités -->
  </select>
  <input type="range" id="confidence-slider" min="0" max="100" value="85">
  <button id="apply-activity">Apply</button>
</div>
```

#### 3. Journal d'Activité
```html
<div class="activity-log">
  <div class="log-filters">
    <button class="filter-btn active" data-filter="all">All</button>
    <button class="filter-btn" data-filter="activity">Activities</button>
    <button class="filter-btn" data-filter="proposal">Proposals</button>
  </div>
  <div id="log-entries"></div>
</div>
```

#### 4. Statistiques Temps Réel
```html
<div class="stats-dashboard">
  <div class="stat-card">
    <h3>Activities Today</h3>
    <span class="stat-value" id="activities-count">0</span>
  </div>
  <div class="stat-card">
    <h3>Proposals Generated</h3>
    <span class="stat-value" id="proposals-count">0</span>
  </div>
</div>
```

## 🔌 API REST

### Endpoints Avatar
```http
# Status et contrôle avatar
GET    /api/avatar/status           # État actuel avatar
POST   /api/avatar/speak            # Faire parler avatar
POST   /api/avatar/emotion          # Changer émotion
GET    /api/avatar/config           # Configuration avatar
PUT    /api/avatar/config           # Modifier configuration

# Exemple requête
POST /api/avatar/speak
Content-Type: application/json
{
  "text": "Bonjour ! Comment allez-vous ?",
  "emotion": "friendly",
  "priority": "high"
}
```

### Endpoints Mode Test
```http
# Simulation
GET    /api/test/health             # Santé du système test
POST   /api/test/simulation/start   # Démarrer simulation
POST   /api/test/simulation/stop    # Arrêter simulation
GET    /api/test/simulation/status  # État simulation

# Activités
GET    /api/test/activity/current   # Activité courante
POST   /api/test/activity/set       # Définir activité
GET    /api/test/activity/history   # Historique activités

# Scénarios
GET    /api/test/scenarios          # Liste scénarios
POST   /api/test/scenario/load      # Charger scénario
POST   /api/test/scenario/create    # Créer scénario

# Statistiques
GET    /api/test/stats/current      # Stats temps réel
GET    /api/test/stats/export       # Export données
```

### Exemples d'Utilisation API
```bash
# Démarrer simulation test
curl -X POST http://localhost:8081/api/test/simulation/start

# Définir activité READING avec 85% confiance
curl -X POST http://localhost:8081/api/test/activity/set \
  -H "Content-Type: application/json" \
  -d '{
    "activity": "READING",
    "confidence": 0.85,
    "duration": 1800000
  }'

# Charger scénario routine matinale
curl -X POST http://localhost:8081/api/test/scenario/load \
  -H "Content-Type: application/json" \
  -d '{"name": "morning_routine"}'

# Obtenir statistiques actuelles
curl http://localhost:8081/api/test/stats/current
```

## 🔄 Communication WebSocket

### WebSocket Avatar
```javascript
// Connexion WebSocket avatar
const avatarWs = new WebSocket('ws://localhost:8081/ws/avatar');

// Messages entrants
avatarWs.onmessage = function(event) {
    const message = JSON.parse(event.data);
    
    switch(message.type) {
        case 'speak':
            // Avatar commence à parler
            handleAvatarSpeech(message.text, message.emotion);
            break;
        case 'emotion_change':
            // Changement d'émotion avatar
            updateAvatarEmotion(message.emotion, message.intensity);
            break;
        case 'wake_word_detected':
            // Mot-clé "Angel" détecté
            activateListeningMode();
            break;
    }
};

// Messages sortants
function sendToAvatar(type, data) {
    avatarWs.send(JSON.stringify({
        type: type,
        timestamp: Date.now(),
        ...data
    }));
}
```

### WebSocket Test Dashboard
```javascript
// Connexion dashboard test
const testWs = new WebSocket('ws://localhost:8081/ws/test');

// Écoute mises à jour temps réel
testWs.onmessage = function(event) {
    const update = JSON.parse(event.data);
    
    switch(update.type) {
        case 'activity_change':
            updateActivityDisplay(update.activity);
            addLogEntry('activity', `Activity changed to ${update.activity.type}`);
            break;
        case 'proposal_generated':
            updateProposalsCount();
            addLogEntry('proposal', update.proposal.text);
            break;
        case 'simulation_stats':
            updateStatsDashboard(update.stats);
            break;
    }
};
```

## 📱 Design Responsive

### Breakpoints
```css
/* Configuration responsive */
.avatar-container {
    width: 100%;
    height: 400px;
}

/* Tablette */
@media (max-width: 1024px) {
    .avatar-container {
        height: 350px;
    }
    
    .dashboard-sidebar {
        transform: translateX(-100%);
    }
    
    .dashboard-sidebar.open {
        transform: translateX(0);
    }
}

/* Mobile */
@media (max-width: 768px) {
    .avatar-container {
        height: 300px;
    }
    
    .avatar-controls {
        position: fixed;
        bottom: 20px;
        right: 20px;
    }
    
    .dashboard-layout {
        flex-direction: column;
    }
}
```

### Adaptations Mobile
- **Menu hamburger** pour navigation dashboard
- **Contrôles tactiles** optimisés
- **Cartes empilées** verticalement
- **Gestures** pinch-to-zoom pour avatar
- **Mode portrait** automatique

## ⚡ Performance et Optimisation

### Optimisations Frontend
```javascript
// Lazy loading des modules
const loadAvatarModule = async () => {
    const { AvatarRenderer } = await import('./avatar/avatar-renderer.js');
    return new AvatarRenderer();
};

// Debounce pour les contrôles
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Application sur les sliders
const updateSpeed = debounce((value) => {
    fetch('/api/test/simulation/speed', {
        method: 'PUT',
        body: JSON.stringify({ speed: value }),
        headers: { 'Content-Type': 'application/json' }
    });
}, 300);
```

### Configuration Spring Boot
```properties
# Compression des ressources
server.compression.enabled=true
server.compression.mime-types=text/html,text/css,application/javascript
server.compression.min-response-size=1024

# Cache des ressources statiques
spring.web.resources.cache.cachecontrol.max-age=31536000
spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**

# Optimisation WebSocket
server.tomcat.websocket.max-idle-timeout=300000
server.tomcat.websocket.buffer-size=8192
```

## 🔒 Sécurité Web

### Configuration CORS
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### Protection CSRF
```properties
# Configuration sécurité
spring.security.csrf.csrf-token-repository=cookie
angel.web.cors.allowed-origins=http://localhost:3000,http://localhost:8080
angel.web.security.api-key-required=false
```

## 🧪 Tests Interface Web

### Tests Fonctionnels
```bash
# Test complet interface web
./angel-launcher.sh test-web

# Test spécifique avatar
./angel-launcher.sh test-avatar-web

# Test dashboard en mode test
./angel-launcher.sh start -p test
# Puis naviguer vers http://localhost:8081/test-dashboard
```

### Tests JavaScript Console
```javascript
// Test WebSocket avatar
if (window.avatarWs && window.avatarWs.readyState === WebSocket.OPEN) {
    console.log('✅ WebSocket Avatar connecté');
} else {
    console.log('❌ WebSocket Avatar déconnecté');
}

// Test API REST
fetch('/api/avatar/status')
    .then(response => response.json())
    .then(data => console.log('Avatar Status:', data))
    .catch(error => console.error('API Error:', error));

// Test reconnaissance vocale
if ('webkitSpeechRecognition' in window) {
    console.log('✅ Speech Recognition supporté');
} else {
    console.log('❌ Speech Recognition non supporté');
}
```

## 🔍 Dépannage Interface Web

### Problèmes Courants

#### Interface avatar ne charge pas
```bash
# Vérifier le serveur Spring Boot
curl -I http://localhost:8081/angel

# Vérifier les logs
grep -i "thymeleaf\|template" logs/angel.log

# Tester avec un navigateur différent
```

#### WebSocket ne se connecte pas
```javascript
// Debug WebSocket dans la console
const testWs = new WebSocket('ws://localhost:8081/ws/avatar');
testWs.onopen = () => console.log('WebSocket ouvert');
testWs.onerror = (error) => console.error('WebSocket erreur:', error);
testWs.onclose = (event) => console.log('WebSocket fermé:', event.code);
```

#### Dashboard test ne répond pas
```bash
# Vérifier le mode test
grep "angel.test.enabled=true" config/application-test.properties

# Vérifier les endpoints API
curl http://localhost:8081/api/test/health

# Redémarrer en mode debug
./angel-launcher.sh start -p test -d
```

#### Ressources statiques 404
```bash
# Vérifier la structure des ressources
ls -la src/main/resources/static/

# Vérifier la configuration Spring
grep "spring.web.resources" config/application*.properties

# Test direct d'une ressource
curl -I http://localhost:8081/css/avatar.css
```

## 📊 Monitoring Interface Web

### Métriques Spring Boot Actuator
```properties
# Activation des endpoints de monitoring
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

### Logs Utiles
```bash
# Logs spécifiques interface web
tail -f logs/angel.log | grep -E "(HTTP|WebSocket|Thymeleaf)"

# Logs erreurs JavaScript (dans navigateur)
# F12 → Console → Voir erreurs JS

# Logs performances
tail -f logs/angel.log | grep -E "(slow|timeout|performance)"
```

## 🚀 Déploiement Interface Web

### Configuration Production
```properties
# Production settings
server.port=8080
server.servlet.context-path=/angel
server.ssl.enabled=true

# Optimisations production
spring.thymeleaf.cache=true
spring.web.resources.cache.period=86400
logging.level.root=WARN
```

### Reverse Proxy Nginx
```nginx
server {
    listen 80;
    server_name angel.example.com;
    
    # Interface web principale
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # WebSocket
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    
    # Ressources statiques avec cache
    location ~* \.(css|js|png|jpg|ico)$ {
        proxy_pass http://localhost:8080;
        expires 1y;
        add_header Cache-Control "public";
    }
}
```

---

L'interface web d'Angel Virtual Assistant offre une expérience utilisateur complète et moderne pour interagir avec l'assistant virtuel, que ce soit en mode normal ou en mode test avec contrôles avancés.
# Interface Web Angel Virtual Assistant

Guide complet de l'interface web intégrée avec Spring Boot.

## Vue d'ensemble

Angel Virtual Assistant dispose maintenant d'une interface web complète basée sur Spring Boot qui permet :

- 🎭 **Interface Avatar** : Interaction avec l'avatar via navigateur web
- 🎮 **Dashboard de Test** : Contrôle complet du mode test
- ⚙️ **Configuration Web** : Gestion des paramètres (à venir)
- 📊 **Monitoring** : Statistiques et logs en temps réel
- 📱 **Interface responsive** : Compatible desktop et mobile

## Architecture Web

### Stack Technique

- **Backend** : Spring Boot 3.2.1 avec Spring MVC
- **Templates** : Thymeleaf pour le rendu HTML
- **Frontend** : HTML5, CSS3, JavaScript vanilla
- **WebSocket** : Communication temps réel avec l'avatar
- **API REST** : Endpoints pour contrôle programmatique

### Structure des Ressources

```
src/main/resources/
├── static/                     # Ressources statiques
│   ├── css/
│   │   ├── avatar.css         # Styles de l'interface avatar
│   │   ├── test-dashboard.css # Styles du dashboard test
│   │   └── common.css         # Styles communs
│   ├── js/
│   │   ├── avatar.js          # Logique client avatar
│   │   ├── test-control.js    # Contrôles du mode test
│   │   └── websocket.js       # Communication WebSocket
│   └── images/
│       ├── avatars/           # Images d'avatars
│       └── icons/             # Icônes de l'interface
└── templates/                 # Templates Thymeleaf
    ├── avatar.html           # Page principale avatar
    ├── test-dashboard.html   # Dashboard de test
    ├── test-help.html        # Aide du mode test
    └── layout/
        └── main.html         # Layout principal
```

## URLs d'Accès

### Mode Normal (Production)
- **Port** : 8080
- **Context-path** : `/angel`
- **Avatar** : http://localhost:8080/angel/
- **Dashboard Test** : http://localhost:8080/angel/test-dashboard
- **Console H2** : http://localhost:8080/angel/h2-console
- **API** : http://localhost:8080/angel/api/

### Mode Test
- **Port** : 8081
- **Context-path** : `/` (racine)
- **Avatar** : http://localhost:8081/angel et http://localhost:8081/
- **Dashboard Test** : http://localhost:8081/test-dashboard
- **Console H2** : http://localhost:8081/h2-console
- **API Test** : http://localhost:8081/api/test/

## Interface Avatar

### Fonctionnalités

L'interface avatar permet d'interagir directement avec l'assistant virtuel :

#### 1. **Affichage Avatar**
- Rendu 3D en temps réel
- Animations faciales synchronisées
- Expressions émotionnelles
- Gestures et mouvements

#### 2. **Contrôles Audio**
- Volume principal
- Activation/désactivation de la voix
- Sélection de la voix (à venir)

#### 3. **Interface de Conversation**
- Zone de chat (à venir)
- Historique des interactions
- Reconnaissance vocale (à venir)

#### 4. **Paramètres d'Affichage**
- Mode plein écran
- Qualité de rendu
- Thème d'interface

### Utilisation

```javascript
// Exemple d'interaction via JavaScript
const avatar = new AvatarInterface({
    container: '#avatar-container',
    websocketUrl: '/ws/avatar',
    enableVoice: true
});

// Faire parler l'avatar
avatar.speak('Bonjour ! Comment allez-vous ?', 'happy');

// Changer l'émotion
avatar.setEmotion('thoughtful', 0.7);

// Écouter les événements
avatar.on('speechEnd', () => {
    console.log('L\\'avatar a fini de parler');
});
```

### Configuration

Configuration dans `config/application.properties` :

```properties
# Avatar Web
avatar.web.enabled=true
avatar.web.websocket.path=/ws/avatar
avatar.web.3d.quality=medium
avatar.web.voice.enabled=true
avatar.web.fullscreen.enabled=true

# Performance
avatar.web.fps.target=30
avatar.web.render.shadows=true
avatar.web.render.antialiasing=true
```

## Dashboard de Test

### Sections Principales

#### 1. **Contrôles de Simulation**
- **Start/Stop** : Démarrage et arrêt de la simulation
- **Vitesse** : Multiplicateur de vitesse (1x à 10x)
- **Randomisation** : Niveau d'aléatoire (0% à 100%)
- **Mode** : Automatique ou manuel

#### 2. **Sélection d'Activité**
- Liste déroulante avec toutes les 27 activités
- Niveau de confiance (0% à 100%)
- Durée personnalisée
- Application immédiate

#### 3. **Gestion des Scénarios**
- Chargement de scénarios prédéfinis
- Création de nouveaux scénarios
- Import/export de configurations
- Bibliothèque de scénarios

#### 4. **Statistiques en Temps Réel**
- Graphique d'activités sur 24h
- Propositions générées
- Temps de réponse moyen
- Taux de confiance

#### 5. **Journal d'Activité**
- Log en temps réel des événements
- Filtrage par type (activité, proposition, erreur)
- Export des logs
- Recherche dans l'historique

#### 6. **Configuration Test**
- Paramètres de simulation
- Intervalles et timeouts
- Sources de données
- Mode debug

### Utilisation API

```bash
# Status du dashboard
curl http://localhost:8081/api/test/dashboard/status

# Démarrer la simulation
curl -X POST http://localhost:8081/api/test/simulation/start

# Définir une activité
curl -X POST http://localhost:8081/api/test/activity/set \
     -H "Content-Type: application/json" \
     -d '{
       "activity": "READING",
       "confidence": 0.85,
       "duration": 300000
     }'

# Charger un scénario
curl -X POST http://localhost:8081/api/test/scenario/load \
     -H "Content-Type: application/json" \
     -d '{"name": "morning_routine"}'

# Obtenir les statistiques
curl http://localhost:8081/api/test/stats/current
```

### Configuration

Configuration dans `config/application-test.properties` :

```properties
# Dashboard Test
angel.test.dashboard.enabled=true
angel.test.dashboard.refresh-interval=5000
angel.test.dashboard.max-log-entries=1000
angel.test.dashboard.stats.enabled=true

# Simulation
angel.test.simulation.interval=30000
angel.test.simulation.randomness=0.3
angel.test.simulation.speed-multiplier=1.0
```

## API REST

### Endpoints Communs

#### Avatar
```http
GET    /api/avatar/status           # Status de l'avatar
POST   /api/avatar/speak            # Faire parler l'avatar
POST   /api/avatar/emotion          # Changer l'émotion
GET    /api/avatar/config           # Configuration actuelle
PUT    /api/avatar/config           # Modifier la configuration
```

#### Propositions
```http
GET    /api/proposals               # Propositions disponibles
POST   /api/proposals/trigger       # Déclencher une proposition
GET    /api/proposals/history       # Historique des propositions
DELETE /api/proposals/history/{id}  # Supprimer une entrée
```

#### Configuration
```http
GET    /api/config                  # Configuration complète
PUT    /api/config                  # Modifier la configuration
GET    /api/config/{section}        # Section spécifique
PUT    /api/config/{section}        # Modifier une section
```

### Endpoints Mode Test

#### Simulation
```http
GET    /api/test/health             # Status du mode test
POST   /api/test/simulation/start   # Démarrer la simulation
POST   /api/test/simulation/stop    # Arrêter la simulation
GET    /api/test/simulation/status  # Status de la simulation
```

#### Activités
```http
GET    /api/test/activity/current   # Activité courante
POST   /api/test/activity/set       # Définir une activité
GET    /api/test/activity/history   # Historique des activités
DELETE /api/test/activity/history   # Vider l'historique
```

#### Scénarios
```http
GET    /api/test/scenarios          # Liste des scénarios
POST   /api/test/scenario/load      # Charger un scénario
POST   /api/test/scenario/create    # Créer un scénario
DELETE /api/test/scenario/{name}    # Supprimer un scénario
```

#### Statistiques
```http
GET    /api/test/stats/current      # Statistiques actuelles
GET    /api/test/stats/history      # Historique des statistiques
GET    /api/test/stats/export       # Export des données
POST   /api/test/stats/reset        # Reset des statistiques
```

## Communication WebSocket

### Avatar WebSocket

L'avatar utilise WebSocket pour la communication temps réel :

```javascript
// Connexion WebSocket
const ws = new WebSocket('ws://localhost:8081/ws/avatar');

// Messages entrants
ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    
    switch(message.type) {
        case 'speak':
            avatar.speak(message.text, message.emotion);
            break;
        case 'emotion':
            avatar.setEmotion(message.emotion, message.intensity);
            break;
        case 'gesture':
            avatar.playGesture(message.gesture);
            break;
    }
};

// Messages sortants
function sendAvatarCommand(type, data) {
    ws.send(JSON.stringify({
        type: type,
        timestamp: Date.now(),
        ...data
    }));
}
```

### Test WebSocket

Communication pour le dashboard de test :

```javascript
// Connexion au WebSocket de test
const testWs = new WebSocket('ws://localhost:8081/ws/test');

// Écouter les mises à jour
testWs.onmessage = function(event) {
    const update = JSON.parse(event.data);
    
    switch(update.type) {
        case 'activity_change':
            updateActivityDisplay(update.activity);
            break;
        case 'proposal_generated':
            addProposalToLog(update.proposal);
            break;
        case 'stats_update':
            updateStatistics(update.stats);
            break;
    }
};
```

## Responsive Design

### Breakpoints

```css
/* Mobile first approach */
.container {
    width: 100%;
}

/* Tablet */
@media (min-width: 768px) {
    .container {
        max-width: 750px;
    }
}

/* Desktop */
@media (min-width: 1024px) {
    .container {
        max-width: 1200px;
    }
    
    .dashboard-sidebar {
        display: block;
    }
}

/* Large desktop */
@media (min-width: 1400px) {
    .container {
        max-width: 1360px;
    }
}
```

### Adaptations Mobile

#### Dashboard Test (Mobile)
- Menu hamburger pour la navigation
- Cartes empilées verticalement
- Contrôles tactiles optimisés
- Graphiques simplifiés

#### Avatar (Mobile)
- Mode portrait optimisé
- Contrôles en bas d'écran
- Gestures tactiles (pinch, swipe)
- Mode plein écran automatique

## Sécurité

### Protection CSRF

```java
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/api/**").permitAll()
            .anyRequest().authenticated();
        
        return http.build();
    }
}
```

### Validation des Entrées

```java
@RestController
@RequestMapping("/api/test")
@Validated
public class TestApiController {
    
    @PostMapping("/activity/set")
    public ResponseEntity<?> setActivity(
            @Valid @RequestBody ActivityRequest request) {
        
        // Validation automatique via annotations
        return ResponseEntity.ok(testService.setActivity(request));
    }
}

@Data
public class ActivityRequest {
    @NotNull
    @Pattern(regexp = "^[A-Z_]+$")
    private String activity;
    
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double confidence;
    
    @Min(1000)
    @Max(3600000)
    private Long duration;
}
```

## Performance

### Optimisations Frontend

#### 1. **Lazy Loading**
```javascript
// Chargement différé des modules
const loadAvatarModule = () => {
    return import('./modules/avatar.js');
};

// Intersection Observer pour le lazy loading
const imageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const img = entry.target;
            img.src = img.dataset.src;
            imageObserver.unobserve(img);
        }
    });
});
```

#### 2. **Mise en Cache**
```javascript
// Service Worker pour la mise en cache
self.addEventListener('fetch', event => {
    if (event.request.url.includes('/api/')) {
        // Cache API responses for 5 minutes
        event.respondWith(
            caches.open('api-cache').then(cache => {
                return cache.match(event.request).then(response => {
                    if (response && isNotExpired(response)) {
                        return response;
                    }
                    return fetch(event.request).then(fetchResponse => {
                        cache.put(event.request, fetchResponse.clone());
                        return fetchResponse;
                    });
                });
            })
        );
    }
});
```

#### 3. **Compression**
```properties
# Configuration Spring Boot
server.compression.enabled=true
server.compression.mime-types=text/html,text/css,application/javascript,application/json
server.compression.min-response-size=1024

# Gzip des ressources statiques
spring.web.resources.chain.compressed=true
```

### Optimisations Backend

#### 1. **Cache Spring**
```java
@Service
@CacheConfig(cacheNames = "proposals")
public class ProposalService {
    
    @Cacheable(key = "#activity.name()")
    public List<Proposal> getProposalsForActivity(Activity activity) {
        // Computation intensive method
        return computeProposals(activity);
    }
    
    @CacheEvict(allEntries = true)
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void evictCache() {
        // Cache cleanup
    }
}
```

#### 2. **Async Processing**
```java
@Service
public class AsyncProposalService {
    
    @Async
    @EventListener
    public void handleActivityChange(ActivityChangeEvent event) {
        // Process activity change asynchronously
        processActivityChange(event.getActivity());
    }
    
    @Async("avatarExecutor")
    public CompletableFuture<Void> displayProposal(Proposal proposal) {
        return avatarController.displayProposal(proposal);
    }
}
```

## Monitoring et Métriques

### Actuator Endpoints

```properties
# Configuration Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

### Métriques Personnalisées

```java
@Component
public class CustomMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter proposalCounter;
    private final Timer responseTimer;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.proposalCounter = Counter.builder("angel.proposals.generated")
            .description("Number of proposals generated")
            .register(meterRegistry);
        
        this.responseTimer = Timer.builder("angel.response.time")
            .description("Response time for proposals")
            .register(meterRegistry);
    }
    
    public void recordProposal(String type) {
        proposalCounter.increment(Tags.of("type", type));
    }
    
    public void recordResponseTime(Duration duration) {
        responseTimer.record(duration);
    }
}
```

### Dashboard de Monitoring

Accès aux métriques :
- **Health** : http://localhost:8081/actuator/health
- **Métriques** : http://localhost:8081/actuator/metrics
- **Prometheus** : http://localhost:8081/actuator/prometheus

## Déploiement

### Configuration de Production

```properties
# Production settings
server.port=8080
server.servlet.context-path=/angel

# Security
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}

# Performance
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10
server.tomcat.connection-timeout=20000

# Caching
spring.web.resources.cache.cachecontrol.max-age=31536000
spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**
```

### Docker

```dockerfile
FROM openjdk:17-jre-slim

COPY target/angel-virtual-assistant-*.jar app.jar
COPY config/ /app/config/

EXPOSE 8080

ENV SPRING_CONFIG_LOCATION=file:/app/config/
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Reverse Proxy (Nginx)

```nginx
server {
    listen 80;
    server_name angel.example.com;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
    
    # Cache static resources
    location ~* \.(css|js|png|jpg|jpeg|gif|ico|svg)$ {
        proxy_pass http://localhost:8080;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## Dépannage

### Problèmes Courants

#### 1. **Template non trouvé**
```
Error: Could not resolve template "avatar"
```

**Solution** :
```bash
# Vérifier la structure des templates
ls -la src/main/resources/templates/

# Vérifier la configuration Thymeleaf
grep thymeleaf config/application*.properties
```

#### 2. **Ressources statiques inaccessibles**
```
404 Not Found - /css/avatar.css
```

**Solution** :
```bash
# Vérifier la structure static
ls -la src/main/resources/static/

# Vérifier la configuration des ressources
grep "spring.web.resources" config/application*.properties
```

#### 3. **WebSocket connection failed**
```
WebSocket connection to 'ws://localhost:8081/ws/avatar' failed
```

**Solution** :
```java
// Vérifier la configuration WebSocket
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new AvatarWebSocketHandler(), "/ws/avatar")
                .setAllowedOrigins("*");
    }
}
```

#### 4. **CORS errors**
```
Access to XMLHttpRequest blocked by CORS policy
```

**Solution** :
```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Logs Utiles

```bash
# Logs Spring Boot
tail -f logs/angel.log | grep -E "(SPRING|Tomcat|Thymeleaf)"

# Logs WebSocket
tail -f logs/angel.log | grep -i websocket

# Logs d'erreur HTTP
tail -f logs/angel.log | grep -E "(404|500|ERROR)"

# Logs de performance
tail -f logs/angel.log | grep -E "(slow|timeout|performance)"
```

## Conclusion

L'interface web d'Angel Virtual Assistant offre une expérience utilisateur moderne et complète pour interagir avec l'assistant virtuel. Elle combine la puissance de Spring Boot côté serveur avec une interface utilisateur responsive et interactive côté client.

Les fonctionnalités principales incluent :
- Interface avatar 3D en temps réel
- Dashboard de test complet avec contrôles avancés
- API REST complète pour l'intégration
- Communication WebSocket pour les mises à jour temps réel
- Design responsive compatible mobile et desktop

Pour plus d'informations :
- [README.md](../README.md) : Documentation principale
- [TEST_MODE.md](TEST_MODE.md) : Guide du mode test
- [SPRING_BOOT_MIGRATION.md](SPRING_BOOT_MIGRATION.md) : Guide de migration
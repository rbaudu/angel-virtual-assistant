# Migration vers Spring Boot

Guide détaillé de la migration d'Angel Virtual Assistant vers Spring Boot avec interface web intégrée.

## Vue d'ensemble

Cette migration transforme Angel Virtual Assistant d'une application Java standalone vers une application Spring Boot moderne avec interface web complète, tout en préservant la configuration externe existante dans le dossier `config/`.

## Changements Principaux

### 1. **Architecture**
- **Avant** : Application Java standalone avec méthode `main()` personnalisée
- **Après** : Application Spring Boot avec serveur web intégré Tomcat

### 2. **Configuration**
- **Avant** : Configuration JSON dans `config/angel-config.json`
- **Après** : Configuration properties dans `config/application.properties` + `config/application-test.properties`

### 3. **Interface**
- **Avant** : Pas d'interface web
- **Après** : Interface web complète avec dashboard et avatar

### 4. **Gestion des Composants**
- **Avant** : Instanciation manuelle des classes
- **Après** : Injection de dépendances Spring avec annotations

## Étapes de Migration

### Étape 1 : Mise à jour du pom.xml

#### Ajouter les dépendances Spring Boot

```xml
<!-- Spring Boot Starter Web (pour REST API et interface web) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>${spring-boot.version}</version>
</dependency>

<!-- Spring Boot Thymeleaf (pour les templates HTML) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
    <version>${spring-boot.version}</version>
</dependency>

<!-- Spring Boot WebSocket (pour avatar temps réel) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
    <version>${spring-boot.version}</version>
</dependency>
```

### Étape 2 : Nouveau Point d'Entrée Spring Boot

#### Créer `SpringBootAngelApplication.java`

```java
package com.angel.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan(basePackages = "com.angel")
public class SpringBootAngelApplication {

    public static void main(String[] args) {
        // Configuration pour utiliser les fichiers externes
        System.setProperty("spring.config.location", "file:./config/");
        System.setProperty("spring.config.name", "application");
        
        SpringApplication.run(SpringBootAngelApplication.class, args);
    }
}

@Component
class AngelApplicationStarter {
    
    @Autowired(required = false)
    private AngelApplication angelApplication;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (angelApplication != null) {
            angelApplication.start();
        }
    }
}
```

### Étape 3 : Transformation de l'Application Principale

#### Modifier `AngelApplication.java`

```java
package com.angel.core;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PreDestroy;

@Component
public class AngelApplication {
    
    private final ConfigManager configManager;
    // ... autres dépendances
    
    @Autowired
    public AngelApplication(ConfigManager configManager) {
        this.configManager = configManager;
        // Initialisation des autres composants
    }
    
    public void start() {
        // Logique de démarrage existante
    }
    
    @PreDestroy
    public void stop() {
        // Logique d'arrêt existante
    }
}
```

### Étape 4 : Configuration Web

#### Créer `WebConfig.java`

```java
package com.angel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // Configuration automatique via Thymeleaf
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
```

### Étape 5 : Contrôleurs Web

#### Créer `AvatarWebController.java`

```java
package com.angel.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class AvatarWebController {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @GetMapping({"/", "/angel"})
    public String showAvatar(Model model) {
        model.addAttribute("avatarEnabled", true);
        model.addAttribute("pageTitle", "Angel Virtual Assistant");
        model.addAttribute("contextPath", contextPath);
        return "avatar";
    }
}
```

#### Mettre à jour `TestDashboardController.java`

```java
package com.angel.ui;

import com.angel.test.TestModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test-dashboard")
@ConditionalOnProperty(
    prefix = "angel.test.dashboard", 
    name = "enabled", 
    havingValue = "true",
    matchIfMissing = true
)
public class TestDashboardController {
    
    @Autowired(required = false)
    private TestModeService testModeService;
    
    // Méthodes existantes...
}
```

### Étape 6 : Migration de la Configuration

#### Configuration existante (à conserver)

Vos fichiers existants `config/application.properties` et `config/application-test.properties` sont préservés.

#### Ajouts nécessaires pour Spring Boot

Ajouter à la fin de `config/application.properties` :

```properties
# ===============================================
# Configuration Spring Boot (ajout pour interface web)
# ===============================================

# Configuration Spring Boot
spring.application.name=Angel Virtual Assistant
spring.main.allow-bean-definition-overriding=true

# Configuration Web Spring
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html

# Configuration des ressources statiques
spring.web.resources.static-locations=classpath:/static/

# Configuration Templates (Thymeleaf)
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Configuration Dashboard Test
angel.test.dashboard.enabled=true
```

Ajouter à la fin de `config/application-test.properties` :

```properties
# ===============================================
# Configuration Spring Boot Test
# ===============================================

# Configuration Spring Boot (test)
spring.application.name=Angel Virtual Assistant Test

# Configuration Web Spring (test - pas de context-path)
server.servlet.context-path=/
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html

# Configuration Templates (test)
spring.thymeleaf.cache=false

# Configuration Dashboard Test (forcé activé en mode test)
angel.test.dashboard.enabled=true
```

### Étape 7 : Ressources Web

#### Structure à créer

```
src/main/resources/
├── static/
│   ├── css/
│   │   ├── avatar.css
│   │   ├── test-dashboard.css
│   │   └── common.css
│   ├── js/
│   │   ├── avatar.js
│   │   ├── test-control.js
│   │   └── websocket.js
│   └── images/
│       └── [icônes et images]
└── templates/
    ├── avatar.html
    ├── test-dashboard.html
    └── test-help.html
```

#### Template exemple `avatar.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${pageTitle}">Angel Virtual Assistant</title>
    <link rel="stylesheet" th:href="@{/css/avatar.css}">
</head>
<body>
    <div class="avatar-container">
        <div id="avatar-display">
            <!-- Avatar 3D sera rendu ici -->
        </div>
        
        <div class="controls">
            <button id="speak-btn">Faire parler</button>
            <button id="emotion-btn">Changer émotion</button>
        </div>
    </div>
    
    <script th:src="@{/js/avatar.js}"></script>
</body>
</html>
```

### Étape 8 : Mise à jour du Script de Lancement

#### Modifications dans `angel-launcher.sh`

```bash
# Configuration des options Java avec Spring Boot
configure_java_opts() {
    # Options de base
    JAVA_OPTS="-Xms$MEMORY_XMS -Xmx$MEMORY_XMX"
    
    # Configuration Spring Boot pour utiliser les fichiers config/ externes
    JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=file:./config/"
    JAVA_OPTS="$JAVA_OPTS -Dspring.config.name=application"
    
    # Profil Spring Boot
    if [[ "$PROFILE" != "default" ]]; then
        JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$PROFILE"
    fi
    
    # Configuration selon le profil...
}
```

## Avantages de la Migration

### 1. **Interface Web Intégrée**
- ✅ Dashboard de test accessible via navigateur
- ✅ Interface avatar web en temps réel
- ✅ API REST complète
- ✅ Pas besoin de serveur externe

### 2. **Configuration Centralisée**
- ✅ Fichiers de configuration préservés dans `config/`
- ✅ Spring Boot utilise directement ces fichiers
- ✅ Pas de duplication de configuration
- ✅ Support natif des profils Spring

### 3. **Développement Simplifié**
- ✅ Hot reload automatique en développement
- ✅ Serveur web intégré
- ✅ Injection de dépendances Spring
- ✅ Annotations Spring Boot

### 4. **Production Ready**
- ✅ Métriques et monitoring intégrés
- ✅ Configuration de sécurité
- ✅ Support SSL/TLS
- ✅ Containerisation Docker facilité

## Compatibilité Ascendante

### Ce qui est préservé

1. **Configuration existante** : Tous vos fichiers `config/` sont conservés
2. **Logique métier** : Le code de `AngelApplication` est préservé
3. **Scripts de lancement** : `angel-launcher.sh` continue de fonctionner
4. **Base de données** : Schema H2 et données existantes préservées
5. **API externe** : Communication avec Angel-server-capture inchangée

### Ce qui change

1. **Point d'entrée** : `SpringBootAngelApplication` au lieu de `AngelApplication.main()`
2. **Serveur web** : Tomcat intégré au lieu d'aucun serveur
3. **URLs d'accès** : Nouvelles URLs pour l'interface web
4. **Gestion des composants** : Spring IoC au lieu d'instanciation manuelle

## Tests de Migration

### 1. **Vérifier la compilation**

```bash
mvn clean compile
```

### 2. **Vérifier la configuration**

```bash
# Tester que les propriétés sont bien chargées
./angel-launcher.sh start -p test
./angel-launcher.sh status
```

### 3. **Vérifier l'interface web**

```bash
# Tester les endpoints web
./angel-launcher.sh test-web

# Ou manuellement
curl http://localhost:8081/test-dashboard
curl http://localhost:8081/angel
```

### 4. **Vérifier la logique métier**

```bash
# Vérifier que l'application Angel fonctionne toujours
./angel-launcher.sh logs | grep "Angel"
```

## Résolution de Problèmes

### 1. **Erreur de compilation**

```
Error: Could not find or load main class SpringBootAngelApplication
```

**Solution** : Vérifier que le nouveau point d'entrée est correctement créé dans `src/main/java/com/angel/core/`.

### 2. **Configuration non trouvée**

```
Error: Could not resolve placeholder 'angel.test.enabled'
```

**Solution** : Vérifier que les propriétés Spring Boot ont été ajoutées aux fichiers de configuration.

### 3. **Template non trouvé**

```
Error: Could not resolve template "avatar"
```

**Solution** : Créer la structure de templates dans `src/main/resources/templates/`.

### 4. **Port déjà utilisé**

```
Error: Port 8080 is already in use
```

**Solution** : Changer le port dans la configuration ou arrêter l'autre service.

## Rollback

Si nécessaire, vous pouvez revenir à l'ancienne version :

### 1. **Sauvegarder les modifications**

```bash
git stash save "Spring Boot migration"
```

### 2. **Revenir à l'ancien point d'entrée**

Modifier le `pom.xml` pour utiliser l'ancienne classe principale :

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <mainClass>com.angel.core.AngelApplication</mainClass>
    </configuration>
</plugin>
```

### 3. **Supprimer les dépendances Spring Boot**

Retirer les dépendances `spring-boot-starter-*` du `pom.xml`.

## Migration Graduelle

Vous pouvez migrer progressivement :

### Phase 1 : Spring Boot sans interface web
- Ajouter Spring Boot Starter Core uniquement
- Transformer en composant Spring
- Garder l'ancienne interface

### Phase 2 : Ajouter l'interface web
- Ajouter Spring Boot Web et Thymeleaf
- Créer les contrôleurs web
- Ajouter les templates

### Phase 3 : Optimisations
- Ajouter WebSocket pour temps réel
- Optimiser les performances
- Ajouter la sécurité

## Conclusion

Cette migration vers Spring Boot apporte de nombreux avantages tout en préservant votre architecture et configuration existantes. L'approche de configuration externe garantit une migration en douceur sans perte de données ou de paramètres.

Les points clés de la migration :

1. **Configuration préservée** : Vos fichiers `config/` restent centralisés
2. **Interface web moderne** : Dashboard et avatar accessibles via navigateur
3. **Compatibilité** : Scripts de lancement et API externes inchangés
4. **Évolutivité** : Base solide pour futures fonctionnalités web

Pour plus d'informations :
- [README.md](../README.md) : Documentation principale mise à jour
- [WEB_INTERFACE.md](WEB_INTERFACE.md) : Guide de l'interface web
- [TEST_MODE.md](TEST_MODE.md) : Documentation du mode test
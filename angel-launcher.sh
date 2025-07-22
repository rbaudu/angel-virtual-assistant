#!/bin/bash

# Angel Virtual Assistant - Script de lancement Spring Boot avec configuration externe
# Version corrigée pour Spring Boot
# Version: 1.1.1

# Configuration par défaut
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
CONFIG_FILE="$PROJECT_DIR/config/application.properties"
LOG_DIR="$PROJECT_DIR/logs"
PID_FILE="$PROJECT_DIR/angel.pid"
JAVA_OPTS=""
MEMORY_XMX="512m"
MEMORY_XMS="256m"
PROFILE="default"
DEBUG_PORT="5005"
DEBUG_MODE=false
DAEMON_MODE=false
VERBOSE=false

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonctions d'affichage
print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Configuration des options Java selon le profil avec configuration externe CORRIGÉE
configure_java_opts() {
    # Options de base
    JAVA_OPTS="-Xms$MEMORY_XMS -Xmx$MEMORY_XMX"
    
    # CORRECTION: Configuration Spring Boot pour utiliser les fichiers config/ externes
    # On utilise à la fois classpath et file pour que Spring Boot trouve les ressources par défaut
	#JAVA_OPTS="$JAVA_OPTS -Dspring.config.additional-location=file:./config/"    
	#JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=optional:classpath:./,optional:file:/config/application.properties,optional:file:/config/application-test.properties"
	
    # Profil Spring Boot (utilise les fichiers application-{profile}.properties)
    if [[ "$PROFILE" != "default" ]]; then
        JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$PROFILE"
    fi
    
    # Configuration selon le profil
    case $PROFILE in
        "dev")
            JAVA_OPTS="$JAVA_OPTS -Dlogging.level.com.angel=DEBUG"
            JAVA_OPTS="$JAVA_OPTS -Dangel.dev.debug-mode=true"
            ;;
        "prod")
            JAVA_OPTS="$JAVA_OPTS -Dlogging.level.root=INFO"
            JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:+UseStringDeduplication"
            JAVA_OPTS="$JAVA_OPTS -Dangel.security.enabled=true"
            ;;
        "test")
            JAVA_OPTS="$JAVA_OPTS -Dlogging.level.com.angel=TRACE"
            JAVA_OPTS="$JAVA_OPTS -Dangel.test.enabled=true"
            JAVA_OPTS="$JAVA_OPTS -Dangel.test.auto-start=true"
            JAVA_OPTS="$JAVA_OPTS -Dangel.test.dashboard.enabled=true"
            ;;
    esac
    
    # Mode debug
    if [[ $DEBUG_MODE == true ]]; then
        JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$DEBUG_PORT"
        print_info "Mode debug activé sur le port $DEBUG_PORT"
    fi
    
    # Mode verbose
    if [[ $VERBOSE == true ]]; then
        JAVA_OPTS="$JAVA_OPTS -verbose:gc"
    fi
    
    print_info "Profil Spring Boot: $PROFILE"
    print_info "Fichier de configuration: config/application.properties"
    if [[ "$PROFILE" != "default" ]]; then
        print_info "Fichier de profil: config/application-$PROFILE.properties"
    fi
    print_info "Options JVM: $JAVA_OPTS"
}

# Vérification des prérequis avec configuration externe
check_prerequisites() {
    print_info "Vérification des prérequis..."
    
    # Vérifier Java
    if ! command -v java &> /dev/null; then
        print_error "Java n'est pas installé ou pas dans le PATH"
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    print_info "Java version: $java_version"
    
    # Vérifier Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven n'est pas installé ou pas dans le PATH"
        exit 1
    fi
    
    local maven_version=$(mvn -version 2>&1 | head -n 1 | cut -d' ' -f3)
    print_info "Maven version: $maven_version"
    
    # Créer les répertoires nécessaires
    mkdir -p "$LOG_DIR"
    
    # Vérifier la configuration principale
    if [[ ! -f "$CONFIG_FILE" ]]; then
        print_error "Fichier de configuration manquant: $CONFIG_FILE"
        print_info "Création d'un fichier de configuration par défaut..."
        create_default_config
    fi
    
    print_info "Configuration principale trouvée: $CONFIG_FILE"
    
    # Vérifier la configuration du profil
    if [[ "$PROFILE" != "default" ]]; then
        local profile_config="$PROJECT_DIR/config/application-$PROFILE.properties"
        if [[ ! -f "$profile_config" ]]; then
            print_error "Fichier de configuration pour le profil '$PROFILE' manquant: $profile_config"
            print_info "Création d'un fichier de configuration par défaut pour le profil $PROFILE..."
            create_profile_config "$PROFILE"
        else
            print_info "Configuration profil trouvée: $profile_config"
        fi
    fi
    
    # Vérifier que les propriétés Spring Boot requises sont présentes
    check_spring_boot_properties
}

# Création d'un fichier de configuration par défaut
create_default_config() {
    mkdir -p "$(dirname "$CONFIG_FILE")"
    cat > "$CONFIG_FILE" << 'EOF'
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
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
EOF
    print_success "Fichier de configuration créé: $CONFIG_FILE"
}

# Création d'un fichier de configuration pour un profil spécifique
create_profile_config() {
    local profile="$1"
    local profile_config="$PROJECT_DIR/config/application-$profile.properties"
    
    mkdir -p "$(dirname "$profile_config")"
    
    case "$profile" in
        "test")
            cat > "$profile_config" << 'EOF'
# ===============================================
# Configuration Angel Virtual Assistant - Mode Test
# ===============================================

# Application (mode test)
system.name=Angel Companion Test
system.version=1.0.0-TEST
system.language=fr

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

# Simulation accélérée pour tests
angel.test.simulation.interval=5000
angel.test.simulation.randomness=0.5
angel.test.simulation.speed-multiplier=5.0
angel.test.simulation.noise-enabled=true

# Dashboard test
angel.test.dashboard.refresh-interval=5000
angel.test.dashboard.max-log-entries=1000

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

# Sécurité désactivée en test
angel.security.enabled=false
EOF
            ;;
        "dev")
            cat > "$profile_config" << 'EOF'
# Configuration développement

# Port développement
server.port=8090
server.servlet.context-path=/

# Hot reload activé
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true

# Cache désactivé
spring.thymeleaf.cache=false
spring.web.resources.cache.period=0

# Logs détaillés
logging.level.com.angel=DEBUG
logging.level.org.springframework=DEBUG
logging.file.name=logs/angel-dev.log

# Base de données développement
spring.datasource.url=jdbc:h2:file:./angel-dev-db
spring.jpa.show-sql=true

# Mode développement
angel.dev.debug-mode=true
angel.test.dashboard.enabled=true
EOF
            ;;
        "prod")
            cat > "$profile_config" << 'EOF'
# Configuration production

# Port production
server.port=8080
server.servlet.context-path=/angel

# Sécurité activée
angel.security.enabled=true

# Cache activé
spring.thymeleaf.cache=true
spring.web.resources.cache.cachecontrol.max-age=31536000

# Logs optimisés
logging.level.root=INFO
logging.level.com.angel=INFO
logging.file.name=logs/angel-prod.log

# Performance
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10

# Monitoring
management.endpoints.web.exposure.include=health,info,metrics,prometheus
EOF
            ;;
    esac
    
    print_success "Fichier de configuration pour le profil '$profile' créé: $profile_config"
}

# Vérification des propriétés Spring Boot dans les fichiers de configuration
check_spring_boot_properties() {
    local missing_props=()
    
    # Vérifier les propriétés essentielles Spring Boot
    if ! grep -q "spring.application.name" "$CONFIG_FILE"; then
        missing_props+=("spring.application.name")
    fi
    
    if ! grep -q "spring.mvc.view.prefix" "$CONFIG_FILE"; then
        missing_props+=("spring.mvc.view.prefix")
    fi
    
    if [[ ${#missing_props[@]} -gt 0 ]]; then
        print_warning "Propriétés Spring Boot manquantes dans $CONFIG_FILE:"
        for prop in "${missing_props[@]}"; do
            print_warning "  - $prop"
        done
        print_info "Ajout automatique des propriétés manquantes..."
        add_missing_spring_properties
    fi
}

# Ajout des propriétés Spring Boot manquantes
add_missing_spring_properties() {
    local temp_file=$(mktemp)
    
    # Copier le fichier existant
    cp "$CONFIG_FILE" "$temp_file"
    
    # Ajouter les propriétés manquantes à la fin
    cat >> "$temp_file" << 'EOF'

# ===============================================
# Configuration Spring Boot (ajoutée automatiquement)
# ===============================================

# Configuration Spring Boot
spring.application.name=Angel Virtual Assistant
spring.main.allow-bean-definition-overriding=true

# Configuration Web Spring
spring.mvc.view.prefix=/templates/
spring.mvc.view.suffix=.html

# Configuration des ressources statiques
spring.web.resources.static-locations=classpath:/static/

# Configuration Templates
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Configuration Dashboard Test
angel.test.dashboard.enabled=true
EOF
    
    # Remplacer le fichier original
    mv "$temp_file" "$CONFIG_FILE"
    print_success "Propriétés Spring Boot ajoutées à $CONFIG_FILE"
}

# Démarrage de l'application avec configuration externe
start_application() {
    print_info "Démarrage d'Angel Virtual Assistant avec Spring Boot..."
    
    # Vérifier si l'application est déjà en cours d'exécution
    if check_status; then
        print_warning "L'application est déjà en cours d'exécution"
        show_status
        return 0
    fi
    
    # Vérifier les prérequis
    check_prerequisites
    
    # Compiler si nécessaire
    if [[ ! -f "$PROJECT_DIR/target/angel-virtual-assistant-1.0.0-SNAPSHOT.jar" ]]; then
        print_info "JAR non trouvé, compilation nécessaire..."
        build_project
    fi
    
    # Configurer les options Java
    configure_java_opts
    
    local jar_file="$PROJECT_DIR/target/angel-virtual-assistant-1.0.0-SNAPSHOT.jar"
    
    # Vérifier que le JAR existe
    if [[ ! -f "$jar_file" ]]; then
        print_error "Fichier JAR introuvable: $jar_file"
        build_project
        if [[ ! -f "$jar_file" ]]; then
            print_error "Impossible de créer le fichier JAR"
            exit 1
        fi
    fi
    
    # Déterminer les URLs selon le profil et la configuration
    local base_url="http://localhost"
    local port=$(grep "^server.port" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
    local context_path=$(grep "^server.servlet.context-path" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
    
    # Ajuster pour le profil test
    if [[ "$PROFILE" == "test" ]]; then
        local test_config="$PROJECT_DIR/config/application-test.properties"
        if [[ -f "$test_config" ]]; then
            local test_port=$(grep "^server.port" "$test_config" | cut -d'=' -f2 | tr -d ' ')
            if [[ -n "$test_port" ]]; then
                port="$test_port"
            fi
            local test_context=$(grep "^server.servlet.context-path" "$test_config" | cut -d'=' -f2 | tr -d ' ')
            if [[ -n "$test_context" ]]; then
                context_path="$test_context"
            fi
        fi
    fi
    
    [[ -z "$port" ]] && port="8080"
    [[ -z "$context_path" ]] && context_path=""
    [[ "$context_path" == "/" ]] && context_path=""
    
    if [[ $DAEMON_MODE == true ]]; then
        # Mode daemon
        print_info "Démarrage en mode daemon..."
        
        nohup java $JAVA_OPTS -jar "$jar_file" \
            > "$LOG_DIR/angel.out" 2> "$LOG_DIR/angel.err" &
        
        local pid=$!
        echo $pid > "$PID_FILE"
        
        # Attendre un peu pour vérifier que l'application démarre
        sleep 5
        
        if check_status; then
            print_success "Angel Virtual Assistant démarré en mode daemon (PID: $pid)"
            print_info "Profil: $PROFILE"
            print_info "Interface web disponible:"
            print_info "  - Dashboard test: ${base_url}:${port}${context_path}/test-dashboard"
            print_info "  - Avatar: ${base_url}:${port}${context_path}/angel"
            if [[ "$PROFILE" == "test" || "$context_path" == "" ]]; then
                print_info "  - Avatar (alt): ${base_url}:${port}/"
            fi
            print_info "Logs disponibles dans: $LOG_DIR/angel.out et $LOG_DIR/angel.err"
        else
            print_error "Échec du démarrage"
            if [[ -f "$LOG_DIR/angel.err" ]]; then
                print_error "Erreurs:"
                cat "$LOG_DIR/angel.err"
            fi
            exit 1
        fi
    else
        # Mode interactif
        print_info "Démarrage en mode interactif..."
        print_info "Profil: $PROFILE"
        print_info "Interface web disponible:"
        print_info "  - Dashboard test: ${base_url}:${port}${context_path}/test-dashboard"
        print_info "  - Avatar: ${base_url}:${port}${context_path}/angel"
        if [[ "$PROFILE" == "test" || "$context_path" == "" ]]; then
            print_info "  - Avatar (alt): ${base_url}:${port}/"
        fi
        print_info "Utilisez Ctrl+C pour arrêter l'application"
        
        # Créer un gestionnaire de signal pour nettoyer le fichier PID
        trap 'rm -f "$PID_FILE"; exit' INT TERM
        
        java $JAVA_OPTS -jar "$jar_file" &
        
        local pid=$!
        echo $pid > "$PID_FILE"
        
        # Attendre le processus
        wait $pid
        rm -f "$PID_FILE"
    fi
}

# Fonctions utilitaires (check_status, stop_application, etc.)
check_status() {
    if [[ -f "$PID_FILE" ]]; then
        local pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null 2>&1; then
            return 0
        else
            rm -f "$PID_FILE"
            return 1
        fi
    else
        return 1
    fi
}

stop_application() {
    print_info "Arrêt d'Angel Virtual Assistant..."
    
    if check_status; then
        local pid=$(cat "$PID_FILE")
        print_info "Arrêt du processus $pid..."
        
        kill -TERM "$pid" 2>/dev/null
        
        local count=0
        while [[ $count -lt 15 ]] && ps -p "$pid" > /dev/null 2>&1; do
            sleep 1
            ((count++))
        done
        
        if ps -p "$pid" > /dev/null 2>&1; then
            print_warning "Arrêt forcé du processus..."
            kill -KILL "$pid" 2>/dev/null
        fi
        
        rm -f "$PID_FILE"
        print_success "Angel Virtual Assistant arrêté"
    else
        print_warning "Angel Virtual Assistant n'est pas en cours d'exécution"
    fi
}

restart_application() {
    print_info "Redémarrage d'Angel Virtual Assistant..."
    stop_application
    sleep 3
    start_application
}

build_project() {
    print_info "Compilation du projet Spring Boot..."
    
    cd "$PROJECT_DIR"
    
    if [[ $VERBOSE == true ]]; then
        mvn clean package -DskipTests=false
    else
        mvn clean package -DskipTests=false -q
    fi
    
    if [[ $? -eq 0 ]]; then
        print_success "Compilation réussie"
    else
        print_error "Échec de la compilation"
        exit 1
    fi
}

clean_project() {
    print_info "Nettoyage du projet..."
    cd "$PROJECT_DIR"
    mvn clean -q
    rm -rf angel-db*
    rm -f "$PID_FILE"
    print_success "Nettoyage terminé"
}

show_status() {
    print_info "Vérification du statut..."
    
    if check_status; then
        local pid=$(cat "$PID_FILE")
        print_success "Angel Virtual Assistant est en cours d'exécution (PID: $pid)"
        print_info "Profil actif: $PROFILE"
        
        # Déterminer les URLs selon la configuration
        local base_url="http://localhost"
        local port=$(grep "^server.port" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
        local context_path=$(grep "^server.servlet.context-path" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
        
        # Ajuster pour le profil test
        if [[ "$PROFILE" == "test" ]]; then
            local test_config="$PROJECT_DIR/config/application-test.properties"
            if [[ -f "$test_config" ]]; then
                local test_port=$(grep "^server.port" "$test_config" | cut -d'=' -f2 | tr -d ' ')
                if [[ -n "$test_port" ]]; then
                    port="$test_port"
                fi
                local test_context=$(grep "^server.servlet.context-path" "$test_config" | cut -d'=' -f2 | tr -d ' ')
                if [[ -n "$test_context" ]]; then
                    context_path="$test_context"
                fi
            fi
        fi
        
        [[ -z "$port" ]] && port="8080"
        [[ -z "$context_path" ]] && context_path=""
        [[ "$context_path" == "/" ]] && context_path=""
        
        print_info "Interface web:"
        print_info "  - Dashboard test: ${base_url}:${port}${context_path}/test-dashboard"
        print_info "  - Avatar: ${base_url}:${port}${context_path}/angel"
        if [[ "$PROFILE" == "test" || "$context_path" == "" ]]; then
            print_info "  - Avatar (alt): ${base_url}:${port}/"
        fi
        print_info "  - Console H2: ${base_url}:${port}${context_path}/h2-console"
        
        # Afficher des informations sur le processus
        echo "Détails du processus:"
        ps -p "$pid" -o pid,ppid,cmd,etime,pmem,pcpu 2>/dev/null || true
        
        # Tester la connectivité web
        if command -v curl &> /dev/null; then
            print_info "Test de connectivité web..."
            if curl -s "${base_url}:${port}${context_path}/" > /dev/null; then
                print_success "Serveur web accessible"
            else
                print_warning "Serveur web non accessible"
            fi
        fi
    else
        print_warning "Angel Virtual Assistant n'est pas en cours d'exécution"
    fi
}

test_web_interface() {
    print_info "Test de l'interface web..."
    
    if ! check_status; then
        print_error "L'application n'est pas en cours d'exécution"
        return 1
    fi
    
    # Déterminer les URLs selon la configuration
    local base_url="http://localhost"
    local port=$(grep "^server.port" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
    local context_path=$(grep "^server.servlet.context-path" "$CONFIG_FILE" | cut -d'=' -f2 | tr -d ' ')
    
    # Ajuster pour le profil test
    if [[ "$PROFILE" == "test" ]]; then
        local test_config="$PROJECT_DIR/config/application-test.properties"
        if [[ -f "$test_config" ]]; then
            local test_port=$(grep "^server.port" "$test_config" | cut -d'=' -f2 | tr -d ' ')
            if [[ -n "$test_port" ]]; then
                port="$test_port"
            fi
            local test_context=$(grep "^server.servlet.context-path" "$test_config" | cut -d'=' -f2 | tr -d ' ')
            if [[ -n "$test_context" ]]; then
                context_path="$test_context"
            fi
        fi
    fi
    
    [[ -z "$port" ]] && port="8080"
    [[ -z "$context_path" ]] && context_path=""
    [[ "$context_path" == "/" ]] && context_path=""
    
    if command -v curl &> /dev/null; then
        print_info "Test des endpoints web..."
        
        # Test du dashboard de test
        if curl -s -o /dev/null -w "%{http_code}" "${base_url}:${port}${context_path}/test-dashboard" | grep -q "200"; then
            print_success "Dashboard test accessible: ${base_url}:${port}${context_path}/test-dashboard"
        else
            print_warning "Dashboard test non accessible"
        fi
        
        # Test de l'avatar
        if curl -s -o /dev/null -w "%{http_code}" "${base_url}:${port}${context_path}/angel" | grep -q "200"; then
            print_success "Page avatar accessible: ${base_url}:${port}${context_path}/angel"
        else
            print_warning "Page avatar non accessible"
        fi
        
        # Test racine si applicable
        if [[ "$PROFILE" == "test" || "$context_path" == "" ]]; then
            if curl -s -o /dev/null -w "%{http_code}" "${base_url}:${port}/" | grep -q "200"; then
                print_success "Page racine accessible: ${base_url}:${port}/"
            else
                print_warning "Page racine non accessible"
            fi
        fi
    else
        print_warning "curl non disponible, impossible de tester les endpoints"
        print_info "Testez manuellement:"
        print_info "  - ${base_url}:${port}${context_path}/test-dashboard"
        print_info "  - ${base_url}:${port}${context_path}/angel"
        if [[ "$PROFILE" == "test" || "$context_path" == "" ]]; then
            print_info "  - ${base_url}:${port}/"
        fi
    fi
}

show_logs() {
    local log_file="$LOG_DIR/angel.log"
    
    if [[ "$PROFILE" == "test" ]]; then
        log_file="$LOG_DIR/angel-test.log"
    fi
    
    if [[ -f "$log_file" ]]; then
        print_info "Affichage des logs: $log_file (Ctrl+C pour quitter)..."
        tail -f "$log_file"
    elif [[ -f "$LOG_DIR/angel.out" ]]; then
        print_info "Affichage des logs: $LOG_DIR/angel.out (Ctrl+C pour quitter)..."
        tail -f "$LOG_DIR/angel.out"
    else
        print_warning "Aucun fichier de log trouvé"
        ls -la "$LOG_DIR"/ 2>/dev/null || print_info "Aucun fichier de log trouvé"
    fi
}

run_tests() {
    print_info "Lancement des tests..."
    cd "$PROJECT_DIR"
    
    if [[ $VERBOSE == true ]]; then
        mvn test
    else
        mvn test -q
    fi
    
    if [[ $? -eq 0 ]]; then
        print_success "Tests réussis"
    else
        print_error "Échec des tests"
        exit 1
    fi
}

show_help() {
    cat << EOF
Angel Virtual Assistant - Script de lancement Spring Boot avec configuration externe

USAGE:
    $0 [COMMAND] [OPTIONS]

COMMANDS:
    start           Démarre l'application Spring Boot
    stop            Arrête l'application
    restart         Redémarre l'application
    status          Affiche le statut de l'application
    build           Compile le projet
    clean           Nettoie les fichiers compilés
    logs            Affiche les logs en temps réel
    test            Lance les tests unitaires
    test-web        Teste l'interface web
    help            Affiche cette aide

OPTIONS:
    -p, --profile PROFILE   Profil d'exécution (dev|prod|test, défaut: default)
    -m, --memory SIZE       Mémoire allouée (défaut: 512m)
    -d, --debug             Active le mode debug sur le port 5005
    -D, --debug-port PORT   Port de debug (défaut: 5005)
    -b, --daemon            Lance en mode daemon (arrière-plan)
    -v, --verbose           Mode verbose
    -h, --help              Affiche cette aide

EXEMPLES:
    $0 start                        # Démarre l'application (config/application.properties)
    $0 start -p test                # Démarre en mode test (config/application-test.properties)
    $0 start -m 1g -p prod          # Démarre avec 1GB de RAM en mode production
    $0 start -d                     # Démarre en mode debug
    $0 status                       # Affiche le statut et les URLs

CONFIGURATION:
    Configuration principale: config/application.properties
    Configuration test:       config/application-test.properties
    
    Les propriétés Spring Boot sont ajoutées automatiquement si manquantes.

INTERFACE WEB (dépend de la configuration):
    Mode normal:  http://localhost:8080/angel/test-dashboard
                  http://localhost:8080/angel/
    Mode test:    http://localhost:8081/test-dashboard
                  http://localhost:8081/angel

EOF
}

# Analyse des arguments
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -p|--profile)
                PROFILE="$2"
                shift 2
                ;;
            -m|--memory)
                MEMORY_XMX="$2"
                MEMORY_XMS="$2"
                shift 2
                ;;
            -d|--debug)
                DEBUG_MODE=true
                shift
                ;;
            -D|--debug-port)
                DEBUG_PORT="$2"
                shift 2
                ;;
            -b|--daemon)
                DAEMON_MODE=true
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            start|stop|restart|status|build|clean|logs|test|test-web|help)
                COMMAND="$1"
                shift
                ;;
            *)
                print_error "Option inconnue: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# Fonction principale
main() {
    COMMAND="${COMMAND:-help}"
    parse_arguments "$@"
    
    case $COMMAND in
        start) start_application ;;
        stop) stop_application ;;
        restart) restart_application ;;
        status) show_status ;;
        build) build_project ;;
        clean) clean_project ;;
        logs) show_logs ;;
        test) run_tests ;;
        test-web) test_web_interface ;;
        help) show_help ;;
        *)
            print_error "Commande inconnue: $COMMAND"
            show_help
            exit 1
            ;;
    esac
}

# Exécution
main "$@"
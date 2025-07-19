#!/bin/bash

# Angel Virtual Assistant - Script de lancement
# Auteur: Angel Project
# Version: 1.0.0

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

# Fonction d'affichage avec couleurs
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Fonction d'aide
show_help() {
    cat << EOF
Angel Virtual Assistant - Script de lancement

USAGE:
    $0 [COMMAND] [OPTIONS]

COMMANDS:
    start           Démarre l'application
    stop            Arrête l'application
    restart         Redémarre l'application
    status          Affiche le statut de l'application
    build           Compile le projet
    clean           Nettoie les fichiers compilés
    logs            Affiche les logs en temps réel
    test            Lance les tests unitaires
    help            Affiche cette aide

OPTIONS:
    -c, --config FILE       Fichier de configuration (défaut: config/application.properties)
    -m, --memory SIZE       Mémoire allouée (défaut: 512m)
    -p, --profile PROFILE   Profil d'exécution (dev|prod|test, défaut: default)
    -d, --debug             Active le mode debug sur le port 5005
    -D, --debug-port PORT   Port de debug (défaut: 5005)
    -b, --daemon            Lance en mode daemon (arrière-plan)
    -v, --verbose           Mode verbose
    -h, --help              Affiche cette aide

EXEMPLES:
    $0 start                        # Démarre l'application
    $0 start -m 1g -p prod          # Démarre avec 1GB de RAM en mode production
    $0 start -p test                # Démarre en mode test
    $0 start -d                     # Démarre en mode debug
    $0 start -b                     # Démarre en mode daemon
    $0 stop                         # Arrête l'application
    $0 restart -m 2g                # Redémarre avec 2GB de RAM
    $0 logs                         # Affiche les logs
    $0 build                        # Compile le projet

PROFILS:
    dev         Mode développement (logs verbeux, hot reload)
    prod        Mode production (logs optimisés, performances)
    test        Mode test (base de données en mémoire, simulation accélérée)
    default     Mode par défaut

FICHIERS DE CONFIGURATION:
    config/application.properties           # Configuration par défaut
    config/application-test.properties      # Configuration mode test
    config/application-prod.properties      # Configuration mode production
    config/application-dev.properties       # Configuration mode développement

GESTION DES PROCESSUS:
    Le script gère automatiquement le fichier PID et vérifie si l'application
    est déjà en cours d'exécution.

LOGS:
    Les logs sont disponibles dans: $LOG_DIR/angel.log
    Mode daemon: $LOG_DIR/angel.out et $LOG_DIR/angel.err

EOF
}

# Vérification des prérequis
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
    
    # Vérifier le fichier de configuration principal
    if [[ ! -f "$CONFIG_FILE" ]]; then
        print_warning "Fichier de configuration non trouvé: $CONFIG_FILE"
        print_info "Création d'un fichier de configuration par défaut..."
        create_default_config
    fi
    
    # Vérifier le fichier de configuration spécifique au profil
    if [[ "$PROFILE" != "default" ]]; then
        local profile_config="$PROJECT_DIR/config/application-$PROFILE.properties"
        if [[ ! -f "$profile_config" ]]; then
            print_warning "Fichier de configuration pour le profil '$PROFILE' non trouvé: $profile_config"
        else
            print_info "Configuration profil trouvée: $profile_config"
        fi
    fi
}

# Création d'un fichier de configuration par défaut
create_default_config() {
    mkdir -p "$(dirname "$CONFIG_FILE")"
    cat > "$CONFIG_FILE" << 'EOF'
# Configuration Angel Virtual Assistant par défaut

# Application
app.name=Angel Companion
app.version=1.0.0
system.name=Angel Companion
system.language=fr
system.wake-word=Angel

# API
api.angel-server-url=http://localhost:8080/api
api.polling-interval=30000
api.timeout=5000

# Base de données
database.url=jdbc:h2:file:./angel-db
database.driver=org.h2.Driver
database.username=angel
database.password=angel123

# Avatar
avatar.enabled=true
avatar.display-time=30000
avatar.transition-effect=fade
avatar.default-mood=neutral

# Logging
logging.level=INFO
logging.file-path=./logs/angel.log
logging.rotation-size=10MB
logging.max-files=5
EOF
    print_success "Fichier de configuration créé: $CONFIG_FILE"
}

# Vérification du statut de l'application
check_status() {
    if [[ -f "$PID_FILE" ]]; then
        local pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null 2>&1; then
            return 0  # Application en cours d'exécution
        else
            rm -f "$PID_FILE"
            return 1  # Fichier PID obsolète
        fi
    else
        return 1  # Pas de fichier PID
    fi
}

# Affichage du statut
show_status() {
    print_info "Vérification du statut..."
    
    if check_status; then
        local pid=$(cat "$PID_FILE")
        print_success "Angel Virtual Assistant est en cours d'exécution (PID: $pid)"
        print_info "Profil actif: $PROFILE"
        
        # Afficher des informations sur le processus
        echo "Détails du processus:"
        ps -p "$pid" -o pid,ppid,cmd,etime,pmem,pcpu 2>/dev/null || true
        
        # Afficher les dernières lignes de log
        local log_file="$LOG_DIR/angel.log"
        if [[ "$PROFILE" == "test" ]]; then
            log_file="$LOG_DIR/angel-test.log"
        fi
        
        if [[ -f "$log_file" ]]; then
            echo -e "\nDernières lignes de log:"
            tail -n 5 "$log_file"
        fi
    else
        print_warning "Angel Virtual Assistant n'est pas en cours d'exécution"
    fi
}

# Compilation du projet
build_project() {
    print_info "Compilation du projet..."
    
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

# Nettoyage
clean_project() {
    print_info "Nettoyage du projet..."
    
    cd "$PROJECT_DIR"
    mvn clean -q
    
    # Supprimer les fichiers temporaires
    rm -rf angel-db*
    rm -f "$PID_FILE"
    
    print_success "Nettoyage terminé"
}

# Configuration des options Java selon le profil
configure_java_opts() {
    # Options de base
    JAVA_OPTS="-Xms$MEMORY_XMS -Xmx$MEMORY_XMX"
    
    # Passer le profil via les arguments du programme ET les propriétés système
    if [[ "$PROFILE" != "default" ]]; then
        JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$PROFILE"
        # Ajouter aussi aux arguments du programme pour le ConfigManager
        PROGRAM_ARGS="-p $PROFILE"
    else
        PROGRAM_ARGS=""
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
            ;;
    esac
    
    # Mode debug
    if [[ $DEBUG_MODE == true ]]; then
        JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$DEBUG_PORT"
        print_info "Mode debug activé sur le port $DEBUG_PORT"
    fi
    
    # Mode verbose
    if [[ $VERBOSE == true ]]; then
        JAVA_OPTS="$JAVA_OPTS -verbose:gc -XX:+PrintGCDetails"
    fi
    
    print_info "Options JVM: $JAVA_OPTS"
    if [[ -n "$PROGRAM_ARGS" ]]; then
        print_info "Arguments programme: $PROGRAM_ARGS"
    fi
}

# Démarrage de l'application
start_application() {
    print_info "Démarrage d'Angel Virtual Assistant..."
    
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
    
    if [[ $DAEMON_MODE == true ]]; then
        # Mode daemon
        print_info "Démarrage en mode daemon..."
        
        if [[ -n "$PROGRAM_ARGS" ]]; then
            nohup java $JAVA_OPTS -jar "$jar_file" $PROGRAM_ARGS \
                > "$LOG_DIR/angel.out" 2> "$LOG_DIR/angel.err" &
        else
            nohup java $JAVA_OPTS -jar "$jar_file" \
                > "$LOG_DIR/angel.out" 2> "$LOG_DIR/angel.err" &
        fi
        
        local pid=$!
        echo $pid > "$PID_FILE"
        
        # Attendre un peu pour vérifier que l'application démarre
        sleep 3
        
        if check_status; then
            print_success "Angel Virtual Assistant démarré en mode daemon (PID: $pid)"
            print_info "Profil: $PROFILE"
            print_info "Logs disponibles dans: $LOG_DIR/angel.out et $LOG_DIR/angel.err"
        else
            print_error "Échec du démarrage"
            cat "$LOG_DIR/angel.err" 2>/dev/null || true
            exit 1
        fi
    else
        # Mode interactif
        print_info "Démarrage en mode interactif..."
        print_info "Profil: $PROFILE"
        print_info "Utilisez Ctrl+C pour arrêter l'application"
        
        # Créer un gestionnaire de signal pour nettoyer le fichier PID
        trap 'rm -f "$PID_FILE"; exit' INT TERM
        
        if [[ -n "$PROGRAM_ARGS" ]]; then
            java $JAVA_OPTS -jar "$jar_file" $PROGRAM_ARGS &
        else
            java $JAVA_OPTS -jar "$jar_file" &
        fi
        
        local pid=$!
        echo $pid > "$PID_FILE"
        
        # Attendre le processus
        wait $pid
        rm -f "$PID_FILE"
    fi
}

# Arrêt de l'application
stop_application() {
    print_info "Arrêt d'Angel Virtual Assistant..."
    
    if check_status; then
        local pid=$(cat "$PID_FILE")
        print_info "Arrêt du processus $pid..."
        
        # Arrêt gracieux
        kill -TERM "$pid" 2>/dev/null
        
        # Attendre jusqu'à 10 secondes pour l'arrêt gracieux
        local count=0
        while [[ $count -lt 10 ]] && ps -p "$pid" > /dev/null 2>&1; do
            sleep 1
            ((count++))
        done
        
        # Arrêt forcé si nécessaire
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

# Redémarrage
restart_application() {
    print_info "Redémarrage d'Angel Virtual Assistant..."
    stop_application
    sleep 2
    start_application
}

# Affichage des logs
show_logs() {
    local log_file="$LOG_DIR/angel.log"
    
    # Choisir le bon fichier de log selon le profil
    if [[ "$PROFILE" == "test" ]]; then
        log_file="$LOG_DIR/angel-test.log"
    fi
    
    if [[ -f "$log_file" ]]; then
        print_info "Affichage des logs: $log_file (Ctrl+C pour quitter)..."
        tail -f "$log_file"
    else
        print_warning "Fichier de log non trouvé: $log_file"
        print_info "Logs disponibles:"
        ls -la "$LOG_DIR"/*.log 2>/dev/null || print_info "Aucun fichier de log trouvé"
    fi
}

# Lancement des tests
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

# Analyse des arguments
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -c|--config)
                CONFIG_FILE="$2"
                shift 2
                ;;
            -m|--memory)
                MEMORY_XMX="$2"
                MEMORY_XMS="$2"
                shift 2
                ;;
            -p|--profile)
                PROFILE="$2"
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
            start|stop|restart|status|build|clean|logs|test|help)
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
    # Définir la commande par défaut
    COMMAND="${COMMAND:-help}"
    
    # Traitement des arguments
    parse_arguments "$@"
    
    # Exécution de la commande
    case $COMMAND in
        start)
            start_application
            ;;
        stop)
            stop_application
            ;;
        restart)
            restart_application
            ;;
        status)
            show_status
            ;;
        build)
            build_project
            ;;
        clean)
            clean_project
            ;;
        logs)
            show_logs
            ;;
        test)
            run_tests
            ;;
        help)
            show_help
            ;;
        *)
            print_error "Commande inconnue: $COMMAND"
            show_help
            exit 1
            ;;
    esac
}

# Exécution
main "$@"

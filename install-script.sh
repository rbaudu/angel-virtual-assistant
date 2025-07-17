#!/bin/bash

# Angel Virtual Assistant - Script d'installation
# Auteur: Angel Project
# Version: 1.0.0

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"
INSTALL_DIR="/opt/angel"
SERVICE_NAME="angel-virtual-assistant"
SERVICE_USER="angel"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Fonctions d'affichage
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
Angel Virtual Assistant - Script d'installation

USAGE:
    $0 [OPTIONS] [COMMAND]

COMMANDS:
    install         Installation complète du système
    uninstall       Désinstallation complète
    update          Mise à jour de l'application
    service         Gestion du service systemd
    help            Affiche cette aide

OPTIONS:
    --user          Installation pour l'utilisateur actuel (pas de service)
    --system        Installation système avec service (défaut)
    --install-dir   Répertoire d'installation (défaut: /opt/angel)
    --service-user  Utilisateur pour le service (défaut: angel)
    --no-service    Ne pas créer de service systemd

EXEMPLES:
    $0 install                          # Installation système complète
    $0 install --user                   # Installation utilisateur
    $0 install --install-dir /usr/local/angel
    $0 service start                    # Démarrer le service
    $0 service enable                   # Activer le service au démarrage
    $0 uninstall                        # Désinstallation

EOF
}

# Vérification des droits
check_permissions() {
    if [[ $EUID -ne 0 ]] && [[ "$USER_INSTALL" != "true" ]]; then
        print_error "Ce script doit être exécuté en tant que root pour une installation système"
        print_info "Utilisez 'sudo $0' ou '--user' pour une installation utilisateur"
        exit 1
    fi
}

# Vérification des prérequis
check_prerequisites() {
    print_info "Vérification des prérequis..."
    
    # Vérifier Java
    if ! command -v java &> /dev/null; then
        print_error "Java n'est pas installé"
        print_info "Installez Java 17 ou supérieur:"
        print_info "  Ubuntu/Debian: sudo apt install openjdk-17-jdk"
        print_info "  CentOS/RHEL: sudo yum install java-17-openjdk-devel"
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [[ $java_version -lt 17 ]]; then
        print_error "Java 17 ou supérieur requis (version détectée: $java_version)"
        exit 1
    fi
    
    # Vérifier Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven n'est pas installé"
        print_info "Installez Maven:"
        print_info "  Ubuntu/Debian: sudo apt install maven"
        print_info "  CentOS/RHEL: sudo yum install maven"
        exit 1
    fi
    
    print_success "Prérequis vérifiés"
}

# Création de l'utilisateur système
create_system_user() {
    if [[ "$USER_INSTALL" == "true" ]]; then
        return 0
    fi
    
    print_info "Création de l'utilisateur système $SERVICE_USER..."
    
    if ! id "$SERVICE_USER" &>/dev/null; then
        useradd -r -s /bin/false -d "$INSTALL_DIR" "$SERVICE_USER"
        print_success "Utilisateur $SERVICE_USER créé"
    else
        print_info "Utilisateur $SERVICE_USER existe déjà"
    fi
}

# Compilation du projet
build_project() {
    print_info "Compilation du projet..."
    
    cd "$PROJECT_DIR"
    
    if ! mvn clean package -DskipTests=false; then
        print_error "Échec de la compilation"
        exit 1
    fi
    
    print_success "Compilation réussie"
}

# Installation des fichiers
install_files() {
    print_info "Installation des fichiers..."
    
    # Créer le répertoire d'installation
    mkdir -p "$INSTALL_DIR"/{bin,config,logs,lib}
    
    # Copier les fichiers
    cp "$PROJECT_DIR/target/angel-virtual-assistant-1.0.0-SNAPSHOT-jar-with-dependencies.jar" "$INSTALL_DIR/lib/"
    cp "$PROJECT_DIR/angel.sh" "$INSTALL_DIR/bin/"
    cp "$PROJECT_DIR/config/angel-config.json" "$INSTALL_DIR/config/"
    
    # Créer le script de lancement système
    cat > "$INSTALL_DIR/bin/angel" << EOF
#!/bin/bash
cd "$INSTALL_DIR"
exec "$INSTALL_DIR/bin/angel.sh" "\$@"
EOF
    
    # Rendre les scripts exécutables
    chmod +x "$INSTALL_DIR/bin/angel"
    chmod +x "$INSTALL_DIR/bin/angel.sh"
    
    # Définir les permissions
    if [[ "$USER_INSTALL" != "true" ]]; then
        chown -R "$SERVICE_USER:$SERVICE_USER" "$INSTALL_DIR"
        chmod 755 "$INSTALL_DIR"
        chmod 755 "$INSTALL_DIR/bin"
        chmod 755 "$INSTALL_DIR/config"
        chmod 755 "$INSTALL_DIR/logs"
        chmod 755 "$INSTALL_DIR/lib"
    fi
    
    print_success "Fichiers installés dans $INSTALL_DIR"
}

# Création du service systemd
create_service() {
    if [[ "$USER_INSTALL" == "true" ]] || [[ "$NO_SERVICE" == "true" ]]; then
        return 0
    fi
    
    print_info "Création du service systemd..."
    
    cat > "/etc/systemd/system/$SERVICE_NAME.service" << EOF
[Unit]
Description=Angel Virtual Assistant
After=network.target

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_USER
WorkingDirectory=$INSTALL_DIR
ExecStart=$INSTALL_DIR/bin/angel start --daemon
ExecStop=$INSTALL_DIR/bin/angel stop
Restart=always
RestartSec=10

# Sécurité
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ReadWritePaths=$INSTALL_DIR/logs $INSTALL_DIR/config
ProtectHome=true

# Limites
LimitNOFILE=65535
LimitNPROC=4096

[Install]
WantedBy=multi-user.target
EOF
    
    # Recharger systemd
    systemctl daemon-reload
    
    print_success "Service systemd créé"
}

# Installation du lien symbolique
install_symlink() {
    if [[ "$USER_INSTALL" == "true" ]]; then
        # Installation utilisateur
        local bin_dir="$HOME/.local/bin"
        mkdir -p "$bin_dir"
        ln -sf "$INSTALL_DIR/bin/angel" "$bin_dir/angel"
        print_success "Lien symbolique créé dans $bin_dir/angel"
    else
        # Installation système
        ln -sf "$INSTALL_DIR/bin/angel" "/usr/local/bin/angel"
        print_success "Lien symbolique créé dans /usr/local/bin/angel"
    fi
}

# Installation complète
install_system() {
    print_info "Installation d'Angel Virtual Assistant..."
    
    check_permissions
    check_prerequisites
    create_system_user
    build_project
    install_files
    create_service
    install_symlink
    
    print_success "Installation terminée !"
    print_info "Commandes disponibles:"
    print_info "  angel start    - Démarrer l'application"
    print_info "  angel stop     - Arrêter l'application"
    print_info "  angel status   - Voir le statut"
    print_info "  angel help     - Aide"
    
    if [[ "$USER_INSTALL" != "true" ]] && [[ "$NO_SERVICE" != "true" ]]; then
        print_info "Service systemd:"
        print_info "  sudo systemctl start $SERVICE_NAME"
        print_info "  sudo systemctl enable $SERVICE_NAME"
        print_info "  sudo systemctl status $SERVICE_NAME"
    fi
}

# Désinstallation
uninstall_system() {
    print_info "Désinstallation d'Angel Virtual Assistant..."
    
    check_permissions
    
    # Arrêter le service
    if systemctl is-active --quiet "$SERVICE_NAME" 2>/dev/null; then
        print_info "Arrêt du service..."
        systemctl stop "$SERVICE_NAME"
    fi
    
    # Désactiver le service
    if systemctl is-enabled --quiet "$SERVICE_NAME" 2>/dev/null; then
        print_info "Désactivation du service..."
        systemctl disable "$SERVICE_NAME"
    fi
    
    # Supprimer le service
    if [[ -f "/etc/systemd/system/$SERVICE_NAME.service" ]]; then
        rm -f "/etc/systemd/system/$SERVICE_NAME.service"
        systemctl daemon-reload
        print_info "Service systemd supprimé"
    fi
    
    # Supprimer les liens symboliques
    rm -f "/usr/local/bin/angel"
    rm -f "$HOME/.local/bin/angel"
    
    # Supprimer le répertoire d'installation
    if [[ -d "$INSTALL_DIR" ]]; then
        read -p "Supprimer le répertoire d'installation $INSTALL_DIR ? (y/N) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            rm -rf "$INSTALL_DIR"
            print_info "Répertoire d'installation supprimé"
        fi
    fi
    
    # Supprimer l'utilisateur système
    if [[ "$USER_INSTALL" != "true" ]] && id "$SERVICE_USER" &>/dev/null; then
        read -p "Supprimer l'utilisateur système $SERVICE_USER ? (y/N) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            userdel "$SERVICE_USER"
            print_info "Utilisateur système supprimé"
        fi
    fi
    
    print_success "Désinstallation terminée"
}

# Mise à jour
update_system() {
    print_info "Mise à jour d'Angel Virtual Assistant..."
    
    check_permissions
    
    # Arrêter le service si actif
    local was_running=false
    if systemctl is-active --quiet "$SERVICE_NAME" 2>/dev/null; then
        was_running=true
        systemctl stop "$SERVICE_NAME"
    fi
    
    # Sauvegarder la configuration
    if [[ -f "$INSTALL_DIR/config/angel-config.json" ]]; then
        cp "$INSTALL_DIR/config/angel-config.json" "$INSTALL_DIR/config/angel-config.json.backup"
        print_info "Configuration sauvegardée"
    fi
    
    # Recompiler et réinstaller
    build_project
    install_files
    
    # Restaurer la configuration
    if [[ -f "$INSTALL_DIR/config/angel-config.json.backup" ]]; then
        mv "$INSTALL_DIR/config/angel-config.json.backup" "$INSTALL_DIR/config/angel-config.json"
        print_info "Configuration restaurée"
    fi
    
    # Redémarrer si nécessaire
    if [[ "$was_running" == "true" ]]; then
        systemctl start "$SERVICE_NAME"
        print_info "Service redémarré"
    fi
    
    print_success "Mise à jour terminée"
}

# Gestion du service
manage_service() {
    local action="$1"
    
    case "$action" in
        start)
            systemctl start "$SERVICE_NAME"
            print_success "Service démarré"
            ;;
        stop)
            systemctl stop "$SERVICE_NAME"
            print_success "Service arrêté"
            ;;
        restart)
            systemctl restart "$SERVICE_NAME"
            print_success "Service redémarré"
            ;;
        enable)
            systemctl enable "$SERVICE_NAME"
            print_success "Service activé au démarrage"
            ;;
        disable)
            systemctl disable "$SERVICE_NAME"
            print_success "Service désactivé au démarrage"
            ;;
        status)
            systemctl status "$SERVICE_NAME"
            ;;
        *)
            print_error "Action inconnue: $action"
            print_info "Actions disponibles: start, stop, restart, enable, disable, status"
            exit 1
            ;;
    esac
}

# Analyse des arguments
COMMAND=""
USER_INSTALL="false"
NO_SERVICE="false"

while [[ $# -gt 0 ]]; do
    case $1 in
        --user)
            USER_INSTALL="true"
            INSTALL_DIR="$HOME/.local/share/angel"
            shift
            ;;
        --system)
            USER_INSTALL="false"
            shift
            ;;
        --install-dir)
            INSTALL_DIR="$2"
            shift 2
            ;;
        --service-user)
            SERVICE_USER="$2"
            shift 2
            ;;
        --no-service)
            NO_SERVICE="true"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        install|uninstall|update|service|help)
            COMMAND="$1"
            shift
            break
            ;;
        *)
            print_error "Option inconnue: $1"
            show_help
            exit 1
            ;;
    esac
done

# Définir la commande par défaut
COMMAND="${COMMAND:-help}"

# Exécution
case "$COMMAND" in
    install)
        install_system
        ;;
    uninstall)
        uninstall_system
        ;;
    update)
        update_system
        ;;
    service)
        manage_service "$1"
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

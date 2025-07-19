#!/bin/bash

# Script de test de configuration Angel Virtual Assistant
# Vérifie que la nouvelle configuration fonctionne correctement

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

print_info "Test de la configuration harmonisée Angel Virtual Assistant"
echo "================================================================"

# Test 1: Vérifier la présence des fichiers de configuration
print_info "Test 1: Vérification des fichiers de configuration"

config_files=(
    "config/application.properties"
    "config/application-test.properties"
    "src/main/resources/config/avatar.properties"
    "src/main/resources/config/phoneme-viseme-mapping.properties"
)

all_configs_ok=true
for file in "${config_files[@]}"; do
    if [[ -f "$PROJECT_DIR/$file" ]]; then
        print_success "✓ $file trouvé"
    else
        print_error "✗ $file manquant"
        all_configs_ok=false
    fi
done

# Vérifier que l'ancien fichier JSON n'est plus utilisé
if [[ -f "$PROJECT_DIR/config/angel-config.json" ]]; then
    if [[ -s "$PROJECT_DIR/config/angel-config.json" ]]; then
        print_warning "⚠ config/angel-config.json n'est pas vide - devrait être supprimé"
    else
        print_success "✓ config/angel-config.json est vide (peut être supprimé)"
    fi
else
    print_success "✓ config/angel-config.json supprimé"
fi

if [[ $all_configs_ok == true ]]; then
    print_success "Tous les fichiers de configuration sont présents"
else
    print_error "Certains fichiers de configuration sont manquants"
    exit 1
fi

echo

# Test 2: Vérifier la compilation
print_info "Test 2: Compilation du projet"
cd "$PROJECT_DIR"

if mvn clean compile -q; then
    print_success "Compilation réussie"
else
    print_error "Échec de la compilation"
    exit 1
fi

echo

# Test 3: Test de la structure des propriétés
print_info "Test 3: Vérification de la structure des propriétés"

required_properties=(
    "system.name"
    "system.language"
    "database.url"
    "database.driver"
    "api.angel-server-url"
    "avatar.enabled"
    "logging.level"
)

config_file="$PROJECT_DIR/config/application.properties"
props_ok=true

for prop in "${required_properties[@]}"; do
    if grep -q "^$prop=" "$config_file"; then
        print_success "✓ Propriété $prop trouvée"
    else
        print_error "✗ Propriété $prop manquante"
        props_ok=false
    fi
done

if [[ $props_ok == true ]]; then
    print_success "Toutes les propriétés essentielles sont présentes"
else
    print_error "Certaines propriétés essentielles sont manquantes"
fi

echo

# Test 4: Test des profils
print_info "Test 4: Test des configurations de profil"

# Vérifier les propriétés spécifiques au mode test
test_config="$PROJECT_DIR/config/application-test.properties"
if [[ -f "$test_config" ]]; then
    if grep -q "angel.test.enabled=true" "$test_config"; then
        print_success "✓ Configuration test correctement définie"
    else
        print_warning "Configuration test incomplète"
    fi
    
    if grep -q "jdbc:h2:mem:" "$test_config"; then
        print_success "✓ Base de données en mémoire configurée pour test"
    else
        print_warning "Base de données test pourrait ne pas être en mémoire"
    fi
else
    print_error "✗ Fichier de configuration test manquant"
fi

echo

# Test 5: Test du script de lancement
print_info "Test 5: Test du script de lancement"

if [[ -x "$PROJECT_DIR/angel-launcher.sh" ]]; then
    print_success "✓ Script de lancement exécutable"
    
    # Test de l'aide
    if "$PROJECT_DIR/angel-launcher.sh" help > /dev/null 2>&1; then
        print_success "✓ Script de lancement fonctionne (commande help)"
    else
        print_warning "Script de lancement pourrait avoir des problèmes"
    fi
else
    print_error "✗ Script de lancement manquant ou non exécutable"
fi

echo

# Test 6: Test de build du JAR
print_info "Test 6: Test de génération du JAR"

if mvn clean package -DskipTests -q; then
    jar_file="$PROJECT_DIR/target/angel-virtual-assistant-1.0.0-SNAPSHOT.jar"
    if [[ -f "$jar_file" ]]; then
        print_success "✓ JAR généré avec succès"
        
        # Test rapide de démarrage (5 secondes max)
        print_info "Test rapide du démarrage de l'application..."
        timeout 5s java -jar "$jar_file" -p test > /dev/null 2>&1 &
        java_pid=$!
        
        sleep 2
        if ps -p $java_pid > /dev/null 2>&1; then
            print_success "✓ Application démarre sans erreur critique"
            kill $java_pid 2>/dev/null
        else
            print_warning "Application s'arrête rapidement (normal si serveur externe absent)"
        fi
    else
        print_error "✗ JAR non généré"
    fi
else
    print_error "✗ Échec de la génération du JAR"
fi

echo

# Résumé
print_info "Résumé des tests"
echo "================================================================"

if [[ $all_configs_ok == true && $props_ok == true ]]; then
    print_success "🎉 Configuration harmonisée avec succès !"
    echo
    print_info "Prêt à utiliser :"
    echo "  • Lancer par défaut    : ./angel-launcher.sh start"
    echo "  • Lancer en mode test  : ./angel-launcher.sh start -p test"
    echo "  • Voir le statut       : ./angel-launcher.sh status"
    echo "  • Voir les logs        : ./angel-launcher.sh logs"
    echo
    print_info "Configuration active :"
    echo "  • Principal : config/application.properties"
    echo "  • Test      : config/application-test.properties"
    echo "  • Techniques: src/main/resources/config/*.properties"
    echo
    print_success "La migration de configuration est terminée ! ✅"
    
    # Suggestion de suppression manuelle
    if [[ -f "$PROJECT_DIR/config/angel-config.json" ]]; then
        echo
        print_info "💡 Optionnel : Vous pouvez supprimer manuellement le fichier vide :"
        echo "    rm config/angel-config.json"
    fi
else
    print_error "❌ Des problèmes ont été détectés dans la configuration"
    echo
    print_info "Veuillez vérifier :"
    echo "  • La présence de tous les fichiers de configuration"
    echo "  • La syntaxe des fichiers properties"
    echo "  • Les propriétés obligatoires"
    exit 1
fi

exit 0

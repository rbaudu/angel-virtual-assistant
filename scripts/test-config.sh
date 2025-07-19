#!/bin/bash

# Script de test de configuration Angel Virtual Assistant
# Vérifie que la nouvelle configuration fonctionne correctement

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

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

print_info "Test de la nouvelle configuration Angel Virtual Assistant"
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

# Test 3: Test du ConfigManager (si possible)
print_info "Test 3: Test du gestionnaire de configuration"

# Créer un petit test Java temporaire
cat > /tmp/ConfigTest.java << 'EOF'
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigTest {
    public static void main(String[] args) {
        System.out.println("Test de base des fichiers de configuration...");
        
        // Vérifier les fichiers externes
        boolean hasMain = Files.exists(Paths.get("config/application.properties"));
        boolean hasTest = Files.exists(Paths.get("config/application-test.properties"));
        
        System.out.println("config/application.properties: " + (hasMain ? "OK" : "MANQUANT"));
        System.out.println("config/application-test.properties: " + (hasTest ? "OK" : "MANQUANT"));
        
        if (hasMain && hasTest) {
            System.out.println("SUCCÈS: Configuration externe trouvée");
            System.exit(0);
        } else {
            System.out.println("ERREUR: Configuration externe manquante");
            System.exit(1);
        }
    }
}
EOF

if javac /tmp/ConfigTest.java -d /tmp && java -cp /tmp ConfigTest; then
    print_success "Test de configuration réussi"
else
    print_warning "Test de configuration partiel"
fi

echo

# Test 4: Vérifier la structure des propriétés
print_info "Test 4: Vérification de la structure des propriétés"

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

# Test 5: Test des profils
print_info "Test 5: Test des configurations de profil"

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

# Test 6: Test du script de lancement
print_info "Test 6: Test du script de lancement"

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

# Résumé
print_info "Résumé des tests"
echo "================================================================"

if [[ $all_configs_ok == true && $props_ok == true ]]; then
    print_success "🎉 Configuration harmonisée avec succès !"
    echo
    print_info "Vous pouvez maintenant :"
    echo "  • Lancer en mode par défaut : ./angel-launcher.sh start"
    echo "  • Lancer en mode test       : ./angel-launcher.sh start -p test"
    echo "  • Voir le statut            : ./angel-launcher.sh status"
    echo "  • Voir les logs             : ./angel-launcher.sh logs"
    echo
    print_info "Fichiers de configuration actifs :"
    echo "  • Principal : config/application.properties"
    echo "  • Test      : config/application-test.properties"
    echo "  • Techniques: src/main/resources/config/*.properties"
    echo
    print_success "La migration de configuration est terminée !"
else
    print_error "❌ Des problèmes ont été détectés dans la configuration"
    echo
    print_info "Veuillez vérifier :"
    echo "  • La présence de tous les fichiers de configuration"
    echo "  • La syntaxe des fichiers properties"
    echo "  • Les propriétés obligatoires"
    exit 1
fi

# Nettoyer les fichiers temporaires
rm -f /tmp/ConfigTest.java /tmp/ConfigTest.class

exit 0

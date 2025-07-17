#!/bin/bash

# Script optimisé pour tester les modèles de détection via ExempleComplet

# Définir le répertoire du projet
PROJECT_DIR="$(dirname "$(dirname "$(readlink -f "$0")")")"
cd "$PROJECT_DIR"

# Charger les fonctions communes
source scripts/common_functions.sh

# Afficher l'aide
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo "Teste des prédictions de tous les modèles via com.angel.tool.ml.examples.ExempleComplet"
    echo "S'appuie sur la configuration donnée par config/application.properties"
    echo ""
    echo "Options:"
    echo "  -h, --help       Afficher l'aide"
    echo "  -m, --memory N   Définit la mémoire JVM maximale en Go (par défaut: 4)"
    echo "  -c, --cpu N      Définit le nombre de threads pour les calculs (par défaut: auto)"
    echo "  -ft, --force     Forcer le ré-entraînement du modèle"
    echo "  -t, --type TYPE  OBLIGATOIRE - Type de modèle à utiliser parmi :"
    echo "                   all, presence, presence_standard, presence_yolo, presence_yolo2,"
    echo "                   activity, activity_standard, activity_vgg16, activity_resnet,"
    echo "                   sound, sound_standard, sound_mfcc, sound_spectrogram"
    echo "  -d, --dir PATH   Chemin des modèles entraînés (ex: models/sound/sound_spectrogram_model.zip)"
    echo "  --debug          Active les logs de débogage"
    echo "  --heap-dump      Active les dumps heap en cas d'erreur OOM"
    echo "  --timeout N      Définit un timeout en secondes (par défaut: 300)"
    echo ""
    echo "Exemples:"
    echo "  $0 -t activity                           # Test du modèle d'activité"
    echo "  $0 -t sound_spectrogram -m 6            # Test du modèle spectrogram avec 6GB RAM"
    echo "  $0 -t presence_yolo -d models/custom.zip # Test avec modèle personnalisé"
    echo "  $0 -t all --force --debug               # Test de tous les modèles avec ré-entraînement"
    echo "  "
    echo "  Note: si '-d' n'est pas fourni, prend les modèles définis dans le fichier de configuration : par exemple si sound.model.type=SPECTROGRAM, prendra la valeur du paramètre 'sound.spectrogram.model.path'"
    echo "  "
    show_common_help
    exit 0
}

# Variables
TYPE=""
MODEL_PATH=""
FORCE=false
MEMORY="4"
CPU=""
DEBUG=false
HEAP_DUMP=false
TIMEOUT=300

# Traiter les arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -m|--memory)
            MEMORY=$2
            shift 2
            ;;
        -c|--cpu)
            CPU=$2
            shift 2
            ;;
        -ft|--force)
            FORCE=true
            shift
            ;;
        -t|--type)
            TYPE=$2
            shift 2
            ;;
        -d|--dir)
            MODEL_PATH=$2
            shift 2
            ;;
        --debug)
            DEBUG=true
            shift
            ;;
        --heap-dump)
            HEAP_DUMP=true
            shift
            ;;
        --timeout)
            TIMEOUT=$2
            shift 2
            ;;
        *)
            echo -e "${RED}Option inconnue: $1${NC}"
            show_help
            ;;
    esac
done

# Vérifier que le type est fourni
if [[ -z "$TYPE" ]]; then
    echo -e "${RED}Erreur: Le paramètre -t/--type est obligatoire${NC}"
    echo "Utilisez -h pour voir les types disponibles"
    exit 1
fi

# Valider le type de modèle
VALID_TYPES="all|presence|presence_standard|presence_yolo|presence_yolo2|activity|activity_standard|activity_vgg16|activity_resnet|sound|sound_standard|sound_mfcc|sound_spectrogram"
if [[ ! "$TYPE" =~ ^($VALID_TYPES)$ ]]; then
    echo -e "${RED}Erreur: Type de modèle invalide '$TYPE'${NC}"
    echo "Types valides: all, presence, presence_standard, presence_yolo, presence_yolo2,"
    echo "               activity, activity_standard, activity_vgg16, activity_resnet,"
    echo "               sound, sound_standard, sound_mfcc, sound_spectrogram"
    exit 1
fi

echo -e "${BLUE}=== Test optimisé des modèles de détection ===${NC}"
echo "Type de modèle(s) à tester: $TYPE"
if [ ! -z "$MODEL_PATH" ]; then
    echo "Modèle spécifique: $MODEL_PATH"
fi
echo "Mémoire JVM: ${MEMORY}G"
if [ ! -z "$CPU" ]; then
    echo "Threads CPU: $CPU"
fi
if [ "$FORCE" = true ]; then
    echo "Ré-entraînement: ${YELLOW}Forcé${NC}"
fi
if [ "$DEBUG" = true ]; then
    echo "Mode debug: ${YELLOW}Activé${NC}"
fi
echo "Timeout: ${TIMEOUT}s"
echo ""

# Vérifier et construire le projet si nécessaire
if ! check_build; then
    echo -e "${RED}Erreur lors de la vérification du build.${NC}"
    exit 1
fi

# Vérifier la configuration
if ! check_config; then
    exit 1
fi

# Afficher l'état des modèles
show_models_status

# Construire le classpath optimisé
CLASSPATH=$(build_classpath)
if [ $? -ne 0 ]; then
    echo -e "${RED}Erreur lors de la construction du classpath${NC}"
    exit 1
fi

# Configurer les options JVM
JVM_OPTS=$(setup_jvm_options "$MEMORY" "$CPU" "$DEBUG" "$HEAP_DUMP")

# Construire les arguments pour ExempleComplet
EXEMPLE_ARGS="-t $TYPE"

if [ "$FORCE" = true ]; then
    EXEMPLE_ARGS="$EXEMPLE_ARGS -ft"
fi

if [ ! -z "$MODEL_PATH" ]; then
    EXEMPLE_ARGS="$EXEMPLE_ARGS -d $MODEL_PATH"
fi

# Fonction pour exécuter le test avec timeout et monitoring
run_prediction_test() {
    echo -e "${BLUE}=== Lancement du test de prédiction ===${NC}"
    echo "Classe: com.angel.tool.ml.examples.ExempleComplet"
    echo "Arguments: $EXEMPLE_ARGS"
    echo ""
    
    # Créer un fichier de log
    local log_file="logs/prediction_test_${TYPE}_$(date +%Y%m%d_%H%M%S).log"
    mkdir -p logs
    
    echo "Logs sauvegardés dans: $log_file"
    echo "Commande complète:"
    echo "java $JVM_OPTS -cp \"target/dl4j-detection-models-1.0-SNAPSHOT.jar;lib/*\" com.angel.tool.ml.examples.ExempleComplet $EXEMPLE_ARGS"
    echo ""
    
    # Exécuter avec timeout
    local start_time=$(date +%s)
    
    if [ "$TIMEOUT" -gt 0 ]; then
        timeout $TIMEOUT java $JVM_OPTS -cp "target/dl4j-detection-models-1.0-SNAPSHOT.jar;lib/*" com.angel.tool.ml.examples.ExempleComplet $EXEMPLE_ARGS 2>&1 | tee "$log_file" &
    else
        #Ne marche pas en git bash : java $JVM_OPTS -cp "$CLASSPATH" com.angel.tool.ml.examples.ExempleComplet $EXEMPLE_ARGS 2>&1 | tee "$log_file" &
        java $JVM_OPTS -cp "target/dl4j-detection-models-1.0-SNAPSHOT.jar;lib/*" com.angel.tool.ml.examples.ExempleComplet $EXEMPLE_ARGS 2>&1 | tee "$log_file" &
    fi
    
    local java_pid=$!
    
    # Fonction de gestion des signaux
    trap 'echo -e "\n${YELLOW}Arrêt demandé...${NC}"; kill $java_pid 2>/dev/null; wait $java_pid 2>/dev/null; exit 130' INT
    
    # Monitoring de la progression
    local last_update=0
    while kill -0 $java_pid 2>/dev/null; do
        local current_time=$(date +%s)
        
        # Afficher la progression toutes les 10 secondes
        if [ $((current_time - last_update)) -ge 10 ]; then
            show_progress "$log_file" "$TYPE"
            last_update=$current_time
        fi
        
        sleep 2
    done
    
    # Récupérer le code de retour
    wait $java_pid
    local result=$?
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    echo ""  # Nouvelle ligne après la progression
    
    # Analyser le résultat
    if [ $result -eq 124 ]; then
        echo -e "${RED}⚠️ TIMEOUT: Le test a dépassé $TIMEOUT secondes et a été interrompu${NC}"
        return 1
    elif [ $result -ne 0 ]; then
        echo -e "${RED}✗ Échec du test de prédiction (code: $result, durée: ${duration}s)${NC}"
        
        # Analyser les erreurs communes
        if grep -q "OutOfMemoryError" "$log_file"; then
            echo -e "${RED}Erreur de mémoire détectée! Essayez d'augmenter la mémoire avec -m${NC}"
        fi
        
        if grep -q "ClassNotFoundException" "$log_file"; then
            echo -e "${RED}Classe non trouvée! Vérifiez que le projet a été compilé correctement.${NC}"
        fi
        
        if grep -q "FileNotFoundException" "$log_file"; then
            echo -e "${RED}Modèle non trouvé! Vérifiez le chemin du modèle ou entraînez-le d'abord.${NC}"
        fi
        
        echo "Consultez le fichier de log pour plus de détails: $log_file"
        return 1
    else
        echo -e "${GREEN}✓ Test de prédiction réussi! (durée: ${duration}s)${NC}"
        
        # Extraire et afficher les résultats de prédiction
        echo ""
        echo -e "${BLUE}Résultats de prédiction:${NC}"
        
        if grep -q "Prédiction:" "$log_file"; then
            echo "Prédictions trouvées:"
            grep "Prédiction:" "$log_file" | tail -10
        fi
        
        if grep -q "Précision:" "$log_file"; then
            local accuracy=$(grep "Précision:" "$log_file" | tail -1)
            echo "Performance: $accuracy"
        fi
        
        if grep -q "Temps de traitement:" "$log_file"; then
            local processing_time=$(grep "Temps de traitement:" "$log_file" | tail -1)
            echo "Performance: $processing_time"
        fi
        
        # Nettoyer le log si debug désactivé et test réussi
        if [ "$DEBUG" = false ]; then
            rm -f "$log_file"
        fi
        
        return 0
    fi
}

# Vérifier l'existence des modèles selon le type
check_model_availability() {
    local type="$1"
    
    case $type in
        all)
            echo -e "${YELLOW}Vérification de la disponibilité des modèles...${NC}"
            local missing_models=()
            
            if ! model_exists "presence"; then
                missing_models+=("presence")
            fi
            if ! model_exists "activity"; then
                missing_models+=("activity")
            fi
            if ! model_exists "sound"; then
                missing_models+=("sound")
            fi
            
            if [ ${#missing_models[@]} -gt 0 ]; then
                echo -e "${YELLOW}Modèles manquants: ${missing_models[*]}${NC}"
                echo "Ces modèles seront ignorés ou créés avec des poids aléatoires."
                return 1
            fi
            ;;
        presence*|activity*|sound*)
            local base_model=$(echo "$type" | cut -d'_' -f1)
             if ! model_exists "$base_model" && [ -z "$MODEL_PATH" ]; then
                echo -e "${YELLOW}Le modèle $base_model n'a pas encore été entraîné.${NC}"
                echo "Vous pouvez l'entraîner avec: ./scripts/train_all.sh $base_model"
                return 1
            fi
            ;;
    esac
    
    return 0
}

# Vérifier la disponibilité des modèles
if [ "$FORCE" = false ]; then
    check_model_availability "$TYPE"
    if [ $? -ne 0 ] && [ -z "$MODEL_PATH" ]; then
        echo ""
        read -p "Voulez-vous continuer malgré les modèles manquants? (o/n): " response
        if [[ ! "$response" =~ ^[oO]$ ]]; then
            echo "Test annulé par l'utilisateur."
            exit 0
        fi
    fi
fi

# Exécuter le test de prédiction
TEST_START_TIME=$(date +%s)

run_prediction_test
TEST_RESULT=$?

TEST_END_TIME=$(date +%s)
TEST_DURATION=$((TEST_END_TIME - TEST_START_TIME))

# Afficher le résumé final
echo ""
echo -e "${BLUE}=== Résumé final du test de prédiction ===${NC}"
echo "Type testé: $TYPE"
echo "Durée totale: $(date -u -d @$TEST_DURATION +'%H:%M:%S')"

if [ $TEST_RESULT -eq 0 ]; then
    echo -e "${GREEN}Test de prédiction terminé avec succès!${NC}"
    echo ""
    echo "Le modèle $TYPE fonctionne correctement et peut être utilisé pour les prédictions."
else
    echo -e "${RED}Test de prédiction terminé avec des erreurs.${NC}"
    echo ""
    echo "Conseils de dépannage:"
    echo "  - Vérifiez que le modèle a été entraîné: ./scripts/train_all.sh ${TYPE%_*}"
    echo "  - Augmentez la mémoire si nécessaire: $0 -t $TYPE -m 6"
    echo "  - Utilisez le mode debug pour plus d'informations: $0 -t $TYPE --debug"
    echo "  - Testez avec un modèle spécifique: $0 -t $TYPE -d path/to/model.zip"
    echo "  - Forcez le ré-entraînement: $0 -t $TYPE --force"
fi

exit $TEST_RESULT
@echo off
setlocal enabledelayedexpansion

:: Angel Virtual Assistant - Script de lancement Windows
:: Auteur: Angel Project
:: Version: 1.0.0

:: Configuration par défaut
set "PROJECT_DIR=%~dp0"
set "CONFIG_FILE=%PROJECT_DIR%config\angel-config.json"
set "LOG_DIR=%PROJECT_DIR%logs"
set "PID_FILE=%PROJECT_DIR%angel.pid"
set "MEMORY_XMX=512m"
set "MEMORY_XMS=256m"
set "PROFILE=default"
set "DEBUG_PORT=5005"
set "DEBUG_MODE=false"
set "DAEMON_MODE=false"
set "VERBOSE=false"
set "COMMAND="

:: Couleurs pour l'affichage (si supportées)
set "RED=[31m"
set "GREEN=[32m"
set "YELLOW=[33m"
set "BLUE=[34m"
set "NC=[0m"

:: Fonction d'aide
:show_help
echo Angel Virtual Assistant - Script de lancement Windows
echo.
echo USAGE:
echo     %~nx0 [COMMAND] [OPTIONS]
echo.
echo COMMANDS:
echo     start           Démarre l'application
echo     stop            Arrête l'application
echo     restart         Redémarre l'application
echo     status          Affiche le statut de l'application
echo     build           Compile le projet
echo     clean           Nettoie les fichiers compilés
echo     test            Lance les tests unitaires
echo     help            Affiche cette aide
echo.
echo OPTIONS:
echo     -c FILE         Fichier de configuration
echo     -m SIZE         Mémoire allouée (défaut: 512m)
echo     -p PROFILE      Profil d'exécution (dev^|prod^|test)
echo     -d              Active le mode debug
echo     -D PORT         Port de debug (défaut: 5005)
echo     -v              Mode verbose
echo     -h              Affiche cette aide
echo.
echo EXEMPLES:
echo     %~nx0 start                 # Démarre l'application
echo     %~nx0 start -m 1g -p prod   # Démarre avec 1GB de RAM en production
echo     %~nx0 start -d              # Démarre en mode debug
echo     %~nx0 stop                  # Arrête l'application
echo     %~nx0 build                 # Compile le projet
echo.
goto :eof

:: Fonction d'affichage avec couleurs
:print_info
echo [INFO] %~1
goto :eof

:print_success
echo [SUCCESS] %~1
goto :eof

:print_warning
echo [WARNING] %~1
goto :eof

:print_error
echo [ERROR] %~1
goto :eof

:: Vérification des prérequis
:check_prerequisites
call :print_info "Vérification des prérequis..."

:: Vérifier Java
java -version >nul 2>&1
if !errorlevel! neq 0 (
    call :print_error "Java n'est pas installé ou pas dans le PATH"
    exit /b 1
)

:: Vérifier Maven
mvn -version >nul 2>&1
if !errorlevel! neq 0 (
    call :print_error "Maven n'est pas installé ou pas dans le PATH"
    exit /b 1
)

:: Créer les répertoires nécessaires
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

:: Vérifier le fichier de configuration
if not exist "%CONFIG_FILE%" (
    call :print_warning "Fichier de configuration non trouvé: %CONFIG_FILE%"
    call :create_default_config
)

goto :eof

:: Création d'un fichier de configuration par défaut
:create_default_config
if not exist "%~dp1" mkdir "%~dp1"
(
echo {
echo   "system": {
echo     "name": "Angel Companion",
echo     "version": "1.0.0",
echo     "language": "fr",
echo     "wakeWord": "Angel"
echo   },
echo   "api": {
echo     "angelServerUrl": "http://localhost:8080/api",
echo     "pollingInterval": 30000,
echo     "timeout": 5000
echo   },
echo   "avatar": {
echo     "enabled": true,
echo     "displayTime": 30000,
echo     "transitionEffect": "fade",
echo     "defaultMood": "neutral"
echo   },
echo   "database": {
echo     "url": "jdbc:h2:file:./angel-db",
echo     "username": "angel",
echo     "password": "angel123",
echo     "driver": "org.h2.Driver"
echo   },
echo   "logging": {
echo     "level": "INFO",
echo     "filePath": "./logs/angel.log",
echo     "rotationSize": "10MB",
echo     "maxFiles": 5
echo   }
echo }
) > "%CONFIG_FILE%"
call :print_success "Fichier de configuration créé: %CONFIG_FILE%"
goto :eof

:: Vérification du statut de l'application
:check_status
if exist "%PID_FILE%" (
    set /p pid=<"%PID_FILE%"
    tasklist /FI "PID eq !pid!" 2>nul | find /I "!pid!" >nul
    if !errorlevel! equ 0 (
        exit /b 0
    ) else (
        del "%PID_FILE%" 2>nul
        exit /b 1
    )
) else (
    exit /b 1
)

:: Affichage du statut
:show_status
call :print_info "Vérification du statut..."

call :check_status
if !errorlevel! equ 0 (
    set /p pid=<"%PID_FILE%"
    call :print_success "Angel Virtual Assistant est en cours d'exécution (PID: !pid!)"
    
    :: Afficher des informations sur le processus
    echo Détails du processus:
    tasklist /FI "PID eq !pid!" 2>nul
    
    :: Afficher les dernières lignes de log
    if exist "%LOG_DIR%\angel.log" (
        echo.
        echo Dernières lignes de log:
        powershell -command "Get-Content '%LOG_DIR%\angel.log' -Tail 5"
    )
) else (
    call :print_warning "Angel Virtual Assistant n'est pas en cours d'exécution"
)
goto :eof

:: Compilation du projet
:build_project
call :print_info "Compilation du projet..."

cd /d "%PROJECT_DIR%"

if "%VERBOSE%"=="true" (
    mvn clean package -DskipTests=false
) else (
    mvn clean package -DskipTests=false -q
)

if !errorlevel! equ 0 (
    call :print_success "Compilation réussie"
) else (
    call :print_error "Échec de la compilation"
    exit /b 1
)
goto :eof

:: Nettoyage
:clean_project
call :print_info "Nettoyage du projet..."

cd /d "%PROJECT_DIR%"
mvn clean -q

:: Supprimer les fichiers temporaires
del /f /q angel-db* 2>nul
del /f /q "%PID_FILE%" 2>nul

call :print_success "Nettoyage terminé"
goto :eof

:: Configuration des options Java
:configure_java_opts
set "JAVA_OPTS=-Xms%MEMORY_XMS% -Xmx%MEMORY_XMX%"
set "JAVA_OPTS=%JAVA_OPTS% -Dangel.config.path=%CONFIG_FILE%"

:: Configuration selon le profil
if "%PROFILE%"=="dev" (
    set "JAVA_OPTS=%JAVA_OPTS% -Dangel.profile=dev"
    set "JAVA_OPTS=%JAVA_OPTS% -Dlogging.level=DEBUG"
) else if "%PROFILE%"=="prod" (
    set "JAVA_OPTS=%JAVA_OPTS% -Dangel.profile=prod"
    set "JAVA_OPTS=%JAVA_OPTS% -Dlogging.level=INFO"
    set "JAVA_OPTS=%JAVA_OPTS% -XX:+UseG1GC -XX:+UseStringDeduplication"
) else if "%PROFILE%"=="test" (
    set "JAVA_OPTS=%JAVA_OPTS% -Dangel.profile=test"
    set "JAVA_OPTS=%JAVA_OPTS% -Dlogging.level=DEBUG"
    set "JAVA_OPTS=%JAVA_OPTS% -Ddatabase.url=jdbc:h2:mem:testdb"
)

:: Mode debug
if "%DEBUG_MODE%"=="true" (
    set "JAVA_OPTS=%JAVA_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:%DEBUG_PORT%"
    call :print_info "Mode debug activé sur le port %DEBUG_PORT%"
)

:: Mode verbose
if "%VERBOSE%"=="true" (
    set "JAVA_OPTS=%JAVA_OPTS% -verbose:gc -XX:+PrintGCDetails"
)
goto :eof

:: Démarrage de l'application
:start_application
call :print_info "Démarrage d'Angel Virtual Assistant..."

:: Vérifier si l'application est déjà en cours d'exécution
call :check_status
if !errorlevel! equ 0 (
    call :print_warning "L'application est déjà en cours d'exécution"
    call :show_status
    goto :eof
)

:: Vérifier les prérequis
call :check_prerequisites

:: Compiler si nécessaire
set "JAR_FILE=%PROJECT_DIR%target\angel-virtual-assistant-1.0.0-SNAPSHOT-jar-with-dependencies.jar"
if not exist "%JAR_FILE%" (
    call :print_info "JAR non trouvé, compilation nécessaire..."
    call :build_project
)

:: Configurer les options Java
call :configure_java_opts

call :print_info "Démarrage de l'application..."
call :print_info "Utilisez Ctrl+C pour arrêter l'application"

:: Démarrer l'application
start "Angel Virtual Assistant" /b java %JAVA_OPTS% -jar "%JAR_FILE%"

:: Obtenir le PID du processus (approximatif)
for /f "tokens=2" %%i in ('tasklist /FI "WINDOWTITLE eq Angel Virtual Assistant" /FO CSV ^| find /v "PID"') do set "PID=%%i"
echo %PID% > "%PID_FILE%"

timeout /t 3 /nobreak >nul

call :check_status
if !errorlevel! equ 0 (
    call :print_success "Angel Virtual Assistant démarré"
) else (
    call :print_error "Échec du démarrage"
    exit /b 1
)
goto :eof

:: Arrêt de l'application
:stop_application
call :print_info "Arrêt d'Angel Virtual Assistant..."

call :check_status
if !errorlevel! equ 0 (
    set /p pid=<"%PID_FILE%"
    call :print_info "Arrêt du processus !pid!..."
    
    :: Arrêt du processus
    taskkill /PID !pid! /F >nul 2>&1
    
    del "%PID_FILE%" 2>nul
    call :print_success "Angel Virtual Assistant arrêté"
) else (
    call :print_warning "Angel Virtual Assistant n'est pas en cours d'exécution"
)
goto :eof

:: Redémarrage
:restart_application
call :print_info "Redémarrage d'Angel Virtual Assistant..."
call :stop_application
timeout /t 2 /nobreak >nul
call :start_application
goto :eof

:: Lancement des tests
:run_tests
call :print_info "Lancement des tests..."

cd /d "%PROJECT_DIR%"

if "%VERBOSE%"=="true" (
    mvn test
) else (
    mvn test -q
)

if !errorlevel! equ 0 (
    call :print_success "Tests réussis"
) else (
    call :print_error "Échec des tests"
    exit /b 1
)
goto :eof

:: Analyse des arguments
:parse_arguments
:parse_loop
if "%~1"=="" goto :parse_done

if "%~1"=="-c" (
    set "CONFIG_FILE=%~2"
    shift
    shift
    goto :parse_loop
) else if "%~1"=="-m" (
    set "MEMORY_XMX=%~2"
    set "MEMORY_XMS=%~2"
    shift
    shift
    goto :parse_loop
) else if "%~1"=="-p" (
    set "PROFILE=%~2"
    shift
    shift
    goto :parse_loop
) else if "%~1"=="-d" (
    set "DEBUG_MODE=true"
    shift
    goto :parse_loop
) else if "%~1"=="-D" (
    set "DEBUG_PORT=%~2"
    shift
    shift
    goto :parse_loop
) else if "%~1"=="-v" (
    set "VERBOSE=true"
    shift
    goto :parse_loop
) else if "%~1"=="-h" (
    call :show_help
    exit /b 0
) else if "%~1"=="start" (
    set "COMMAND=start"
    shift
    goto :parse_loop
) else if "%~1"=="stop" (
    set "COMMAND=stop"
    shift
    goto :parse_loop
) else if "%~1"=="restart" (
    set "COMMAND=restart"
    shift
    goto :parse_loop
) else if "%~1"=="status" (
    set "COMMAND=status"
    shift
    goto :parse_loop
) else if "%~1"=="build" (
    set "COMMAND=build"
    shift
    goto :parse_loop
) else if "%~1"=="clean" (
    set "COMMAND=clean"
    shift
    goto :parse_loop
) else if "%~1"=="test" (
    set "COMMAND=test"
    shift
    goto :parse_loop
) else if "%~1"=="help" (
    set "COMMAND=help"
    shift
    goto :parse_loop
) else (
    call :print_error "Option inconnue: %~1"
    call :show_help
    exit /b 1
)

:parse_done
goto :eof

:: Fonction principale
:main
:: Définir la commande par défaut
if "%COMMAND%"=="" set "COMMAND=help"

:: Traitement des arguments
call :parse_arguments %*

:: Exécution de la commande
if "%COMMAND%"=="start" (
    call :start_application
) else if "%COMMAND%"=="stop" (
    call :stop_application
) else if "%COMMAND%"=="restart" (
    call :restart_application
) else if "%COMMAND%"=="status" (
    call :show_status
) else if "%COMMAND%"=="build" (
    call :build_project
) else if "%COMMAND%"=="clean" (
    call :clean_project
) else if "%COMMAND%"=="test" (
    call :run_tests
) else if "%COMMAND%"=="help" (
    call :show_help
) else (
    call :print_error "Commande inconnue: %COMMAND%"
    call :show_help
    exit /b 1
)

goto :eof

:: Exécution
call :main %*

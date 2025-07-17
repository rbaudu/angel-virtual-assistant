package com.angel.core;

import com.angel.api.AngelServerClient;
import com.angel.config.ConfigManager;
import com.angel.intelligence.ProposalEngine;
import com.angel.intelligence.proposals.*;
import com.angel.model.Activity;
import com.angel.model.UserProfile;
import com.angel.persistence.DatabaseManager;
import com.angel.persistence.dao.ProposalDAO;
import com.angel.persistence.dao.UserPreferenceDAO;
import com.angel.ui.AvatarController;
import com.angel.util.LogUtil;
import com.angel.voice.WakeWordDetector;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principale de l'application Angel qui orchestre tous les composants.
 */
public class AngelApplication {

    private static final Logger LOGGER = LogUtil.getLogger(AngelApplication.class);
    
    private final ConfigManager configManager;
    private final AngelServerClient apiClient;
    private final ProposalEngine proposalEngine;
    private final AvatarController avatarController;
    private final WakeWordDetector wakeWordDetector;
    private final DatabaseManager databaseManager;
    private final ProposalDAO proposalDAO;
    private final UserPreferenceDAO userPreferenceDAO;
    private final ScheduledExecutorService scheduler;
    
    private Activity lastActivity = Activity.UNKNOWN;
    private final Map<LocalDateTime, Activity> activityHistory = new HashMap<>();
    private UserProfile userProfile;
    private boolean isRunning = false;
    
    /**
     * Constructeur principal qui initialise tous les composants.
     */
    public AngelApplication() {
        // Initialiser le gestionnaire de configuration
        this.configManager = new ConfigManager();
        
        // Initialiser le gestionnaire de base de données
        this.databaseManager = new DatabaseManager(configManager);
        
        // Initialiser les DAOs
        this.proposalDAO = new ProposalDAO(databaseManager);
        this.userPreferenceDAO = new UserPreferenceDAO(databaseManager);
        
        // Initialiser le client API
        this.apiClient = new AngelServerClient(configManager);
        
        // Charger le profil utilisateur
        this.userProfile = loadUserProfile();
        
        // Créer toutes les propositions disponibles
        List<Proposal> availableProposals = createAvailableProposals();
        
        // Initialiser le moteur de propositions
        this.proposalEngine = new ProposalEngine(configManager, proposalDAO, availableProposals);
        
        // Initialiser le contrôleur d'avatar
        this.avatarController = new AvatarController(configManager);
        
        // Initialiser le détecteur de mot-clé
        this.wakeWordDetector = new WakeWordDetector(configManager);
        
        // Initialiser le scheduler pour les tâches périodiques
        this.scheduler = Executors.newScheduledThreadPool(2);
    }
    
    /**
     * Démarre l'application.
     */
    public void start() {
        if (isRunning) {
            LOGGER.log(Level.WARNING, "L'application est déjà en cours d'exécution");
            return;
        }
        
        LOGGER.log(Level.INFO, "Démarrage de l'application Angel...");
        
        // Vérifier la connexion au serveur Angel-capture
        if (!apiClient.isServerAvailable()) {
            LOGGER.log(Level.SEVERE, "Impossible de se connecter au serveur Angel-capture");
            // Dans un cas réel, on pourrait soit attendre et réessayer,
            // soit demander une intervention utilisateur
        }
        
        // Configurer le polling périodique pour récupérer l'activité courante
        long pollingInterval = configManager.getLong("api.pollingInterval", 30000);
        scheduler.scheduleAtFixedRate(
            this::pollCurrentActivity,
            0,
            pollingInterval,
            TimeUnit.MILLISECONDS
        );
        
        // Configurer la vérification périodique pour les propositions
        scheduler.scheduleAtFixedRate(
            this::checkForProposals,
            10000, // Attendre un peu avant la première vérification
            60000, // Vérifier toutes les minutes
            TimeUnit.MILLISECONDS
        );
        
        // Démarrer l'écoute du mot-clé
        wakeWordDetector.startListening(unused -> handleWakeWord());
        
        isRunning = true;
        LOGGER.log(Level.INFO, "Application Angel démarrée");
    }
    
    /**
     * Arrête l'application et libère les ressources.
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        LOGGER.log(Level.INFO, "Arrêt de l'application Angel...");
        
        // Arrêter le scheduler
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Interruption lors de l'arrêt du scheduler", e);
        }
        
        // Arrêter l'écoute du mot-clé
        wakeWordDetector.shutdown();
        
        // Fermer la connexion à la base de données
        databaseManager.closeConnection();
        
        isRunning = false;
        LOGGER.log(Level.INFO, "Application Angel arrêtée");
    }
    
    /**
     * Récupère l'activité courante depuis le serveur Angel-capture.
     */
    private void pollCurrentActivity() {
        try {
            apiClient.getCurrentActivity()
                .thenAccept(activity -> {
                    if (activity != lastActivity) {
                        LOGGER.log(Level.INFO, "Nouvelle activité détectée: {0}", activity);
                        lastActivity = activity;
                        
                        // Enregistrer l'activité dans l'historique
                        LocalDateTime now = LocalDateTime.now();
                        activityHistory.put(now, activity);
                        
                        // Limiter la taille de l'historique (garder les 24 dernières heures)
                        LocalDateTime cutoff = now.minusHours(24);
                        activityHistory.entrySet().removeIf(entry -> entry.getKey().isBefore(cutoff));
                    }
                })
                .exceptionally(ex -> {
                    LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de l'activité", ex);
                    return null;
                });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception lors du polling de l'activité", e);
        }
    }
    
    /**
     * Vérifie s'il faut faire une proposition à l'utilisateur.
     */
    private void checkForProposals() {
        if (lastActivity == Activity.UNKNOWN || !lastActivity.allowsProposals()) {
            return; // Ne pas faire de proposition si l'activité n'est pas reconnue ou ne le permet pas
        }
        
        try {
            // Déterminer la meilleure proposition
            Proposal bestProposal = proposalEngine.determineBestProposal(
                lastActivity,
                activityHistory,
                userProfile
            );
            
            if (bestProposal != null) {
                // Afficher la proposition via l'avatar
                avatarController.displayProposal(bestProposal)
                    .thenRun(() -> {
                        // Enregistrer la proposition dans l'historique
                        proposalEngine.recordProposal(bestProposal, lastActivity);
                    });
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification des propositions", e);
        }
    }
    
    /**
     * Gère l'activation du système quand l'utilisateur dit le mot-clé.
     */
    private void handleWakeWord() {
        LOGGER.log(Level.INFO, "Mot-clé détecté, activation du système");
        
        // Afficher un message de confirmation via l'avatar
        avatarController.displayMessage(
            "Oui, comment puis-je vous aider ?",
            "attentive",
            5000
        ).thenRun(() -> {
            // Ici, on pourrait démarrer une reconnaissance vocale plus complète
            // pour comprendre la commande de l'utilisateur
            
            // Pour cet exemple, on pourrait simplement proposer quelque chose de pertinent
            checkForProposals();
        });
    }
    
    /**
     * Charge le profil utilisateur depuis la base de données.
     * 
     * @return Le profil utilisateur chargé
     */
    private UserProfile loadUserProfile() {
        // Dans une implémentation réelle, on chargerait le profil depuis la base de données
        // Pour cet exemple, on crée un profil par défaut
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setName("Utilisateur");
        
        // Essayer de charger les préférences si elles existent
        Map<String, String> preferences = userPreferenceDAO.getUserPreferences(profile.getId());
        if (!preferences.isEmpty()) {
            profile.setPreferences(preferences);
        } else {
            // Définir quelques préférences par défaut
            Map<String, String> defaultPrefs = new HashMap<>();
            defaultPrefs.put("news.preferredSources", "local,national");
            defaultPrefs.put("weather.showTomorrow", "true");
            defaultPrefs.put("reminders.medicationTime", "8:00,12:00,19:00");
            profile.setPreferences(defaultPrefs);
        }
        
        return profile;
    }
    
    /**
     * Crée la liste de toutes les propositions disponibles dans le système.
     * 
     * @return Liste de toutes les propositions
     */
    private List<Proposal> createAvailableProposals() {
        List<Proposal> proposals = new ArrayList<>();
        
        // Ajouter toutes les propositions disponibles
        proposals.add(new WeatherProposal(configManager));
        // Note: Les autres propositions comme NewsProposal, StoryProposal, etc.
        // peuvent être ajoutées ici une fois implémentées
        
        return proposals;
    }
    
    /**
     * Point d'entrée principal de l'application.
     * 
     * @param args Arguments de ligne de commande
     */
    public static void main(String[] args) {
        // Configurer le système de logging
        LogUtil.configureLogging();
        
        // Démarrer l'application
        AngelApplication app = new AngelApplication();
        app.start();
        
        // Ajouter un hook d'arrêt pour nettoyer proprement
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        
        // Garder l'application en vie
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Interruption du thread principal", e);
        }
    }
}
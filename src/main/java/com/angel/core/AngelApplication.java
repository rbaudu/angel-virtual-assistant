package com.angel.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.angel.api.AngelServerClient;
import com.angel.avatar.AvatarManager;
import com.angel.avatar.EmotionAnalyzer;
import com.angel.avatar.TextToSpeechService;
import com.angel.avatar.WebSocketService;
import com.angel.config.ConfigManager;
import com.angel.intelligence.ProposalEngine;
import com.angel.intelligence.proposals.Proposal;
import com.angel.intelligence.proposals.WeatherProposal;
import com.angel.model.Activity;
import com.angel.model.UserProfile;
import com.angel.persistence.DatabaseManager;
import com.angel.persistence.dao.ProposalDAO;
import com.angel.persistence.dao.UserPreferenceDAO;
import com.angel.ui.AvatarController;
import com.angel.util.LogUtil;
import com.angel.voice.VoiceActivityManager;
import com.angel.voice.VoiceQuestionContext;
import com.angel.voice.VoiceQuestionProcessor;
import com.angel.voice.WakeWordDetector;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Classe principale de l'application Angel qui orchestre tous les composants.
 */
@Component
@Lazy
public class AngelApplication {

    private static final Logger LOGGER = LogUtil.getLogger(AngelApplication.class);
    
    @Autowired
    private ConfigManager configManager;
    
    // Injection du processeur de questions
    @Autowired
    private VoiceQuestionProcessor voiceQuestionProcessor;
    
    @Autowired
    private VoiceActivityManager voiceActivityManager;

    // Injection des services avatar via Spring
    @Autowired
    private WebSocketService webSocketService;

    private AngelServerClient apiClient;
    private ProposalEngine proposalEngine;
    private AvatarController avatarController;
    private WakeWordDetector wakeWordDetector;
    private DatabaseManager databaseManager;
    private ProposalDAO proposalDAO;
    private UserPreferenceDAO userPreferenceDAO;
    private ScheduledExecutorService scheduler;
    
    private Activity lastActivity = Activity.UNKNOWN;
    private final Map<LocalDateTime, Activity> activityHistory = new HashMap<>();
    private UserProfile userProfile;
    private boolean isRunning = false;
    
    public AngelApplication() {
        // Constructeur vide pour Spring
    }

    /**
     * Constructeur principal qui initialise tous les composants.
     */
    @PostConstruct
    public void initialize() {
        LOGGER.log(Level.INFO, "Initialisation des composants Angel...");
        try {
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
            
            // Initialiser les services avatar
            TextToSpeechService ttsService = new TextToSpeechService(configManager);
            EmotionAnalyzer emotionAnalyzer = new EmotionAnalyzer();
            AvatarManager avatarManager = new AvatarManager(configManager, ttsService,
                     webSocketService, emotionAnalyzer);
            
            // Initialiser le contrôleur d'avatar avec tous les paramètres requis
            this.avatarController = new AvatarController(configManager, avatarManager, webSocketService);
            
            // Initialiser le détecteur de mot-clé
            this.wakeWordDetector = new WakeWordDetector(configManager);
            
            // Initialiser le scheduler pour les tâches périodiques
            this.scheduler = Executors.newScheduledThreadPool(2);
            LOGGER.log(Level.INFO, "Composants Angel initialisés avec succès");

            // Démarrer l'écoute continue
            voiceActivityManager.startContinuousListening();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation des composants Angel", e);
            throw new RuntimeException("Échec de l'initialisation d'Angel", e);
        }
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

        // Vérifier que l'initialisation a eu lieu
        if (configManager == null) {
            throw new IllegalStateException("AngelApplication n'a pas été correctement initialisé par Spring");
        }
       
        // Vérifier la connexion au serveur Angel-capture
        if (!apiClient.isServerAvailable()) {
            LOGGER.log(Level.WARNING, "Impossible de se connecter au serveur Angel-capture");
            // Continuer le démarrage même si le serveur externe n'est pas disponible
        }
        
        // Configurer le polling périodique pour récupérer l'activité courante
        long pollingInterval = configManager.getLong("api.polling-interval", 30000);
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
        LOGGER.log(Level.INFO, "Application Angel démarrée avec succès");
    }
    
    /**
     * Arrête l'application et libère les ressources.
     */
    @PreDestroy
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        LOGGER.log(Level.INFO, "Arrêt de l'application Angel...");
        
        // Arrêter le scheduler
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interruption lors de l'arrêt du scheduler", e);
            }
        }
        
        // Arrêter l'écoute du mot-clé
        if (wakeWordDetector != null) {
            wakeWordDetector.shutdown();
        }
        
        // Fermer la connexion à la base de données
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        isRunning = false;
        LOGGER.log(Level.INFO, "Application Angel arrêtée");
    }
    
    /**
     * Récupère l'activité courante depuis le serveur Angel-capture.
     */
    private void pollCurrentActivity() {
        LOGGER.log(Level.INFO, "Poll current activitiy...");
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
                    LOGGER.log(Level.FINE, "Serveur Angel-capture non disponible: {0}", ex.getMessage());
                    return null;
                });
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Exception lors du polling de l'activité: {0}", e.getMessage());
        }
    }
    
    /**
     * Vérifie s'il faut faire une proposition à l'utilisateur.
     */
    private void checkForProposals() {
        LOGGER.log(Level.INFO, "Check for proposals, last activity being ='"+lastActivity+"' ...");
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
 
        // Générer le message d'activation personnalisé
        voiceQuestionProcessor.generateActivationMessage().thenAccept(activationMessage -> {
            // Le code qui utilisait activationMessage
            // Par exemple, si vous l'envoyiez à l'interface :
            // sendActivationMessageToClient(activationMessage);
            LOGGER.log(Level.INFO, "Message d'activation généré : {0}", activationMessage);
            // Afficher un message de confirmation via l'avatar et le faire parler
            avatarController.displayMessage(
                activationMessage,
                "attentive",
                5000
            ).thenRun(() -> {
                // Ici, on pourrait démarrer une reconnaissance vocale plus complète
                // pour comprendre la commande de l'utilisateur
                
                // Pour cet exemple, on pourrait simplement proposer quelque chose de pertinent
                checkForProposals();
            });
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
     * Traite une question vocale de l'utilisateur.
     * Délègue le traitement au processeur spécialisé.
     * 
     * @param question La question posée par l'utilisateur
     * @param confidence Niveau de confiance de la reconnaissance
     * @return CompletableFuture avec la réponse
     */
    public CompletableFuture<String> processUserQuestion(String question, float confidence) {
        LOGGER.log(Level.INFO, "Délégation question au VoiceQuestionProcessor: {0}", question);
        
        // Créer le contexte pour la question
        VoiceQuestionContext context = new VoiceQuestionContext(
            getUserProfile(),
            getLastActivity(), 
            getActivityHistory(),
            "angel-session-" + System.currentTimeMillis(),
            confidence
        );
        
        if (voiceActivityManager.handleUICommand(question)) {
            LOGGER.log(Level.INFO, "Commande d''interface trait\u00e9e : {0}", question);
            return CompletableFuture.completedFuture(null);
        }

        // Déléguer au processeur spécialisé
        return voiceQuestionProcessor.processQuestion(question, confidence, context);
    }

    /**
     * Getters pour exposer les données contextuelles (pas de changement).
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    public Activity getLastActivity() {
        return lastActivity;
    }

    public Map<LocalDateTime, Activity> getActivityHistory() {
        return new HashMap<>(activityHistory); // Copie défensive
    }
}
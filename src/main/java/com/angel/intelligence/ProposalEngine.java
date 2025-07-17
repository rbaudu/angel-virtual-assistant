package com.angel.intelligence;

import com.angel.config.ConfigManager;
import com.angel.intelligence.proposals.Proposal;
import com.angel.model.Activity;
import com.angel.model.ProposalHistory;
import com.angel.model.UserProfile;
import com.angel.persistence.dao.ProposalDAO;
import com.angel.util.LogUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Moteur d'intelligence qui décide quelles propositions faire à l'utilisateur
 * en fonction de son activité actuelle et du contexte.
 */
public class ProposalEngine {

    private static final Logger LOGGER = LogUtil.getLogger(ProposalEngine.class);
    
    private final ConfigManager configManager;
    private final ProposalDAO proposalDAO;
    private final List<Proposal> availableProposals;
    
    /**
     * Constructeur avec injection des dépendances.
     * 
     * @param configManager Gestionnaire de configuration
     * @param proposalDAO DAO pour accéder à l'historique des propositions
     * @param availableProposals Liste des propositions disponibles
     */
    public ProposalEngine(ConfigManager configManager, ProposalDAO proposalDAO, List<Proposal> availableProposals) {
        this.configManager = configManager;
        this.proposalDAO = proposalDAO;
        this.availableProposals = availableProposals;
    }
    
    /**
     * Détermine la meilleure proposition à faire à l'utilisateur en fonction
     * de son activité actuelle et du contexte.
     * 
     * @param currentActivity Activité actuelle de l'utilisateur
     * @param previousActivities Historique des activités récentes
     * @param userProfile Profil de l'utilisateur
     * @return La meilleure proposition à faire, ou null si aucune n'est appropriée
     */
    public Proposal determineBestProposal(
        Activity currentActivity,
        Map<LocalDateTime, Activity> previousActivities,
        UserProfile userProfile
    ) {
        // Si l'activité est UNKNOWN ou ne permet pas de propositions, ne rien proposer
        if (currentActivity == Activity.UNKNOWN || !currentActivity.allowsProposals()) {
            LOGGER.log(Level.INFO, "Aucune proposition pour l'activité {0}", currentActivity);
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<ProposalHistory> proposalHistory = proposalDAO.getRecentProposals(24); // Dernier 24h
        
        // Filtrer les propositions appropriées pour l'activité actuelle
        List<Proposal> appropriateProposals = availableProposals.stream()
            .filter(p -> isProposalAllowedForActivity(p, currentActivity))
            .filter(p -> !isDailyLimitReached(p.getId(), proposalHistory))
            .filter(p -> p.isAppropriate(currentActivity, previousActivities, userProfile, now, proposalHistory))
            .collect(Collectors.toList());
        
        if (appropriateProposals.isEmpty()) {
            LOGGER.log(Level.INFO, "Aucune proposition appropriée trouvée pour l'activité {0}", currentActivity);
            return null;
        }
        
        // Trier par priorité décroissante
        appropriateProposals.sort((p1, p2) -> {
            int priority1 = p1.getPriority(currentActivity, previousActivities, userProfile, now, proposalHistory);
            int priority2 = p2.getPriority(currentActivity, previousActivities, userProfile, now, proposalHistory);
            return Integer.compare(priority2, priority1); // Ordre décroissant
        });
        
        // Choisir la proposition avec la plus haute priorité
        Proposal bestProposal = appropriateProposals.get(0);
        LOGGER.log(Level.INFO, "Proposition sélectionnée: {0}", bestProposal.getId());
        
        // Préparer le contenu de la proposition
        bestProposal.prepare(currentActivity, userProfile, now);
        
        return bestProposal;
    }
    
    /**
     * Vérifie si une proposition est autorisée pour une activité donnée
     * selon la configuration.
     * 
     * @param proposal La proposition à vérifier
     * @param activity L'activité actuelle
     * @return true si la proposition est autorisée, false sinon
     */
    private boolean isProposalAllowedForActivity(Proposal proposal, Activity activity) {
        List<String> allowedProposalTypes = configManager.getStringList(
            "proposals.activityMapping." + activity.name()
        );
        
        // Si la liste est vide pour cette activité, aucune proposition n'est autorisée
        if (allowedProposalTypes.isEmpty()) {
            return false;
        }
        
        // Vérifier si le type de proposition est autorisé directement ou via un préfixe
        String proposalId = proposal.getId();
        for (String allowedType : allowedProposalTypes) {
            if (allowedType.equals(proposalId) || 
                (allowedType.contains(".") && proposalId.startsWith(allowedType.split("\\.")[0]))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Vérifie si la fréquence quotidienne de propositions a été atteinte
     * pour un type de proposition donné.
     * 
     * @param proposalType Type de proposition à vérifier
     * @param proposalHistory Historique des propositions
     * @return true si la limite quotidienne est atteinte, false sinon
     */
    private boolean isDailyLimitReached(String proposalType, List<ProposalHistory> proposalHistory) {
        // Récupérer la limite quotidienne depuis la configuration
        int maxPerDay = configManager.getInt("proposals.daily." + proposalType + ".maxPerDay", 0);
        if (maxPerDay <= 0) {
            return false; // Pas de limite définie
        }
        
        // Compter le nombre de propositions du même type aujourd'hui
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayCount = proposalHistory.stream()
            .filter(p -> p.getProposalType().equals(proposalType))
            .filter(p -> p.getTimestamp().isAfter(startOfDay))
            .count();
        
        return todayCount >= maxPerDay;
    }
    
    /**
     * Enregistre une proposition qui a été présentée à l'utilisateur.
     * 
     * @param proposal La proposition qui a été présentée
     * @param activity L'activité de l'utilisateur lors de la présentation
     */
    public void recordProposal(Proposal proposal, Activity activity) {
        ProposalHistory history = new ProposalHistory();
        history.setProposalType(proposal.getId());
        history.setTimestamp(LocalDateTime.now());
        history.setActivityType(activity.name());
        history.setTitle(proposal.getTitle());
        
        proposalDAO.saveProposal(history);
        LOGGER.log(Level.INFO, "Proposition enregistrée: {0}", proposal.getId());
    }
}
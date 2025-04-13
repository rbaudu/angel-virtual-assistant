package com.angel.intelligence.proposals;

import com.angel.model.Activity;
import com.angel.model.UserProfile;
import com.angel.model.ProposalHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface définissant le comportement commun à toutes les propositions
 * que le système peut faire à l'utilisateur.
 */
public interface Proposal {
    
    /**
     * Retourne l'identifiant unique du type de proposition.
     * @return L'identifiant de la proposition (ex: "news", "weather")
     */
    String getId();
    
    /**
     * Retourne le titre de la proposition à afficher.
     * @return Le titre de la proposition
     */
    String getTitle();
    
    /**
     * Retourne le contenu détaillé de la proposition.
     * @return Le contenu de la proposition
     */
    String getContent();
    
    /**
     * Détermine si cette proposition est appropriée dans le contexte actuel.
     * 
     * @param currentActivity L'activité actuelle de l'utilisateur
     * @param previousActivities Historique des activités récentes
     * @param userProfile Profil de l'utilisateur avec ses préférences
     * @param currentTime Date et heure actuelles
     * @param proposalHistory Historique des propositions déjà faites
     * @return true si la proposition est appropriée, false sinon
     */
    boolean isAppropriate(
        Activity currentActivity,
        Map<LocalDateTime, Activity> previousActivities,
        UserProfile userProfile,
        LocalDateTime currentTime,
        List<ProposalHistory> proposalHistory
    );
    
    /**
     * Détermine la priorité de cette proposition par rapport au contexte actuel.
     * Plus la valeur est élevée, plus la proposition est prioritaire.
     * 
     * @param currentActivity L'activité actuelle de l'utilisateur
     * @param previousActivities Historique des activités récentes
     * @param userProfile Profil de l'utilisateur avec ses préférences
     * @param currentTime Date et heure actuelles
     * @param proposalHistory Historique des propositions déjà faites
     * @return Une valeur de priorité entre 0 et 100
     */
    int getPriority(
        Activity currentActivity,
        Map<LocalDateTime, Activity> previousActivities,
        UserProfile userProfile,
        LocalDateTime currentTime,
        List<ProposalHistory> proposalHistory
    );
    
    /**
     * Prépare le contenu concret de la proposition en fonction du contexte.
     * Cette méthode est appelée juste avant de présenter la proposition à l'utilisateur.
     * 
     * @param currentActivity L'activité actuelle de l'utilisateur
     * @param userProfile Profil de l'utilisateur
     * @param currentTime Date et heure actuelles
     */
    void prepare(Activity currentActivity, UserProfile userProfile, LocalDateTime currentTime);
    
    /**
     * Génère une requête pour l'avatar qui présentera la proposition.
     * @return Texte que l'avatar doit dire pour présenter la proposition
     */
    String generateAvatarPrompt();
    
    /**
     * Retourne la durée estimée de cette proposition en secondes.
     * @return Durée estimée en secondes
     */
    int getEstimatedDuration();
    
    /**
     * Retourne une liste d'activités qui sont compatibles avec cette proposition.
     * @return Liste des activités compatibles
     */
    List<Activity> getCompatibleActivities();
    
    /**
     * Retourne une liste d'activités qui ne devraient pas être suivies
     * par cette proposition (ex: ne pas proposer d'exercice après manger).
     * @return Liste des activités incompatibles
     */
    List<Activity> getIncompatibleFollowUpActivities();
}
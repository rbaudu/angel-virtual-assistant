package com.angel.intelligence.proposals;

import com.angel.config.ConfigManager;
import com.angel.model.Activity;
import com.angel.model.UserProfile;
import com.angel.model.ProposalHistory;
import com.angel.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Proposition de météo qui présente les prévisions du jour et du lendemain.
 */
public class WeatherProposal implements Proposal {
    
    private final ConfigManager configManager;
    private String title;
    private String content;
    private boolean includeTomorrow;
    
    // Liste des activités compatibles avec cette proposition
    private static final List<Activity> COMPATIBLE_ACTIVITIES = Arrays.asList(
        Activity.WAITING, 
        Activity.EATING, 
        Activity.WAKING_UP,
        Activity.GOING_TO_SLEEP,
        Activity.USING_SCREEN
    );
    
    // Pas d'activités incompatibles spécifiques pour la météo
    private static final List<Activity> INCOMPATIBLE_FOLLOW_UP_ACTIVITIES = List.of();
    
    public WeatherProposal(ConfigManager configManager) {
        this.configManager = configManager;
        this.title = "Météo du jour";
        this.content = "";
        this.includeTomorrow = true;
    }
    
    @Override
    public String getId() {
        return "weather";
    }
    
    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public String getContent() {
        return content;
    }
    
    @Override
    public boolean isAppropriate(
        Activity currentActivity,
        Map<LocalDateTime, Activity> previousActivities,
        UserProfile userProfile,
        LocalDateTime currentTime,
        List<ProposalHistory> proposalHistory
    ) {
        // Vérifier si l'activité actuelle est compatible
        if (!COMPATIBLE_ACTIVITIES.contains(currentActivity)) {
            return false;
        }
        
        // Vérifier si on est dans les heures préférées pour la météo
        List<Integer> preferredHours = configManager.getIntegerList("proposals.timeConstraints.weather.preferredHours");
        int currentHour = currentTime.getHour();
        if (!preferredHours.contains(currentHour)) {
            return false;
        }
        
        // Vérifier si on n'a pas déjà proposé la météo récemment
        long minTimeBetween = configManager.getLong("proposals.daily.weather.minTimeBetween");
        return proposalHistory.stream()
            .filter(p -> p.getProposalType().equals(getId()))
            .noneMatch(p -> DateTimeUtil.millisBetween(p.getTimestamp(), currentTime) < minTimeBetween);
    }
    
    @Override
    public int getPriority(
        Activity currentActivity,
        Map<LocalDateTime, Activity> previousActivities,
        UserProfile userProfile,
        LocalDateTime currentTime,
        List<ProposalHistory> proposalHistory
    ) {
        int basePriority = 50; // Priorité de base moyenne
        
        // Augmenter la priorité tôt le matin ou en fin d'après-midi
        int hour = currentTime.getHour();
        if (hour >= 7 && hour <= 9) {
            basePriority += 20; // Priorité plus élevée le matin
        } else if (hour >= 17 && hour <= 19) {
            basePriority += 15; // Priorité élevée en fin d'après-midi
        }
        
        // Augmenter la priorité si la personne vient de se réveiller
        if (currentActivity == Activity.WAKING_UP) {
            basePriority += 25;
        }
        
        // Augmenter la priorité si la personne va se coucher (météo de demain)
        if (currentActivity == Activity.GOING_TO_SLEEP) {
            basePriority += 15;
            includeTomorrow = true;
        }
        
        // Réduire la priorité si c'est une activité qui demande de l'attention
        if (currentActivity.requiresAttention()) {
            basePriority -= 10;
        }
        
        return Math.min(basePriority, 100); // Limiter à 100
    }
    
    @Override
    public void prepare(Activity currentActivity, UserProfile userProfile, LocalDateTime currentTime) {
        // Adapter le titre selon qu'on inclut la météo de demain ou non
        if (includeTomorrow || currentActivity == Activity.GOING_TO_SLEEP) {
            this.title = "Météo d'aujourd'hui et de demain";
            this.includeTomorrow = true;
        } else {
            this.title = "Météo du jour";
            this.includeTomorrow = false;
        }
        
        // Ici, on simulerait un appel à un service météo
        // Pour l'exemple, on met un contenu statique
        this.content = "Aujourd'hui: Ensoleillé, température maximale de 22°C.\n";
        
        if (includeTomorrow) {
            this.content += "Demain: Partiellement nuageux, température maximale de 19°C.\n";
        }
        
        // Adapter le contenu selon l'activité
        if (currentActivity == Activity.GOING_TO_SLEEP) {
            this.content += "Bonne nuit, et à demain !";
        } else if (currentActivity == Activity.WAKING_UP) {
            this.content += "Je vous souhaite une excellente journée !";
        }
    }
    
    @Override
    public String generateAvatarPrompt() {
        if (includeTomorrow) {
            return "Bonjour ! Voici la météo d'aujourd'hui et de demain.";
        } else {
            return "Bonjour ! Voici la météo du jour.";
        }
    }
    
    @Override
    public int getEstimatedDuration() {
        return includeTomorrow ? 20 : 10; // Durée en secondes
    }
    
    @Override
    public List<Activity> getCompatibleActivities() {
        return COMPATIBLE_ACTIVITIES;
    }
    
    @Override
    public List<Activity> getIncompatibleFollowUpActivities() {
        return INCOMPATIBLE_FOLLOW_UP_ACTIVITIES;
    }
}
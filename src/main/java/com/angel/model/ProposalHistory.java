package com.angel.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Classe représentant l'historique d'une proposition faite à l'utilisateur.
 */
public class ProposalHistory {
    
    private Long id;
    private String proposalType;
    private LocalDateTime timestamp;
    private String activityType;
    private String title;
    private boolean accepted;
    private LocalDateTime completionTime;
    
    /**
     * Constructeur par défaut.
     */
    public ProposalHistory() {
        this.timestamp = LocalDateTime.now();
        this.accepted = false;
    }
    
    /**
     * Constructeur avec paramètres principaux.
     * 
     * @param proposalType Type de proposition
     * @param activityType Type d'activité de l'utilisateur lors de la proposition
     * @param title Titre de la proposition
     */
    public ProposalHistory(String proposalType, String activityType, String title) {
        this.proposalType = proposalType;
        this.timestamp = LocalDateTime.now();
        this.activityType = activityType;
        this.title = title;
        this.accepted = false;
    }
    
    // Getters et setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProposalType() {
        return proposalType;
    }
    
    public void setProposalType(String proposalType) {
        this.proposalType = proposalType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getActivityType() {
        return activityType;
    }
    
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
    
    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }
    
    /**
     * Calcule la durée d'engagement de l'utilisateur avec cette proposition.
     * 
     * @return Durée en secondes, ou 0 si la proposition n'a pas été acceptée ou complétée
     */
    public long getDurationInSeconds() {
        if (!accepted || completionTime == null) {
            return 0;
        }
        
        return ChronoUnit.SECONDS.between(timestamp, completionTime);
    }
    
    /**
     * Marque cette proposition comme acceptée par l'utilisateur.
     */
    public void markAsAccepted() {
        this.accepted = true;
    }
    
    /**
     * Marque cette proposition comme complétée par l'utilisateur.
     */
    public void markAsCompleted() {
        if (this.accepted) {
            this.completionTime = LocalDateTime.now();
        }
    }
    
    @Override
    public String toString() {
        return "ProposalHistory{" +
                "id=" + id +
                ", proposalType='" + proposalType + '\'' +
                ", timestamp=" + timestamp +
                ", activityType='" + activityType + '\'' +
                ", title='" + title + '\'' +
                ", accepted=" + accepted +
                ", completionTime=" + completionTime +
                '}';
    }
}
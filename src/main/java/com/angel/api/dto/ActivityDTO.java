package com.angel.api.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object pour représenter une activité venant de l'API Angel-server-capture.
 */
public class ActivityDTO {

    private String activityType;
    private long timestamp;
    private double confidence;
    private String source; // "video", "audio", "combined", etc.
    private String additionalInfo;

    // Constructeur par défaut pour la désérialisation JSON
    public ActivityDTO() {
    }

    /**
     * Constructeur avec tous les paramètres.
     * 
     * @param activityType Type d'activité détectée
     * @param timestamp Timestamp de la détection (en millisecondes depuis l'epoch)
     * @param confidence Niveau de confiance de la détection (0.0 à 1.0)
     * @param source Source de la détection (vidéo, audio, combiné, etc.)
     * @param additionalInfo Informations supplémentaires éventuelles
     */
    public ActivityDTO(String activityType, long timestamp, double confidence, String source, String additionalInfo) {
        this.activityType = activityType;
        this.timestamp = timestamp;
        this.confidence = confidence;
        this.source = source;
        this.additionalInfo = additionalInfo;
    }

    // Getters et setters
    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * Convertit le timestamp en objet LocalDateTime.
     * 
     * @return LocalDateTime correspondant au timestamp
     */
    public LocalDateTime getDateTime() {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp), 
            java.time.ZoneId.systemDefault()
        );
    }

    @Override
    public String toString() {
        return "ActivityDTO{" +
                "activityType='" + activityType + '\'' +
                ", timestamp=" + timestamp +
                ", confidence=" + confidence +
                ", source='" + source + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }
}
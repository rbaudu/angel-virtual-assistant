package com.angel.api.dto;

import com.angel.model.Activity;

/**
 * DTO spécifique au mode test pour représenter une activité simulée.
 * Compatible avec ActivityDTO existant mais avec constructeur adapté.
 */
public class TestActivityDTO {
    private Activity activity;
    private double confidence;
    private long timestamp;
    private String source;
    private String description;

    public TestActivityDTO() {
    }

    public TestActivityDTO(Activity activity, double confidence, long timestamp) {
        this.activity = activity;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.source = "test_simulation";
        this.description = activity.getDescription();
    }

    public TestActivityDTO(Activity activity, double confidence, long timestamp, String description) {
        this.activity = activity;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.source = "test_simulation";
        this.description = description;
    }

    // Getters et setters
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Convertit vers ActivityDTO pour compatibilité avec le reste du système.
     */
    public ActivityDTO toActivityDTO() {
        return new ActivityDTO(
            activity.name(),
            timestamp,
            confidence,
            source,
            description
        );
    }

    /**
     * Crée un TestActivityDTO depuis un ActivityDTO.
     */
    public static TestActivityDTO fromActivityDTO(ActivityDTO dto) {
        try {
            Activity activity = Activity.valueOf(dto.getActivityType());
            return new TestActivityDTO(activity, dto.getConfidence(), dto.getTimestamp());
        } catch (IllegalArgumentException e) {
            // Si l'activité n'existe pas, utiliser UNKNOWN
            return new TestActivityDTO(Activity.UNKNOWN, dto.getConfidence(), dto.getTimestamp());
        }
    }

    @Override
    public String toString() {
        return "TestActivityDTO{" +
                "activity=" + activity +
                ", confidence=" + confidence +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
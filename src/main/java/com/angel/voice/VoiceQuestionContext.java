package com.angel.voice;

import com.angel.model.Activity;
import com.angel.model.UserProfile;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Contexte pour le traitement des questions vocales.
 * Encapsule toutes les informations nécessaires pour traiter une question.
 */
public class VoiceQuestionContext {
    
    private final UserProfile userProfile;
    private final Activity currentActivity;
    private final Map<LocalDateTime, Activity> activityHistory;
    private final String sessionId;
    private final LocalDateTime timestamp;
    private final float confidence;
    
    /**
     * Constructeur du contexte de question vocale.
     */
    public VoiceQuestionContext(UserProfile userProfile, 
                               Activity currentActivity,
                               Map<LocalDateTime, Activity> activityHistory,
                               String sessionId,
                               float confidence) {
        this.userProfile = userProfile;
        this.currentActivity = currentActivity;
        this.activityHistory = activityHistory;
        this.sessionId = sessionId;
        this.confidence = confidence;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public UserProfile getUserProfile() { return userProfile; }
    public Activity getCurrentActivity() { return currentActivity; }
    public Map<LocalDateTime, Activity> getActivityHistory() { return activityHistory; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public float getConfidence() { return confidence; }
    
    @Override
    public String toString() {
        return String.format(
            "VoiceQuestionContext{sessionId='%s', activity=%s, user=%s, confidence=%.2f, timestamp=%s}",
            sessionId, currentActivity, 
            userProfile != null ? userProfile.getName() : "unknown",
            confidence, timestamp
        );
    }
}
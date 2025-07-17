package com.angel.avatar;


/**
 * Message pour changer l'émotion de l'avatar.
 */
public class AvatarEmotionMessage {
    private String type = "AVATAR_EMOTION";
    private String emotion;
    private double intensity;
    private long transitionDuration;
    
    public AvatarEmotionMessage(String emotion, double intensity, long transitionDuration) {
        this.emotion = emotion;
        this.intensity = intensity;
        this.transitionDuration = transitionDuration;
    }
    
    // Getters
    public String getType() { return type; }
    public String getEmotion() { return emotion; }
    public double getIntensity() { return intensity; }
    public long getTransitionDuration() { return transitionDuration; }
}


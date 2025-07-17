package com.angel.avatar;


/**
 * Données d'animation complètes pour l'avatar.
 */
public class AvatarAnimationData {
    private String baseAnimation = "neutral_idle";
    private HeadMovementData headMovements;
    private GestureData gestures;
    private BlinkPattern blinkPattern;
    
    public AvatarAnimationData() {
        this.headMovements = new HeadMovementData();
        this.gestures = new GestureData();
        this.blinkPattern = new BlinkPattern();
    }
    
    // Getters et setters
    public String getBaseAnimation() { return baseAnimation; }
    public void setBaseAnimation(String baseAnimation) { this.baseAnimation = baseAnimation; }
    
    public HeadMovementData getHeadMovements() { return headMovements; }
    public void setHeadMovements(HeadMovementData headMovements) { this.headMovements = headMovements; }
    
    public GestureData getGestures() { return gestures; }
    public void setGestures(GestureData gestures) { this.gestures = gestures; }
    
    public BlinkPattern getBlinkPattern() { return blinkPattern; }
    public void setBlinkPattern(BlinkPattern blinkPattern) { this.blinkPattern = blinkPattern; }
}


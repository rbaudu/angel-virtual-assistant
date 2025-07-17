package com.angel.avatar;

/**
 * Message pour déclencher un geste.
 */
public class AvatarGestureMessage {
    private String type = "AVATAR_GESTURE";
    private String gestureType;
    
    public AvatarGestureMessage(String gestureType) {
        this.gestureType = gestureType;
    }
    
    // Getters
    public String getType() { return type; }
    public String getGestureType() { return gestureType; }
}

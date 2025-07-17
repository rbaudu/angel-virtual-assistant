package com.angel.avatar;


/**
 * Message pour contrôler la visibilité de l'avatar.
 */
public class AvatarVisibilityMessage {
    private String type = "AVATAR_VISIBILITY";
    private boolean visible;
    
    public AvatarVisibilityMessage(boolean visible) {
        this.visible = visible;
    }
    
    // Getters
    public String getType() { return type; }
    public boolean isVisible() { return visible; }
}

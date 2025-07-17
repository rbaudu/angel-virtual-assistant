package com.angel.avatar;


// Messages WebSocket pour la communication avec le frontend

/**
 * Message d'initialisation de l'avatar.
 */
public class AvatarInitMessage {
    private String type = "AVATAR_INIT";
    private AvatarConfig config;
    
    public AvatarInitMessage(AvatarConfig config) {
        this.config = config;
    }
    
    public String getType() { return type; }
    public AvatarConfig getConfig() { return config; }
}


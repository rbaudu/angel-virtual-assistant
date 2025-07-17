package com.angel.avatar;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.angel.util.LogUtil;

/**
 * Contrôleur WebSocket pour gérer les connexions avatar.
 * Dans une vraie implémentation, utiliser @Controller et @MessageMapping
 */
public class AvatarWebSocketController {
    
    private static final Logger LOGGER = LogUtil.getLogger(AvatarWebSocketController.class);
    
    private final AvatarManager avatarManager;
    
    public AvatarWebSocketController(AvatarManager avatarManager) {
        this.avatarManager = avatarManager;
    }
    
    /**
     * Gère les connexions WebSocket entrantes.
     */
    public void handleConnection(/* WebSocketSession session */) {
        LOGGER.log(Level.INFO, "Nouvelle connexion avatar WebSocket");
        
        // Initialiser l'avatar pour le nouveau client
        avatarManager.initializeAvatar();
    }
    
    /**
     * Gère les déconnexions WebSocket.
     */
    public void handleDisconnection(/* WebSocketSession session */) {
        LOGGER.log(Level.INFO, "Déconnexion avatar WebSocket");
    }
    
    /**
     * Gère les messages entrants du frontend.
     */
    public void handleMessage(String message /* WebSocketSession session */) {
        try {
            // Parser le message JSON et réagir en conséquence
            // Par exemple: changement d'apparence, réglages, etc.
            
            LOGGER.log(Level.FINE, "Message reçu du frontend avatar: {0}", message);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du traitement du message avatar", e);
        }
    }
}
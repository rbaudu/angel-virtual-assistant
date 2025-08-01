package com.angel.avatar;

import com.angel.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les communications WebSocket avec l'avatar frontend.
 * Version corrigée sans erreur de compilation.
 */
@Service
public class WebSocketService {
    
    private static final Logger LOGGER = LogUtil.getLogger(WebSocketService.class);
    
    private final ConcurrentMap<String, WebSocketSession> avatarSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Enregistre une session WebSocket pour l'avatar.
     */
    public void registerAvatarSession(String sessionId, WebSocketSession session) {
        avatarSessions.put(sessionId, session);
        LOGGER.log(Level.INFO, "🔌 Session avatar enregistrée: {0} (Total: {1})", 
                  new Object[]{sessionId, avatarSessions.size()});
    }
    
    /**
     * Désenregistre une session WebSocket.
     */
    public void unregisterAvatarSession(String sessionId) {
        WebSocketSession removed = avatarSessions.remove(sessionId);
        if (removed != null) {
            LOGGER.log(Level.INFO, "🔌 Session avatar désenregistrée: {0} (Restant: {1})", 
                      new Object[]{sessionId, avatarSessions.size()});
        }
    }
    
    /**
     * Envoie un message à tous les clients avatar connectés.
     */
    public void sendToAvatar(Object message) {
        LOGGER.log(Level.INFO, "📤 Tentative envoi message avatar: {0} (Sessions: {1})", 
                  new Object[]{message.getClass().getSimpleName(), avatarSessions.size()});
        
        if (avatarSessions.isEmpty()) {
            LOGGER.log(Level.WARNING, "⚠️ Aucune session avatar connectée pour envoyer: {0}", 
                      message.getClass().getSimpleName());
            return;
        }
        
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            LOGGER.log(Level.INFO, "📋 Message JSON créé: {0}", jsonMessage);
            
            // Envoyer à toutes les sessions actives
            int sentCount = 0;
            List<String> sessionsToRemove = new ArrayList<>();
            
            for (ConcurrentMap.Entry<String, WebSocketSession> entry : avatarSessions.entrySet()) {
                WebSocketSession session = entry.getValue();
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(jsonMessage));
                        sentCount++;
                        LOGGER.log(Level.INFO, "✅ Message envoyé à la session {0}: {1}", 
                                  new Object[]{entry.getKey(), message.getClass().getSimpleName()});
                    } else {
                        LOGGER.log(Level.FINE, "🔒 Session fermée marquée pour suppression: {0}", entry.getKey());
                        sessionsToRemove.add(entry.getKey());
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "❌ Erreur envoi message à la session " + entry.getKey(), e);
                    sessionsToRemove.add(entry.getKey());
                }
            }
            
            // Supprimer les sessions fermées
            for (String sessionId : sessionsToRemove) {
                avatarSessions.remove(sessionId);
            }
            
            LOGGER.log(Level.INFO, "📊 Messages envoyés: {0}/{1} sessions", 
                      new Object[]{sentCount, avatarSessions.size()});
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur lors de la sérialisation du message avatar", e);
        }
    }
    
    /**
     * Envoie un message de synthèse vocale directement.
     * Cette méthode est spécialement conçue pour la synthèse vocale.
     */
    public void sendSpeechMessage(String text, String emotion) {
        LOGGER.log(Level.INFO, "🗣️ DEMANDE synthèse vocale: \"{0}\" (émotion: {1})", 
                  new Object[]{text, emotion});
        
        if (avatarSessions.isEmpty()) {
            LOGGER.log(Level.WARNING, "⚠️ Aucune session connectée pour la synthèse vocale");
            return;
        }
        
        try {
            // Créer un message simple pour la synthèse vocale
            String speechMessage = String.format(
                "{\"type\":\"AVATAR_SPEAK\",\"text\":\"%s\",\"emotion\":\"%s\",\"timestamp\":%d}",
                text.replace("\"", "\\\"").replace("\n", "\\n"),
                emotion,
                System.currentTimeMillis()
            );
            
            LOGGER.log(Level.INFO, "📋 Message vocal JSON: {0}", speechMessage);
            
            // Envoyer à toutes les sessions
            int sentCount = 0;
            for (ConcurrentMap.Entry<String, WebSocketSession> entry : avatarSessions.entrySet()) {
                WebSocketSession session = entry.getValue();
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(speechMessage));
                        sentCount++;
                        LOGGER.log(Level.INFO, "✅ Message vocal envoyé à session {0}: \"{1}\"", 
                                  new Object[]{entry.getKey(), text});
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "❌ Erreur envoi message vocal à session " + entry.getKey(), e);
                }
            }
            
            if (sentCount > 0) {
                LOGGER.log(Level.INFO, "🎯 Message vocal envoyé avec succès à {0} session(s)", sentCount);
            } else {
                LOGGER.log(Level.WARNING, "⚠️ Aucun message vocal envoyé (aucune session active)");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur lors de l'envoi du message vocal", e);
        }
    }
    
    /**
     * Vérifie si des sessions avatar sont connectées.
     */
    public boolean hasConnectedAvatarSessions() {
        // Nettoyer les sessions fermées
        cleanClosedSessions();
        boolean hasConnected = !avatarSessions.isEmpty();
        
        LOGGER.log(Level.FINE, "🔍 Sessions connectées: {0} (hasConnected: {1})", 
                  new Object[]{avatarSessions.size(), hasConnected});
        
        return hasConnected;
    }
    
    /**
     * Obtient le nombre de sessions avatar connectées.
     */
    public int getConnectedAvatarSessionCount() {
        // Nettoyer les sessions fermées avant de compter
        cleanClosedSessions();
        int count = avatarSessions.size();
        
        LOGGER.log(Level.FINE, "📊 Nombre de sessions actives: {0}", count);
        return count;
    }
    
    /**
     * Obtient toutes les sessions actives.
     */
    public ConcurrentMap<String, WebSocketSession> getActiveSessions() {
        // Nettoyer les sessions fermées
        cleanClosedSessions();
        return new ConcurrentHashMap<>(avatarSessions);
    }
    
    /**
     * Nettoie les sessions fermées.
     */
    private void cleanClosedSessions() {
        List<String> sessionsToRemove = new ArrayList<>();
        
        for (ConcurrentMap.Entry<String, WebSocketSession> entry : avatarSessions.entrySet()) {
            if (!entry.getValue().isOpen()) {
                sessionsToRemove.add(entry.getKey());
            }
        }
        
        for (String sessionId : sessionsToRemove) {
            avatarSessions.remove(sessionId);
            LOGGER.log(Level.FINE, "🧹 Session fermée nettoyée: {0}", sessionId);
        }
    }
    
    /**
     * Méthode de debug pour lister toutes les sessions.
     */
    public void debugListSessions() {
        LOGGER.log(Level.INFO, "🔍 DEBUG - Sessions WebSocket:");
        if (avatarSessions.isEmpty()) {
            LOGGER.log(Level.INFO, "  ❌ Aucune session enregistrée");
        } else {
            for (ConcurrentMap.Entry<String, WebSocketSession> entry : avatarSessions.entrySet()) {
                WebSocketSession session = entry.getValue();
                LOGGER.log(Level.INFO, "  📍 Session {0}: {1} (ouvert: {2})", 
                          new Object[]{entry.getKey(), session.getClass().getSimpleName(), session.isOpen()});
            }
        }
    }
}
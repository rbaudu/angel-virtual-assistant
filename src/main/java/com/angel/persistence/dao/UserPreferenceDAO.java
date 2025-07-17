package com.angel.persistence.dao;

import com.angel.persistence.DatabaseManager;
import com.angel.util.LogUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object pour la gestion des préférences utilisateur dans la base de données.
 */
public class UserPreferenceDAO {

    private static final Logger LOGGER = LogUtil.getLogger(UserPreferenceDAO.class);
    
    private final DatabaseManager databaseManager;
    
    /**
     * Constructeur avec injection du gestionnaire de base de données.
     * 
     * @param databaseManager Le gestionnaire de base de données
     */
    public UserPreferenceDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Récupère toutes les préférences d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @return Map des préférences (clé -> valeur)
     */
    public Map<String, String> getUserPreferences(Long userId) {
        String sql = """
            SELECT preference_key, preference_value
            FROM user_preferences
            WHERE user_id = ?
        """;
        
        Map<String, String> preferences = new HashMap<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    preferences.put(rs.getString("preference_key"), rs.getString("preference_value"));
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des préférences utilisateur", e);
            throw new RuntimeException("Impossible de récupérer les préférences utilisateur", e);
        }
        
        return preferences;
    }
    
    /**
     * Récupère une préférence spécifique d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param key Clé de la préférence
     * @return Valeur de la préférence, ou null si elle n'existe pas
     */
    public String getUserPreference(Long userId, String key) {
        String sql = """
            SELECT preference_value
            FROM user_preferences
            WHERE user_id = ? AND preference_key = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setString(2, key);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("preference_value");
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la préférence utilisateur", e);
            throw new RuntimeException("Impossible de récupérer la préférence utilisateur", e);
        }
        
        return null;
    }
    
    /**
     * Définit une préférence pour un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param key Clé de la préférence
     * @param value Valeur de la préférence
     */
    public void setUserPreference(Long userId, String key, String value) {
        String sql = """
            INSERT INTO user_preferences (user_id, preference_key, preference_value)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE preference_value = ?, updated_at = CURRENT_TIMESTAMP
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setString(2, key);
            stmt.setString(3, value);
            stmt.setString(4, value);
            
            stmt.executeUpdate();
            
            LOGGER.log(Level.FINE, "Préférence mise à jour pour l'utilisateur {0}: {1} = {2}", 
                      new Object[]{userId, key, value});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la préférence utilisateur", e);
            throw new RuntimeException("Impossible de mettre à jour la préférence utilisateur", e);
        }
    }
    
    /**
     * Définit plusieurs préférences pour un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param preferences Map des préférences à définir
     */
    public void setUserPreferences(Long userId, Map<String, String> preferences) {
        String sql = """
            INSERT INTO user_preferences (user_id, preference_key, preference_value)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE preference_value = ?, updated_at = CURRENT_TIMESTAMP
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Map.Entry<String, String> entry : preferences.entrySet()) {
                stmt.setLong(1, userId);
                stmt.setString(2, entry.getKey());
                stmt.setString(3, entry.getValue());
                stmt.setString(4, entry.getValue());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            
            LOGGER.log(Level.FINE, "Mise à jour de {0} préférences pour l'utilisateur {1}", 
                      new Object[]{preferences.size(), userId});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour des préférences utilisateur", e);
            throw new RuntimeException("Impossible de mettre à jour les préférences utilisateur", e);
        }
    }
    
    /**
     * Supprime une préférence d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param key Clé de la préférence à supprimer
     */
    public void removeUserPreference(Long userId, String key) {
        String sql = """
            DELETE FROM user_preferences
            WHERE user_id = ? AND preference_key = ?
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setString(2, key);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.FINE, "Préférence supprimée pour l'utilisateur {0}: {1}", 
                          new Object[]{userId, key});
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la préférence utilisateur", e);
            throw new RuntimeException("Impossible de supprimer la préférence utilisateur", e);
        }
    }
    
    /**
     * Supprime toutes les préférences d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     */
    public void removeAllUserPreferences(Long userId) {
        String sql = "DELETE FROM user_preferences WHERE user_id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            LOGGER.log(Level.FINE, "Suppression de {0} préférences pour l'utilisateur {1}", 
                      new Object[]{affectedRows, userId});
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression des préférences utilisateur", e);
            throw new RuntimeException("Impossible de supprimer les préférences utilisateur", e);
        }
    }
}
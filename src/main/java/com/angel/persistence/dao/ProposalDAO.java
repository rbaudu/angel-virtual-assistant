package com.angel.persistence.dao;

import com.angel.model.ProposalHistory;
import com.angel.persistence.DatabaseManager;
import com.angel.util.LogUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object pour la gestion des propositions dans la base de données.
 */
public class ProposalDAO {

    private static final Logger LOGGER = LogUtil.getLogger(ProposalDAO.class);
    
    private final DatabaseManager databaseManager;
    
    /**
     * Constructeur avec injection du gestionnaire de base de données.
     * 
     * @param databaseManager Le gestionnaire de base de données
     */
    public ProposalDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    /**
     * Sauvegarde une proposition dans la base de données.
     * 
     * @param proposalHistory L'historique de proposition à sauvegarder
     * @return L'ID généré pour la proposition
     */
    public Long saveProposal(ProposalHistory proposalHistory) {
        String sql = """
            INSERT INTO proposal_history (proposal_type, timestamp, activity_type, title, accepted, completion_time)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, proposalHistory.getProposalType());
            stmt.setTimestamp(2, Timestamp.valueOf(proposalHistory.getTimestamp()));
            stmt.setString(3, proposalHistory.getActivityType());
            stmt.setString(4, proposalHistory.getTitle());
            stmt.setBoolean(5, proposalHistory.isAccepted());
            
            if (proposalHistory.getCompletionTime() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(proposalHistory.getCompletionTime()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        proposalHistory.setId(id);
                        LOGGER.log(Level.FINE, "Proposition sauvegardée avec l'ID: {0}", id);
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde de la proposition", e);
            throw new RuntimeException("Impossible de sauvegarder la proposition", e);
        }
        
        return null;
    }
    
    /**
     * Récupère les propositions récentes.
     * 
     * @param hoursBack Nombre d'heures en arrière à partir de maintenant
     * @return Liste des propositions récentes
     */
    public List<ProposalHistory> getRecentProposals(int hoursBack) {
        String sql = """
            SELECT id, proposal_type, timestamp, activity_type, title, accepted, completion_time
            FROM proposal_history
            WHERE timestamp >= ?
            ORDER BY timestamp DESC
        """;
        
        List<ProposalHistory> proposals = new ArrayList<>();
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursBack);
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(cutoffTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProposalHistory proposal = new ProposalHistory();
                    proposal.setId(rs.getLong("id"));
                    proposal.setProposalType(rs.getString("proposal_type"));
                    proposal.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    proposal.setActivityType(rs.getString("activity_type"));
                    proposal.setTitle(rs.getString("title"));
                    proposal.setAccepted(rs.getBoolean("accepted"));
                    
                    Timestamp completionTime = rs.getTimestamp("completion_time");
                    if (completionTime != null) {
                        proposal.setCompletionTime(completionTime.toLocalDateTime());
                    }
                    
                    proposals.add(proposal);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des propositions récentes", e);
            throw new RuntimeException("Impossible de récupérer les propositions récentes", e);
        }
        
        return proposals;
    }
    
    /**
     * Récupère les propositions d'un type spécifique.
     * 
     * @param proposalType Type de proposition à récupérer
     * @param hoursBack Nombre d'heures en arrière à partir de maintenant
     * @return Liste des propositions du type spécifié
     */
    public List<ProposalHistory> getProposalsByType(String proposalType, int hoursBack) {
        String sql = """
            SELECT id, proposal_type, timestamp, activity_type, title, accepted, completion_time
            FROM proposal_history
            WHERE proposal_type = ? AND timestamp >= ?
            ORDER BY timestamp DESC
        """;
        
        List<ProposalHistory> proposals = new ArrayList<>();
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursBack);
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, proposalType);
            stmt.setTimestamp(2, Timestamp.valueOf(cutoffTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProposalHistory proposal = new ProposalHistory();
                    proposal.setId(rs.getLong("id"));
                    proposal.setProposalType(rs.getString("proposal_type"));
                    proposal.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    proposal.setActivityType(rs.getString("activity_type"));
                    proposal.setTitle(rs.getString("title"));
                    proposal.setAccepted(rs.getBoolean("accepted"));
                    
                    Timestamp completionTime = rs.getTimestamp("completion_time");
                    if (completionTime != null) {
                        proposal.setCompletionTime(completionTime.toLocalDateTime());
                    }
                    
                    proposals.add(proposal);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des propositions par type", e);
            throw new RuntimeException("Impossible de récupérer les propositions par type", e);
        }
        
        return proposals;
    }
    
    /**
     * Met à jour une proposition pour marquer qu'elle a été acceptée.
     * 
     * @param proposalId ID de la proposition
     */
    public void markProposalAsAccepted(Long proposalId) {
        String sql = "UPDATE proposal_history SET accepted = TRUE WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, proposalId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.FINE, "Proposition {0} marquée comme acceptée", proposalId);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la proposition", e);
            throw new RuntimeException("Impossible de mettre à jour la proposition", e);
        }
    }
    
    /**
     * Met à jour une proposition pour marquer qu'elle a été complétée.
     * 
     * @param proposalId ID de la proposition
     */
    public void markProposalAsCompleted(Long proposalId) {
        String sql = "UPDATE proposal_history SET completion_time = ? WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, proposalId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.FINE, "Proposition {0} marquée comme complétée", proposalId);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la proposition", e);
            throw new RuntimeException("Impossible de mettre à jour la proposition", e);
        }
    }
    
    /**
     * Supprime les anciennes propositions au-delà d'un certain nombre de jours.
     * 
     * @param daysToKeep Nombre de jours à conserver
     * @return Nombre de propositions supprimées
     */
    public int cleanupOldProposals(int daysToKeep) {
        String sql = "DELETE FROM proposal_history WHERE timestamp < ?";
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(cutoffTime));
            
            int deletedRows = stmt.executeUpdate();
            LOGGER.log(Level.INFO, "Suppression de {0} anciennes propositions", deletedRows);
            
            return deletedRows;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du nettoyage des anciennes propositions", e);
            throw new RuntimeException("Impossible de nettoyer les anciennes propositions", e);
        }
    }
}
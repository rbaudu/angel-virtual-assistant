package com.angel.persistence;

import com.angel.config.ConfigManager;
import com.angel.util.LogUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de base de données qui gère la connexion et l'initialisation
 * de la base de données H2.
 */
public class DatabaseManager {

    private static final Logger LOGGER = LogUtil.getLogger(DatabaseManager.class);
    
    private final ConfigManager configManager;
    private Connection connection;
    
    /**
     * Constructeur avec injection du gestionnaire de configuration.
     * 
     * @param configManager Le gestionnaire de configuration
     */
    public DatabaseManager(ConfigManager configManager) {
        this.configManager = configManager;
        initializeDatabase();
    }
    
    /**
     * Initialise la base de données et crée les tables nécessaires.
     */
    private void initializeDatabase() {
        try {
            // Charger le driver H2
            Class.forName(configManager.getString("database.driver"));
            
            // Établir la connexion
            String url = configManager.getString("database.url");
            String username = configManager.getString("database.username");
            String password = configManager.getString("database.password");
            
            connection = DriverManager.getConnection(url, username, password);
            
            LOGGER.log(Level.INFO, "Connexion à la base de données établie");
            
            // Créer les tables
            createTables();
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver de base de données non trouvé", e);
            throw new RuntimeException("Impossible de charger le driver de base de données", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la connexion à la base de données", e);
            throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
    }
    
    /**
     * Crée les tables nécessaires si elles n'existent pas.
     */
    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            
            // Table pour l'historique des propositions
            String createProposalHistoryTable = """
                CREATE TABLE IF NOT EXISTS proposal_history (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    proposal_type VARCHAR(50) NOT NULL,
                    timestamp TIMESTAMP NOT NULL,
                    activity_type VARCHAR(50) NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    accepted BOOLEAN DEFAULT FALSE,
                    completion_time TIMESTAMP NULL
                )
            """;
            stmt.execute(createProposalHistoryTable);
            
            // Table pour les préférences utilisateur
            String createUserPreferencesTable = """
                CREATE TABLE IF NOT EXISTS user_preferences (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    preference_key VARCHAR(100) NOT NULL,
                    preference_value VARCHAR(500) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE(user_id, preference_key)
                )
            """;
            stmt.execute(createUserPreferencesTable);
            
            // Table pour les activités détectées (cache local)
            String createActivitiesTable = """
                CREATE TABLE IF NOT EXISTS activities (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    activity_type VARCHAR(50) NOT NULL,
                    timestamp TIMESTAMP NOT NULL,
                    confidence DOUBLE NOT NULL,
                    source VARCHAR(50) NOT NULL,
                    additional_info TEXT
                )
            """;
            stmt.execute(createActivitiesTable);
            
            // Index pour améliorer les performances
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_proposal_history_type_timestamp ON proposal_history(proposal_type, timestamp)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_activities_timestamp ON activities(timestamp)");
            
            LOGGER.log(Level.INFO, "Tables créées avec succès");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création des tables", e);
            throw new RuntimeException("Impossible de créer les tables", e);
        }
    }
    
    /**
     * Obtient la connexion à la base de données.
     * 
     * @return La connexion à la base de données
     */
    public Connection getConnection() {
        try {
            // Vérifier si la connexion est encore valide
            if (connection == null || connection.isClosed()) {
                initializeDatabase();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la vérification de la connexion", e);
            initializeDatabase();
        }
        
        return connection;
    }
    
    /**
     * Ferme la connexion à la base de données.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.log(Level.INFO, "Connexion à la base de données fermée");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la connexion", e);
            }
        }
    }
    
    /**
     * Exécute une requête de test pour vérifier la connexion.
     * 
     * @return true si la connexion fonctionne, false sinon
     */
    public boolean testConnection() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Test de connexion échoué", e);
            return false;
        }
    }
}
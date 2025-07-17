package com.angel.config;

import com.angel.util.LogUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de configuration qui charge et fournit l'accès
 * aux paramètres de configuration centralisés.
 */
public class ConfigManager {

    private static final Logger LOGGER = LogUtil.getLogger(ConfigManager.class);
    private static final String DEFAULT_CONFIG_PATH = "./config/angel-config.json";
    
    private final ObjectMapper objectMapper;
    private JsonNode rootNode;
    private final String configPath;

    /**
     * Constructeur avec chemin de configuration par défaut.
     */
    public ConfigManager() {
        this(DEFAULT_CONFIG_PATH);
    }

    /**
     * Constructeur avec chemin de configuration spécifié.
     * 
     * @param configPath Chemin vers le fichier de configuration JSON
     */
    public ConfigManager(String configPath) {
        this.configPath = configPath;
        this.objectMapper = new ObjectMapper();
        loadConfiguration();
    }

    /**
     * Charge la configuration à partir du fichier JSON.
     */
    private void loadConfiguration() {
        try {
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                LOGGER.log(Level.SEVERE, "Fichier de configuration non trouvé: {0}", configPath);
                throw new IOException("Fichier de configuration non trouvé");
            }
            
            rootNode = objectMapper.readTree(configFile);
            LOGGER.log(Level.INFO, "Configuration chargée avec succès depuis {0}", configPath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la configuration", e);
            throw new RuntimeException("Impossible de charger la configuration", e);
        }
    }

    /**
     * Recharge la configuration depuis le fichier.
     */
    public void reloadConfiguration() {
        loadConfiguration();
    }

    /**
     * Récupère une valeur de configuration sous forme de chaîne.
     * 
     * @param path Chemin d'accès à la propriété (format: \"section.subsection.property\")
     * @return La valeur de la propriété, ou null si elle n'existe pas
     */
    public String getString(String path) {
        JsonNode node = getNodeAtPath(path);
        return node != null && !node.isNull() ? node.asText() : null;
    }
    
    /**
     * Récupère une valeur de configuration sous forme de chaîne, avec valeur par défaut.
     * 
     * @param path Chemin d'accès à la propriété
     * @param defaultValue Valeur par défaut si la propriété n'existe pas
     * @return La valeur de la propriété, ou la valeur par défaut si elle n'existe pas
     */
    public String getString(String path, String defaultValue) {
        String value = getString(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Récupère une valeur de configuration sous forme d'entier.
     * 
     * @param path Chemin d'accès à la propriété
     * @return La valeur de la propriété, ou 0 si elle n'existe pas ou n'est pas un entier
     */
    public int getInt(String path) {
        JsonNode node = getNodeAtPath(path);
        return node != null && node.isInt() ? node.asInt() : 0;
    }
    
    /**
     * Récupère une valeur de configuration sous forme d'entier, avec valeur par défaut.
     * 
     * @param path Chemin d'accès à la propriété
     * @param defaultValue Valeur par défaut si la propriété n'existe pas
     * @return La valeur de la propriété, ou la valeur par défaut si elle n'existe pas
     */
    public int getInt(String path, int defaultValue) {
        JsonNode node = getNodeAtPath(path);
        return node != null && node.isInt() ? node.asInt() : defaultValue;
    }

    /**
     * Récupère une valeur de configuration sous forme de long.
     * 
     * @param path Chemin d'accès à la propriété
     * @return La valeur de la propriété, ou 0L si elle n'existe pas ou n'est pas un nombre
     */
    public long getLong(String path) {
        JsonNode node = getNodeAtPath(path);
        return node != null && node.isNumber() ? node.asLong() : 0L;
    }
    
    /**
     * Récupère une valeur de configuration sous forme de long, avec valeur par défaut.
     * 
     * @param path Chemin d'accès à la propriété
     * @param defaultValue Valeur par défaut si la propriété n'existe pas
     * @return La valeur de la propriété, ou la valeur par défaut si elle n'existe pas
     */
    public long getLong(String path, long defaultValue) {
        JsonNode node = getNodeAtPath(path);
        return node != null && node.isNumber() ? node.asLong() : defaultValue;
    }

    /**
     * Récupère une valeur de configuration sous forme de booléen.
     * 
     * @param path Chemin d'accès à la propriété
     * @return La valeur de la propriété, ou false si elle n'existe pas
     */
    public boolean getBoolean(String path) {
        JsonNode node = getNodeAtPath(path);
        return node != null && node.isBoolean() && node.asBoolean();
    }
    
    /**
     * Récupère une valeur de configuration sous forme de booléen, avec valeur par défaut.
     * 
     * @param path Chemin d'accès à la propriété
     * @param defaultValue Valeur par défaut si la propriété n'existe pas
     * @return La valeur de la propriété, ou la valeur par défaut si elle n'existe pas
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        JsonNode node = getNodeAtPath(path);
        return node != null && node.isBoolean() ? node.asBoolean() : defaultValue;
    }

    /**
     * Récupère une liste d'entiers depuis la configuration.
     * 
     * @param path Chemin d'accès à la propriété (qui doit être un tableau JSON)
     * @return Liste d'entiers, ou liste vide si la propriété n'existe pas ou n'est pas un tableau
     */
    public List<Integer> getIntegerList(String path) {
        JsonNode node = getNodeAtPath(path);
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }
        
        List<Integer> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item.isInt()) {
                result.add(item.asInt());
            }
        }
        return result;
    }

    /**
     * Récupère une liste de chaînes depuis la configuration.
     * 
     * @param path Chemin d'accès à la propriété (qui doit être un tableau JSON)
     * @return Liste de chaînes, ou liste vide si la propriété n'existe pas ou n'est pas un tableau
     */
    public List<String> getStringList(String path) {
        JsonNode node = getNodeAtPath(path);
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }
        
        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            if (item.isTextual()) {
                result.add(item.asText());
            }
        }
        return result;
    }

    /**
     * Récupère un nœud JsonNode à partir d'un chemin d'accès.
     * 
     * @param path Chemin d'accès séparé par des points (ex: \"section.subsection.property\")
     * @return Le nœud JsonNode correspondant, ou null s'il n'existe pas
     */
    private JsonNode getNodeAtPath(String path) {
        if (rootNode == null) {
            return null;
        }
        
        JsonNode currentNode = rootNode;
        for (String part : path.split("\\.")) {
            if (currentNode == null || !currentNode.has(part)) {
                return null;
            }
            currentNode = currentNode.get(part);
        }
        
        return currentNode;
    }
}
package com.angel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de configuration centralisé pour l'application Angel.
 * Charge et gère les configurations depuis les fichiers properties.
 */
@Configuration
public class ConfigManager {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    
    private final Map<String, Properties> configSources = new HashMap<>();
    private final Properties mergedConfig = new Properties();
    
    @Value("${avatar.config.path:config/avatar.properties}")
    private String avatarConfigPath;
    
    /**
     * Constructeur qui charge automatiquement les configurations.
     */
    public ConfigManager() {
        loadConfigurations();
    }
    
    /**
     * Charge toutes les configurations depuis les fichiers properties.
     */
    private void loadConfigurations() {
        try {
            // Charger la configuration principale de l'avatar
            loadConfigFile("avatar", avatarConfigPath);
            
            // Charger d'autres fichiers de configuration
            loadConfigFile("phoneme-viseme", "config/phoneme-viseme-mapping.properties");
            
            // Fusionner toutes les configurations
            mergeConfigurations();
            
            LOGGER.log(Level.INFO, "Configurations chargées avec succès");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des configurations", e);
        }
    }
    
    /**
     * Charge un fichier de configuration spécifique.
     */
    private void loadConfigFile(String name, String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                Properties props = new Properties();
                try (InputStream is = resource.getInputStream()) {
                    props.load(is);
                    configSources.put(name, props);
                    LOGGER.log(Level.INFO, "Configuration ''{0}'' chargée depuis {1}", new Object[]{name, path});
                }
            } else {
                LOGGER.log(Level.WARNING, "Fichier de configuration non trouvé: {0}", path);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erreur lors du chargement de " + path, e);
        }
    }
    
    /**
     * Fusionne toutes les configurations dans une seule Properties.
     */
    private void mergeConfigurations() {
        mergedConfig.clear();
        for (Properties props : configSources.values()) {
            mergedConfig.putAll(props);
        }
    }
    
    /**
     * Obtient une valeur de configuration sous forme de chaîne.
     */
    public String getString(String key, String defaultValue) {
        return mergedConfig.getProperty(key, defaultValue);
    }
    
    /**
     * Obtient une valeur de configuration sous forme de chaîne.
     */
    public String getString(String key) {
        return mergedConfig.getProperty(key);
    }
    
    /**
     * Méthodes pour compatibilité avec le mode test.
     */
    public String getSystemProperty(String key, String defaultValue) {
        return getString(key, defaultValue);
    }
    
    public String getStringProperty(String key, String defaultValue) {
        return getString(key, defaultValue);
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue);
    }
    
    /**
     * Obtient une valeur de configuration sous forme de boolean.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = mergedConfig.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Obtient une valeur de configuration sous forme d'entier.
     */
    public int getInt(String key, int defaultValue) {
        String value = mergedConfig.getProperty(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Valeur invalide pour la clé ''{0}'': {1}", new Object[]{key, value});
            return defaultValue;
        }
    }
    
    /**
     * Obtient une valeur de configuration sous forme de long.
     */
    public long getLong(String key, long defaultValue) {
        String value = mergedConfig.getProperty(key);
        try {
            return value != null ? Long.parseLong(value) : defaultValue;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Valeur invalide pour la clé ''{0}'': {1}", new Object[]{key, value});
            return defaultValue;
        }
    }
    
    /**
     * Obtient une valeur de configuration sous forme de long (sans valeur par défaut).
     */
    public Long getLong(String key) {
        String value = mergedConfig.getProperty(key);
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Valeur invalide pour la clé ''{0}'': {1}", new Object[]{key, value});
            return null;
        }
    }
    
    /**
     * Obtient une valeur de configuration sous forme de double.
     */
    public double getDouble(String key, double defaultValue) {
        String value = mergedConfig.getProperty(key);
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Valeur invalide pour la clé ''{0}'': {1}", new Object[]{key, value});
            return defaultValue;
        }
    }
    
    /**
     * Obtient une liste d'entiers à partir d'une chaîne séparée par des virgules.
     */
    public List<Integer> getIntegerList(String key) {
        String value = mergedConfig.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Integer> result = new ArrayList<>();
        String[] parts = value.split(",");
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Valeur invalide dans la liste pour la clé ''{0}'': {1}", new Object[]{key, part});
            }
        }
        return result;
    }
    
    /**
     * Obtient une liste de chaînes à partir d'une chaîne séparée par des virgules.
     */
    public List<String> getStringList(String key) {
        String value = mergedConfig.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.asList(value.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
    
    /**
     * Définit une valeur de configuration.
     */
    public void setProperty(String key, String value) {
        mergedConfig.setProperty(key, value);
    }
    
    /**
     * Vérifie si une clé existe dans la configuration.
     */
    public boolean hasProperty(String key) {
        return mergedConfig.containsKey(key);
    }
    
    /**
     * Obtient toutes les propriétés avec un préfixe donné.
     */
    public Properties getPropertiesWithPrefix(String prefix) {
        Properties result = new Properties();
        String prefixWithDot = prefix.endsWith(".") ? prefix : prefix + ".";
        
        for (String key : mergedConfig.stringPropertyNames()) {
            if (key.startsWith(prefixWithDot)) {
                String newKey = key.substring(prefixWithDot.length());
                result.setProperty(newKey, mergedConfig.getProperty(key));
            }
        }
        
        return result;
    }
    
    /**
     * Recharge les configurations depuis les fichiers.
     */
    public void reload() {
        loadConfigurations();
        LOGGER.log(Level.INFO, "Configurations rechargées");
    }
    
    /**
     * Obtient toutes les configurations sous forme de Map pour l'API.
     */
    public Map<String, Object> getAllConfigAsMap() {
        Map<String, Object> configMap = new HashMap<>();
        
        for (String key : mergedConfig.stringPropertyNames()) {
            String value = mergedConfig.getProperty(key);
            
            // Essayer de convertir en types appropriés
            Object convertedValue = convertValue(value);
            configMap.put(key, convertedValue);
        }
        
        return configMap;
    }
    
    /**
     * Convertit une valeur string en type approprié.
     */
    private Object convertValue(String value) {
        if (value == null) return null;
        
        // Boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        
        // Nombre entier
        try {
            if (!value.contains(".")) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException ignored) {
            // Pas un entier
        }
        
        // Nombre décimal
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            // Pas un nombre
        }
        
        // String par défaut
        return value;
    }
    
    /**
     * Obtient les statistiques de configuration.
     */
    public Map<String, Object> getConfigStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProperties", mergedConfig.size());
        stats.put("configSources", configSources.size());
        stats.put("sourceNames", configSources.keySet());
        return stats;
    }
}
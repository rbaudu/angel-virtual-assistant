package com.angel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestionnaire de configuration centralisé pour l'application Angel.
 * Charge et gère les configurations depuis les fichiers properties externes et internes.
 * 
 * Ordre de priorité de chargement :
 * 1. config/application-{profile}.properties (externe)
 * 2. config/application.properties (externe)
 * 3. src/main/resources/config/*.properties (classpath)
 * 4. Variables système
 */
@Configuration
public class ConfigManager {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    
    private final Properties mergedConfig = new Properties();
    private String activeProfile = "default";
    
    @Value("${spring.profiles.active:}")
    private String springProfilesActive;
    
    /**
     * Constructeur qui charge automatiquement les configurations.
     */
    public ConfigManager() {
        // Chargement immédiat pour compatibilité avec les classes non-Spring
        loadConfigurations();
    }
 
    /**
     * Initialisation après injection des dépendances Spring.
     */
    @PostConstruct
    public void init() {
        // Déterminer le profil actif
        determineActiveProfile();
        
        // Recharger avec le profil correct
        loadConfigurations();
    }
    
    /**
     * Détermine le profil actif à partir des arguments ou variables d'environnement.
     */
    private void determineActiveProfile() {
        // 1. Vérifier les arguments du programme
        String[] args = getMainArgs();
        for (int i = 0; i < args.length - 1; i++) {
            if ("-p".equals(args[i]) || "--profile".equals(args[i])) {
                activeProfile = args[i + 1];
                LOGGER.log(Level.INFO, "Profil défini par argument : {0}", activeProfile);
                return;
            }
        }
        
        // 2. Vérifier Spring profiles
        if (springProfilesActive != null && !springProfilesActive.isEmpty()) {
            activeProfile = springProfilesActive;
            LOGGER.log(Level.INFO, "Profil défini par Spring : {0}", activeProfile);
            return;
        }
        
        // 3. Vérifier les variables d'environnement
        String envProfile = System.getProperty("spring.profiles.active");
        if (envProfile == null) {
            envProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        if (envProfile != null && !envProfile.isEmpty()) {
            activeProfile = envProfile;
            LOGGER.log(Level.INFO, "Profil défini par variable d'environnement : {0}", activeProfile);
            return;
        }
        
        LOGGER.log(Level.INFO, "Utilisation du profil par défaut : {0}", activeProfile);
    }
    
    /**
     * Récupère les arguments du main (approximation).
     */
    private String[] getMainArgs() {
        // En l'absence d'accès direct aux args, on essaie de les récupérer
        // depuis les propriétés système ou on retourne un tableau vide
        String argsProperty = System.getProperty("sun.java.command");
        if (argsProperty != null) {
            return argsProperty.split("\\s+");
        }
        return new String[0];
    }
    
    /**
     * Charge toutes les configurations dans l'ordre de priorité.
     */
    private void loadConfigurations() {
        mergedConfig.clear();
        
        try {
            // 1. Charger les fichiers du classpath (priorité basse)
            loadClasspathConfigurations();
            
            // 2. Charger le fichier externe principal
            loadExternalConfiguration("config/application.properties");
            
            // 3. Charger le fichier externe spécifique au profil (priorité haute)
            if (!"default".equals(activeProfile)) {
                loadExternalConfiguration("config/application-" + activeProfile + ".properties");
            }
            
            // 4. Appliquer les variables système (priorité maximale)
            applySystemProperties();
            
            LOGGER.log(Level.INFO, "Configuration chargée avec succès. Profil actif : {0}, Total propriétés : {1}", 
                       new Object[]{activeProfile, mergedConfig.size()});
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des configurations", e);
            throw new RuntimeException("Échec du chargement de la configuration", e);
        }
    }
    
    /**
     * Charge les configurations depuis le classpath.
     */
    private void loadClasspathConfigurations() {
        // Charger les fichiers de configuration techniques
        loadClasspathFile("config/avatar.properties");
        loadClasspathFile("config/phoneme-viseme-mapping.properties");
    }
    
    /**
     * Charge un fichier de configuration depuis le classpath.
     */
    private void loadClasspathFile(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                Properties props = new Properties();
                try (InputStream is = resource.getInputStream()) {
                    props.load(is);
                    // Ajouter avec une priorité plus basse (ne pas écraser les existantes)
                    for (String key : props.stringPropertyNames()) {
                        if (!mergedConfig.containsKey(key)) {
                            mergedConfig.setProperty(key, props.getProperty(key));
                        }
                    }
                    LOGGER.log(Level.INFO, "Configuration classpath chargée : {0} ({1} propriétés)", 
                               new Object[]{path, props.size()});
                }
            } else {
                LOGGER.log(Level.WARNING, "Fichier de configuration classpath non trouvé : {0}", path);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erreur lors du chargement du fichier classpath : " + path, e);
        }
    }
    
    /**
     * Charge un fichier de configuration externe.
     */
    private void loadExternalConfiguration(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            LOGGER.log(Level.WARNING, "Fichier de configuration externe non trouvé : {0}", filePath);
            return;
        }
        
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(path)) {
                props.load(is);
                // Écraser les propriétés existantes (priorité plus haute)
                mergedConfig.putAll(props);
                LOGGER.log(Level.INFO, "Configuration externe chargée : {0} ({1} propriétés)", 
                           new Object[]{filePath, props.size()});
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du fichier externe : " + filePath, e);
            throw new RuntimeException("Impossible de charger le fichier de configuration : " + filePath, e);
        }
    }
    
    /**
     * Applique les propriétés système qui commencent par "angel."
     */
    private void applySystemProperties() {
        int count = 0;
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("angel.") || key.startsWith("spring.") || key.startsWith("logging.")) {
                mergedConfig.setProperty(key, System.getProperty(key));
                count++;
            }
        }
        if (count > 0) {
            LOGGER.log(Level.INFO, "Propriétés système appliquées : {0}", count);
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
     * Obtient le profil actif.
     */
    public String getActiveProfile() {
        return activeProfile;
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
        stats.put("activeProfile", activeProfile);
        stats.put("loadedSources", getLoadedSources());
        return stats;
    }
    
    /**
     * Obtient la liste des sources de configuration chargées.
     */
    private List<String> getLoadedSources() {
        List<String> sources = new ArrayList<>();
        
        // Vérifier les fichiers externes
        if (Files.exists(Paths.get("config/application.properties"))) {
            sources.add("config/application.properties");
        }
        if (!"default".equals(activeProfile) && Files.exists(Paths.get("config/application-" + activeProfile + ".properties"))) {
            sources.add("config/application-" + activeProfile + ".properties");
        }
        
        // Ajouter les sources classpath
        sources.add("classpath:config/avatar.properties");
        sources.add("classpath:config/phoneme-viseme-mapping.properties");
        
        return sources;
    }
}

package com.angel.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant le profil d'un utilisateur avec ses préférences
 * et ses informations personnelles.
 */
public class UserProfile {
    
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private Map<String, String> preferences;
    private Map<String, Object> personalInfo;
    
    /**
     * Constructeur par défaut.
     */
    public UserProfile() {
        this.preferences = new HashMap<>();
        this.personalInfo = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec nom.
     * 
     * @param name Nom de l'utilisateur
     */
    public UserProfile(String name) {
        this();
        this.name = name;
    }
    
    // Getters et setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }
    
    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
    
    public Map<String, String> getPreferences() {
        return preferences;
    }
    
    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }
    
    public Map<String, Object> getPersonalInfo() {
        return personalInfo;
    }
    
    public void setPersonalInfo(Map<String, Object> personalInfo) {
        this.personalInfo = personalInfo;
    }
    
    /**
     * Obtient une préférence spécifique.
     * 
     * @param key Clé de la préférence
     * @return Valeur de la préférence, ou null si elle n'existe pas
     */
    public String getPreference(String key) {
        return preferences.get(key);
    }
    
    /**
     * Obtient une préférence spécifique avec valeur par défaut.
     * 
     * @param key Clé de la préférence
     * @param defaultValue Valeur par défaut si la préférence n'existe pas
     * @return Valeur de la préférence, ou la valeur par défaut
     */
    public String getPreference(String key, String defaultValue) {
        return preferences.getOrDefault(key, defaultValue);
    }
    
    /**
     * Définit une préférence.
     * 
     * @param key Clé de la préférence
     * @param value Valeur de la préférence
     */
    public void setPreference(String key, String value) {
        preferences.put(key, value);
    }
    
    /**
     * Supprime une préférence.
     * 
     * @param key Clé de la préférence à supprimer
     */
    public void removePreference(String key) {
        preferences.remove(key);
    }
    
    /**
     * Obtient une information personnelle.
     * 
     * @param key Clé de l'information
     * @return Valeur de l'information, ou null si elle n'existe pas
     */
    public Object getPersonalInfo(String key) {
        return personalInfo.get(key);
    }
    
    /**
     * Définit une information personnelle.
     * 
     * @param key Clé de l'information
     * @param value Valeur de l'information
     */
    public void setPersonalInfo(String key, Object value) {
        personalInfo.put(key, value);
    }
    
    /**
     * Met à jour le timestamp de dernière activité.
     */
    public void updateLastActivity() {
        this.lastActiveAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", lastActiveAt=" + lastActiveAt +
                ", preferences=" + preferences +
                ", personalInfo=" + personalInfo +
                '}';
    }
}
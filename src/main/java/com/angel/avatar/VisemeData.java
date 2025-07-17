package com.angel.avatar;


/**
 * Données de visème pour la synchronisation labiale.
 */
public class VisemeData {
    private String phoneme;
    private long timestamp; // en millisecondes
    private double intensity; // 0.0 à 1.0
    
    public VisemeData(String phoneme, long timestamp) {
        this.phoneme = phoneme;
        this.timestamp = timestamp;
        this.intensity = 1.0;
    }
    
    public VisemeData(String phoneme, long timestamp, double intensity) {
        this.phoneme = phoneme;
        this.timestamp = timestamp;
        this.intensity = intensity;
    }
    
    // Getters
    public String getPhoneme() { return phoneme; }
    public long getTimestamp() { return timestamp; }
    public double getIntensity() { return intensity; }
}

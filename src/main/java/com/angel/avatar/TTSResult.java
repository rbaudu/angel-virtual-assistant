package com.angel.avatar;


/**
 * Résultat de la synthèse vocale avec données de synchronisation.
 */
public class TTSResult {
    private byte[] audioData;
    private java.util.List<VisemeData> visemeData;
    private long duration; // en millisecondes
    
    public TTSResult(byte[] audioData, java.util.List<VisemeData> visemeData, long duration) {
        this.audioData = audioData;
        this.visemeData = visemeData;
        this.duration = duration;
    }
    
    // Getters
    public byte[] getAudioData() { return audioData; }
    public java.util.List<VisemeData> getVisemeData() { return visemeData; }
    public long getDuration() { return duration; }
}


package com.angel.avatar;


/**
 * Données des gestes corporels.
 */
public class GestureData {
    private java.util.List<Gesture> gestures;
    
    public GestureData() {
        this.gestures = new java.util.ArrayList<>();
    }
    
    public void addGesture(String type, double startTime, double duration) {
        gestures.add(new Gesture(type, startTime, duration, 1.0));
    }
    
    public void addGesture(String type, double startTime, double duration, double intensity) {
        gestures.add(new Gesture(type, startTime, duration, intensity));
    }
    
    public java.util.List<Gesture> getGestures() { return gestures; }
    
    public static class Gesture {
        private String type;
        private double startTime;
        private double duration;
        private double intensity;
        
        public Gesture(String type, double startTime, double duration, double intensity) {
            this.type = type;
            this.startTime = startTime;
            this.duration = duration;
            this.intensity = intensity;
        }
        
        // Getters
        public String getType() { return type; }
        public double getStartTime() { return startTime; }
        public double getDuration() { return duration; }
        public double getIntensity() { return intensity; }
    }
}


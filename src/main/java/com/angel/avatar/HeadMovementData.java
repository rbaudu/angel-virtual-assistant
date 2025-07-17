package com.angel.avatar;


/**
 * Données des mouvements de tête.
 */
public class HeadMovementData {
    private java.util.List<HeadMovement> movements;
    
    public HeadMovementData() {
        this.movements = new java.util.ArrayList<>();
    }
    
    public void addNod(double startTime, double intensity) {
        movements.add(new HeadMovement("nod", startTime, 1.0, intensity));
    }
    
    public void addShake(double startTime, double intensity) {
        movements.add(new HeadMovement("shake", startTime, 1.5, intensity));
    }
    
    public void addTilt(double startTime, double intensity) {
        movements.add(new HeadMovement("tilt", startTime, 0.8, intensity));
    }
    
    public java.util.List<HeadMovement> getMovements() { return movements; }
    
    public static class HeadMovement {
        private String type;
        private double startTime;
        private double duration;
        private double intensity;
        
        public HeadMovement(String type, double startTime, double duration, double intensity) {
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


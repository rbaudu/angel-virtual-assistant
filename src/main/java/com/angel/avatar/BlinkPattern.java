package com.angel.avatar;


/**
 * Pattern de clignements naturels.
 */
public class BlinkPattern {
    private java.util.List<Blink> blinks;
    
    public BlinkPattern() {
        this.blinks = new java.util.ArrayList<>();
    }
    
    public void addBlink(double time, double duration) {
        blinks.add(new Blink(time, duration));
    }
    
    public java.util.List<Blink> getBlinks() { return blinks; }
    
    public static class Blink {
        private double time;
        private double duration;
        
        public Blink(double time, double duration) {
            this.time = time;
            this.duration = duration;
        }
        
        // Getters
        public double getTime() { return time; }
        public double getDuration() { return duration; }
    }
}


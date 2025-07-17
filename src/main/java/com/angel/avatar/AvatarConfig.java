package com.angel.avatar;

/**
 * Configuration complète pour l'avatar réaliste.
 */
public class AvatarConfig {
    
    // Configuration générale
    private boolean enabled = true;
    private String type = "3d_realistic";
    private String model = "female_30_casual";
    private String voiceType = "female_french_warm";
    
    // Apparence
    private int age = 30;
    private String gender = "female";
    private String style = "casual_friendly";
    private String hairColor = "brown";
    private String eyeColor = "brown";
    private String skinTone = "medium";
    
    // Capacités
    private boolean lipSyncEnabled = true;
    private boolean blinkingEnabled = true;
    private boolean headMovementEnabled = true;
    private boolean bodyLanguageEnabled = true;
    
    // Constructeurs
    public AvatarConfig() {}
    
    // Getters et setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getVoiceType() { return voiceType; }
    public void setVoiceType(String voiceType) { this.voiceType = voiceType; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    
    public String getHairColor() { return hairColor; }
    public void setHairColor(String hairColor) { this.hairColor = hairColor; }
    
    public String getEyeColor() { return eyeColor; }
    public void setEyeColor(String eyeColor) { this.eyeColor = eyeColor; }
    
    public String getSkinTone() { return skinTone; }
    public void setSkinTone(String skinTone) { this.skinTone = skinTone; }
    
    public boolean isLipSyncEnabled() { return lipSyncEnabled; }
    public void setLipSyncEnabled(boolean lipSyncEnabled) { this.lipSyncEnabled = lipSyncEnabled; }
    
    public boolean isBlinkingEnabled() { return blinkingEnabled; }
    public void setBlinkingEnabled(boolean blinkingEnabled) { this.blinkingEnabled = blinkingEnabled; }
    
    public boolean isHeadMovementEnabled() { return headMovementEnabled; }
    public void setHeadMovementEnabled(boolean headMovementEnabled) { this.headMovementEnabled = headMovementEnabled; }
    
    public boolean isBodyLanguageEnabled() { return bodyLanguageEnabled; }
    public void setBodyLanguageEnabled(boolean bodyLanguageEnabled) { this.bodyLanguageEnabled = bodyLanguageEnabled; }
    
    @Override
    public String toString() {
        return String.format("AvatarConfig{gender=%s, age=%d, style=%s, model=%s}", 
                           gender, age, style, model);
    }
}


package com.angel.avatar;


/**
 * Message pour changer l'apparence de l'avatar.
 */
public class AvatarAppearanceMessage {
    private String type = "AVATAR_APPEARANCE";
    private String modelPath;
    private String gender;
    private int age;
    private String style;
    
    public AvatarAppearanceMessage(String modelPath, String gender, int age, String style) {
        this.modelPath = modelPath;
        this.gender = gender;
        this.age = age;
        this.style = style;
    }
    
    // Getters
    public String getType() { return type; }
    public String getModelPath() { return modelPath; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getStyle() { return style; }
}


package com.angel.avatar;


/**
 * Message pour faire parler l'avatar.
 */
public class AvatarSpeechMessage {
    private String type = "AVATAR_SPEAK";
    private String text;
    private byte[] audioData;
    private java.util.List<VisemeData> visemeData;
    private String emotion;
    private AvatarAnimationData animationData;
    private long duration;
    
    public AvatarSpeechMessage(String text, byte[] audioData, java.util.List<VisemeData> visemeData,
                              String emotion, AvatarAnimationData animationData, long duration) {
        this.text = text;
        this.audioData = audioData;
        this.visemeData = visemeData;
        this.emotion = emotion;
        this.animationData = animationData;
        this.duration = duration;
    }
    
    // Getters
    public String getType() { return type; }
    public String getText() { return text; }
    public byte[] getAudioData() { return audioData; }
    public java.util.List<VisemeData> getVisemeData() { return visemeData; }
    public String getEmotion() { return emotion; }
    public AvatarAnimationData getAnimationData() { return animationData; }
    public long getDuration() { return duration; }
}


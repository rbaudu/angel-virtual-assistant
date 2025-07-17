package com.angel.avatar;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.angel.util.LogUtil;

/**
 * Service d'analyse d'émotions dans le texte.
 */
@Service
public class EmotionAnalyzer {
    
    private static final Logger LOGGER = LogUtil.getLogger(EmotionAnalyzer.class);
    
    // Dictionnaires d'émotions
    private static final Map<String, String> EMOTION_KEYWORDS = Map.of(
        "happy", "content heureux joie ravi enchanté satisfait génial super formidable",
        "sad", "triste malheureux déprimé mélancolique désolé peine chagrin",
        "excited", "excité enthousiaste passionné énergique dynamique motivé",
        "concerned", "inquiet préoccupé soucieux troublé perplexe embarrassé",
        "surprised", "surpris étonné stupéfait abasourdi ébahi",
        "neutral", "information rappel météo horaire programme"
    );
    
    private static final Map<String, String> PUNCTUATION_EMOTIONS = Map.of(
        "!", "excited",
        "?", "curious", 
        "...", "thoughtful"
    );
    
    /**
     * Analyse l'émotion dominante dans un texte.
     * 
     * @param text Texte à analyser
     * @return Émotion détectée
     */
    public String analyzeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "neutral";
        }
        
        try {
            String cleanText = text.toLowerCase()
                .replaceAll("[^a-zA-ZÀ-ÿ\\s!?.]", " ")
                .replaceAll("\\s+", " ")
                .trim();
            
            // Analyser les mots-clés émotionnels
            Map<String, Integer> emotionScores = new HashMap<>();
            
            for (Map.Entry<String, String> entry : EMOTION_KEYWORDS.entrySet()) {
                String emotion = entry.getKey();
                String[] keywords = entry.getValue().split(" ");
                
                int score = 0;
                for (String keyword : keywords) {
                    if (cleanText.contains(keyword)) {
                        score += countOccurrences(cleanText, keyword);
                    }
                }
                
                if (score > 0) {
                    emotionScores.put(emotion, score);
                }
            }
            
            // Analyser la ponctuation
            for (Map.Entry<String, String> entry : PUNCTUATION_EMOTIONS.entrySet()) {
                String punct = entry.getKey();
                String emotion = entry.getValue();
                
                if (text.contains(punct)) {
                    emotionScores.merge(emotion, 1, Integer::sum);
                }
            }
            
            // Trouver l'émotion dominante
            String dominantEmotion = emotionScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");
            
            LOGGER.log(Level.FINE, "Émotion analysée pour '{0}': {1}", new Object[]{text, dominantEmotion});
            
            return dominantEmotion;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'analyse d'émotion", e);
            return "neutral";
        }
    }
    
    /**
     * Analyse l'intensité émotionnelle du texte.
     * 
     * @param text Texte à analyser
     * @return Intensité entre 0.0 et 1.0
     */
    public double analyzeIntensity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.3;
        }
        
        double intensity = 0.5; // Base
        
        // Augmenter selon la ponctuation
        long exclamations = text.chars().filter(ch -> ch == '!').count();
        intensity += exclamations * 0.15;
        
        // Majuscules (indication d'emphase)
        long upperCaseCount = text.chars().filter(Character::isUpperCase).count();
        double upperCaseRatio = (double) upperCaseCount / text.length();
        if (upperCaseRatio > 0.3) {
            intensity += 0.2;
        }
        
        // Mots intensificateurs
        String[] intensifiers = {"très", "vraiment", "extrêmement", "absolument", "complètement"};
        for (String intensifier : intensifiers) {
            if (text.toLowerCase().contains(intensifier)) {
                intensity += 0.1;
            }
        }
        
        return Math.max(0.1, Math.min(1.0, intensity));
    }
    
    /**
     * Compte les occurrences d'un mot dans un texte.
     */
    private int countOccurrences(String text, String word) {
        return text.split("\\b" + Pattern.quote(word) + "\\b").length - 1;
    }
}


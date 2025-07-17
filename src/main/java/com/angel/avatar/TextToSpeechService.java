package com.angel.avatar;

import com.angel.config.ConfigManager;
import com.angel.util.LogUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service de synthèse vocale avancé avec support des visèmes
 * pour la synchronisation labiale.
 */
@Service
public class TextToSpeechService {
    
    private static final Logger LOGGER = LogUtil.getLogger(TextToSpeechService.class);
    
    private final ConfigManager configManager;
    
    // Mapping phonèmes français vers visèmes
    private static final Map<String, String> PHONEME_TO_VISEME = loadPhonemeMapping();
    /*private static final Map<String, String> PHONEME_TO_VISEME = Map.of(
        "a", "A", "ɑ", "A", "e", "E", "ɛ", "E", "ə", "E",
        "i", "I", "y", "I", "o", "O", "ɔ", "O", "u", "U",
        "p", "P", "b", "P", "m", "P", "f", "F", "v", "F",
        "t", "T", "d", "T", "n", "T", "l", "T", "r", "R",
        "k", "K", "g", "K", "s", "S", "z", "S", "ʃ", "S", "ʒ", "S"
    );*/
    
    public TextToSpeechService(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    /**
     * Génère l'audio et les données de visèmes pour un texte donné.
     * 
     * @param text Texte à synthétiser
     * @param voiceType Type de voix à utiliser
     * @return Résultat contenant audio et données de synchronisation
     */
    public TTSResult generateSpeechWithVisemes(String text, String voiceType) {
        try {
            LOGGER.log(Level.INFO, "Génération TTS pour: {0} (voix: {1})", new Object[]{text, voiceType});
            
            // 1. Nettoyer et préparer le texte
            String cleanText = preprocessText(text);
            
            // 2. Générer l'audio (simulation - remplacer par vraie API TTS)
            byte[] audioData = generateAudioData(cleanText, voiceType);
            
            // 3. Analyser le texte pour extraire les phonèmes
            List<PhonemeData> phonemes = extractPhonemes(cleanText);
            
            // 4. Convertir les phonèmes en visèmes avec timing
            List<VisemeData> visemes = convertToVisemes(phonemes, calculateDuration(cleanText));
            
            // 5. Calculer la durée totale
            long duration = calculateDuration(cleanText);
            
            LOGGER.log(Level.INFO, "TTS généré: {0}ms, {1} visèmes", new Object[]{duration, visemes.size()});
            
            return new TTSResult(audioData, visemes, duration);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la génération TTS", e);
            
            // Retourner un résultat minimal en cas d'erreur
            return new TTSResult(
                generateSilence(2000), 
                generateFallbackVisemes(text), 
                calculateDuration(text)
            );
        }
    }
    
    /**
     * Préprocessing du texte pour améliorer la synthèse.
     */
    private String preprocessText(String text) {
        // Normaliser les espaces
        text = text.replaceAll("\\s+", " ").trim();
        
        // Remplacer les abréviations courantes
        text = text.replaceAll("\\bM\\.", "Monsieur");
        text = text.replaceAll("\\bMme\\.", "Madame");
        text = text.replaceAll("\\bDr\\.", "Docteur");
        text = text.replaceAll("\\betc\\.", "et cetera");
        
        // Normaliser la ponctuation pour les pauses
        text = text.replaceAll("[.!?]+", ".");
        text = text.replaceAll("[,;:]+", ",");
        
        return text;
    }
    
    /**
     * Génère les données audio (simulation - à remplacer par vraie API).
     */
    private byte[] generateAudioData(String text, String voiceType) {
        // Dans une vraie implémentation, ici on appellerait:
        // - Azure Cognitive Services Speech API
        // - Google Cloud Text-to-Speech
        // - Amazon Polly
        // - ou une solution locale comme eSpeak/Festival
        
        // Pour la simulation, on génère un tableau d'octets vide
        int estimatedSize = text.length() * 100; // Estimation basique
        return new byte[estimatedSize];
    }
    
    /**
     * Extrait les phonèmes du texte français.
     */
    private List<PhonemeData> extractPhonemes(String text) {
        List<PhonemeData> phonemes = new ArrayList<>();
        
        // Algorithme simplifié d'extraction de phonèmes
        // Dans une vraie implémentation, utiliser une bibliothèque de phonétique
        
        char[] chars = text.toLowerCase().toCharArray();
        double currentTime = 0.0;
        
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            if (Character.isLetter(c)) {
                String phoneme = mapCharToPhoneme(c, chars, i);
                double duration = getPhonemeBaseDuration(phoneme);
                
                phonemes.add(new PhonemeData(phoneme, currentTime, duration));
                currentTime += duration;
                
            } else if (c == ' ') {
                // Pause courte pour les espaces
                currentTime += 100;
                
            } else if (c == ',' || c == ';') {
                // Pause moyenne pour la ponctuation
                currentTime += 300;
                
            } else if (c == '.' || c == '!' || c == '?') {
                // Pause longue pour la fin de phrase
                currentTime += 600;
            }
        }
        
        return phonemes;
    }
    
    /**
     * Mappe un caractère vers son phonème approximatif.
     */
    private String mapCharToPhoneme(char c, char[] context, int position) {
        // Mapping simplifié français
        switch (c) {
            case 'a': return "a";
            case 'e': 
                // 'e' muet en fin de mot
                if (position == context.length - 1) return "ə";
                return "e";
            case 'i': case 'y': return "i";
            case 'o': return "o";
            case 'u': return "u";
            case 'p': case 'b': return "p";
            case 't': case 'd': return "t";
            case 'k': case 'c': case 'g': return "k";
            case 'f': case 'v': return "f";
            case 's': case 'z': return "s";
            case 'l': return "l";
            case 'r': return "r";
            case 'm': case 'n': return "n";
            default: return "ə"; // Schwa par défaut
        }
    }
    
    /**
     * Durée de base d'un phonème en millisecondes.
     */
    private double getPhonemeBaseDuration(String phoneme) {
        // Durées typiques des phonèmes en français
        switch (phoneme) {
            case "a": case "e": case "i": case "o": case "u": 
                return 120; // Voyelles
            case "p": case "t": case "k": 
                return 80;  // Consonnes occlusives
            case "f": case "s": 
                return 100; // Consonnes fricatives
            case "l": case "r": case "n": case "m": 
                return 90;  // Consonnes liquides et nasales
            default: 
                return 100;
        }
    }
    
    /**
     * Convertit les phonèmes en visèmes avec timing précis.
     */
    private List<VisemeData> convertToVisemes(List<PhonemeData> phonemes, long totalDuration) {
        List<VisemeData> visemes = new ArrayList<>();
        
        for (PhonemeData phoneme : phonemes) {
            String viseme = PHONEME_TO_VISEME.getOrDefault(phoneme.getPhoneme(), "A");
            
            // Calculer l'intensité basée sur le contexte
            double intensity = calculateVisemeIntensity(phoneme, phonemes);
            
            visemes.add(new VisemeData(
                viseme, 
                (long) phoneme.getStartTime(), 
                intensity
            ));
        }
        
        return visemes;
    }
    
    /**
     * Calcule l'intensité d'un visème selon le contexte.
     */
    private double calculateVisemeIntensity(PhonemeData current, List<PhonemeData> context) {
        // Base intensity
        double intensity = 0.8;
        
        // Augmenter l'intensité pour les voyelles
        if (Arrays.asList("a", "e", "i", "o", "u").contains(current.getPhoneme())) {
            intensity = 1.0;
        }
        
        // Réduire l'intensité pour les consonnes faibles
        if (Arrays.asList("ə", "l", "r").contains(current.getPhoneme())) {
            intensity = 0.6;
        }
        
        return Math.max(0.3, Math.min(1.0, intensity));
    }
    
    /**
     * Calcule la durée estimée de lecture du texte.
     */
    private long calculateDuration(String text) {
        // Estimation basée sur la vitesse de parole moyenne (150 mots/minute)
        int words = text.split("\\s+").length;
        double wordsPerSecond = 150.0 / 60.0; // 2.5 mots/seconde
        
        long baseDuration = (long) (words / wordsPerSecond * 1000);
        
        // Ajouter du temps pour la ponctuation
        long punctuationTime = text.replaceAll("[^.!?]", "").length() * 600;
        punctuationTime += text.replaceAll("[^,;:]", "").length() * 300;
        
        return baseDuration + punctuationTime;
    }
    
    /**
     * Génère du silence audio.
     */
    private byte[] generateSilence(long durationMs) {
        // 44.1kHz, 16-bit, mono
        int samples = (int) (44100 * durationMs / 1000);
        return new byte[samples * 2]; // 2 bytes par sample
    }
    
    /**
     * Génère des visèmes de fallback en cas d'erreur.
     */
    private List<VisemeData> generateFallbackVisemes(String text) {
        List<VisemeData> fallback = new ArrayList<>();
        
        // Créer des visèmes basiques basés sur les voyelles détectées
        long time = 0;
        for (char c : text.toLowerCase().toCharArray()) {
            if ("aeiou".indexOf(c) >= 0) {
                String viseme = String.valueOf(c).toUpperCase();
                fallback.add(new VisemeData(viseme, time, 0.8));
                time += 200;
            } else if (Character.isLetter(c)) {
                time += 100;
            }
        }
        
        return fallback;
    }
    
    /**
     * Classe interne pour représenter les données de phonèmes.
     */
    private static class PhonemeData {
        private final String phoneme;
        private final double startTime;
        private final double duration;
        
        public PhonemeData(String phoneme, double startTime, double duration) {
            this.phoneme = phoneme;
            this.startTime = startTime;
            this.duration = duration;
        }
        
        public String getPhoneme() { return phoneme; }
        public double getStartTime() { return startTime; }
        public double getDuration() { return duration; }
    }
    

    private static Map<String, String> loadPhonemeMapping() {
        Properties props = new Properties();
        try (InputStream input = TextToSpeechService.class
                .getResourceAsStream("/config/phoneme-viseme-mapping.properties")) {
            props.load(input);
            return props.stringPropertyNames().stream()
                    .collect(Collectors.toMap(
                        key -> key,
                        props::getProperty,
                        (a, b) -> b,
                        LinkedHashMap::new
                    ));
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement du mapping phonème-visème", e);
        }
    }
}



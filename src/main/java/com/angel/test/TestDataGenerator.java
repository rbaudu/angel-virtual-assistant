package com.angel.test;

import com.angel.api.dto.ActivityDTO;
import com.angel.model.Activity;
import com.angel.util.LogUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Générateur de données de test pour la simulation d'activités.
 * Crée des séquences réalistes d'activités avec variations temporelles.
 */
@Component
public class TestDataGenerator {
    
    private static final Logger logger = LogUtil.getLogger(TestDataGenerator.class);
    
    private final Random random = new Random();
    
    /**
     * Génère une séquence d'activités pour une journée complète.
     */
    public List<ActivityDTO> generateDailySequence(LocalDateTime startDate) {
        List<ActivityDTO> sequence = new ArrayList<>();
        LocalDateTime current = startDate.withHour(7).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startDate.withHour(23).withMinute(0).withSecond(0);
        
        logger.info("Génération d'une séquence quotidienne de " + current + " à " + endOfDay);
        
        while (current.isBefore(endOfDay)) {
            Activity activity = selectActivityForTime(current);
            double confidence = generateRealisticConfidence(activity);
            long duration = generateRealisticDuration(activity, current);
            
            sequence.add(new ActivityDTO(activity, confidence, 
                current.toEpochSecond(java.time.ZoneOffset.UTC) * 1000));
            
            current = current.plus(duration, ChronoUnit.MILLIS);
        }
        
        logger.info("Séquence générée avec " + sequence.size() + " activités");
        return sequence;
    }
    
    /**
     * Génère une séquence d'activités pour une semaine.
     */
    public List<ActivityDTO> generateWeeklySequence(LocalDateTime startDate) {
        List<ActivityDTO> sequence = new ArrayList<>();
        
        for (int day = 0; day < 7; day++) {
            LocalDateTime dayStart = startDate.plusDays(day);
            List<ActivityDTO> dailySequence = generateDailySequence(dayStart);
            sequence.addAll(dailySequence);
        }
        
        logger.info("Séquence hebdomadaire générée avec " + sequence.size() + " activités");
        return sequence;
    }
    
    /**
     * Génère des activités aléatoires pour une période donnée.
     */
    public List<ActivityDTO> generateRandomSequence(int count, long intervalMs) {
        List<ActivityDTO> sequence = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        Activity[] activities = Activity.values();
        
        for (int i = 0; i < count; i++) {
            Activity activity = activities[random.nextInt(activities.length)];
            double confidence = 0.6 + random.nextDouble() * 0.35; // 0.6 à 0.95
            
            sequence.add(new ActivityDTO(activity, confidence, currentTime));
            currentTime += intervalMs + random.nextInt((int) (intervalMs / 2)); // Variation de ±25%
        }
        
        logger.info("Séquence aléatoire générée avec " + count + " activités");
        return sequence;
    }
    
    /**
     * Génère une séquence d'activités avec transitions graduelles.
     */
    public List<ActivityDTO> generateGradualTransitionSequence(Activity startActivity, 
                                                               Activity endActivity, 
                                                               int steps, 
                                                               long totalDuration) {
        List<ActivityDTO> sequence = new ArrayList<>();
        long stepDuration = totalDuration / steps;
        long currentTime = System.currentTimeMillis();
        
        // Activités intermédiaires pour la transition
        Activity[] intermediateActivities = getIntermediateActivities(startActivity, endActivity);
        
        for (int i = 0; i < steps; i++) {
            Activity activity;
            if (i == 0) {
                activity = startActivity;
            } else if (i == steps - 1) {
                activity = endActivity;
            } else {
                activity = intermediateActivities[random.nextInt(intermediateActivities.length)];
            }
            
            // Confiance progressive (haute au début et à la fin, plus basse au milieu)
            double confidence = calculateTransitionConfidence(i, steps);
            
            sequence.add(new ActivityDTO(activity, confidence, currentTime));
            currentTime += stepDuration;
        }
        
        logger.info(String.format("Séquence de transition générée: %s -> %s (%d étapes)", 
            startActivity.getFrenchName(), endActivity.getFrenchName(), steps));
        
        return sequence;
    }
    
    /**
     * Sélectionne une activité appropriée selon l'heure de la journée.
     */
    private Activity selectActivityForTime(LocalDateTime time) {
        int hour = time.getHour();
        
        if (hour >= 6 && hour < 8) {
            // Matin tôt
            return randomChoice(Activity.WAKING_UP, Activity.WASHING, Activity.EATING);
        } else if (hour >= 8 && hour < 12) {
            // Matinée
            return randomChoice(Activity.READING, Activity.CLEANING, Activity.USING_SCREEN, 
                              Activity.COOKING, Activity.PUTTING_AWAY);
        } else if (hour >= 12 && hour < 14) {
            // Déjeuner
            return randomChoice(Activity.COOKING, Activity.EATING, Activity.CONVERSING);
        } else if (hour >= 14 && hour < 18) {
            // Après-midi
            return randomChoice(Activity.READING, Activity.WATCHING_TV, Activity.USING_SCREEN,
                              Activity.CLEANING, Activity.WAITING, Activity.LISTENING_MUSIC);
        } else if (hour >= 18 && hour < 20) {
            // Soirée
            return randomChoice(Activity.COOKING, Activity.EATING, Activity.CONVERSING);
        } else if (hour >= 20 && hour < 23) {
            // Fin de soirée
            return randomChoice(Activity.WATCHING_TV, Activity.READING, Activity.LISTENING_MUSIC,
                              Activity.CONVERSING, Activity.WAITING);
        } else {
            // Nuit
            return randomChoice(Activity.GOING_TO_SLEEP, Activity.WASHING, Activity.READING);
        }
    }
    
    /**
     * Génère une confiance réaliste selon le type d'activité.
     */
    private double generateRealisticConfidence(Activity activity) {
        // Certaines activités sont plus facilement détectables
        double baseConfidence;
        
        switch (activity) {
            case EATING:
            case COOKING:
            case WATCHING_TV:
                baseConfidence = 0.85; // Facilement détectables
                break;
            case READING:
            case USING_SCREEN:
            case WASHING:
                baseConfidence = 0.80;
                break;
            case CLEANING:
            case LISTENING_MUSIC:
            case CONVERSING:
                baseConfidence = 0.75;
                break;
            case WAITING:
            case MOVING:
            case PUTTING_AWAY:
                baseConfidence = 0.70;
                break;
            default:
                baseConfidence = 0.72;
        }
        
        // Ajouter une variation aléatoire de ±0.15
        double variation = (random.nextDouble() - 0.5) * 0.3;
        double confidence = baseConfidence + variation;
        
        // Assurer que la confiance reste dans les limites raisonnables
        return Math.max(0.5, Math.min(0.98, confidence));
    }
    
    /**
     * Génère une durée réaliste pour une activité selon l'heure.
     */
    private long generateRealisticDuration(Activity activity, LocalDateTime time) {
        // Durées de base en millisecondes
        long baseDuration;
        
        switch (activity) {
            case EATING:
                baseDuration = 20 * 60 * 1000; // 20 minutes
                break;
            case COOKING:
                baseDuration = 30 * 60 * 1000; // 30 minutes
                break;
            case WATCHING_TV:
                baseDuration = 60 * 60 * 1000; // 1 heure
                break;
            case READING:
                baseDuration = 45 * 60 * 1000; // 45 minutes
                break;
            case CLEANING:
                baseDuration = 40 * 60 * 1000; // 40 minutes
                break;
            case WASHING:
                baseDuration = 15 * 60 * 1000; // 15 minutes
                break;
            case USING_SCREEN:
                baseDuration = 35 * 60 * 1000; // 35 minutes
                break;
            case WAITING:
                baseDuration = 25 * 60 * 1000; // 25 minutes
                break;
            case CONVERSING:
                baseDuration = 20 * 60 * 1000; // 20 minutes
                break;
            case WAKING_UP:
            case GOING_TO_SLEEP:
                baseDuration = 10 * 60 * 1000; // 10 minutes
                break;
            default:
                baseDuration = 30 * 60 * 1000; // 30 minutes par défaut
        }
        
        // Variation selon l'heure (activités plus longues en soirée)
        int hour = time.getHour();
        if (hour >= 19 && hour <= 22) {
            baseDuration = (long) (baseDuration * 1.3); // +30% en soirée
        } else if (hour >= 6 && hour <= 8) {
            baseDuration = (long) (baseDuration * 0.8); // -20% le matin
        }
        
        // Ajouter une variation aléatoire de ±40%
        double variation = 0.6 + random.nextDouble() * 0.8; // 0.6 à 1.4
        return (long) (baseDuration * variation);
    }
    
    /**
     * Calcule la confiance pour une transition graduelle.
     */
    private double calculateTransitionConfidence(int step, int totalSteps) {
        if (step == 0 || step == totalSteps - 1) {
            return 0.85 + random.nextDouble() * 0.1; // Haute confiance au début/fin
        }
        
        // Confiance plus basse au milieu de la transition
        double middle = totalSteps / 2.0;
        double distance = Math.abs(step - middle) / middle;
        return 0.6 + (distance * 0.25) + random.nextDouble() * 0.1;
    }
    
    /**
     * Retourne les activités intermédiaires pour une transition.
     */
    private Activity[] getIntermediateActivities(Activity start, Activity end) {
        // Activités neutres qui peuvent servir de transition
        return new Activity[] {
            Activity.WAITING, Activity.MOVING, Activity.PUTTING_AWAY,
            Activity.USING_SCREEN, Activity.CONVERSING
        };
    }
    
    /**
     * Sélectionne aléatoirement parmi plusieurs choix.
     */
    @SafeVarargs
    private final <T> T randomChoice(T... choices) {
        return choices[random.nextInt(choices.length)];
    }
    
    /**
     * Génère des statistiques sur une séquence d'activités.
     */
    public SequenceStats analyzeSequence(List<ActivityDTO> sequence) {
        if (sequence.isEmpty()) {
            return new SequenceStats(0, 0, 0, null, null);
        }
        
        long totalDuration = 0;
        double totalConfidence = 0;
        Activity mostFrequent = null;
        java.util.Map<Activity, Integer> activityCounts = new java.util.HashMap<>();
        
        for (int i = 0; i < sequence.size(); i++) {
            ActivityDTO activity = sequence.get(i);
            totalConfidence += activity.getConfidence();
            
            // Compter les occurrences
            activityCounts.put(activity.getActivity(), 
                activityCounts.getOrDefault(activity.getActivity(), 0) + 1);
            
            // Calculer la durée (sauf pour le dernier)
            if (i < sequence.size() - 1) {
                totalDuration += sequence.get(i + 1).getTimestamp() - activity.getTimestamp();
            }
        }
        
        // Trouver l'activité la plus fréquente
        int maxCount = 0;
        for (java.util.Map.Entry<Activity, Integer> entry : activityCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        
        double averageConfidence = totalConfidence / sequence.size();
        
        return new SequenceStats(
            sequence.size(),
            totalDuration,
            averageConfidence,
            mostFrequent,
            activityCounts
        );
    }
    
    /**
     * Classe pour les statistiques de séquence.
     */
    public static class SequenceStats {
        private final int activityCount;
        private final long totalDuration;
        private final double averageConfidence;
        private final Activity mostFrequentActivity;
        private final java.util.Map<Activity, Integer> activityDistribution;
        
        public SequenceStats(int activityCount, long totalDuration, double averageConfidence,
                           Activity mostFrequentActivity, java.util.Map<Activity, Integer> activityDistribution) {
            this.activityCount = activityCount;
            this.totalDuration = totalDuration;
            this.averageConfidence = averageConfidence;
            this.mostFrequentActivity = mostFrequentActivity;
            this.activityDistribution = activityDistribution;
        }
        
        public int getActivityCount() { return activityCount; }
        public long getTotalDuration() { return totalDuration; }
        public double getAverageConfidence() { return averageConfidence; }
        public Activity getMostFrequentActivity() { return mostFrequentActivity; }
        public java.util.Map<Activity, Integer> getActivityDistribution() { return activityDistribution; }
    }
}
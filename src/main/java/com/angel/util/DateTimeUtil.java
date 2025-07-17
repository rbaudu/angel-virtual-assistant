package com.angel.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Utilitaires pour la manipulation des dates et heures.
 */
public class DateTimeUtil {

    /**
     * Calcule le nombre de millisecondes entre deux LocalDateTime.
     * 
     * @param from DateTime de début
     * @param to DateTime de fin
     * @return Nombre de millisecondes entre les deux dates
     */
    public static long millisBetween(LocalDateTime from, LocalDateTime to) {
        return ChronoUnit.MILLIS.between(from, to);
    }

    /**
     * Calcule le nombre de secondes entre deux LocalDateTime.
     * 
     * @param from DateTime de début
     * @param to DateTime de fin
     * @return Nombre de secondes entre les deux dates
     */
    public static long secondsBetween(LocalDateTime from, LocalDateTime to) {
        return ChronoUnit.SECONDS.between(from, to);
    }

    /**
     * Calcule le nombre de minutes entre deux LocalDateTime.
     * 
     * @param from DateTime de début
     * @param to DateTime de fin
     * @return Nombre de minutes entre les deux dates
     */
    public static long minutesBetween(LocalDateTime from, LocalDateTime to) {
        return ChronoUnit.MINUTES.between(from, to);
    }

    /**
     * Calcule le nombre d'heures entre deux LocalDateTime.
     * 
     * @param from DateTime de début
     * @param to DateTime de fin
     * @return Nombre d'heures entre les deux dates
     */
    public static long hoursBetween(LocalDateTime from, LocalDateTime to) {
        return ChronoUnit.HOURS.between(from, to);
    }

    /**
     * Convertit un LocalDateTime en timestamp Unix (millisecondes depuis epoch).
     * 
     * @param dateTime La date/heure à convertir
     * @return Timestamp Unix en millisecondes
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Convertit un timestamp Unix en LocalDateTime.
     * 
     * @param timestamp Timestamp Unix en millisecondes
     * @return LocalDateTime correspondant
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp), 
            ZoneId.systemDefault()
        );
    }

    /**
     * Vérifie si une date/heure est dans une plage donnée.
     * 
     * @param dateTime La date/heure à vérifier
     * @param startHour Heure de début (0-23)
     * @param endHour Heure de fin (0-23)
     * @return true si la date/heure est dans la plage, false sinon
     */
    public static boolean isInHourRange(LocalDateTime dateTime, int startHour, int endHour) {
        int hour = dateTime.getHour();
        if (startHour <= endHour) {
            return hour >= startHour && hour <= endHour;
        } else {
            // Plage qui traverse minuit (ex: 22h-6h)
            return hour >= startHour || hour <= endHour;
        }
    }

    /**
     * Vérifie si c'est le matin (entre 6h et 12h).
     * 
     * @param dateTime La date/heure à vérifier
     * @return true si c'est le matin, false sinon
     */
    public static boolean isMorning(LocalDateTime dateTime) {
        return isInHourRange(dateTime, 6, 12);
    }

    /**
     * Vérifie si c'est l'après-midi (entre 12h et 18h).
     * 
     * @param dateTime La date/heure à vérifier
     * @return true si c'est l'après-midi, false sinon
     */
    public static boolean isAfternoon(LocalDateTime dateTime) {
        return isInHourRange(dateTime, 12, 18);
    }

    /**
     * Vérifie si c'est le soir (entre 18h et 22h).
     * 
     * @param dateTime La date/heure à vérifier
     * @return true si c'est le soir, false sinon
     */
    public static boolean isEvening(LocalDateTime dateTime) {
        return isInHourRange(dateTime, 18, 22);
    }

    /**
     * Vérifie si c'est la nuit (entre 22h et 6h).
     * 
     * @param dateTime La date/heure à vérifier
     * @return true si c'est la nuit, false sinon
     */
    public static boolean isNight(LocalDateTime dateTime) {
        return isInHourRange(dateTime, 22, 6);
    }
}
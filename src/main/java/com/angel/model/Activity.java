package com.angel.model;

/**
 * Énumération des activités que le système peut détecter via Angel-server-capture.
 * Chaque activité est associée à une description en français.
 */
public enum Activity {
    CLEANING("Nettoyer"),
    CONVERSING("Converser, parler"),
    COOKING("Préparer à manger"),
    DANCING("Danser"),
    EATING("Manger"),
    FEEDING("Nourrir les animaux de compagnie"),
    GOING_TO_SLEEP("Se coucher"),
    KNITTING("Tricoter/coudre"),
    IRONING("Repasser"),
    LISTENING_MUSIC("Écouter de la musique/radio"),
    MOVING("Se déplacer"),
    NEEDING_HELP("Avoir besoin d'assistance"),
    PHONING("Téléphoner"),
    PLAYING("Jouer"),
    PLAYING_MUSIC("Jouer de la musique"),
    PUTTING_AWAY("Ranger"),
    READING("Lire"),
    RECEIVING("Recevoir quelqu'un (Sonnette, aller ouvrir)"),
    SINGING("Chanter"),
    SLEEPING("Dormir"),
    UNKNOWN("Autre - Other"),
    USING_SCREEN("Utiliser un écran (pc, laptop, tablet, smartphone)"),
    WAITING("Ne rien faire, s'ennuyer"),
    WAKING_UP("Se lever"),
    WASHING("Se laver, passer aux toilettes"),
    WATCHING_TV("Regarder la télévision"),
    WRITING("Écrire");

    private final String description;

    Activity(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Alias pour getDescription() pour compatibilité avec le code de test.
     */
    public String getFrenchName() {
        return description;
    }

    /**
     * Vérifie si l'activité courante est compatible avec les propositions.
     * Les activités comme UNKNOWN, SLEEPING ou certaines activités engageantes
     * ne sont pas compatibles avec des propositions.
     *
     * @return true si l'activité permet des propositions, false sinon
     */
    public boolean allowsProposals() {
        return this != UNKNOWN && 
               this != SLEEPING && 
               this != NEEDING_HELP && 
               this != CONVERSING && 
               this != PHONING;
    }

    /**
     * Détermine si l'activité courante est une activité passive
     * où la personne est plus susceptible d'accepter des propositions.
     *
     * @return true si l'activité est passive, false sinon
     */
    public boolean isPassive() {
        return this == WAITING || 
               this == WATCHING_TV || 
               this == LISTENING_MUSIC;
    }

    /**
     * Vérifie si l'activité courante nécessite une attention importante
     * et ne devrait être interrompue que pour des propositions importantes.
     *
     * @return true si l'activité requiert de l'attention, false sinon
     */
    public boolean requiresAttention() {
        return this == COOKING || 
               this == READING || 
               this == WRITING || 
               this == USING_SCREEN;
    }
}
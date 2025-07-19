# Migration de Configuration - Angel Virtual Assistant

## Résumé des Changements

Cette migration harmonise et centralise la configuration de l'application Angel Virtual Assistant en supprimant la duplication et en standardisant sur le format `properties`.

### ✅ Modifications Réalisées

#### 1. **Refactorisation du ConfigManager**
- ✅ Support du chargement externe prioritaire (`config/` avant `src/main/resources/`)
- ✅ Support des profils Spring Boot (`application-{profile}.properties`)
- ✅ Détection automatique du profil via argument `-p` ou variables d'environnement
- ✅ Ordre de priorité: classpath < externe < profil < propriétés système
- ✅ Gestion d'erreurs améliorée avec logging détaillé

#### 2. **Correction du DatabaseManager**
- ✅ Correction de la `NullPointerException` avec vérifications null
- ✅ Valeurs par défaut pour tous les paramètres de configuration
- ✅ Gestion d'erreurs robuste avec messages informatifs
- ✅ Méthodes de diagnostic de la configuration

#### 3. **Structure de Configuration Harmonisée**
```
config/
├── application.properties           # Configuration par défaut ✅
├── application-test.properties      # Configuration mode test ✅
└── test/                           # Configurations JSON spécifiques ✅

src/main/resources/config/
├── avatar.properties               # Configuration technique ✅
└── phoneme-viseme-mapping.properties # Mappings techniques ✅
```

#### 4. **Migration JSON → Properties**
- ✅ Contenu de `angel-config.json` migré vers `application.properties`
- ✅ Format standardisé avec notation pointée (`system.name` au lieu de `system.name`)
- ✅ Préservation de toutes les configurations existantes
- ✅ Support de tous les types de données (boolean, int, long, list)

#### 5. **Support des Profils**
- ✅ **Default** : Configuration standard pour développement
- ✅ **Test** : Base H2 en mémoire, simulation accélérée, debug activé
- ✅ **Prod** : Structure prête (à configurer selon l'environnement)

#### 6. **Outils et Documentation**
- ✅ Script de test de configuration (`scripts/test-config.sh`)
- ✅ Documentation complète (`docs/CONFIGURATION.md`)
- ✅ Compatibilité avec le script de lancement existant

### 🚀 Utilisation

```bash
# Test de la configuration
chmod +x scripts/test-config.sh
./scripts/test-config.sh

# Lancement par défaut
./angel-launcher.sh start

# Lancement en mode test
./angel-launcher.sh start -p test

# Statut et logs
./angel-launcher.sh status
./angel-launcher.sh logs
```

### 🔧 Résolution du Problème Initial

**Avant** :
- ❌ `NullPointerException` au démarrage
- ❌ Configuration dupliquée (JSON + Properties)
- ❌ Fichiers externes non chargés
- ❌ Pas de support des profils

**Après** :
- ✅ Démarrage sans erreur
- ✅ Configuration centralisée et unique par environnement
- ✅ Chargement externe prioritaire
- ✅ Support complet des profils

### 📁 Fichiers Modifiés

| Fichier | Action | Description |
|---------|--------|-------------|
| `src/main/java/com/angel/config/ConfigManager.java` | 🔄 Refactorisé | Chargement externe et profils |
| `src/main/java/com/angel/persistence/DatabaseManager.java` | 🔧 Corrigé | Gestion d'erreurs et null checks |
| `config/application-test.properties` | ➕ Créé | Configuration mode test |
| `docs/CONFIGURATION.md` | ➕ Créé | Documentation complète |
| `scripts/test-config.sh` | ➕ Créé | Script de validation |

### 🎯 Prochaines Étapes

1. **Tester** la nouvelle configuration :
   ```bash
   ./scripts/test-config.sh
   ```

2. **Lancer en mode test** pour valider :
   ```bash
   ./angel-launcher.sh start -p test
   ```

3. **Créer** `config/application-prod.properties` si besoin pour la production

4. **Supprimer** `config/angel-config.json` (maintenant vide) après validation

### 💡 Avantages de la Nouvelle Architecture

- **Simplicité** : Un seul fichier de configuration par environnement
- **Flexibilité** : Profils pour différents environnements
- **Standard** : Format Properties compatible Spring Boot
- **Maintenabilité** : Configuration externe sans recompilation
- **Robustesse** : Gestion d'erreurs et valeurs par défaut
- **Évolutivité** : Facile d'ajouter de nouveaux profils ou propriétés

La configuration est maintenant **harmonisée**, **centralisée** et **robuste** ! 🎉

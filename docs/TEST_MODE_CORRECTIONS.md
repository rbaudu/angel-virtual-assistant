# Corrections et Mises à Jour - Mode Test

## 🔧 Corrections apportées

### 1. Compatibilité avec angel-launcher.sh

**Problème identifié** : La documentation mentionnait `angel.sh` mais vos scripts s'appellent `angel-launcher.sh` et `angel-launcher.bat`.

**Solution** : Le mode test est maintenant **parfaitement compatible** avec votre système existant.

### 2. Détection du mode test

Le service `TestModeService` détecte maintenant le mode test via **4 méthodes** :

#### ✅ Méthode 1 : Via angel-launcher.sh (Recommandée)
```bash
./angel-launcher.sh start -p test
```
Cela ajoute `-Dangel.profile=test` qui est automatiquement détecté.

#### ✅ Méthode 2 : Via propriété directe
```bash
java -Dangel.test.enabled=true -jar app.jar
```

#### ✅ Méthode 3 : Via variable d'environnement
```bash
export ANGEL_TEST_ENABLED=true
./angel-launcher.sh start
```

#### ✅ Méthode 4 : Via configuration JSON
```json
// config/angel-config.json
{
  "system": {
    "mode": "test"
  }
}
```

## 🚀 Utilisation Corrigée

### Démarrage en mode test

```bash
# ✅ CORRECT - Avec votre script existant
./angel-launcher.sh start -p test

# ✅ CORRECT - Mode daemon
./angel-launcher.sh start -p test -b

# ✅ CORRECT - Avec debug
./angel-launcher.sh start -p test -d

# ✅ CORRECT - Avec mémoire personnalisée
./angel-launcher.sh start -p test -m 1g
```

### Autres commandes

```bash
# Voir le statut
./angel-launcher.sh status

# Voir les logs
./angel-launcher.sh logs

# Arrêter
./angel-launcher.sh stop

# Redémarrer en mode test
./angel-launcher.sh restart -p test
```

### Windows

```batch
# ✅ CORRECT - Windows
angel-launcher.bat start -p test
angel-launcher.bat status
angel-launcher.bat stop
```

## 📋 Vérification

Pour vérifier que le mode test est bien activé :

```bash
# 1. Démarrer en mode test
./angel-launcher.sh start -p test

# 2. Vérifier l'API de test
curl http://localhost:8080/api/test/health

# 3. Accéder au dashboard
open http://localhost:8080/test-dashboard

# 4. Vérifier les logs
./angel-launcher.sh logs | grep -i "test"
```

Vous devriez voir dans les logs :
```
[INFO] Mode test détecté via angel.profile=test
[INFO] Configuration de test chargée avec succès
[INFO] Service de mode test initialisé avec succès
[INFO] Mode test activé
```

## 🎯 Avantages de cette approche

1. **Compatible** avec votre infrastructure existante
2. **Flexible** : 4 méthodes d'activation
3. **Priorité** : angel.profile=test (votre méthode) est vérifiée en premier
4. **Robuste** : Fallback si une méthode échoue
5. **Transparent** : Aucun changement requis dans vos scripts

## 📝 Mise à jour de la Pull Request

La Pull Request a été mise à jour avec :
- ✅ Détection compatible avec `-Dangel.profile=test`
- ✅ Documentation corrigée
- ✅ Exemples mis à jour avec `angel-launcher.sh`
- ✅ Support multi-méthodes d'activation

**Votre commande fonctionnera parfaitement** :
```bash
./angel-launcher.sh start -p test
```

🎉 **Prêt à tester !**
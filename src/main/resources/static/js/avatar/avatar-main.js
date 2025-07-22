/**
 * Application principale Angel Avatar
 * Gère l'orchestration de tous les composants avatar
 */
class AngelAvatarApp {
    constructor() {
        this.avatarRenderer = null;
        this.websocketManager = null;
        this.isInitialized = false;
        this.isMuted = false;
        
        // Configuration initiale
        this.config = {
            gender: 'female',
            age: 30,
            style: 'casual',
            voice: 'female_french_warm'
        };
        
        console.log('🎭 Création de AngelAvatarApp...');
    }
    
    async init() {
        console.log('🚀 Initialisation de Angel Avatar App...');
        
        try {
            // Vérifier que les dépendances sont chargées
            if (!window.AvatarSystem) {
                throw new Error('AvatarSystem non disponible');
            }
            
            // Initialiser le système d'avatar
            this.avatarRenderer = window.AvatarSystem.init('avatar-viewport');
            
            // Configurer les gestionnaires d'événements
            this.setupEventHandlers();
            
            // Charger l'avatar par défaut
            await this.loadDefaultAvatar();
            
            // Initialiser WebSocket si disponible
            if (window.AvatarWebSocketManager) {
                this.websocketManager = new AvatarWebSocketManager(this.avatarRenderer);
            }
            
            // Masquer le spinner de chargement
            this.hideLoadingSpinner();
            
            // Afficher l'avatar
            this.showAvatar();
            
            this.isInitialized = true;
            this.updateStatus('Prêt');
            
            console.log('✅ Angel Avatar App initialisé avec succès');
            
        } catch (error) {
            console.error('❌ Erreur lors de l\'initialisation:', error);
            this.handleInitError(error);
        }
    }
    
    handleInitError(error) {
        // Afficher l'erreur dans l'interface
        const errorDiv = document.createElement('div');
        errorDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #f44336;
            color: white;
            padding: 15px;
            border-radius: 5px;
            z-index: 9999;
            max-width: 300px;
        `;
        errorDiv.textContent = `Erreur Avatar: ${error.message}`;
        document.body.appendChild(errorDiv);
        
        // Masquer après 10 secondes
        setTimeout(() => {
            if (errorDiv.parentNode) {
                errorDiv.parentNode.removeChild(errorDiv);
            }
        }, 10000);
    }
    
    // ... (reste des méthodes existantes dans le HTML)
    // Copier toutes les méthodes depuis le script dans avatar.html
    
    async loadDefaultAvatar() {
        const modelPath = this.getModelPath(this.config.gender, this.config.age, this.config.style);
        
        if (this.avatarRenderer && this.avatarRenderer.loadAvatar) {
            const success = await this.avatarRenderer.loadAvatar(modelPath);
            
            if (!success) {
                console.warn('⚠️ Chargement du modèle par défaut échoué, utilisation du fallback');
                // Charger un modèle de fallback
                await this.avatarRenderer.loadAvatar('/models/avatars/default.glb');
            }
        } else {
            console.warn('⚠️ avatarRenderer.loadAvatar non disponible');
        }
    }
    
    getModelPath(gender, age, style) {
        const ageGroup = this.getAgeGroup(age);
        return `/models/avatars/${gender}_${ageGroup}_${style}.glb`;
    }
    
    getAgeGroup(age) {
        if (age < 30) return 'young';
        if (age < 45) return 'adult';
        if (age < 60) return 'mature';
        return 'senior';
    }
    
    setupEventHandlers() {
        console.log('⚙️ Configuration des gestionnaires d\'événements...');
        
        // Vérifier que les éléments existent avant d'ajouter les listeners
        const elements = {
            muteBtn: document.getElementById('mute-btn'),
            settingsBtn: document.getElementById('settings-btn'),
            cancelSettings: document.getElementById('cancel-settings'),
            settingsOverlay: document.getElementById('settings-overlay'),
            applySettings: document.getElementById('apply-settings'),
            genderSelect: document.getElementById('gender-select')
        };
        
        // Bouton muet
        if (elements.muteBtn) {
            elements.muteBtn.addEventListener('click', () => {
                this.toggleMute();
            });
        }
        
        // Bouton paramètres
        if (elements.settingsBtn) {
            elements.settingsBtn.addEventListener('click', () => {
                this.showSettings();
            });
        }
        
        // Fermeture des paramètres
        if (elements.cancelSettings) {
            elements.cancelSettings.addEventListener('click', () => {
                this.hideSettings();
            });
        }
        
        if (elements.settingsOverlay) {
            elements.settingsOverlay.addEventListener('click', () => {
                this.hideSettings();
            });
        }
        
        // Application des paramètres
        if (elements.applySettings) {
            elements.applySettings.addEventListener('click', () => {
                this.applySettings();
            });
        }
        
        // Mise à jour de la voix selon le genre
        if (elements.genderSelect) {
            elements.genderSelect.addEventListener('change', (e) => {
                this.updateVoiceOptions(e.target.value);
            });
        }
        
        // Gestion du redimensionnement
        window.addEventListener('resize', () => {
            if (this.avatarRenderer && this.avatarRenderer.resize) {
                this.avatarRenderer.resize();
            }
        });
        
        // Gestion des raccourcis clavier
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideSettings();
            }
        });
        
        console.log('✅ Gestionnaires d\'événements configurés');
    }
    
    // ... (copier toutes les autres méthodes depuis le HTML)
    
    updateStatus(status) {
        const statusElement = document.getElementById('avatar-status');
        if (statusElement) {
            statusElement.textContent = status;
        }
    }
    
    showLoadingSpinner() {
        const spinner = document.getElementById('loading-spinner');
        if (spinner) {
            spinner.style.display = 'block';
        }
    }
    
    hideLoadingSpinner() {
        const spinner = document.getElementById('loading-spinner');
        if (spinner) {
            spinner.style.display = 'none';
        }
    }
    
    showAvatar() {
        const container = document.getElementById('avatar-container');
        if (container) {
            container.classList.add('visible');
        }
        
        if (this.avatarRenderer && this.avatarRenderer.setVisible) {
            this.avatarRenderer.setVisible(true);
        }
    }
    
    // ... (continuer avec toutes les méthodes)
}

// Export pour utilisation globale
window.AngelAvatarApp = AngelAvatarApp;

// Initialisation automatique
document.addEventListener('DOMContentLoaded', () => {
    console.log('📄 DOM prêt, initialisation AngelAvatarApp...');
    
    // Attendre que tous les scripts soient chargés
    setTimeout(() => {
        try {
            window.angelApp = new AngelAvatarApp();
            window.angelApp.init();
        } catch (error) {
            console.error('❌ Erreur création AngelAvatarApp:', error);
        }
    }, 100);
});
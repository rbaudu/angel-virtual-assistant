/**
 * Intégration avec Ready Player Me pour la création et gestion d'avatars 3D
 * Fournit une interface pour créer, personnaliser et charger des avatars
 */

class ReadyPlayerMeIntegration {
    constructor(config) {
        this.config = config;
        this.apiKey = config.get('readyPlayerMe.apiKey');
        this.baseUrl = config.get('readyPlayerMe.baseUrl', 'https://api.readyplayer.me/v1');
        this.quality = config.get('readyPlayerMe.quality', 'high');
        this.format = config.get('readyPlayerMe.format', 'glb');
        
        this.avatarCache = new Map();
        this.isEnabled = config.isEnabled('readyPlayerMe.enabled');
        
        if (this.isEnabled && (!this.apiKey || this.apiKey === 'YOUR_READY_PLAYER_ME_API_KEY_HERE')) {
            console.warn('Ready Player Me activé mais clé API manquante ou invalide');
            this.isEnabled = false;
        }
    }
    
    /**
     * Vérifie si Ready Player Me est disponible
     */
    isAvailable() {
        return this.isEnabled && !!this.apiKey;
    }
    
    /**
     * Crée un nouvel avatar avec les paramètres spécifiés
     */
    async createAvatar(params) {
        if (!this.isAvailable()) {
            throw new Error('Ready Player Me non disponible');
        }
        
        const { gender, age, style, customization } = params;
        
        try {
            console.log('Création d\'un avatar Ready Player Me:', params);
            
            const requestBody = this.buildCreateRequest(gender, age, style, customization);
            
            const response = await fetch(`${this.baseUrl}/avatars`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${this.apiKey}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            if (!response.ok) {
                throw new Error(`Erreur HTTP: ${response.status}`);
            }
            
            const result = await response.json();
            console.log('Avatar créé avec succès:', result);
            
            return result;
            
        } catch (error) {
            console.error('Erreur lors de la création de l\'avatar:', error);
            throw error;
        }
    }
    
    /**
     * Obtient l'URL du modèle 3D d'un avatar
     */
    async getAvatarModelUrl(avatarId) {
        if (!this.isAvailable()) {
            return this.getFallbackModelUrl();
        }
        
        // Vérifier le cache d'abord
        const cacheKey = `${avatarId}_${this.quality}_${this.format}`;
        if (this.avatarCache.has(cacheKey)) {
            return this.avatarCache.get(cacheKey);
        }
        
        try {
            const url = `${this.baseUrl}/avatars/${avatarId}.${this.format}?quality=${this.quality}`;
            
            // Vérifier que l'avatar existe
            const response = await fetch(url, {
                method: 'HEAD',
                headers: {
                    'Authorization': `Bearer ${this.apiKey}`
                }
            });
            
            if (response.ok) {
                this.avatarCache.set(cacheKey, url);
                return url;
            } else {
                console.warn(`Avatar Ready Player Me non trouvé: ${avatarId}`);
                return this.getFallbackModelUrl();
            }
            
        } catch (error) {
            console.error('Erreur lors de l\'obtention de l\'URL du modèle:', error);
            return this.getFallbackModelUrl();
        }
    }
    
    /**
     * Liste les avatars disponibles pour l'utilisateur
     */
    async listUserAvatars() {
        if (!this.isAvailable()) {
            return [];
        }
        
        try {
            const response = await fetch(`${this.baseUrl}/avatars`, {
                headers: {
                    'Authorization': `Bearer ${this.apiKey}`
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                return data.data || [];
            } else {
                console.warn('Erreur lors de la récupération des avatars:', response.status);
                return [];
            }
            
        } catch (error) {
            console.error('Exception lors de la récupération des avatars:', error);
            return [];
        }
    }
    
    /**
     * Supprime un avatar
     */
    async deleteAvatar(avatarId) {
        if (!this.isAvailable()) {
            return false;
        }
        
        try {
            const response = await fetch(`${this.baseUrl}/avatars/${avatarId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${this.apiKey}`
                }
            });
            
            const success = response.ok;
            if (success) {
                console.log('Avatar supprimé:', avatarId);
                // Nettoyer le cache
                for (const [key] of this.avatarCache) {
                    if (key.startsWith(avatarId)) {
                        this.avatarCache.delete(key);
                    }
                }
            }
            
            return success;
            
        } catch (error) {
            console.error('Erreur lors de la suppression de l\'avatar:', error);
            return false;
        }
    }
    
    /**
     * Met à jour les paramètres d'un avatar existant
     */
    async updateAvatar(avatarId, params) {
        if (!this.isAvailable()) {
            throw new Error('Ready Player Me non disponible');
        }
        
        try {
            const requestBody = this.buildUpdateRequest(params);
            
            const response = await fetch(`${this.baseUrl}/avatars/${avatarId}`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${this.apiKey}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            if (!response.ok) {
                throw new Error(`Erreur HTTP: ${response.status}`);
            }
            
            const result = await response.json();
            
            // Invalider le cache pour cet avatar
            for (const [key] of this.avatarCache) {
                if (key.startsWith(avatarId)) {
                    this.avatarCache.delete(key);
                }
            }
            
            return result;
            
        } catch (error) {
            console.error('Erreur lors de la mise à jour de l\'avatar:', error);
            throw error;
        }
    }
    
    /**
     * Construit la requête de création d'avatar
     */
    buildCreateRequest(gender, age, style, customization = {}) {
        const request = {
            bodyType: gender,
            appearancePreset: this.determineAppearancePreset(gender, age),
            style: style,
            ageGroup: this.determineAgeGroup(age)
        };
        
        // Ajouter les personnalisations spécifiques
        if (customization.hairColor) {
            request.hairColor = customization.hairColor;
        }
        
        if (customization.eyeColor) {
            request.eyeColor = customization.eyeColor;
        }
        
        if (customization.skinTone) {
            request.skinTone = customization.skinTone;
        }
        
        if (customization.outfit) {
            request.outfit = customization.outfit;
        }
        
        return request;
    }
    
    /**
     * Construit la requête de mise à jour d'avatar
     */
    buildUpdateRequest(params) {
        const request = {};
        
        if (params.hairColor) request.hairColor = params.hairColor;
        if (params.eyeColor) request.eyeColor = params.eyeColor;
        if (params.skinTone) request.skinTone = params.skinTone;
        if (params.outfit) request.outfit = params.outfit;
        if (params.accessories) request.accessories = params.accessories;
        
        return request;
    }
    
    /**
     * Détermine le preset d'apparence selon le genre et l'âge
     */
    determineAppearancePreset(gender, age) {
        const presets = {
            female: {
                young: 'young_woman',
                adult: 'adult_woman',
                mature: 'mature_woman'
            },
            male: {
                young: 'young_man',
                adult: 'adult_man', 
                mature: 'mature_man'
            }
        };
        
        const ageGroup = this.determineAgeGroup(age);
        return presets[gender]?.[ageGroup] || 'adult_woman';
    }
    
    /**
     * Détermine le groupe d'âge
     */
    determineAgeGroup(age) {
        if (age < 25) return 'young';
        if (age < 40) return 'adult';
        return 'mature';
    }
    
    /**
     * Obtient l'URL du modèle de fallback
     */
    getFallbackModelUrl() {
        const gender = this.config.get('appearance.gender', 'female');
        const age = this.config.get('appearance.age', 30);
        const basePath = this.config.get('fallbackModels.basePath', '/static/models/avatars/');
        
        const ageGroup = this.determineAgeGroup(age);
        const modelFile = this.config.get(`fallbackModels.${gender}.${ageGroup}`, 'default_avatar.glb');
        
        return basePath + modelFile;
    }
    
    /**
     * Crée un avatar avec un assistant visuel (iframe Ready Player Me)
     */
    openAvatarCreator(options = {}) {
        return new Promise((resolve, reject) => {
            const iframe = document.createElement('iframe');
            iframe.src = 'https://demo.readyplayer.me/avatar';
            iframe.style.width = '100%';
            iframe.style.height = '100%';
            iframe.style.border = 'none';
            
            // Créer un overlay modal
            const overlay = document.createElement('div');
            overlay.style.position = 'fixed';
            overlay.style.top = '0';
            overlay.style.left = '0';
            overlay.style.width = '100%';
            overlay.style.height = '100%';
            overlay.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
            overlay.style.zIndex = '10000';
            overlay.style.display = 'flex';
            overlay.style.justifyContent = 'center';
            overlay.style.alignItems = 'center';
            
            const container = document.createElement('div');
            container.style.width = '80%';
            container.style.height = '80%';
            container.style.backgroundColor = 'white';
            container.style.borderRadius = '10px';
            container.style.position = 'relative';
            
            // Bouton de fermeture
            const closeButton = document.createElement('button');
            closeButton.textContent = '✕';
            closeButton.style.position = 'absolute';
            closeButton.style.top = '10px';
            closeButton.style.right = '10px';
            closeButton.style.zIndex = '10001';
            closeButton.style.border = 'none';
            closeButton.style.background = '#ff4444';
            closeButton.style.color = 'white';
            closeButton.style.borderRadius = '50%';
            closeButton.style.width = '30px';
            closeButton.style.height = '30px';
            closeButton.style.cursor = 'pointer';
            
            closeButton.onclick = () => {
                document.body.removeChild(overlay);
                reject(new Error('Création d\'avatar annulée'));
            };
            
            container.appendChild(iframe);
            container.appendChild(closeButton);
            overlay.appendChild(container);
            document.body.appendChild(overlay);
            
            // Écouter les messages de l'iframe
            window.addEventListener('message', function handleMessage(event) {
                if (event.data?.source === 'readyplayerme') {
                    if (event.data.eventName === 'v1.avatar.exported') {
                        window.removeEventListener('message', handleMessage);
                        document.body.removeChild(overlay);
                        resolve({
                            avatarId: event.data.avatarId,
                            modelUrl: event.data.url
                        });
                    }
                }
            });
        });
    }
    
    /**
     * Nettoie le cache
     */
    clearCache() {
        this.avatarCache.clear();
        console.log('Cache Ready Player Me nettoyé');
    }
    
    /**
     * Obtient les statistiques du cache
     */
    getCacheStats() {
        return {
            size: this.avatarCache.size,
            keys: Array.from(this.avatarCache.keys())
        };
    }
}

// Export global
window.ReadyPlayerMeIntegration = ReadyPlayerMeIntegration;
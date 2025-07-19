/**
 * Contrôleur JavaScript pour le dashboard de test mode
 * Gère l'interaction avec l'API et la mise à jour de l'interface
 */

class TestModeController {
    constructor() {
        this.baseUrl = window.location.origin;
        this.apiUrl = `${this.baseUrl}/api/test`;
        this.isSimulationRunning = false;
        this.currentScenario = null;
        this.updateInterval = null;
        this.logContainer = null;
        
        this.init();
    }
    
    /**
     * Initialise le contrôleur
     */
    init() {
        console.log('Initialisation du TestModeController');
        
        this.setupEventListeners();
        this.initializeUI();
        this.startPeriodicUpdates();
        this.loadAvailableScenarios();
        this.updateStatus();
    }
    
    /**
     * Configure les écouteurs d'événements
     */
    setupEventListeners() {
        // Boutons de simulation
        const startBtn = document.getElementById('startBtn');
        const stopBtn = document.getElementById('stopBtn');
        
        if (startBtn) {
            startBtn.addEventListener('click', () => this.startSimulation());
        }
        
        if (stopBtn) {
            stopBtn.addEventListener('click', () => this.stopSimulation());
        }
        
        // Contrôle manuel d'activité
        const setActivityBtn = document.getElementById('setActivityBtn');
        if (setActivityBtn) {
            setActivityBtn.addEventListener('click', () => this.setManualActivity());
        }
        
        // Slider de confiance
        const confidenceSlider = document.getElementById('confidence-slider');
        if (confidenceSlider) {
            confidenceSlider.addEventListener('input', (e) => {
                document.getElementById('confidence-value').textContent = 
                    `${Math.round(e.target.value * 100)}%`;
            });
        }
        
        // Boutons de scénarios
        const loadScenarioBtn = document.getElementById('loadScenarioBtn');
        const stopScenarioBtn = document.getElementById('stopScenarioBtn');
        
        if (loadScenarioBtn) {
            loadScenarioBtn.addEventListener('click', () => this.loadSelectedScenario());
        }
        
        if (stopScenarioBtn) {
            stopScenarioBtn.addEventListener('click', () => this.stopCurrentScenario());
        }
        
        // Bouton de rafraîchissement
        const refreshBtn = document.getElementById('refreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => this.refreshData());
        }
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    initializeUI() {
        // Initialiser l'affichage de la confiance
        const confidenceSlider = document.getElementById('confidence-slider');
        if (confidenceSlider) {
            const value = Math.round(confidenceSlider.value * 100);
            document.getElementById('confidence-value').textContent = `${value}%`;
        }
        
        // Initialiser le conteneur de logs
        this.logContainer = document.getElementById('logs-container');
        if (this.logContainer) {
            this.addLogEntry('INFO', 'Interface de test initialisée');
        }
    }
    
    /**
     * Démarre les mises à jour périodiques
     */
    startPeriodicUpdates() {
        this.updateInterval = setInterval(() => {
            this.updateCurrentActivity();
            this.updateSimulationStatus();
        }, 3000); // Mise à jour toutes les 3 secondes
    }
    
    /**
     * Arrête les mises à jour périodiques
     */
    stopPeriodicUpdates() {
        if (this.updateInterval) {
            clearInterval(this.updateInterval);
            this.updateInterval = null;
        }
    }
    
    /**
     * Démarre la simulation
     */
    async startSimulation() {
        try {
            this.showLoading('startBtn');
            
            const response = await fetch(`${this.apiUrl}/simulation/start`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            
            const data = await response.json();
            
            if (data.success) {
                this.isSimulationRunning = true;
                this.showAlert('success', 'Simulation démarrée avec succès');
                this.addLogEntry('INFO', 'Simulation démarrée');
                this.updateSimulationControls();
            } else {
                this.showAlert('danger', 'Erreur lors du démarrage: ' + data.message);
                this.addLogEntry('ERROR', 'Erreur démarrage: ' + data.message);
            }
        } catch (error) {
            console.error('Erreur startSimulation:', error);
            this.showAlert('danger', 'Erreur de communication avec le serveur');
            this.addLogEntry('ERROR', 'Erreur communication: ' + error.message);
        } finally {
            this.hideLoading('startBtn');
        }
    }
    
    /**
     * Arrête la simulation
     */
    async stopSimulation() {
        try {
            this.showLoading('stopBtn');
            
            const response = await fetch(`${this.apiUrl}/simulation/stop`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            
            const data = await response.json();
            
            if (data.success) {
                this.isSimulationRunning = false;
                this.showAlert('warning', 'Simulation arrêtée');
                this.addLogEntry('INFO', 'Simulation arrêtée');
                this.updateSimulationControls();
            } else {
                this.showAlert('danger', 'Erreur lors de l\'arrêt: ' + data.message);
                this.addLogEntry('ERROR', 'Erreur arrêt: ' + data.message);
            }
        } catch (error) {
            console.error('Erreur stopSimulation:', error);
            this.showAlert('danger', 'Erreur de communication avec le serveur');
            this.addLogEntry('ERROR', 'Erreur communication: ' + error.message);
        } finally {
            this.hideLoading('stopBtn');
        }
    }
    
    /**
     * Définit manuellement une activité
     */
    async setManualActivity() {
        try {
            const activitySelector = document.getElementById('activity-selector');
            const confidenceSlider = document.getElementById('confidence-slider');
            
            if (!activitySelector || !confidenceSlider) {
                this.showAlert('danger', 'Éléments de contrôle introuvables');
                return;
            }
            
            const activity = activitySelector.value;
            const confidence = parseFloat(confidenceSlider.value);
            
            this.showLoading('setActivityBtn');
            
            const response = await fetch(`${this.apiUrl}/activity/set`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ activity, confidence })
            });
            
            const data = await response.json();
            
            if (data.success) {
                this.showAlert('success', `Activité définie: ${activity}`);
                this.addLogEntry('INFO', `Activité manuelle: ${activity} (${Math.round(confidence * 100)}%)`);
                this.updateCurrentActivity();
            } else {
                this.showAlert('danger', 'Erreur lors de la définition: ' + data.message);
                this.addLogEntry('ERROR', 'Erreur définition activité: ' + data.message);
            }
        } catch (error) {
            console.error('Erreur setManualActivity:', error);
            this.showAlert('danger', 'Erreur de communication avec le serveur');
            this.addLogEntry('ERROR', 'Erreur communication: ' + error.message);
        } finally {
            this.hideLoading('setActivityBtn');
        }
    }
    
    /**
     * Charge le scénario sélectionné
     */
    async loadSelectedScenario() {
        try {
            const scenarioSelector = document.getElementById('scenario-selector');
            if (!scenarioSelector) {
                this.showAlert('danger', 'Sélecteur de scénario introuvable');
                return;
            }
            
            const scenarioId = scenarioSelector.value;
            if (!scenarioId) {
                this.showAlert('warning', 'Veuillez sélectionner un scénario');
                return;
            }
            
            this.showLoading('loadScenarioBtn');
            
            const response = await fetch(`${this.apiUrl}/scenario/load/${scenarioId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            
            const data = await response.json();
            
            if (data.success) {
                this.currentScenario = data.scenarioInfo;
                this.showAlert('success', `Scénario chargé: ${scenarioId}`);
                this.addLogEntry('INFO', `Scénario chargé: ${scenarioId}`);
                this.updateCurrentScenarioDisplay();
            } else {
                this.showAlert('danger', 'Erreur lors du chargement: ' + data.message);
                this.addLogEntry('ERROR', 'Erreur chargement scénario: ' + data.message);
            }
        } catch (error) {
            console.error('Erreur loadSelectedScenario:', error);
            this.showAlert('danger', 'Erreur de communication avec le serveur');
            this.addLogEntry('ERROR', 'Erreur communication: ' + error.message);
        } finally {
            this.hideLoading('loadScenarioBtn');
        }
    }
    
    /**
     * Arrête le scénario en cours
     */
    async stopCurrentScenario() {
        try {
            this.showLoading('stopScenarioBtn');
            
            const response = await fetch(`${this.apiUrl}/scenario/stop`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            
            const data = await response.json();
            
            if (data.success) {
                this.currentScenario = null;
                this.showAlert('warning', 'Scénario arrêté');
                this.addLogEntry('INFO', 'Scénario arrêté');
                this.updateCurrentScenarioDisplay();
            } else {
                this.showAlert('danger', 'Erreur lors de l\'arrêt: ' + data.message);
                this.addLogEntry('ERROR', 'Erreur arrêt scénario: ' + data.message);
            }
        } catch (error) {
            console.error('Erreur stopCurrentScenario:', error);
            this.showAlert('danger', 'Erreur de communication avec le serveur');
            this.addLogEntry('ERROR', 'Erreur communication: ' + error.message);
        } finally {
            this.hideLoading('stopScenarioBtn');
        }
    }
    
    /**
     * Met à jour l'activité courante
     */
    async updateCurrentActivity() {
        try {
            const response = await fetch(`${this.apiUrl}/activity/current`);
            const activity = await response.json();
            
            if (activity) {
                const nameElement = document.getElementById('activity-name');
                const confidenceElement = document.getElementById('activity-confidence');
                
                if (nameElement) {
                    nameElement.textContent = activity.activity || 'Inconnue';
                }
                
                if (confidenceElement) {
                    const confidence = Math.round((activity.confidence || 0) * 100);
                    confidenceElement.innerHTML = `<span class="percentage">${confidence}%</span> de confiance`;
                }
                
                // Mettre à jour l'horodatage
                const timestampElement = document.getElementById('activity-timestamp');
                if (timestampElement && activity.timestamp) {
                    const date = new Date(activity.timestamp);
                    timestampElement.textContent = `Mis à jour: ${date.toLocaleTimeString()}`;
                }
            }
        } catch (error) {
            console.error('Erreur updateCurrentActivity:', error);
        }
    }
    
    /**
     * Met à jour le statut de la simulation
     */
    async updateSimulationStatus() {
        try {
            const response = await fetch(`${this.apiUrl}/simulation/status`);
            const status = await response.json();
            
            this.isSimulationRunning = status.running;
            this.updateSimulationControls();
            
            // Mettre à jour l'indicateur de statut
            const statusIndicator = document.getElementById('status-indicator');
            if (statusIndicator) {
                statusIndicator.className = 'status-indicator ' + 
                    (status.running ? 'status-running' : 'status-stopped');
            }
            
            // Mettre à jour le texte de statut
            const statusText = document.getElementById('status-text');
            if (statusText) {
                statusText.textContent = status.running ? 'En cours' : 'Arrêtée';
            }
            
            // Mettre à jour le mode
            const modeText = document.getElementById('mode-text');
            if (modeText) {
                modeText.textContent = status.mode || 'Inconnu';
            }
            
            // Mettre à jour le temps jusqu'au prochain changement
            const nextChangeText = document.getElementById('next-change-text');
            if (nextChangeText && status.nextChangeIn) {
                const seconds = Math.round(status.nextChangeIn / 1000);
                nextChangeText.textContent = `${seconds}s`;
            }
            
        } catch (error) {
            console.error('Erreur updateSimulationStatus:', error);
        }
    }
    
    /**
     * Charge les scénarios disponibles
     */
    async loadAvailableScenarios() {
        try {
            const response = await fetch(`${this.apiUrl}/scenarios`);
            const scenarios = await response.json();
            
            const scenarioSelector = document.getElementById('scenario-selector');
            if (scenarioSelector && scenarios) {
                scenarioSelector.innerHTML = '<option value="">Sélectionner un scénario...</option>';
                
                scenarios.forEach(scenario => {
                    const option = document.createElement('option');
                    option.value = scenario.id;
                    option.textContent = `${scenario.name} - ${scenario.description}`;
                    scenarioSelector.appendChild(option);
                });
            }
            
            this.addLogEntry('INFO', `${scenarios.length} scénarios chargés`);
        } catch (error) {
            console.error('Erreur loadAvailableScenarios:', error);
            this.addLogEntry('ERROR', 'Erreur chargement scénarios: ' + error.message);
        }
    }
    
    /**
     * Met à jour l'affichage du scénario courant
     */
    updateCurrentScenarioDisplay() {
        const currentScenarioElement = document.getElementById('current-scenario');
        if (currentScenarioElement) {
            if (this.currentScenario) {
                currentScenarioElement.innerHTML = `
                    <strong>${this.currentScenario.name}</strong><br>
                    <small>${this.currentScenario.description}</small>
                `;
            } else {
                currentScenarioElement.textContent = 'Aucun scénario actif';
            }
        }
    }
    
    /**
     * Met à jour les contrôles de simulation
     */
    updateSimulationControls() {
        const startBtn = document.getElementById('startBtn');
        const stopBtn = document.getElementById('stopBtn');
        
        if (startBtn) {
            startBtn.disabled = this.isSimulationRunning;
        }
        
        if (stopBtn) {
            stopBtn.disabled = !this.isSimulationRunning;
        }
    }
    
    /**
     * Met à jour le statut général
     */
    async updateStatus() {
        try {
            const response = await fetch(`${this.apiUrl}/health`);
            const health = await response.json();
            
            if (health.status === 'OK') {
                this.showAlert('success', 'Connexion au serveur de test établie');
                this.addLogEntry('INFO', 'Connexion serveur OK');
            }
        } catch (error) {
            console.error('Erreur updateStatus:', error);
            this.showAlert('danger', 'Impossible de se connecter au serveur de test');
            this.addLogEntry('ERROR', 'Erreur connexion serveur: ' + error.message);
        }
    }
    
    /**
     * Rafraîchit toutes les données
     */
    async refreshData() {
        this.addLogEntry('INFO', 'Rafraîchissement des données...');
        
        await Promise.all([
            this.updateCurrentActivity(),
            this.updateSimulationStatus(),
            this.loadAvailableScenarios()
        ]);
        
        this.showAlert('info', 'Données rafraîchies');
    }
    
    /**
     * Affiche une alerte
     */
    showAlert(type, message, duration = 5000) {
        const alertContainer = document.getElementById('alert-container');
        if (!alertContainer) return;
        
        const alert = document.createElement('div');
        alert.className = `alert alert-${type} fade-in`;
        alert.textContent = message;
        
        alertContainer.appendChild(alert);
        
        // Supprimer l'alerte après la durée spécifiée
        setTimeout(() => {
            if (alert.parentNode) {
                alert.parentNode.removeChild(alert);
            }
        }, duration);
    }
    
    /**
     * Ajoute une entrée dans les logs
     */
    addLogEntry(level, message) {
        if (!this.logContainer) return;
        
        const timestamp = new Date().toLocaleTimeString();
        const logEntry = document.createElement('div');
        logEntry.className = 'log-entry';
        logEntry.innerHTML = `
            <span class="log-timestamp">${timestamp}</span>
            <span class="log-level-${level.toLowerCase()}">[${level}]</span>
            ${message}
        `;
        
        this.logContainer.appendChild(logEntry);
        
        // Faire défiler vers le bas
        this.logContainer.scrollTop = this.logContainer.scrollHeight;
        
        // Limiter le nombre d'entrées de log
        while (this.logContainer.children.length > 100) {
            this.logContainer.removeChild(this.logContainer.firstChild);
        }
    }
    
    /**
     * Affiche un indicateur de chargement
     */
    showLoading(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.disabled = true;
            element.innerHTML = '<div class="loading-spinner"></div>';
        }
    }
    
    /**
     * Cache l'indicateur de chargement
     */
    hideLoading(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.disabled = false;
            // Restaurer le texte original selon l'ID
            switch (elementId) {
                case 'startBtn':
                    element.textContent = 'Démarrer Simulation';
                    break;
                case 'stopBtn':
                    element.textContent = 'Arrêter Simulation';
                    break;
                case 'setActivityBtn':
                    element.textContent = 'Définir Activité';
                    break;
                case 'loadScenarioBtn':
                    element.textContent = 'Charger Scénario';
                    break;
                case 'stopScenarioBtn':
                    element.textContent = 'Arrêter Scénario';
                    break;
                default:
                    element.textContent = 'Action';
            }
        }
    }
    
    /**
     * Nettoie les ressources lors de la fermeture
     */
    cleanup() {
        this.stopPeriodicUpdates();
        this.addLogEntry('INFO', 'Interface de test fermée');
    }
}

// Initialiser le contrôleur quand le DOM est prêt
document.addEventListener('DOMContentLoaded', function() {
    window.testController = new TestModeController();
});

// Nettoyer lors de la fermeture de la page
window.addEventListener('beforeunload', function() {
    if (window.testController) {
        window.testController.cleanup();
    }
});
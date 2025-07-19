package com.angel.ui;

import com.angel.test.TestModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur pour l'interface web du dashboard de test.
 * Sert la page HTML du dashboard de test.
 */
@Controller
@RequestMapping("/test-dashboard")
@ConditionalOnProperty(
    prefix = "angel.test", 
    name = "dashboard.enabled", 
    havingValue = "true",
    matchIfMissing = true
)
public class TestDashboardController {
    
    @Autowired
    private TestModeService testModeService;
    
    /**
     * Affiche le dashboard de test.
     */
    @GetMapping
    public String showTestDashboard(Model model) {
        // Vérifier si le mode test est disponible
        boolean testModeAvailable = testModeService.isTestModeEnabled();
        
        model.addAttribute("testModeEnabled", testModeAvailable);
        
        if (testModeAvailable) {
            TestModeService.TestModeServiceInfo serviceInfo = testModeService.getServiceInfo();
            model.addAttribute("serviceInfo", serviceInfo);
            model.addAttribute("testConfig", testModeService.getTestConfig());
        }
        
        return "test-dashboard";
    }
    
    /**
     * Page d'aide pour le mode test.
     */
    @GetMapping("/help")
    public String showTestHelp(Model model) {
        model.addAttribute("testModeEnabled", testModeService.isTestModeEnabled());
        return "test-help";
    }
}
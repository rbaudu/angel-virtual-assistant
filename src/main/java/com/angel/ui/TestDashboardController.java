package com.angel.ui;

import com.angel.test.TestModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;

/**
 * Contrôleur pour l'interface web du dashboard de test.
 * Activé uniquement si le dashboard test est configuré comme activé.
 */
@Controller
@RequestMapping("/test-dashboard")
@ConditionalOnProperty(
    prefix = "angel.test.dashboard", 
    name = "enabled", 
    havingValue = "true",
    matchIfMissing = true
)
public class TestDashboardController {
    
    @Autowired(required = false)
    private TestModeService testModeService;
    
    @Value("${angel.test.enabled:false}")
    private boolean testModeEnabled;
    
    @Value("${server.servlet.context-path:/}")
    private String contextPath;
    
    /**
     * Affiche le dashboard de test.
     */
    @GetMapping
    public String showTestDashboard(Model model) {
        // Vérifier si le service test est disponible
        boolean testServiceAvailable = testModeService != null && testModeService.isTestModeEnabled();
        
        model.addAttribute("testModeEnabled", testModeEnabled);
        model.addAttribute("testServiceAvailable", testServiceAvailable);
        model.addAttribute("contextPath", contextPath);
        
        if (testServiceAvailable) {
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
        model.addAttribute("testModeEnabled", testModeEnabled);
        model.addAttribute("contextPath", contextPath);
        return "test-help";
    }
}
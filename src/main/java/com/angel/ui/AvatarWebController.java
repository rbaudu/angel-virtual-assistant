package com.angel.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;

/**
 * Contrôleur Web pour afficher l'interface avatar.
 * Compatible avec le context-path configuré (/angel en mode normal, / en mode test).
 */
@Controller
public class AvatarWebController {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * Affiche la page de l'avatar à la racine du context-path.
     * En mode normal : /angel/
     * En mode test : /angel
     */
    @GetMapping({"/", "/angel"})
    public String showAvatar(Model model) {
        model.addAttribute("avatarEnabled", true);
        model.addAttribute("pageTitle", "Angel Virtual Assistant");
        model.addAttribute("contextPath", contextPath);
        return "avatar";
    }
    
    /**
     * Route alternative pour l'avatar (au cas où).
     */
    @GetMapping("/avatar")
    public String showAvatarPage(Model model) {
        return showAvatar(model);
    }
}
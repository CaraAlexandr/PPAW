package com.ppaw.passwordvault.controller.mvc;

import com.ppaw.passwordvault.dto.ServicePlanDTO;
import com.ppaw.passwordvault.dto.UserAdminViewModel;
import com.ppaw.passwordvault.service.ServicePlanService;
import com.ppaw.passwordvault.service.UserAdminService;
import com.ppaw.passwordvault.validation.ValidationGroups;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * UserAdminController - MVC Controller pentru Admin Panel
 * Echivalent Controller pentru administrarea utilizatorilor
 * Folosește @Controller pentru a returna view-uri Thymeleaf
 */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final ServicePlanService servicePlanService;

    /**
     * GET /users - Index (Listare utilizatori)
     */
    @GetMapping
    public String index(Model model) {
        List<UserAdminViewModel> users = userAdminService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", "Administrare Utilizatori");
        
        // Statistici pentru dashboard
        long activeUsers = users.stream().filter(u -> u.getIsActive() != null && u.getIsActive()).count();
        long inactiveUsers = users.size() - activeUsers;
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        
        return "users/index";
    }

    /**
     * GET /users/{id} - Details
     */
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        UserAdminViewModel user = userAdminService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Detalii Utilizator - " + user.getUsername());
        return "users/details";
    }

    /**
     * GET /users/create - Afișare formular creare
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new UserAdminViewModel());
        List<ServicePlanDTO> plans = servicePlanService.getAllServicePlans();
        model.addAttribute("servicePlans", plans);
        model.addAttribute("pageTitle", "Adaugă Utilizator Nou");
        return "users/create";
    }

    /**
     * POST /users/create - Salvare utilizator nou
     */
    @PostMapping("/create")
    public String create(@Validated(ValidationGroups.Create.class) @ModelAttribute("user") UserAdminViewModel userViewModel,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<ServicePlanDTO> plans = servicePlanService.getAllServicePlans();
            model.addAttribute("servicePlans", plans);
            model.addAttribute("pageTitle", "Adaugă Utilizator Nou");
            return "users/create";
        }
        
        try {
            UserAdminViewModel saved = userAdminService.createUser(userViewModel);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Utilizatorul '" + saved.getUsername() + "' a fost creat cu succes!");
            return "redirect:/users/" + saved.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<ServicePlanDTO> plans = servicePlanService.getAllServicePlans();
            model.addAttribute("servicePlans", plans);
            model.addAttribute("pageTitle", "Adaugă Utilizator Nou");
            return "users/create";
        }
    }

    /**
     * GET /users/{id}/edit - Afișare formular editare
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        UserAdminViewModel user = userAdminService.getUserById(id);
        List<ServicePlanDTO> plans = servicePlanService.getAllServicePlans();
        model.addAttribute("user", user);
        model.addAttribute("servicePlans", plans);
        model.addAttribute("pageTitle", "Editare Utilizator - " + user.getUsername());
        return "users/edit";
    }

    /**
     * POST /users/{id}/edit - Salvare modificări
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @Validated(ValidationGroups.Update.class) @ModelAttribute("user") UserAdminViewModel userViewModel,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<ServicePlanDTO> plans = servicePlanService.getAllServicePlans();
            model.addAttribute("servicePlans", plans);
            UserAdminViewModel existingUser = userAdminService.getUserById(id);
            model.addAttribute("pageTitle", "Editare Utilizator - " + existingUser.getUsername());
            return "users/edit";
        }
        
        try {
            UserAdminViewModel updated = userAdminService.updateUser(id, userViewModel);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Utilizatorul '" + updated.getUsername() + "' a fost actualizat cu succes!");
            return "redirect:/users/" + updated.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<ServicePlanDTO> plans = servicePlanService.getAllServicePlans();
            model.addAttribute("servicePlans", plans);
            UserAdminViewModel existingUser = userAdminService.getUserById(id);
            model.addAttribute("pageTitle", "Editare Utilizator - " + existingUser.getUsername());
            return "users/edit";
        }
    }

    /**
     * POST /users/{id}/delete - Ștergere utilizator
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            UserAdminViewModel user = userAdminService.getUserById(id);
            String username = user.getUsername();
            userAdminService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Utilizatorul '" + username + "' a fost șters cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la ștergerea utilizatorului: " + e.getMessage());
        }
        return "redirect:/users";
    }
}


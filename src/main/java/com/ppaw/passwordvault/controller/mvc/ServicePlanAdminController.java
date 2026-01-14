package com.ppaw.passwordvault.controller.mvc;

import com.ppaw.passwordvault.dto.ServicePlanAdminViewModel;
import com.ppaw.passwordvault.service.ServicePlanAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ServicePlanAdminController - MVC Controller pentru Admin Panel
 * Folosit pentru administrarea planurilor de servicii
 */
@Controller
@RequestMapping("/service-plans")
@RequiredArgsConstructor
public class ServicePlanAdminController {

    private static final Logger logger = LoggerFactory.getLogger(ServicePlanAdminController.class);

    private final ServicePlanAdminService servicePlanAdminService;

    /**
     * GET /service-plans - Index (Listare planuri)
     */
    @GetMapping
    public String index(Model model) {
        logger.info("GET /service-plans - Request to list all service plans");
        try {
            List<ServicePlanAdminViewModel> plans = servicePlanAdminService.getAllServicePlans();
            model.addAttribute("plans", plans);
            model.addAttribute("pageTitle", "Administrare Planuri de Servicii");
            
            // Statistici
            long activePlans = plans.stream().filter(p -> p.getIsActive() != null && p.getIsActive()).count();
            long inactivePlans = plans.size() - activePlans;
            model.addAttribute("totalPlans", plans.size());
            model.addAttribute("activePlans", activePlans);
            model.addAttribute("inactivePlans", inactivePlans);
            
            logger.info("GET /service-plans - Successfully displayed {} service plans", plans.size());
            return "service-plans/index";
        } catch (Exception e) {
            logger.error("GET /service-plans - Error listing service plans", e);
            throw e;
        }
    }

    /**
     * GET /service-plans/{id} - Details
     */
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        logger.info("GET /service-plans/{} - Request to view service plan details", id);
        try {
            ServicePlanAdminViewModel plan = servicePlanAdminService.getServicePlanById(id);
            model.addAttribute("plan", plan);
            model.addAttribute("pageTitle", "Detalii Plan - " + plan.getName());
            logger.info("GET /service-plans/{} - Successfully displayed service plan: {}", id, plan.getName());
            return "service-plans/details";
        } catch (Exception e) {
            logger.error("GET /service-plans/{} - Error viewing service plan details", id, e);
            throw e;
        }
    }

    /**
     * GET /service-plans/create - Afișare formular creare
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        logger.debug("GET /service-plans/create - Displaying create form");
        model.addAttribute("plan", new ServicePlanAdminViewModel());
        model.addAttribute("pageTitle", "Adaugă Plan Nou");
        return "service-plans/create";
    }

    /**
     * POST /service-plans/create - Salvare plan nou
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("plan") ServicePlanAdminViewModel planViewModel,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        logger.info("POST /service-plans/create - Request to create service plan: {}", planViewModel.getName());
        if (bindingResult.hasErrors()) {
            logger.warn("POST /service-plans/create - Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Adaugă Plan Nou");
            return "service-plans/create";
        }
        
        try {
            ServicePlanAdminViewModel saved = servicePlanAdminService.createServicePlan(planViewModel);
            logger.info("POST /service-plans/create - Successfully created service plan: {} (id: {})", 
                    saved.getName(), saved.getId());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Planul '" + saved.getName() + "' a fost creat cu succes!");
            return "redirect:/service-plans/" + saved.getId();
        } catch (Exception e) {
            logger.error("POST /service-plans/create - Error creating service plan: {}", planViewModel.getName(), e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Adaugă Plan Nou");
            return "service-plans/create";
        }
    }

    /**
     * GET /service-plans/{id}/edit - Afișare formular editare
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        logger.debug("GET /service-plans/{}/edit - Displaying edit form", id);
        try {
            ServicePlanAdminViewModel plan = servicePlanAdminService.getServicePlanById(id);
            model.addAttribute("plan", plan);
            model.addAttribute("pageTitle", "Editare Plan - " + plan.getName());
            return "service-plans/edit";
        } catch (Exception e) {
            logger.error("GET /service-plans/{}/edit - Error displaying edit form", id, e);
            throw e;
        }
    }

    /**
     * POST /service-plans/{id}/edit - Salvare modificări
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("plan") ServicePlanAdminViewModel planViewModel,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        logger.info("POST /service-plans/{}/edit - Request to update service plan", id);
        if (bindingResult.hasErrors()) {
            logger.warn("POST /service-plans/{}/edit - Validation errors: {}", id, bindingResult.getAllErrors());
            ServicePlanAdminViewModel existingPlan = servicePlanAdminService.getServicePlanById(id);
            model.addAttribute("pageTitle", "Editare Plan - " + existingPlan.getName());
            return "service-plans/edit";
        }
        
        try {
            ServicePlanAdminViewModel updated = servicePlanAdminService.updateServicePlan(id, planViewModel);
            logger.info("POST /service-plans/{}/edit - Successfully updated service plan: {}", id, updated.getName());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Planul '" + updated.getName() + "' a fost actualizat cu succes!");
            return "redirect:/service-plans/" + updated.getId();
        } catch (Exception e) {
            logger.error("POST /service-plans/{}/edit - Error updating service plan", id, e);
            model.addAttribute("errorMessage", e.getMessage());
            ServicePlanAdminViewModel existingPlan = servicePlanAdminService.getServicePlanById(id);
            model.addAttribute("pageTitle", "Editare Plan - " + existingPlan.getName());
            return "service-plans/edit";
        }
    }

    /**
     * POST /service-plans/{id}/delete - Ștergere plan
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("POST /service-plans/{}/delete - Request to delete service plan", id);
        try {
            ServicePlanAdminViewModel plan = servicePlanAdminService.getServicePlanById(id);
            String planName = plan.getName();
            servicePlanAdminService.deleteServicePlan(id);
            logger.info("POST /service-plans/{}/delete - Successfully deleted service plan: {}", id, planName);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Planul '" + planName + "' a fost șters cu succes!");
        } catch (Exception e) {
            logger.error("POST /service-plans/{}/delete - Error deleting service plan", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Eroare la ștergerea planului: " + e.getMessage());
        }
        return "redirect:/service-plans";
    }
}


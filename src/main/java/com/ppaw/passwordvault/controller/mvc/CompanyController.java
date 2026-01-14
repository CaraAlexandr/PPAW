package com.ppaw.passwordvault.controller.mvc;

import com.ppaw.passwordvault.dto.CompanyViewModel;
import com.ppaw.passwordvault.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * CompanyController - MVC Controller
 * Echivalent Controller din ASP.NET MVC
 * Folosește @Controller (nu @RestController) pentru a returna view-uri Thymeleaf
 */
@Controller
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // Lista de țări pentru dropdown
    private static final List<String> COUNTRIES = Arrays.asList(
        "România", "Moldova", "Statele Unite", "Marea Britanie", 
        "Franța", "Germania", "Italia", "Spania", "Altele"
    );

    /**
     * GET /companies - Index
     * Echivalent Index() din ASP.NET MVC
     */
    @GetMapping
    public String index(Model model) {
        List<CompanyViewModel> companies = companyService.getAll();
        model.addAttribute("companies", companies);
        return "companies/index";
    }

    /**
     * GET /companies/{id} - Details
     * Echivalent Details(int id) din ASP.NET MVC
     */
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        CompanyViewModel company = companyService.getById(id);
        model.addAttribute("company", company);
        return "companies/details";
    }

    /**
     * GET /companies/create - Afișare formular creare
     * Echivalent Create() GET din ASP.NET MVC
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("company", new CompanyViewModel());
        model.addAttribute("countries", COUNTRIES);
        return "companies/create";
    }

    /**
     * POST /companies/create - Salvare companie nouă
     * Echivalent Create(CompanyViewModel model) POST din ASP.NET MVC
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("company") CompanyViewModel companyViewModel,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("countries", COUNTRIES);
            return "companies/create";
        }
        
        CompanyViewModel saved = companyService.create(companyViewModel);
        redirectAttributes.addFlashAttribute("successMessage", "Compania a fost creată cu succes!");
        return "redirect:/companies/" + saved.getId();
    }

    /**
     * GET /companies/{id}/edit - Afișare formular editare
     * Echivalent Edit(int id) GET din ASP.NET MVC
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CompanyViewModel company = companyService.getById(id);
        model.addAttribute("company", company);
        model.addAttribute("countries", COUNTRIES);
        return "companies/edit";
    }

    /**
     * POST /companies/{id}/edit - Salvare modificări
     * Echivalent Edit(int id, CompanyViewModel model) POST din ASP.NET MVC
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("company") CompanyViewModel companyViewModel,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("countries", COUNTRIES);
            return "companies/edit";
        }
        
        CompanyViewModel updated = companyService.update(id, companyViewModel);
        redirectAttributes.addFlashAttribute("successMessage", "Compania a fost actualizată cu succes!");
        return "redirect:/companies/" + updated.getId();
    }

    /**
     * POST /companies/{id}/delete - Ștergere companie
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        companyService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Compania a fost ștearsă cu succes!");
        return "redirect:/companies";
    }
}




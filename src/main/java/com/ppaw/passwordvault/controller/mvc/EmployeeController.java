package com.ppaw.passwordvault.controller.mvc;

import com.ppaw.passwordvault.dto.CompanyViewModel;
import com.ppaw.passwordvault.dto.EmployeeViewModel;
import com.ppaw.passwordvault.service.CompanyService;
import com.ppaw.passwordvault.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * EmployeeController - MVC Controller pentru Admin Panel
 * Echivalent Controller pentru administrarea angajaților
 * Folosește @Controller pentru a returna view-uri Thymeleaf
 */
@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final CompanyService companyService;

    /**
     * GET /employees - Index (Listare angajați)
     */
    @GetMapping
    public String index(Model model) {
        List<EmployeeViewModel> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        model.addAttribute("pageTitle", "Administrare Angajați");
        
        // Statistici pentru dashboard
        long activeEmployees = employees.stream()
                .filter(e -> e.getIsActive() != null && e.getIsActive())
                .count();
        long inactiveEmployees = employees.size() - activeEmployees;
        model.addAttribute("totalEmployees", employees.size());
        model.addAttribute("activeEmployees", activeEmployees);
        model.addAttribute("inactiveEmployees", inactiveEmployees);
        
        return "employees/index";
    }

    /**
     * GET /employees/create - Afișare formular creare
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("employee", new EmployeeViewModel());
        List<CompanyViewModel> companies = companyService.getAll();
        model.addAttribute("companies", companies);
        model.addAttribute("pageTitle", "Adaugă Angajat Nou");
        return "employees/create";
    }

    /**
     * POST /employees/create - Salvare angajat nou
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("employee") EmployeeViewModel employeeViewModel,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<CompanyViewModel> companies = companyService.getAll();
            model.addAttribute("companies", companies);
            model.addAttribute("pageTitle", "Adaugă Angajat Nou");
            return "employees/create";
        }
        
        try {
            EmployeeViewModel saved = employeeService.createEmployee(employeeViewModel);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Angajatul '" + saved.getFirstName() + " " + saved.getLastName() + "' a fost creat cu succes!");
            return "redirect:/employees";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<CompanyViewModel> companies = companyService.getAll();
            model.addAttribute("companies", companies);
            model.addAttribute("pageTitle", "Adaugă Angajat Nou");
            return "employees/create";
        }
    }
}


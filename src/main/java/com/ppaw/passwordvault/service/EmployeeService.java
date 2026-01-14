package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.EmployeeViewModel;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.exception.ValidationException;
import com.ppaw.passwordvault.model.Company;
import com.ppaw.passwordvault.model.Employee;
import com.ppaw.passwordvault.repository.CompanyRepository;
import com.ppaw.passwordvault.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * EmployeeService - Service pentru administrarea angajaților
 * Folosit în admin panel MVC
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    public List<EmployeeViewModel> getAllEmployees() {
        logger.info("Getting all employees");
        try {
            List<EmployeeViewModel> employees = employeeRepository.findAllWithCompany().stream()
                    .map(this::toViewModel)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} employees", employees.size());
            return employees;
        } catch (Exception e) {
            logger.error("Error on getting employees from database", e);
            throw e;
        }
    }

    public EmployeeViewModel getEmployeeById(Long id) {
        logger.debug("Getting employee by id: {}", id);
        try {
            Employee employee = employeeRepository.findByIdWithCompany(id)
                    .orElseThrow(() -> {
                        logger.warn("Employee not found with id: {}", id);
                        return new ResourceNotFoundException("Employee", id);
                    });
            logger.info("Successfully retrieved employee: {} {} (id: {})", 
                    employee.getFirstName(), employee.getLastName(), id);
            return toViewModel(employee);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting employee by id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public EmployeeViewModel createEmployee(EmployeeViewModel viewModel) {
        logger.info("Creating new employee with email: {} and company id: {}", 
                viewModel.getEmail(), viewModel.getCompanyId());
        try {
            // Validare email unic
            if (employeeRepository.findByEmail(viewModel.getEmail()).isPresent()) {
                logger.warn("Email already exists: {}", viewModel.getEmail());
                throw new ValidationException("Email deja există");
            }

            // Găsește Company după id
            Company company = companyRepository.findById(viewModel.getCompanyId())
                    .orElseThrow(() -> {
                        logger.error("Company not found with id: {}", viewModel.getCompanyId());
                        return new ResourceNotFoundException("Company", viewModel.getCompanyId());
                    });

            Employee employee = new Employee();
            employee.setFirstName(viewModel.getFirstName());
            employee.setLastName(viewModel.getLastName());
            employee.setEmail(viewModel.getEmail());
            employee.setPhone(viewModel.getPhone());
            employee.setPosition(viewModel.getPosition());
            employee.setIsActive(viewModel.getIsActive() != null ? viewModel.getIsActive() : true);
            employee.setCompany(company);

            Employee saved = employeeRepository.save(employee);
            logger.info("Employee created successfully: {} {} (id: {})", 
                    saved.getFirstName(), saved.getLastName(), saved.getId());
            return toViewModel(saved);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on creating employee with email: {}", viewModel.getEmail(), e);
            throw e;
        }
    }

    @Transactional
    public EmployeeViewModel updateEmployee(Long id, EmployeeViewModel viewModel) {
        logger.info("Updating employee with id: {}", id);
        try {
            Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Employee not found for update with id: {}", id);
                        return new ResourceNotFoundException("Employee", id);
                    });

            // Validare email unic (dacă s-a schimbat)
            if (viewModel.getEmail() != null && !viewModel.getEmail().equals(employee.getEmail())) {
                logger.debug("Updating email for employee id: {} to: {}", id, viewModel.getEmail());
                employeeRepository.findByEmail(viewModel.getEmail())
                        .ifPresent(existing -> {
                            if (!existing.getId().equals(id)) {
                                logger.warn("Email already exists: {}", viewModel.getEmail());
                                throw new ValidationException("Email deja există");
                            }
                        });
                employee.setEmail(viewModel.getEmail());
            }

            if (viewModel.getFirstName() != null) {
                employee.setFirstName(viewModel.getFirstName());
            }
            if (viewModel.getLastName() != null) {
                employee.setLastName(viewModel.getLastName());
            }
            if (viewModel.getPhone() != null) {
                employee.setPhone(viewModel.getPhone());
            }
            if (viewModel.getPosition() != null) {
                employee.setPosition(viewModel.getPosition());
            }
            if (viewModel.getIsActive() != null) {
                employee.setIsActive(viewModel.getIsActive());
            }

            // Actualizare companie dacă s-a schimbat
            if (viewModel.getCompanyId() != null && !viewModel.getCompanyId().equals(employee.getCompany().getId())) {
                logger.debug("Updating company for employee id: {} to: {}", id, viewModel.getCompanyId());
                Company company = companyRepository.findById(viewModel.getCompanyId())
                        .orElseThrow(() -> {
                            logger.error("Company not found with id: {}", viewModel.getCompanyId());
                            return new ResourceNotFoundException("Company", viewModel.getCompanyId());
                        });
                employee.setCompany(company);
            }

            Employee updated = employeeRepository.save(employee);
            logger.info("Employee updated successfully: {} {} (id: {})", 
                    updated.getFirstName(), updated.getLastName(), id);
            return toViewModel(updated);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on updating employee with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteEmployee(Long id) {
        logger.info("Deleting employee with id: {}", id);
        try {
            Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Employee not found for deletion with id: {}", id);
                        return new ResourceNotFoundException("Employee", id);
                    });
            String employeeName = employee.getFirstName() + " " + employee.getLastName();
            employeeRepository.delete(employee);
            logger.info("Employee deleted successfully: {} (id: {})", employeeName, id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on deleting employee with id: {}", id, e);
            throw e;
        }
    }

    public EmployeeViewModel toViewModel(Employee employee) {
        EmployeeViewModel viewModel = new EmployeeViewModel();
        viewModel.setId(employee.getId());
        viewModel.setFirstName(employee.getFirstName());
        viewModel.setLastName(employee.getLastName());
        viewModel.setEmail(employee.getEmail());
        viewModel.setPhone(employee.getPhone());
        viewModel.setPosition(employee.getPosition());
        viewModel.setIsActive(employee.getIsActive());
        viewModel.setCompanyId(employee.getCompany() != null ? employee.getCompany().getId() : null);
        
        // Câmpuri din Company pentru afișare
        if (employee.getCompany() != null) {
            viewModel.setCompanyName(employee.getCompany().getName());
            viewModel.setCompanyCountry(employee.getCompany().getCountry());
        }
        
        return viewModel;
    }
}


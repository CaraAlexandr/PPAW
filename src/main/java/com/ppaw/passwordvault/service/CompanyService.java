package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.CompanyViewModel;
import com.ppaw.passwordvault.dto.EmployeeViewModel;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.model.Company;
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
 * CompanyService
 * Recomandat pentru separarea logicii de business
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public List<CompanyViewModel> getAll() {
        logger.info("Getting all companies");
        try {
            List<CompanyViewModel> companies = companyRepository.findAll().stream()
                    .map(this::toViewModel)
                    .collect(Collectors.toList());
            
            // Adaugă numărul de angajați pentru fiecare companie
            companies.forEach(company -> {
                long employeeCount = employeeRepository.countByCompanyId(company.getId());
                company.setEmployeeCount(Long.valueOf(employeeCount));
            });
            
            logger.info("Successfully retrieved {} companies", companies.size());
            return companies;
        } catch (Exception e) {
            logger.error("Error on getting companies from database", e);
            throw e;
        }
    }

    public CompanyViewModel getById(Long id) {
        logger.debug("Getting company by id: {}", id);
        try {
            Company company = companyRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Company not found with id: {}", id);
                        return new ResourceNotFoundException("Company", id);
                    });
            CompanyViewModel viewModel = toViewModel(company);
            
            // Încarcă angajații companiei
            List<EmployeeViewModel> employees = employeeRepository.findByCompanyIdWithCompany(id).stream()
                    .map(employee -> employeeService.toViewModel(employee))
                    .collect(Collectors.toList());
            
            viewModel.setEmployees(employees);
            viewModel.setEmployeeCount(Long.valueOf(employees.size()));
            
            logger.info("Successfully retrieved company: {} (id: {}) with {} employees", 
                    company.getName(), id, employees.size());
            return viewModel;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting company by id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public CompanyViewModel create(CompanyViewModel viewModel) {
        logger.info("Creating new company with name: {}", viewModel.getName());
        try {
            Company company = toEntity(viewModel);
            Company saved = companyRepository.save(company);
            logger.info("Company created successfully: {} (id: {})", saved.getName(), saved.getId());
            return toViewModel(saved);
        } catch (Exception e) {
            logger.error("Error on creating company with name: {}", viewModel.getName(), e);
            throw e;
        }
    }

    @Transactional
    public CompanyViewModel update(Long id, CompanyViewModel viewModel) {
        logger.info("Updating company with id: {}", id);
        try {
            Company company = companyRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Company not found for update with id: {}", id);
                        return new ResourceNotFoundException("Company", id);
                    });
            
            company.setName(viewModel.getName());
            company.setDescription(viewModel.getDescription());
            company.setCountry(viewModel.getCountry());
            company.setIsActive(viewModel.getIsActive() != null ? viewModel.getIsActive() : true);
            company.setEmail(viewModel.getEmail());
            company.setPhone(viewModel.getPhone());
            
            Company updated = companyRepository.save(company);
            logger.info("Company updated successfully: {} (id: {})", updated.getName(), id);
            return toViewModel(updated);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on updating company with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Deleting company with id: {}", id);
        try {
            Company company = companyRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Company not found for deletion with id: {}", id);
                        return new ResourceNotFoundException("Company", id);
                    });
            String companyName = company.getName();
            companyRepository.delete(company);
            logger.info("Company deleted successfully: {} (id: {})", companyName, id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on deleting company with id: {}", id, e);
            throw e;
        }
    }

    private CompanyViewModel toViewModel(Company company) {
        CompanyViewModel viewModel = new CompanyViewModel();
        viewModel.setId(company.getId());
        viewModel.setName(company.getName());
        viewModel.setDescription(company.getDescription());
        viewModel.setCountry(company.getCountry());
        viewModel.setIsActive(company.getIsActive());
        viewModel.setEmail(company.getEmail());
        viewModel.setPhone(company.getPhone());
        return viewModel;
    }

    private Company toEntity(CompanyViewModel viewModel) {
        Company company = new Company();
        company.setName(viewModel.getName());
        company.setDescription(viewModel.getDescription());
        company.setCountry(viewModel.getCountry());
        company.setIsActive(viewModel.getIsActive() != null ? viewModel.getIsActive() : true);
        company.setEmail(viewModel.getEmail());
        company.setPhone(viewModel.getPhone());
        return company;
    }
}



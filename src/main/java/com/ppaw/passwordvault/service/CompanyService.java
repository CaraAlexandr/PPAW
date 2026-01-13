package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.CompanyViewModel;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.model.Company;
import com.ppaw.passwordvault.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
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

    private final CompanyRepository companyRepository;

    public List<CompanyViewModel> getAll() {
        return companyRepository.findAll().stream()
                .map(this::toViewModel)
                .collect(Collectors.toList());
    }

    public CompanyViewModel getById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        return toViewModel(company);
    }

    @Transactional
    public CompanyViewModel create(CompanyViewModel viewModel) {
        Company company = toEntity(viewModel);
        Company saved = companyRepository.save(company);
        return toViewModel(saved);
    }

    @Transactional
    public CompanyViewModel update(Long id, CompanyViewModel viewModel) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        
        company.setName(viewModel.getName());
        company.setDescription(viewModel.getDescription());
        company.setCountry(viewModel.getCountry());
        company.setIsActive(viewModel.getIsActive() != null ? viewModel.getIsActive() : true);
        company.setEmail(viewModel.getEmail());
        company.setPhone(viewModel.getPhone());
        
        Company updated = companyRepository.save(company);
        return toViewModel(updated);
    }

    @Transactional
    public void delete(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        companyRepository.delete(company);
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



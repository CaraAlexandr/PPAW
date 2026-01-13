package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CompanyRepository
 * Echivalent NivelAccesDate / Accesor din ASP.NET MVC
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    List<Company> findByIsActive(Boolean isActive);
    
    List<Company> findByCountry(String country);
}



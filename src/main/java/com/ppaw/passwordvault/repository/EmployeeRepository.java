package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * EmployeeRepository - Repository pentru entitatea Employee
 * Echivalent NivelAccesDate / Accesor din ASP.NET MVC
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    List<Employee> findByIsActive(Boolean isActive);
    
    List<Employee> findByCompanyId(Long companyId);
    
    Optional<Employee> findByEmail(String email);
    
    // Query cu JOIN pentru listare eficientă cu date din Company
    @Query("SELECT e FROM Employee e JOIN FETCH e.company WHERE e.id = :id")
    Optional<Employee> findByIdWithCompany(@Param("id") Long id);
    
    // Listare cu JOIN pentru evitarea N+1 problem
    @Query("SELECT DISTINCT e FROM Employee e JOIN FETCH e.company ORDER BY e.lastName, e.firstName")
    List<Employee> findAllWithCompany();
    
    // Query pentru filtrare după companie
    @Query("SELECT e FROM Employee e JOIN FETCH e.company WHERE e.company.id = :companyId")
    List<Employee> findByCompanyIdWithCompany(@Param("companyId") Long companyId);
    
    // Numără angajații unei companii
    long countByCompanyId(Long companyId);
}


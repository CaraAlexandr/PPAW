package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.PlanLimitsDTO;
import com.ppaw.passwordvault.dto.ServicePlanDTO;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.model.PlanLimits;
import com.ppaw.passwordvault.model.ServicePlan;
import com.ppaw.passwordvault.repository.PlanLimitsRepository;
import com.ppaw.passwordvault.repository.ServicePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicePlanService {

    private final ServicePlanRepository servicePlanRepository;
    private final PlanLimitsRepository planLimitsRepository;

    public List<ServicePlanDTO> getAllServicePlans() {
        return servicePlanRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ServicePlanDTO> getActiveServicePlans() {
        return servicePlanRepository.findByIsActive(true).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ServicePlanDTO getServicePlanById(Long id) {
        ServicePlan plan = servicePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServicePlan", id));
        return toDTO(plan);
    }

    public ServicePlanDTO getServicePlanWithLimits(Long id) {
        ServicePlan plan = servicePlanRepository.findByIdWithLimits(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServicePlan", id));
        return toDTO(plan);
    }

    private ServicePlanDTO toDTO(ServicePlan plan) {
        ServicePlanDTO dto = ServicePlanDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .isActive(plan.getIsActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();

        // Eager loading pentru limits
        PlanLimits limits = planLimitsRepository.findByServicePlanId(plan.getId()).orElse(null);
        if (limits != null) {
            dto.setLimits(toLimitsDTO(limits));
        }

        return dto;
    }

    private PlanLimitsDTO toLimitsDTO(PlanLimits limits) {
        return PlanLimitsDTO.builder()
                .id(limits.getId())
                .planId(limits.getServicePlan().getId())
                .maxVaultItems(limits.getMaxVaultItems())
                .maxPasswordLength(limits.getMaxPasswordLength())
                .canExport(limits.getCanExport())
                .canImport(limits.getCanImport())
                .canShare(limits.getCanShare())
                .maxHistoryVersions(limits.getMaxHistoryVersions())
                .canAttachments(limits.getCanAttachments())
                .maxDevices(limits.getMaxDevices())
                .excludeAmbiguous(limits.getExcludeAmbiguous())
                .createdAt(limits.getCreatedAt())
                .updatedAt(limits.getUpdatedAt())
                .build();
    }
}


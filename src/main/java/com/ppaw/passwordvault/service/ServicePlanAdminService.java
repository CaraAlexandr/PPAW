package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.cache.CacheService;
import com.ppaw.passwordvault.dto.PlanLimitsDTO;
import com.ppaw.passwordvault.dto.ServicePlanAdminViewModel;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.exception.ValidationException;
import com.ppaw.passwordvault.model.PlanLimits;
import com.ppaw.passwordvault.model.ServicePlan;
import com.ppaw.passwordvault.repository.PlanLimitsRepository;
import com.ppaw.passwordvault.repository.ServicePlanRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ServicePlanAdminService - Service pentru administrarea planurilor de servicii
 * Folosit în admin panel MVC
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicePlanAdminService {

    private static final Logger logger = LoggerFactory.getLogger(ServicePlanAdminService.class);

    private final ServicePlanRepository servicePlanRepository;
    private final PlanLimitsRepository planLimitsRepository;
    private final CacheService cacheService;

    public List<ServicePlanAdminViewModel> getAllServicePlans() {
        logger.info("Getting all service plans for admin panel");
        try {
            List<ServicePlanAdminViewModel> plans = servicePlanRepository.findAll().stream()
                    .map(this::toAdminViewModel)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} service plans for admin panel", plans.size());
            return plans;
        } catch (Exception e) {
            logger.error("Error on getting service plans from database for admin panel", e);
            throw e;
        }
    }

    public ServicePlanAdminViewModel getServicePlanById(Long id) {
        logger.debug("Getting service plan by id for admin panel: {}", id);
        try {
            ServicePlan plan = servicePlanRepository.findByIdWithLimits(id)
                    .orElseThrow(() -> {
                        logger.warn("Service plan not found for admin panel with id: {}", id);
                        return new ResourceNotFoundException("ServicePlan", id);
                    });
            logger.info("Successfully retrieved service plan for admin panel: {} (id: {})", plan.getName(), id);
            return toAdminViewModel(plan);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting service plan by id for admin panel: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public ServicePlanAdminViewModel createServicePlan(ServicePlanAdminViewModel viewModel) {
        logger.info("Creating new service plan via admin panel - Name: {}, Price: {} {}, Active: {}", 
                viewModel.getName(), viewModel.getPrice(), viewModel.getCurrency(), viewModel.getIsActive());
        try {
            // Validare - check if name already exists
            if (servicePlanRepository.findByName(viewModel.getName()).isPresent()) {
                logger.warn("Service plan creation failed: Name already exists - {}", viewModel.getName());
                throw new ValidationException("Numele planului deja există");
            }

            // Create ServicePlan
            ServicePlan plan = new ServicePlan();
            plan.setName(viewModel.getName());
            plan.setPrice(viewModel.getPrice());
            plan.setCurrency(viewModel.getCurrency() != null ? viewModel.getCurrency() : "USD");
            plan.setIsActive(viewModel.getIsActive() != null ? viewModel.getIsActive() : true);

            logger.debug("Saving service plan entity to database");
            ServicePlan saved = servicePlanRepository.save(plan);
            logger.debug("Service plan saved with id: {}", saved.getId());

            // Create PlanLimits
            PlanLimits limits = new PlanLimits();
            limits.setServicePlan(saved);
            limits.setMaxVaultItems(viewModel.getMaxVaultItems() != null ? viewModel.getMaxVaultItems() : 10);
            limits.setMaxPasswordLength(viewModel.getMaxPasswordLength() != null ? viewModel.getMaxPasswordLength() : 50);
            limits.setCanExport(viewModel.getCanExport() != null ? viewModel.getCanExport() : false);
            limits.setCanImport(viewModel.getCanImport() != null ? viewModel.getCanImport() : false);
            limits.setCanShare(viewModel.getCanShare() != null ? viewModel.getCanShare() : false);
            limits.setMaxHistoryVersions(viewModel.getMaxHistoryVersions() != null ? viewModel.getMaxHistoryVersions() : 0);
            limits.setCanAttachments(viewModel.getCanAttachments() != null ? viewModel.getCanAttachments() : false);
            limits.setMaxDevices(viewModel.getMaxDevices() != null ? viewModel.getMaxDevices() : 1);
            limits.setExcludeAmbiguous(viewModel.getExcludeAmbiguous() != null ? viewModel.getExcludeAmbiguous() : false);

            logger.debug("Saving plan limits for service plan id: {} - MaxItems: {}, MaxPasswordLength: {}, Features: Export={}, Import={}, Share={}", 
                    saved.getId(), limits.getMaxVaultItems(), limits.getMaxPasswordLength(), 
                    limits.getCanExport(), limits.getCanImport(), limits.getCanShare());
            planLimitsRepository.save(limits);
            logger.debug("Plan limits saved successfully");

            // Clear cache - remove all service plan related entries
            logger.debug("Clearing service plan cache after creation");
            cacheService.removeByPattern("service_plan");
            cacheService.removeByPattern("service_plans");
            // Explicitly clear list caches
            cacheService.remove("service_plans_all");
            cacheService.remove("service_plans_active");
            logger.debug("Cache cleared successfully");

            logger.info("Service plan created successfully via admin panel: {} (id: {}) with limits: MaxItems={}, MaxPasswordLength={}", 
                    saved.getName(), saved.getId(), limits.getMaxVaultItems(), limits.getMaxPasswordLength());
            return toAdminViewModel(saved);
        } catch (ValidationException e) {
            logger.warn("Validation error while creating service plan: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error on creating service plan via admin panel with name: {}", viewModel.getName(), e);
            throw e;
        }
    }

    @Transactional
    public ServicePlanAdminViewModel updateServicePlan(Long id, ServicePlanAdminViewModel viewModel) {
        logger.info("Updating service plan via admin panel - ID: {}, Name: {}, Price: {} {}", 
                id, viewModel.getName(), viewModel.getPrice(), viewModel.getCurrency());
        try {
            ServicePlan plan = servicePlanRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Service plan update failed: Plan not found with id: {}", id);
                        return new ResourceNotFoundException("ServicePlan", id);
                    });

            logger.debug("Found existing service plan: {} (id: {})", plan.getName(), id);

            // Validare nume (dacă s-a schimbat)
            if (viewModel.getName() != null && !viewModel.getName().equals(plan.getName())) {
                logger.debug("Updating name for service plan id: {} from '{}' to '{}'", id, plan.getName(), viewModel.getName());
                servicePlanRepository.findByName(viewModel.getName())
                        .ifPresent(existing -> {
                            if (!existing.getId().equals(id)) {
                                logger.warn("Service plan update failed: Name already exists - {}", viewModel.getName());
                                throw new ValidationException("Numele planului deja există");
                            }
                        });
                plan.setName(viewModel.getName());
            }

            boolean planChanged = false;
            if (viewModel.getPrice() != null && !viewModel.getPrice().equals(plan.getPrice())) {
                logger.debug("Updating price for service plan id: {} from {} to {}", id, plan.getPrice(), viewModel.getPrice());
                plan.setPrice(viewModel.getPrice());
                planChanged = true;
            }
            if (viewModel.getCurrency() != null && !viewModel.getCurrency().equals(plan.getCurrency())) {
                logger.debug("Updating currency for service plan id: {} from {} to {}", id, plan.getCurrency(), viewModel.getCurrency());
                plan.setCurrency(viewModel.getCurrency());
                planChanged = true;
            }
            if (viewModel.getIsActive() != null && !viewModel.getIsActive().equals(plan.getIsActive())) {
                logger.debug("Updating isActive for service plan id: {} from {} to {}", id, plan.getIsActive(), viewModel.getIsActive());
                plan.setIsActive(viewModel.getIsActive());
                planChanged = true;
            }

            ServicePlan updatedPlan = plan;
            if (planChanged) {
                logger.debug("Saving updated service plan entity");
                updatedPlan = servicePlanRepository.save(plan);
            }

            // Update PlanLimits - use final reference for lambda
            final ServicePlan finalPlan = updatedPlan;
            PlanLimits limits = planLimitsRepository.findByServicePlanId(id)
                    .orElseGet(() -> {
                        logger.debug("No existing limits found for plan id: {}, creating new limits", id);
                        PlanLimits newLimits = new PlanLimits();
                        newLimits.setServicePlan(finalPlan);
                        return newLimits;
                    });

            boolean limitsChanged = false;
            if (viewModel.getMaxVaultItems() != null && !viewModel.getMaxVaultItems().equals(limits.getMaxVaultItems())) {
                logger.debug("Updating maxVaultItems for plan id: {} from {} to {}", id, limits.getMaxVaultItems(), viewModel.getMaxVaultItems());
                limits.setMaxVaultItems(viewModel.getMaxVaultItems());
                limitsChanged = true;
            }
            if (viewModel.getMaxPasswordLength() != null && !viewModel.getMaxPasswordLength().equals(limits.getMaxPasswordLength())) {
                logger.debug("Updating maxPasswordLength for plan id: {} from {} to {}", id, limits.getMaxPasswordLength(), viewModel.getMaxPasswordLength());
                limits.setMaxPasswordLength(viewModel.getMaxPasswordLength());
                limitsChanged = true;
            }
            if (viewModel.getCanExport() != null && !viewModel.getCanExport().equals(limits.getCanExport())) {
                logger.debug("Updating canExport for plan id: {} from {} to {}", id, limits.getCanExport(), viewModel.getCanExport());
                limits.setCanExport(viewModel.getCanExport());
                limitsChanged = true;
            }
            if (viewModel.getCanImport() != null && !viewModel.getCanImport().equals(limits.getCanImport())) {
                logger.debug("Updating canImport for plan id: {} from {} to {}", id, limits.getCanImport(), viewModel.getCanImport());
                limits.setCanImport(viewModel.getCanImport());
                limitsChanged = true;
            }
            if (viewModel.getCanShare() != null && !viewModel.getCanShare().equals(limits.getCanShare())) {
                logger.debug("Updating canShare for plan id: {} from {} to {}", id, limits.getCanShare(), viewModel.getCanShare());
                limits.setCanShare(viewModel.getCanShare());
                limitsChanged = true;
            }
            if (viewModel.getMaxHistoryVersions() != null && !viewModel.getMaxHistoryVersions().equals(limits.getMaxHistoryVersions())) {
                logger.debug("Updating maxHistoryVersions for plan id: {} from {} to {}", id, limits.getMaxHistoryVersions(), viewModel.getMaxHistoryVersions());
                limits.setMaxHistoryVersions(viewModel.getMaxHistoryVersions());
                limitsChanged = true;
            }
            if (viewModel.getCanAttachments() != null && !viewModel.getCanAttachments().equals(limits.getCanAttachments())) {
                logger.debug("Updating canAttachments for plan id: {} from {} to {}", id, limits.getCanAttachments(), viewModel.getCanAttachments());
                limits.setCanAttachments(viewModel.getCanAttachments());
                limitsChanged = true;
            }
            if (viewModel.getMaxDevices() != null && !viewModel.getMaxDevices().equals(limits.getMaxDevices())) {
                logger.debug("Updating maxDevices for plan id: {} from {} to {}", id, limits.getMaxDevices(), viewModel.getMaxDevices());
                limits.setMaxDevices(viewModel.getMaxDevices());
                limitsChanged = true;
            }
            if (viewModel.getExcludeAmbiguous() != null && !viewModel.getExcludeAmbiguous().equals(limits.getExcludeAmbiguous())) {
                logger.debug("Updating excludeAmbiguous for plan id: {} from {} to {}", id, limits.getExcludeAmbiguous(), viewModel.getExcludeAmbiguous());
                limits.setExcludeAmbiguous(viewModel.getExcludeAmbiguous());
                limitsChanged = true;
            }

            if (limitsChanged) {
                logger.debug("Saving updated plan limits");
                planLimitsRepository.save(limits);
            }

            // Clear cache - remove all service plan related entries
            logger.debug("Clearing service plan cache after update");
            cacheService.removeByPattern("service_plan");
            cacheService.removeByPattern("service_plans");
            // Also clear specific plan cache
            cacheService.remove("service_plan_" + id);
            cacheService.remove("service_plan_with_limits_" + id);
            cacheService.remove("service_plans_all");
            cacheService.remove("service_plans_active");
            logger.debug("Cache cleared successfully");

            logger.info("Service plan updated successfully via admin panel: {} (id: {}) - Plan changed: {}, Limits changed: {}", 
                    updatedPlan.getName(), id, planChanged, limitsChanged);
            return toAdminViewModel(updatedPlan);
        } catch (ValidationException | ResourceNotFoundException e) {
            if (e instanceof ValidationException) {
                logger.warn("Validation error while updating service plan id {}: {}", id, e.getMessage());
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error on updating service plan via admin panel with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteServicePlan(Long id) {
        logger.info("Deleting service plan via admin panel - ID: {}", id);
        try {
            ServicePlan plan = servicePlanRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Service plan deletion failed: Plan not found with id: {}", id);
                        return new ResourceNotFoundException("ServicePlan", id);
                    });
            
            String planName = plan.getName();
            logger.debug("Found service plan to delete: {} (id: {})", planName, id);
            
            // Delete limits first
            planLimitsRepository.findByServicePlanId(id).ifPresent(limits -> {
                logger.debug("Deleting plan limits for plan id: {}", id);
                planLimitsRepository.delete(limits);
                logger.debug("Plan limits deleted successfully");
            });
            
            // Delete plan
            logger.debug("Deleting service plan entity");
            servicePlanRepository.delete(plan);
            logger.debug("Service plan entity deleted successfully");
            
            // Clear cache - remove all service plan related entries
            logger.debug("Clearing service plan cache after deletion");
            cacheService.removeByPattern("service_plan");
            cacheService.removeByPattern("service_plans");
            // Also clear specific plan cache
            cacheService.remove("service_plan_" + id);
            cacheService.remove("service_plan_with_limits_" + id);
            cacheService.remove("service_plans_all");
            cacheService.remove("service_plans_active");
            logger.debug("Cache cleared successfully");
            
            logger.info("Service plan deleted successfully via admin panel: {} (id: {})", planName, id);
        } catch (ResourceNotFoundException e) {
            logger.warn("Resource not found error while deleting service plan id {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error on deleting service plan via admin panel with id: {}", id, e);
            throw e;
        }
    }

    private ServicePlanAdminViewModel toAdminViewModel(ServicePlan plan) {
        ServicePlanAdminViewModel viewModel = new ServicePlanAdminViewModel();
        viewModel.setId(plan.getId());
        viewModel.setName(plan.getName());
        viewModel.setPrice(plan.getPrice());
        viewModel.setCurrency(plan.getCurrency());
        viewModel.setIsActive(plan.getIsActive());

        // Load limits
        planLimitsRepository.findByServicePlanId(plan.getId()).ifPresent(limits -> {
            viewModel.setLimitsId(limits.getId());
            viewModel.setMaxVaultItems(limits.getMaxVaultItems());
            viewModel.setMaxPasswordLength(limits.getMaxPasswordLength());
            viewModel.setCanExport(limits.getCanExport());
            viewModel.setCanImport(limits.getCanImport());
            viewModel.setCanShare(limits.getCanShare());
            viewModel.setMaxHistoryVersions(limits.getMaxHistoryVersions());
            viewModel.setCanAttachments(limits.getCanAttachments());
            viewModel.setMaxDevices(limits.getMaxDevices());
            viewModel.setExcludeAmbiguous(limits.getExcludeAmbiguous());
        });

        return viewModel;
    }
}


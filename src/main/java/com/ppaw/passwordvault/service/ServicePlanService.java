package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.cache.CacheService;
import com.ppaw.passwordvault.dto.PlanLimitsDTO;
import com.ppaw.passwordvault.dto.ServicePlanDTO;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicePlanService {

    private static final Logger logger = LoggerFactory.getLogger(ServicePlanService.class);
    
    // Cache keys
    private static final String CACHE_KEY_ALL_PLANS = "service_plans_all";
    private static final String CACHE_KEY_ACTIVE_PLANS = "service_plans_active";
    private static final String CACHE_KEY_PLAN_BY_ID = "service_plan_";
    private static final String CACHE_KEY_PLAN_WITH_LIMITS = "service_plan_with_limits_";
    private static final String CACHE_PATTERN_PLANS = "service_plan";

    private final ServicePlanRepository servicePlanRepository;
    private final PlanLimitsRepository planLimitsRepository;
    private final CacheService cacheService;

    @SuppressWarnings("unchecked")
    public List<ServicePlanDTO> getAllServicePlans() {
        logger.info("Getting all service plans");
        try {
            // Try to get from cache first
            Object cached = cacheService.get(CACHE_KEY_ALL_PLANS, Object.class);
            if (cached instanceof List) {
                List<ServicePlanDTO> plans = (List<ServicePlanDTO>) cached;
                logger.info("Retrieved {} service plans from cache", plans.size());
                return plans;
            }
            
            // If not in cache, get from database
            List<ServicePlanDTO> plans = servicePlanRepository.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            
            // Store in cache
            cacheService.set(CACHE_KEY_ALL_PLANS, plans, 60);
            logger.info("Successfully retrieved {} service plans from database and cached", plans.size());
            return plans;
        } catch (Exception e) {
            logger.error("Error on getting service plans from database", e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public List<ServicePlanDTO> getActiveServicePlans() {
        logger.info("Getting all active service plans");
        try {
            // Try to get from cache first
            Object cached = cacheService.get(CACHE_KEY_ACTIVE_PLANS, Object.class);
            if (cached instanceof List) {
                List<ServicePlanDTO> plans = (List<ServicePlanDTO>) cached;
                logger.info("Retrieved {} active service plans from cache", plans.size());
                return plans;
            }
            
            // If not in cache, get from database
            List<ServicePlanDTO> plans = servicePlanRepository.findByIsActive(true).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            
            // Store in cache
            cacheService.set(CACHE_KEY_ACTIVE_PLANS, plans, 60);
            logger.info("Successfully retrieved {} active service plans from database and cached", plans.size());
            return plans;
        } catch (Exception e) {
            logger.error("Error on getting active service plans from database", e);
            throw e;
        }
    }

    public ServicePlanDTO getServicePlanById(Long id) {
        logger.debug("Getting service plan by id: {}", id);
        try {
            String cacheKey = CACHE_KEY_PLAN_BY_ID + id;
            
            // Try to get from cache first
            ServicePlanDTO plan = cacheService.get(cacheKey, ServicePlanDTO.class);
            if (plan != null) {
                logger.info("Retrieved service plan: {} (id: {}) from cache", plan.getName(), id);
                return plan;
            }
            
            // If not in cache, get from database
            ServicePlan servicePlan = servicePlanRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Service plan not found with id: {}", id);
                        return new ResourceNotFoundException("ServicePlan", id);
                    });
            
            plan = toDTO(servicePlan);
            
            // Store in cache
            cacheService.set(cacheKey, plan, 60);
            logger.info("Successfully retrieved service plan: {} (id: {}) from database and cached", plan.getName(), id);
            return plan;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting service plan by id: {}", id, e);
            throw e;
        }
    }

    public ServicePlanDTO getServicePlanWithLimits(Long id) {
        logger.debug("Getting service plan with limits by id: {}", id);
        try {
            String cacheKey = CACHE_KEY_PLAN_WITH_LIMITS + id;
            
            // Try to get from cache first
            ServicePlanDTO plan = cacheService.get(cacheKey, ServicePlanDTO.class);
            if (plan != null) {
                logger.info("Retrieved service plan with limits: {} (id: {}) from cache", plan.getName(), id);
                return plan;
            }
            
            // If not in cache, get from database
            ServicePlan servicePlan = servicePlanRepository.findByIdWithLimits(id)
                    .orElseThrow(() -> {
                        logger.warn("Service plan not found with id: {}", id);
                        return new ResourceNotFoundException("ServicePlan", id);
                    });
            
            plan = toDTO(servicePlan);
            
            // Store in cache
            cacheService.set(cacheKey, plan, 60);
            logger.info("Successfully retrieved service plan with limits: {} (id: {}) from database and cached", plan.getName(), id);
            return plan;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting service plan with limits by id: {}", id, e);
            throw e;
        }
    }
    
    /**
     * Clears all service plan related cache entries.
     * Should be called when service plans are updated, created, or deleted.
     */
    @Transactional
    public void clearServicePlanCache() {
        logger.info("Clearing service plan cache");
        cacheService.removeByPattern(CACHE_PATTERN_PLANS);
    }
    
    /**
     * Clears cache for a specific service plan.
     * @param planId The service plan ID
     */
    @Transactional
    public void clearServicePlanCache(Long planId) {
        logger.info("Clearing cache for service plan: {}", planId);
        cacheService.remove(CACHE_KEY_PLAN_BY_ID + planId);
        cacheService.remove(CACHE_KEY_PLAN_WITH_LIMITS + planId);
        // Also clear list caches as they might contain this plan
        cacheService.remove(CACHE_KEY_ALL_PLANS);
        cacheService.remove(CACHE_KEY_ACTIVE_PLANS);
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


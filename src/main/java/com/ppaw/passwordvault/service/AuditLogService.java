package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.AuditLogDTO;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.model.AuditLog;
import com.ppaw.passwordvault.model.User;
import com.ppaw.passwordvault.repository.AuditLogRepository;
import com.ppaw.passwordvault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public List<AuditLogDTO> getAuditLogsByUserId(Long userId) {
        logger.debug("Getting audit logs for user id: {}", userId);
        try {
            List<AuditLogDTO> logs = auditLogRepository.findByUserId(userId).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} audit logs for user id: {}", logs.size(), userId);
            return logs;
        } catch (Exception e) {
            logger.error("Error on getting audit logs for user id: {}", userId, e);
            throw e;
        }
    }

    public List<AuditLogDTO> getAuditLogsByAction(String action) {
        logger.debug("Getting audit logs by action: {}", action);
        try {
            List<AuditLogDTO> logs = auditLogRepository.findByAction(action).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} audit logs for action: {}", logs.size(), action);
            return logs;
        } catch (Exception e) {
            logger.error("Error on getting audit logs by action: {}", action, e);
            throw e;
        }
    }

    public List<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        logger.debug("Getting audit logs by date range: {} to {}", start, end);
        try {
            List<AuditLogDTO> logs = auditLogRepository.findByCreatedAtBetween(start, end).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} audit logs for date range", logs.size());
            return logs;
        } catch (Exception e) {
            logger.error("Error on getting audit logs by date range", e);
            throw e;
        }
    }

    public AuditLogDTO getAuditLogById(Long id) {
        logger.debug("Getting audit log by id: {}", id);
        try {
            AuditLog log = auditLogRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Audit log not found with id: {}", id);
                        return new ResourceNotFoundException("AuditLog", id);
                    });
            logger.info("Successfully retrieved audit log: {} (id: {})", log.getAction(), id);
            return toDTO(log);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting audit log by id: {}", id, e);
            throw e;
        }
    }

    public void logAction(Long userId, String action, String description, String ipAddress) {
        logger.debug("Logging action: {} for user id: {}", action, userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("User not found for audit log with id: {}", userId);
                        return new ResourceNotFoundException("User", userId);
                    });

            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setDescription(description);
            log.setIpAddress(ipAddress);

            auditLogRepository.save(log);
            logger.debug("Audit log saved successfully: {} for user: {}", action, user.getUsername());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on logging action: {} for user id: {}", action, userId, e);
            throw e;
        }
    }

    private AuditLogDTO toDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .username(log.getUser().getUsername())
                .action(log.getAction())
                .description(log.getDescription())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}


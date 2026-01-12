package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.AuditLogDTO;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.model.AuditLog;
import com.ppaw.passwordvault.model.User;
import com.ppaw.passwordvault.repository.AuditLogRepository;
import com.ppaw.passwordvault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public List<AuditLogDTO> getAuditLogsByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetween(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AuditLogDTO getAuditLogById(Long id) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", id));
        return toDTO(log);
    }

    public void logAction(Long userId, String action, String description, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setDescription(description);
        log.setIpAddress(ipAddress);

        auditLogRepository.save(log);
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


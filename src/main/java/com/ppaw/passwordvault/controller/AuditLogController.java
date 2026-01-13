package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.AuditLogDTO;
import com.ppaw.passwordvault.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogsByUserId(@PathVariable Long userId) {
        List<AuditLogDTO> logs = auditLogService.getAuditLogsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogsByAction(@PathVariable String action) {
        List<AuditLogDTO> logs = auditLogService.getAuditLogsByAction(action);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogDTO>> getAuditLogById(@PathVariable Long id) {
        AuditLogDTO log = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success("Audit log retrieved successfully", log));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<AuditLogDTO> logs = auditLogService.getAuditLogsByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }
}


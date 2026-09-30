package com.kristalball.militaryasset.controller;

import com.kristalball.militaryasset.entity.AuditLog;
import com.kristalball.militaryasset.service.AuditLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for audit log records.
 * Base URL: /api/audit-logs
 *
 * IMPORTANT: Audit logs are immutable — update and delete endpoints are intentionally
 * NOT provided. Only GET (read) and POST (create by the system) are allowed.
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /** GET /api/audit-logs — returns all audit log entries */
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }

    /** GET /api/audit-logs/{id} — returns a single audit log entry or 404 */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(auditLogService.getAuditLogById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/audit-logs — saves a new audit log entry, returns 201 CREATED.
     * In practice, this is called internally by other services, not by external clients.
     */
    @PostMapping
    public ResponseEntity<AuditLog> createAuditLog(@RequestBody AuditLog auditLog) {
        AuditLog created = auditLogService.saveAuditLog(auditLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // NOTE: No PUT or DELETE — audit logs are immutable records.
}

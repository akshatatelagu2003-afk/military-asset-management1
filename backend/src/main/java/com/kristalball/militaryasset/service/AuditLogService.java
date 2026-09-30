package com.kristalball.militaryasset.service;

import com.kristalball.militaryasset.entity.AuditLog;
import com.kristalball.militaryasset.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for the AuditLog entity.
 * Audit logs are immutable records of what happened in the system.
 * They can be created and read, but should NOT be updated or deleted.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /** Return all audit log entries. */
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * Return a single audit log entry by ID.
     * Throws RuntimeException if not found.
     */
    public AuditLog getAuditLogById(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AuditLog not found with id: " + id));
    }

    /**
     * Save a new audit log entry.
     * This is typically called internally after other service operations
     * to record what action was taken.
     */
    public AuditLog saveAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    // NOTE: Update and Delete are intentionally NOT provided for AuditLog.
    // Audit logs are immutable records and must not be modified or removed.
}

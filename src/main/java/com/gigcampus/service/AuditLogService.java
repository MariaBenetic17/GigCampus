package com.gigcampus.service;

import com.gigcampus.entity.AuditLog;
import com.gigcampus.entity.User;
import com.gigcampus.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(User adminUser, String action, String targetType, Long targetId, String details, String ipAddress) {
        AuditLog log = new AuditLog(adminUser, action, targetType, targetId, details, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}

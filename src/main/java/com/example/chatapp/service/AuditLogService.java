package com.example.chatapp.service;

import com.example.chatapp.model.AuditLog;
import com.example.chatapp.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public Mono<AuditLog> logLoginSuccess(UUID userId, String username) {
        return logAudit(userId, username, "LOGIN", "SUCCESS", null);
    }


    public Mono<AuditLog> logAudit(UUID userId, String username, String action, String status, String details) {
        return Mono.deferContextual(ctx -> {
            String ipAddress = ctx.get("ipAddress");

            AuditLog audit = new AuditLog();
            audit.setAuditId(UUID.randomUUID());
            audit.setUserId(userId);
            audit.setUsername(username);
            audit.setAction(action);
            audit.setStatus(status);
            audit.setTimestamp(Instant.now());
            audit.setIpAddress(ipAddress);
            audit.setDetails(details);

            log.info("AUDIT: action={} status={} userId={} username={} ip={}", action, status, userId, username, ipAddress);

            return auditLogRepository.save(audit);

        });

    }
}

package com.example.chatapp.service;

import com.example.chatapp.model.AuditLog;
import com.example.chatapp.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public Mono<AuditLog> logLoginSuccess(UUID userId, String username) {
        return Mono.deferContextual(ctx -> {
            String ipAddress = ctx.get("ipAddress");

            AuditLog audit = new AuditLog();
            audit.setAuditId(UUID.randomUUID());
            audit.setUserId(userId);
            audit.setUsername(username);
            audit.setAction("LOGIN");
            audit.setStatus("SUCCESS");
            audit.setTimestamp(Instant.now());
            audit.setIpAddress(ipAddress);

            return auditLogRepository.save(audit);
        });

    }
}

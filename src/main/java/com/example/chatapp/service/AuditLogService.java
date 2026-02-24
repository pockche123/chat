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

    public Mono<AuditLog> logLoginSuccess(UUID userId) {
        return logAudit(userId, "LOGIN", "SUCCESS", null);
    }

    public Mono<AuditLog> logLoginFailure(UUID userId, String reason) {
        return logAudit(userId, "LOGIN", "FAILURE", reason);
    }

    public Mono<AuditLog> logAudit(UUID userId, String action, String status, String details) {
        return Mono.deferContextual(ctx -> {
            String ipAddress = ctx.get("ipAddress");

            AuditLog audit = new AuditLog();
            audit.setAuditId(UUID.randomUUID());
            audit.setUserId(userId);
            audit.setAction(action);
            audit.setStatus(status);
            audit.setTimestamp(Instant.now());
            audit.setIpAddress(ipAddress);
            audit.setDetails(details);

            log.info("AUDIT: action={} status={} userId={} ip={}", action, status, userId, ipAddress);

            return auditLogRepository.save(audit);
        });
    }

    public Mono<AuditLog> logLogout(UUID userId) {
        return logAudit(userId, "LOGOUT", "SUCCESS", null);
    }

    public Mono<AuditLog> logUserRegistration(UUID userId) {
        String message = "New user registered successfully: " + userId;
        return logAudit(userId, "REGISTER", "SUCCESS", message);
    }

    public Mono<AuditLog> logUserRegistrationFailure(String reason) {
        return logAudit(null, "REGISTER", "FAILURE", reason);
    }

    // WebSocket Connection
    public Mono<AuditLog> logWebSocketConnect(UUID userId) {
        return logAudit(userId, "WEBSOCKET_CONNECT", "SUCCESS", null);
    }

    public Mono<AuditLog> logWebSocketDisconnect(UUID userId) {
        return logAudit(userId, "WEBSOCKET_DISCONNECT", "SUCCESS", null);
    }

    public Mono<AuditLog> logAuthenticationFailure(String reason) {
        return logAudit(null, "AUTHENTICATION", "FAILURE", reason);
    }

    // Group Management
    public Mono<AuditLog> logGroupCreated(UUID userId, UUID groupId, int memberCount) {
        String details = "groupId: " + groupId + ", members: " + memberCount;
        return logAudit(userId, "GROUP_CREATED", "SUCCESS", details);
    }

    public Mono<AuditLog> logGroupMemberAdded(UUID userId, UUID groupId, UUID addedUserId) {
        String details = "groupId: " + groupId + ", addedUser: " + addedUserId;
        return logAudit(userId, "GROUP_MEMBER_ADDED", "SUCCESS", details);
    }

    public Mono<AuditLog> logGroupMemberRemoved(UUID userId, UUID groupId, UUID removedUserId) {
        String details = "groupId: " + groupId + ", removedUser: " + removedUserId;
        return logAudit(userId, "GROUP_MEMBER_REMOVED", "SUCCESS", details);
    }

    // Message Operations
    public Mono<AuditLog> logMessageSent(UUID userId, UUID messageId, UUID conversationId, String messageType) {
        String details = "messageId: " + messageId + ", conversationId: " + conversationId + ", type: " + messageType;
        return logAudit(userId, "MESSAGE_SENT", "SUCCESS", details);
    }

    public Mono<AuditLog> logMessageDelivered(UUID messageId, UUID receiverId) {
        String details = "messageId: " + messageId + ", receiverId: " + receiverId;
        return logAudit(receiverId, "MESSAGE_DELIVERED", "SUCCESS", details);
    }

    public Mono<AuditLog> logMessageRead(UUID messageId, UUID receiverId) {
        String details = "messageId: " + messageId;
        return logAudit(receiverId, "MESSAGE_READ", "SUCCESS", details);
    }

    // Media/File Access
    public Mono<AuditLog> logFileUploadUrlGenerated(UUID userId, String fileName, String contentType) {
        String details = "fileName: " + fileName + ", contentType: " + contentType;
        return logAudit(userId, "FILE_UPLOAD_URL_GENERATED", "SUCCESS", details);
    }

    public Mono<AuditLog> logFileDownloadUrlGenerated(UUID userId, String fileKey) {
        String details = "fileKey: " + fileKey;
        return logAudit(userId, "FILE_DOWNLOAD_URL_GENERATED", "SUCCESS", details);
    }

    // Security Events
    public Mono<AuditLog> logRateLimitExceeded(UUID userId, String action) {
        String details = "action: " + action;
        return logAudit(userId, "RATE_LIMIT_EXCEEDED", "FAILURE", details);
    }
}

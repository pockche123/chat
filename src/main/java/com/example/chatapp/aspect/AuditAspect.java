package com.example.chatapp.aspect;

import com.example.chatapp.annotation.Audited;
import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.model.AuditLog;
import com.example.chatapp.repository.AuditLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


import java.time.Instant;
import java.util.UUID;

@Component
@Aspect
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(audited)")
    public Object auditSuccess(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof Mono) {
            return ((Mono<?>) result)
                    .flatMap(value ->
                            auditSuccess(value, audited)
                                    .thenReturn(value)
                    )
                    .onErrorResume(error ->
                            auditFailure(error, audited)
                                    .then(Mono.error(error))
                    );
        }
        return result;
    }


    private Mono<AuditLog> auditSuccess(Object value, Audited audited) {
        UUID userId = extractUserId(value);
        return saveAuditLog(userId, audited.action(), "SUCCESS", null);
    }

    private Mono<AuditLog> auditFailure(Throwable error, Audited audited) {
        return saveAuditLog(null, audited.action(), "FAILURE", error.getMessage());
    }

    private Mono<AuditLog> saveAuditLog(UUID userId, String action, String status, String detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditId(UUID.randomUUID());
        auditLog.setUserId(userId);
        auditLog.setAction(action);
        auditLog.setStatus(status);
        auditLog.setDetails(detail);
        auditLog.setTimestamp(Instant.now());

        return auditLogRepository.save(auditLog);
    }

    private UUID extractUserId(Object value) {
        if (value instanceof AuthDTO) {
            return ((AuthDTO) value).getUserId();
        }
        return null;
    }

}

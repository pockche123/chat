package com.example.chatapp.unit.service;

import com.example.chatapp.model.AuditLog;
import com.example.chatapp.repository.AuditLogRepository;
import com.example.chatapp.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void logLoginSuccess_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logLoginSuccess(userId, username)
                .contextWrite(Context.of("ipAddress", ipAddress))
        ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                        audit.getUsername().equals(username) &&
                        audit.getAction().equals("LOGIN") &&
                        audit.getStatus().equals("SUCCESS")

                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}

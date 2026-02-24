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
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logLoginSuccess(userId)
                .contextWrite(Context.of("ipAddress", ipAddress))
        ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                        audit.getAction().equals("LOGIN") &&
                        audit.getStatus().equals("SUCCESS")

                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logLoginFailure_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        String ipAddress = "127.0.0.1";
        String failure = "Username or Password wrong.";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logLoginFailure(userId, failure)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("LOGIN") &&
                                audit.getStatus().equals("FAILURE") &&
                                audit.getDetails().equals(failure)

                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logUserLogOut_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        String ipAddress = "127.0.0.1";


        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logLogout(userId)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("LOGOUT")

                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logUserRegistration_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        String ipAddress = "127.0.0.1";
        String message = "New user registered successfully: " + userId;


        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logUserRegistration(userId)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("REGISTER") &&
                                audit.getDetails().equals(message)
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logUserRegistrationFailure_savesAuditLog(){
        String ipAddress = "127.0.0.1";
        String reason = "Username already exists";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logUserRegistrationFailure(reason)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                                audit.getAction().equals("REGISTER") &&
                                audit.getStatus().equals("FAILURE") &&
                                audit.getDetails().equals(reason) &&
                                audit.getIpAddress().equals(ipAddress)
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logWebSocketConnect_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logWebSocketConnect(userId)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("WEBSOCKET_CONNECT") &&
                                audit.getStatus().equals("SUCCESS")
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logWebSocketDisconnect_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logWebSocketDisconnect(userId)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("WEBSOCKET_DISCONNECT") &&
                                audit.getStatus().equals("SUCCESS")
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logAuthenticationFailure_savesAuditLog(){
        String ipAddress = "127.0.0.1";
        String reason = "Invalid JWT token";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logAuthenticationFailure(reason)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getAction().equals("AUTHENTICATION") &&
                                audit.getStatus().equals("FAILURE") &&
                                audit.getDetails().equals(reason)
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logGroupCreated_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        
        UUID groupId = UUID.randomUUID();
        int memberCount = 5;
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logGroupCreated(userId,groupId, memberCount)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("GROUP_CREATED") &&
                                audit.getStatus().equals("SUCCESS") &&
                                audit.getDetails().contains(groupId.toString())
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logGroupMemberAdded_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        
        UUID groupId = UUID.randomUUID();
        UUID addedUserId = UUID.randomUUID();
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logGroupMemberAdded(userId,groupId, addedUserId)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("GROUP_MEMBER_ADDED") &&
                                audit.getDetails().contains(addedUserId.toString())
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logGroupMemberRemoved_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        
        UUID groupId = UUID.randomUUID();
        UUID removedUserId = UUID.randomUUID();
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logGroupMemberRemoved(userId, groupId, removedUserId)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("GROUP_MEMBER_REMOVED") &&
                                audit.getDetails().contains(removedUserId.toString())
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logMessageSent_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String messageType = "TEXT";
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logMessageSent(userId, messageId, conversationId, messageType)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("MESSAGE_SENT") &&
                                audit.getDetails().contains(messageId.toString())
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logMessageDelivered_savesAuditLog(){
        UUID messageId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logMessageDelivered(messageId, receiverId)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(receiverId) &&
                                audit.getAction().equals("MESSAGE_DELIVERED") &&
                                audit.getDetails().contains(messageId.toString())
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logMessageRead_savesAuditLog(){
        UUID messageId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logMessageRead(messageId, receiverId)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(receiverId) &&
                                audit.getAction().equals("MESSAGE_READ") &&
                                audit.getDetails().contains(messageId.toString())
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logFileUploadUrlGenerated_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        
        String fileName = "document.pdf";
        String contentType = "application/pdf";
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logFileUploadUrlGenerated(userId, fileName, contentType)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("FILE_UPLOAD_URL_GENERATED") &&
                                audit.getDetails().contains(fileName)
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logFileDownloadUrlGenerated_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        
        String fileKey = "chat-media/abc-123-document.pdf";
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logFileDownloadUrlGenerated(userId, fileKey)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("FILE_DOWNLOAD_URL_GENERATED") &&
                                audit.getDetails().contains(fileKey)
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logRateLimitExceeded_savesAuditLog(){
        UUID userId = UUID.randomUUID();
        
        String action = "LOGIN";
        String ipAddress = "127.0.0.1";

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(auditLogService.logRateLimitExceeded(userId, action)
                        .contextWrite(Context.of("ipAddress", ipAddress))
                ).expectNextMatches( audit ->
                        audit.getUserId().equals(userId) &&
                                audit.getAction().equals("RATE_LIMIT_EXCEEDED") &&
                                audit.getStatus().equals("FAILURE") &&
                                audit.getDetails().contains(action)
                )
                .verifyComplete();

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}

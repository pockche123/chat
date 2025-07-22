package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class LoggingPushNotificationServiceTest {

    private LoggingPushNotificationService pushNotificationService = new LoggingPushNotificationService();

    @Test
    void sendNotification_shouldNotThrowException() {
        // Given

        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());

        // When/Then - Just verify it doesn't throw
        assertDoesNotThrow(() -> pushNotificationService.sendNotification(message));
    }

}

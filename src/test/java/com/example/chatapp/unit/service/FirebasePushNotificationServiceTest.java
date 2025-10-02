package com.example.chatapp.unit.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.UserDevice;
import com.example.chatapp.service.DeviceTokenService;
import com.example.chatapp.service.FirebasePushNotificationService;
import com.google.firebase.messaging.FirebaseMessaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.firebase.messaging.Message;


import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class FirebasePushNotificationServiceTest {

    @Mock
    DeviceTokenService deviceTokenService;

    @Mock
    FirebaseMessaging firebaseMessaging;

    @InjectMocks
    FirebasePushNotificationService firebasePushNotificationService;

    @Test
    void shouldCallFirebase_whenUserHasDeviceToken() throws FirebaseMessagingException {
        UUID userId = UUID.randomUUID();
        UserDevice device = new UserDevice();
        device.setDeviceToken("device-token");
        device.setDeviceType("ios");
        device.setUserId(userId);

        ChatMessage message = new ChatMessage();
        message.setReceiverId(userId);
        message.setContent("Test message content");

        when(deviceTokenService.getDeviceToken(userId)).thenReturn("device-token");


        firebasePushNotificationService.sendNotification(message);

        // Then - what should we verify?
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    void shouldNotCallFirebase_whenUserDoesNotHaveDeviceToken() throws FirebaseMessagingException {
        UUID userId = UUID.randomUUID();

        ChatMessage message = new ChatMessage();
        message.setReceiverId(userId);
        message.setContent("Test message content");

        when(deviceTokenService.getDeviceToken(userId)).thenReturn(null);

        firebasePushNotificationService.sendNotification(message);


        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    void shouldHandleException_whenFirebaseThrowsException() throws FirebaseMessagingException {
        UUID userId = UUID.randomUUID();
        UserDevice device = new UserDevice();
        device.setDeviceToken("device-token");
        device.setDeviceType("ios");
        device.setUserId(userId);

        ChatMessage message = new ChatMessage();
        message.setReceiverId(userId);
        message.setContent("Test message content");

        when(deviceTokenService.getDeviceToken(userId)).thenReturn("device-token");

        doThrow(FirebaseMessagingException.class).when(firebaseMessaging).send(any(Message.class));

        assertDoesNotThrow(() -> firebasePushNotificationService.sendNotification(message));
    }
}

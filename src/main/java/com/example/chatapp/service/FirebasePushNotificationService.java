package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
public class FirebasePushNotificationService implements PushNotificationService{
    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenService deviceTokenService;

    public FirebasePushNotificationService(FirebaseMessaging firebaseMessaging, DeviceTokenService deviceTokenService) {
        this.firebaseMessaging = firebaseMessaging;
        this.deviceTokenService = deviceTokenService;
    }

    @Override
    public void sendNotification(ChatMessage message) {
        try {
            String deviceToken = deviceTokenService.getDeviceToken(message.getReceiverId());
            if(deviceToken != null) {
                Message firebaseMessage = Message.builder()
                        .setToken(deviceToken)
                        .putData("title", "New Message")
                        .putData("body", message.getContent())
                        .build();

                firebaseMessaging.send(firebaseMessage);
            }
        }catch (FirebaseMessagingException e) {
            log.error("Failed to send notification: {}", e.getMessage());

        }
    }
}

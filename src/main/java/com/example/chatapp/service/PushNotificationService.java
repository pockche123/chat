package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    public void sendPushNotification(ChatMessage message) {
        // Placeholder implementation - will be enhanced later
        // to actually send push notifications
        System.out.println("Sending push notification to user " + ": " + message);
    }
}

package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import org.springframework.stereotype.Service;

@Service
public class LoggingPushNotificationService implements PushNotificationService {

    @Override
    public void sendNotification(ChatMessage message) {

    }
}

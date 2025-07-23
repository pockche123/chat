package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;

public interface PushNotificationService {
    void sendNotification(ChatMessage message);
}

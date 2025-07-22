package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingPushNotificationService implements PushNotificationService {

    @Override
    public void sendNotification(ChatMessage message) {
        log.info("PUSH NOTIFICATION: Message {} for user {}",
                message.getMessageId(), message.getReceiverId());
    }
}

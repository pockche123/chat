package com.example.chatapp.service;

import com.example.chatapp.event.ChatMessageEvent;
import com.example.chatapp.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * In-memory implementation of MessageQueueService using Spring Events.
 * This can be replaced with a Kafka implementation later.
 */
@Slf4j
@Service
public class SpringEventMessageQueueService implements MessageQueueService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public SpringEventMessageQueueService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void enqueueMessage(ChatMessage message) {
        log.info("[THREAD: {}] Enqueueing message: {} from {} to {}", 
                Thread.currentThread().getName(), 
                message.getMessageId(),
                message.getSenderId(),
                message.getReceiverId());
                
        // Publish message event to internal queue
        eventPublisher.publishEvent(new ChatMessageEvent(message));
    }

//    @EventListener
//    public void handleChatMessage(ChatMessageEvent event) {
//        ChatMessage message = event.getMessage();
//
//        // Always persist the message first
//        ChatMessage savedMessage = chatMessageService.saveMessage(message);
//
//        if (onlineUserService.isUserOnline(message.getReceiverId())) {
//            // User online - deliver immediately
//            messageDeliveryService.deliverMessage(savedMessage);
//        } else {
//            // User offline - queue message and send push notification
//            messageQueueService.queueMessage(savedMessage);
//            pushNotificationService.sendNotification(savedMessage);
//        }
//    }
//
//    @EventListener
//    public void handleUserOnline(UserOnlineEvent event) {
//        // When user comes online, deliver all queued messages
//        messageQueueService.deliverQueuedMessages(event.getUserId());
//    }
}
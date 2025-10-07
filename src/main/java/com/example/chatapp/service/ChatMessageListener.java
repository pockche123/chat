package com.example.chatapp.service;


import com.example.chatapp.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ChatMessageListener {


    @Autowired
    private DistributedMessageDeliveryService distributedMessageDeliveryService;

    @Autowired
    private FirebasePushNotificationService pushNotificationService;

    @Autowired
    private DistributedOnlineUserService distributedOnlineUserService;


    private Mono<Void> processMessage(ChatMessage message, OnlineUserService onlineUserService, MessageDeliveryService messageDeliveryService){
        log.info("Processing message for user: {}", message.getReceiverId());
        boolean isOnline = onlineUserService.isUserOnline(message.getReceiverId());
        log.info("User {} is online: {}", message.getReceiverId(), isOnline);
        
        if(isOnline){
            // Deliver immediately if user is online
            log.info("Delivering message to online user: {}", message.getReceiverId());
            return messageDeliveryService.deliverMessage(message)
                    .doOnError(error -> log.error("Failed to deliver message {}: {}",
                            message.getMessageId(), error.getMessage()))
                    .onErrorReturn(message)
                    .then();

        } else {
            // Send push notification if user is offline
            log.info("Sending push notification to offline user: {}", message.getReceiverId());
            pushNotificationService.sendNotification(message);
            log.info("Push notification sent for message: {}", message.getMessageId());
            return Mono.empty();

            // Message is already stored and will be delivered when user comes online
        }
    }

    @KafkaListener(topics = "chat-messages", groupId = "chat-service")
    public void handleKafkaMessage(ChatMessage message) {
        log.info("Received Kafka message: {}", message);
        processMessage(message, distributedOnlineUserService, distributedMessageDeliveryService)
                .doOnError(error -> log.error("Failed to process message: {}", error.getMessage()))
                .block();
    }
}

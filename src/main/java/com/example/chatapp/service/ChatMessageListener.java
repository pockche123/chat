package com.example.chatapp.service;

import com.example.chatapp.event.ChatMessageEvent;
import com.example.chatapp.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ChatMessageListener {


    @Autowired
    private MessageDeliveryService messageDeliveryService;

    @Autowired
    private PushNotificationService pushNotificationService;
    @Autowired
    private LocalOnlineUserService localOnlineUserService;
    @Autowired
    private DistributedOnlineUserService distributedOnlineUserService;


    @EventListener
    public Mono<Void> handleChatMessage(ChatMessageEvent event){
        ChatMessage message = event.getMessage();

        return processMessage(message, localOnlineUserService);

    }

    private Mono<Void> processMessage(ChatMessage message, OnlineUserService onlineUserService){
        if(onlineUserService.isUserOnline(message.getReceiverId())){
            // Deliver immediately if user is online
            return messageDeliveryService.deliverMessage(message)
                    .doOnError(error -> log.error("Failed to deliver message {}: {}",
                            message.getMessageId(), error.getMessage()))
                    .onErrorReturn(message)
                    .then();

        } else {
            // Send push notification if user is offline
            pushNotificationService.sendNotification(message);
            return Mono.empty();

            // Message is already stored and will be delivered when user comes online
        }
    }


    public Mono<Void> handleKafkaMessage(ChatMessage message) {
        return processMessage(message, distributedOnlineUserService);
    }
}

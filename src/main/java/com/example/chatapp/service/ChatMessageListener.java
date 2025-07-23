package com.example.chatapp.service;

import com.example.chatapp.event.ChatMessageEvent;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageListener {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private MessageDeliveryService messageDeliveryService;

    @Autowired
    private PushNotificationService pushNotificationService;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @EventListener
    public void handleChatMessage(ChatMessageEvent event){
        ChatMessage message = event.getMessage();
        
        // Always store the message regardless of user's online status
        chatMessageRepository.save(message).subscribe();
        
        if(onlineUserService.isUserOnline(message.getReceiverId())){
            // Deliver immediately if user is online
            messageDeliveryService.deliverMessage(message);
        } else {
            // Send push notification if user is offline
            pushNotificationService.sendNotification(message);
            // Message is already stored and will be delivered when user comes online
        }
    }
}

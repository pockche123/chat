package com.example.chatapp.service;

import com.example.chatapp.event.ChatMessageEvent;
import com.example.chatapp.model.ChatMessage;
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

    @EventListener
    public void handleChatMessage(ChatMessageEvent event){
        ChatMessage message = event.getMessage();
            if(onlineUserService.isUserOnline(message.getReceiverId())){
                messageDeliveryService.deliverMessage(message);
            } else{
                pushNotificationService.sendPushNotification(message);
            }
    }
}

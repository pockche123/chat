package com.example.chatapp.unit.service;

import com.example.chatapp.event.ChatMessageEvent;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.ChatMessageListener;
import com.example.chatapp.service.MessageDeliveryService;
import com.example.chatapp.service.OnlineUserService;
import com.example.chatapp.service.PushNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ChatMessageListenerTest {

    @Mock
    private OnlineUserService onlineUserService;

    @Mock
    private MessageDeliveryService messageDeliveryService;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private ChatMessageListener chatMessageListener;

    @Test
    public void should_deliverMessage_whenUserOnline(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());
        ChatMessageEvent event = new ChatMessageEvent(message);

        when(onlineUserService.isUserOnline(message.getReceiverId())).thenReturn(true);

        chatMessageListener.handleChatMessage(event);

        verify(messageDeliveryService).deliverMessage(message);
        verify(pushNotificationService, never()).sendNotification(message);

    }

    @Test
    public void should_pushNotification_whenUserOffline(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());
        ChatMessageEvent event = new ChatMessageEvent(message);

        when(onlineUserService.isUserOnline(message.getReceiverId())).thenReturn(false);

        chatMessageListener.handleChatMessage(event);

        verify(messageDeliveryService, never()).deliverMessage(message);
        verify(pushNotificationService).sendNotification(message);

    }
}

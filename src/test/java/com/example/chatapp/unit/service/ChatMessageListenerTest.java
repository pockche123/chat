package com.example.chatapp.unit.service;

import com.example.chatapp.event.ChatMessageEvent;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;


import static org.mockito.Mockito.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ChatMessageListenerTest {

    @Mock
    private LocalOnlineUserService LocalOnlineUserService;

    @Mock
    private MessageDeliveryService messageDeliveryService;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private DistributedOnlineUserService distributedOnlineUserService;

    @InjectMocks
    private ChatMessageListener chatMessageListener;

    @Test
    public void should_deliverMessage_whenUserOnline(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());
        ChatMessageEvent event = new ChatMessageEvent(message);

        when(LocalOnlineUserService.isUserOnline(message.getReceiverId())).thenReturn(true);
        when(messageDeliveryService.deliverMessage(message)).thenReturn(Mono.empty());

        chatMessageListener.handleChatMessage(event).block();

        verify(messageDeliveryService).deliverMessage(message);
        verify(pushNotificationService, never()).sendNotification(message);

    }

    @Test
    public void should_pushNotification_whenUserOffline(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());
        ChatMessageEvent event = new ChatMessageEvent(message);

        when(LocalOnlineUserService.isUserOnline(message.getReceiverId())).thenReturn(false);

        chatMessageListener.handleChatMessage(event).block();

        verify(messageDeliveryService, never()).deliverMessage(message);
        verify(pushNotificationService).sendNotification(message);

    }

    @Test
    public void should_deliverKafkaMessage_whenUserOnline(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());

        when(distributedOnlineUserService.isUserOnline(message.getReceiverId())).thenReturn(true);
        when(messageDeliveryService.deliverMessage(message)).thenReturn(Mono.empty());

        chatMessageListener.handleKafkaMessage(message).block();

        verify(messageDeliveryService).deliverMessage(message);
    }



}

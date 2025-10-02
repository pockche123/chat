package com.example.chatapp.unit.service;


import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.DistributedOnlineUserService;
import com.example.chatapp.service.PushNotificationService;
import com.example.chatapp.service.DistributedMessageDeliveryService;
import com.example.chatapp.service.ChatMessageListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMessageListenerTest {


    @Mock
    private DistributedMessageDeliveryService distributedMessageDeliveryService;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private DistributedOnlineUserService distributedOnlineUserService;

    @InjectMocks
    private ChatMessageListener chatMessageListener;


    @Test
    public void should_pushNotification_whenUserOffline(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());


        when(distributedOnlineUserService.isUserOnline(message.getReceiverId())).thenReturn(false);

        chatMessageListener.handleKafkaMessage(message);

        verify(distributedMessageDeliveryService, never()).deliverMessage(message);
        verify(pushNotificationService).sendNotification(message);

    }

    @Test
    public void should_deliverKafkaMessage_whenUserOnline(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());

        when(distributedOnlineUserService.isUserOnline(message.getReceiverId())).thenReturn(true);
        when(distributedMessageDeliveryService.deliverMessage(message)).thenReturn(Mono.just(message));

        chatMessageListener.handleKafkaMessage(message);

        verify(distributedMessageDeliveryService).deliverMessage(message);
    }
}
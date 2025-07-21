package com.example.chatapp.service;

import com.example.chatapp.event.ChatMessageEvent;
import com.example.chatapp.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;


import java.util.UUID;

import static reactor.core.publisher.Mono.when;

public class ChatMessageListenerTest {

    @Mock
    private OnlineUserService onlineUserService;

    @Test
    public void should_deliverMessage_whenUserOnline(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());
        ChatMessageEvent event = new ChatMessageEvent(message);


        when(onlineUserService.isUserOnline(message.getReceiverId())).thenReturn(true);

    }
}

package com.example.chatapp.integration;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.OnlineUserService;
import com.example.chatapp.service.UndeliveredMessageService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class OnlineUserServiceIntegrationTest {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;

    @Autowired
    private UndeliveredMessageService undeliveredMessageService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void shouldDeliverUndeliveredMessagesWhenUserComesOnline(){
        UUID userId = UUID.randomUUID();
        ChatMessage undeliveredMessage= createUndeliveredMessage(userId);
        chatMessageRepository.save(undeliveredMessage).block();
        onlineUserService.markUserOnline(userId).block();

        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.send(any())).thenReturn(Mono.empty());
        when(mockSession.textMessage(anyString())).thenReturn(mock(WebSocketMessage.class));

        webSocketMessageDeliveryService.registerSession(userId, mockSession);

        StepVerifier.create(undeliveredMessageService.deliverUndeliveredMessage(userId))
                .expectNextCount(1)
                .verifyComplete();

        ChatMessage deliveredMessage = chatMessageRepository.findByMessageId(undeliveredMessage.getMessageId())
                .blockFirst();
        assertEquals(MessageStatus.DELIVERED, deliveredMessage.getStatus());
    }

    private ChatMessage createUndeliveredMessage(UUID receiverId){
        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());
        message.setConversationId(UUID.randomUUID());
        message.setReceiverId(receiverId);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));
        message.setSenderId(UUID.randomUUID());
        message.setStatus(MessageStatus.SENT);
        message.setContent("test message");
        return message;
    }
}

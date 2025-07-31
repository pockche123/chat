package com.example.chatapp.integration;

import com.example.chatapp.handler.ChatWebSocketHandler;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.LocalOnlineUserService;
import com.example.chatapp.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ReadReceiptIntegrationTest {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;


    @Autowired
    private LocalOnlineUserService LocalOnlineUserService;



    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private JwtUtil jwtUtil;


    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        public LocalOnlineUserService LocalOnlineUserService() {
            return mock(LocalOnlineUserService.class);
        }

    }



//    too much mocking; grey area for integration testing
    @Test
    void should_markMessagesAsRead_when_readReceiptReceived_viaWebSocket() throws JsonProcessingException {

        UUID receiverId = UUID.randomUUID();
        UUID conversationId =  UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        WebSocketSession mockSession = mock(WebSocketSession.class, RETURNS_DEEP_STUBS);
        when(mockSession.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("Bearer token");


        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(senderId);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(LocalOnlineUserService.markUserOnline(any())).thenReturn(Mono.empty());

        ChatMessage deliveredMessage = createDeliveredMessage(receiverId, senderId, conversationId);
        chatMessageRepository.save(deliveredMessage).block();


        String json = "{\"type\":\"read_receipt\",\"receiverId\":\"" + receiverId + "\",\"content\":\"hello\", \"conversationId\": \"" + conversationId + "\"}";

        // Create a mock WebSocketMessage that returns our JSON
        WebSocketMessage webSocketMessage = mock(WebSocketMessage.class);
        when(webSocketMessage.getPayloadAsText()).thenReturn(json);

        // Mock session.receive() to return our message
        when(mockSession.receive()).thenReturn(Flux.just(webSocketMessage));

        chatWebSocketHandler.handle(mockSession).block();
        ChatMessage updatedMessage = chatMessageRepository.findByMessageId(deliveredMessage.getMessageId())
                .blockFirst();
        assertEquals(MessageStatus.READ, updatedMessage.getStatus());
    }


    private ChatMessage createDeliveredMessage(UUID receiverId, UUID senderId, UUID conversationId){
        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());
        message.setConversationId(conversationId);
        message.setReceiverId(receiverId);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));
        message.setSenderId(senderId);
        message.setStatus(MessageStatus.DELIVERED);
        message.setContent("hello");
        return message;
    }


}
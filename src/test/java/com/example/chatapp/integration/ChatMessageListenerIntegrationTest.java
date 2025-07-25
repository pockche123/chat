package com.example.chatapp.integration;

import com.example.chatapp.event.ChatMessageEvent;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.ChatMessageListener;
import com.example.chatapp.service.MessageDeliveryService;
import com.example.chatapp.service.OnlineUserService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class ChatMessageListenerIntegrationTest {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private ChatMessageListener chatMessageListener;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MessageDeliveryService messageDeliveryService;

    @Autowired
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;

    @Test
    void test_chatMessageListener_deliversMessage(){
        UUID userId = UUID.randomUUID();
        ChatMessage message = createMessage(userId);
        ChatMessageEvent messageEvent = new ChatMessageEvent(message);
        chatMessageRepository.save(message).block();

        onlineUserService.markUserOnline(userId).block();
        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.send(any())).thenReturn(Mono.empty());
        when(mockSession.textMessage(anyString())).thenReturn(mock(WebSocketMessage.class));
        webSocketMessageDeliveryService.registerSession(userId, mockSession);

        chatMessageListener.handleChatMessage(messageEvent).block();

        ChatMessage deliveredMessage = chatMessageRepository.findByMessageId(message.getMessageId())
                .blockFirst();

        assertTrue(onlineUserService.isUserOnline(userId));
        assertEquals(MessageStatus.DELIVERED, deliveredMessage.getStatus());

    }

    private ChatMessage createMessage(UUID receiverId){
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

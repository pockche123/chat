package com.example.chatapp.unit.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebSocketMessageDeliveryServiceTest {

    @Mock
    private WebSocketSession session;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;

    @Test
    void should_deliver_message_to_registered_session() {
        UUID receiverId  = UUID.randomUUID();
        ChatMessage message = new ChatMessage();
        message.setSenderId(UUID.randomUUID());
        message.setReceiverId(receiverId);
        message.setContent("Hello");

        // Register session for receiver
        when(session.isOpen()).thenReturn(true);
        when(session.send(any(Publisher.class))).thenReturn(Mono.empty());
        when(session.textMessage(anyString())).thenReturn(mock(WebSocketMessage.class));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(Mono.just(message));


        webSocketMessageDeliveryService.registerSession(receiverId, session);

        webSocketMessageDeliveryService.deliverMessage(message);

//        with this we are checking to see if the message was actually sent to the recipient
        verify(session).textMessage(anyString());
        verify(session).send(any(Publisher.class));

    }

    @Test
    void should_not_deliver_message_to_closed_session(){
//        Given
        UUID receivedId = UUID.randomUUID();
        ChatMessage message  = new ChatMessage();
        message.setReceiverId(receivedId);

        when(session.isOpen()).thenReturn(false);
        webSocketMessageDeliveryService.registerSession(receivedId, session);

        webSocketMessageDeliveryService.deliverMessage(message);

        verify(session, never()).send(any(Publisher.class));

    }

    @Test
    void should_remove_session_when_requested(){
        UUID receiverId = UUID.randomUUID();
        ChatMessage message  = new ChatMessage();
        message.setReceiverId(receiverId);

        webSocketMessageDeliveryService.registerSession(receiverId, session);

        webSocketMessageDeliveryService.removeSession(receiverId);
        webSocketMessageDeliveryService.deliverMessage(message);

        verify(session, never()).send(any(Publisher.class));
    }
}
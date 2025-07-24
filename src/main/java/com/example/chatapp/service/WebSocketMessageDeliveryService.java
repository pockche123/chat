package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of MessageDeliveryService that delivers messages via WebSocket.
 * This is a placeholder implementation that will be enhanced later.
 */
@Slf4j
@Service
public class WebSocketMessageDeliveryService implements MessageDeliveryService {

    private final Map<UUID, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ChatMessageRepository chatMessageRepository;


    public void registerSession(UUID userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }
    
    public void removeSession(UUID userId) {
        userSessions.remove(userId);
    }
    
    @Override
    public Mono<ChatMessage> deliverMessage(ChatMessage message) {
        log.info("Delivering message {} to user {}", 
                message.getMessageId(), message.getReceiverId());

        WebSocketSession session = userSessions.get(message.getReceiverId());
        if (session != null && session.isOpen()) {
            return Mono.fromCallable(() -> objectMapper.writeValueAsString(message))
                    .map(session::textMessage)
                    .flatMap(webSocketMessage -> session.send(Mono.just(webSocketMessage)))
                    .then(Mono.fromRunnable(() -> message.setStatus(MessageStatus.DELIVERED)))
                    .then(chatMessageRepository.save(message))
                    .onErrorResume(e -> {
                        log.error("Failed to deliver message: {}", e.getMessage());
                        return Mono.just(message);
                    });
        }
        return Mono.just(message);
    }
}
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


    public void registerSession(UUID userId, WebSocketSession session){
        userSessions.put(userId, session);
    }
    
    public void removeSession(UUID userId) {
        userSessions.remove(userId);
    }
    
    @Override
    public void deliverMessage(ChatMessage message) {
        log.info("Delivering message {} to user {}", 
                message.getMessageId(), message.getReceiverId());

        WebSocketSession session = userSessions.get(message.getReceiverId());
        
        if (session != null && session.isOpen()) {
            try {
                message.setStatus(MessageStatus.DELIVERED);
                chatMessageRepository.save(message).subscribe();

                String json = objectMapper.writeValueAsString(message);
                WebSocketMessage webSocketMessage = session.textMessage(json);
                session.send(Mono.just(webSocketMessage)).subscribe();
            } catch(Exception e){
                log.error("Failed to deliver message: {}", e.getMessage());
            }
        }
    }
}
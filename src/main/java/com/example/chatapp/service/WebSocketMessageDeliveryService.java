package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of MessageDeliveryService that delivers messages via WebSocket.
 * This is a placeholder implementation that will be enhanced later.
 */
@Slf4j
@Service
public class WebSocketMessageDeliveryService implements MessageDeliveryService {
    
    @Override
    public void deliverMessage(ChatMessage message) {
        // Placeholder implementation - will be enhanced later
        // to actually deliver messages to WebSocket sessions
        log.info("Delivering message {} to user {}", 
                message.getMessageId(), message.getReceiverId());
    }
}
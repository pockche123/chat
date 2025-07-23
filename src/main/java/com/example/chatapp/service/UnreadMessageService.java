package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Service to handle unread messages for users when they come back online
 */
@Slf4j
@Service
public class UnreadMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private MessageDeliveryService messageDeliveryService;
    
    /**
     * Fetches and delivers unread messages for a user who just came online
     * 
     * @param userId The ID of the user who came online
     */
    public void deliverUnreadMessages(UUID userId) {
        log.info("Delivering unread messages for user {}", userId);
        
        // In a real implementation, you would query for unread messages
        // This is a simplified version that would need to be enhanced with:
        // - Proper unread message tracking
        // - Pagination for large message volumes
        // - Message ordering by timestamp
        
        Flux<ChatMessage> unreadMessages = chatMessageRepository.findByReceiverId(userId)
                .filter(message -> isUnread(message));
                
        unreadMessages.subscribe(
            message -> messageDeliveryService.deliverMessage(message),
            error -> log.error("Error delivering unread messages: {}", error.getMessage()),
            () -> log.info("Completed delivering unread messages for user {}", userId)
        );
    }
    
    /**
     * Determines if a message is unread
     * This is a placeholder method - in a real implementation, you would:
     * - Track message read status in the database
     * - Check against a read receipts table
     * - Or use message timestamps against last user activity
     */
    private boolean isUnread(ChatMessage message) {
        // Placeholder implementation
        // In a real system, you would check against a read status field or table
        return true; // For now, treat all messages as unread
    }
}
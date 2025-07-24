package com.example.chatapp.repository;

import com.example.chatapp.model.ChatMessage;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends ReactiveCassandraRepository<ChatMessage, UUID> {
    Flux<ChatMessage> findByMessageId(UUID messageId);
    
    /**
     * Find all messages where the specified user is the receiver
     */
    Flux<ChatMessage> findByReceiverId(UUID receiverId);
    Flux<ChatMessage> findByReceiverIdAndStatus(UUID receiverId, String status);
}

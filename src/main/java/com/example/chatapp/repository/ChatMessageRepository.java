package com.example.chatapp.repository;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import org.springframework.data.cassandra.repository.Query;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends ReactiveCassandraRepository<ChatMessage, UUID> {
    @Query("SELECT * FROM chat_messages WHERE message_id = ?0 ALLOW FILTERING")
    Flux<ChatMessage> findByMessageId(UUID messageId);
    
    /**
     * Find all messages where the specified user is the receiver
     */
    Flux<ChatMessage> findByReceiverId(UUID receiverId);
    @Query("SELECT * FROM chat_messages WHERE receiver_id = ?0 AND status = ?1 ALLOW FILTERING")
    Flux<ChatMessage> findByReceiverIdAndStatus(UUID receiverId, String status);

    @Query("SELECT * FROM chat_messages WHERe conversation_id = ?0 AND receiver_id = ?1 AND status = ?2 ALLOW FILTERING")
    Flux<ChatMessage> findByConversationIdAndReceiverIdAndStatus(UUID conversationId, UUID receiverId, MessageStatus status);
}

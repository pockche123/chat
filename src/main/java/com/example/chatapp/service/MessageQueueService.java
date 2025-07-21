package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;

/**
 * Interface for message queue operations.
 * This allows for different implementations (in-memory, Kafka, etc.)
 */
public interface MessageQueueService {
    
    /**
     * Enqueue a message for delivery
     */
    void enqueueMessage(ChatMessage message);
}
package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageQueueService implements MessageQueueService {
    KafkaTemplate<String, ChatMessage> kafkaTemplate;
    @Override
    public void enqueueMessage(ChatMessage message) {
        kafkaTemplate.send("chat-messages", message.getReceiverId().toString(), message);
    }
}

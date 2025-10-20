package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
public class KafkaMessageQueueService implements MessageQueueService {
    @Autowired
    private KafkaTemplate<String, ChatMessage> kafkaTemplate;
    
    @Override
    public void enqueueMessage(ChatMessage message) {
        log.info("Sending message to Kafka: messageId={}, receiverId={}", 
                message.getMessageId(), message.getReceiverId());
        kafkaTemplate.send("chat-messages", message.getReceiverId().toString(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent successfully: {}", result.getRecordMetadata());
                    } else {
                        log.error("Failed to send message: {}", ex.getMessage());
                    }
                });
    }
}

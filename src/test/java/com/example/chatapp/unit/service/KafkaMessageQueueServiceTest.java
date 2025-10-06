package com.example.chatapp.unit.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.KafkaMessageQueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaMessageQueueServiceTest {

    @Mock
    private KafkaTemplate<String, ChatMessage> kafkaTemplate;

    @InjectMocks
    private KafkaMessageQueueService kafkaMessageQueueService;

    @Test
    void should_publishMessage_to_kafkaTopic(){
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());
        
        CompletableFuture<SendResult<String, ChatMessage>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any(ChatMessage.class))).thenReturn(future);

        // Act
        kafkaMessageQueueService.enqueueMessage(message);

        // Assert
        verify(kafkaTemplate).send("chat-messages", message.getReceiverId().toString(), message);
    }
}

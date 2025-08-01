package com.example.chatapp.unit.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.KafkaMessageQueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KafkaMessageQueueServiceTest {

    @Mock
    private KafkaTemplate<String, ChatMessage> kafkaTemplate;

    @InjectMocks
    private KafkaMessageQueueService kafkaMessageQueueService;

    @Test
    void should_publishMessage_to_kafkaTopic(){
        ChatMessage message = new ChatMessage();

        kafkaMessageQueueService.enqueueMessage(message);


        verify(kafkaTemplate).send("chat-messages", message);




    }
}

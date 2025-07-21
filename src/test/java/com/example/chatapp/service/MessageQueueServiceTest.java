package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class MessageQueueServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MessageQueueService messageQueueService;

    @Test
    public void should_enqueue_message(){
        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());

        message
    }
}

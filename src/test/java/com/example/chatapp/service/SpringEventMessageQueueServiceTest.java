package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.event.ChatMessageEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SpringEventMessageQueueServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SpringEventMessageQueueService messageQueueService;

    @Test
    public void should_enqueue_message(){
        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());

        messageQueueService.enqueueMessage(message);

        verify(eventPublisher).publishEvent(any(ChatMessageEvent.class));
    }
}

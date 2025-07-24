package com.example.chatapp.unit.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.MessageDeliveryService;
import com.example.chatapp.service.UndeliveredMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
public class UndeliveredMessageServiceTest {

    @Mock
    ChatMessageRepository chatMessageRepository;

    @Mock
    MessageDeliveryService messageDeliveryService;

    @InjectMocks
    UndeliveredMessageService undeliveredMessageService;

    @Test
    void should_deliver_SENTMessagesToUser(){
        UUID receiverId = UUID.randomUUID();
        ChatMessage message1 = new ChatMessage();
        message1.setContent("Hello");
        message1.setReceiverId(receiverId);
        message1.setStatus(MessageStatus.SENT);
        ChatMessage message2 = new ChatMessage();
        message2.setContent("World");
        message2.setReceiverId(receiverId);
        message2.setStatus(MessageStatus.SENT);

        when(chatMessageRepository.findByReceiverIdAndStatus(receiverId, MessageStatus.SENT.toString())).thenReturn(Flux.just(message1, message2));
        undeliveredMessageService.deliverUndeliveredMessage(receiverId);

        verify(messageDeliveryService).deliverMessage(message1);

    }

}

package com.example.chatapp.unit.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.MessageDeliveryService;
import com.example.chatapp.service.UndeliveredMessageService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UndeliveredMessageServiceTest {

    @Mock
    ChatMessageRepository chatMessageRepository;

    @Mock
    WebSocketMessageDeliveryService messageDeliveryService;

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
        when(messageDeliveryService.deliverMessage(any(ChatMessage.class)))
                .thenAnswer(invocation -> {
                    ChatMessage msg = invocation.getArgument(0);
                    return Mono.just(msg);
                });

        StepVerifier.create(undeliveredMessageService.deliverUndeliveredMessages(receiverId))
                .expectNext(message1, message2)
                .verifyComplete();

        verify(messageDeliveryService).deliverMessage(message1);
        verify(messageDeliveryService).deliverMessage(message2);

    }

}

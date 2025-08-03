package com.example.chatapp.unit.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DistributedMessageDeliveryServiceTest {

    @Mock
    private ServerRegistryService serverRegistry;

    @Mock
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;

    @Mock
    private KafkaTemplate kafkaTemplate;

    @InjectMocks
    private DistributedMessageDeliveryService distributedMessageDeliveryService;

    @BeforeEach
    void setUp() {
        distributedMessageDeliveryService = new DistributedMessageDeliveryService(serverRegistry, "server-1", webSocketMessageDeliveryService, kafkaTemplate);
    }



    @Test
    void should_deliverLocally_whenUserOnSameServer(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());

        when(serverRegistry.findUserServer(message.getReceiverId())).thenReturn(Mono.just("server-1"));
        when(webSocketMessageDeliveryService.deliverMessage(message)).thenReturn(Mono.just(message));
        System.out.println("isCurrentServer result: " + distributedMessageDeliveryService.isCurrentServer("server-1"));

        distributedMessageDeliveryService.deliverMessage(message).block();

        verify(webSocketMessageDeliveryService).deliverMessage(message);
    }


    @Test
    void should_forwardToKafka_whenUserOnDifferentServer(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());

        when(serverRegistry.findUserServer(message.getReceiverId())).thenReturn(Mono.just("server-2"));

        distributedMessageDeliveryService.deliverMessage(message).block();

        verify(kafkaTemplate).send("chat-messages", message.getReceiverId().toString(), message);
        verify(webSocketMessageDeliveryService, never()).deliverMessage(message);

    }
}

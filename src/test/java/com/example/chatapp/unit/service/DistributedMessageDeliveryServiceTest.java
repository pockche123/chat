package com.example.chatapp.unit.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.DistributedMessageDeliveryService;
import com.example.chatapp.service.LocalOnlineUserService;
import com.example.chatapp.service.ServerRegistryService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DistributedMessageDeliveryServiceTest {

    @Mock
    private ServerRegistryService serverRegistry;

    @Mock
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;

    @InjectMocks
    private DistributedMessageDeliveryService distributedMessageDeliveryService;

    @BeforeEach
    void setUp() {
        distributedMessageDeliveryService = new DistributedMessageDeliveryService(serverRegistry, "server-1", webSocketMessageDeliveryService);
    }



    @Test
    void should_deliverLocally_whenUserOnSameServer(){
        ChatMessage message = new ChatMessage();
        message.setReceiverId(UUID.randomUUID());

        when(serverRegistry.findUserServer(message.getReceiverId())).thenReturn(Mono.just("server-1"));

        when(webSocketMessageDeliveryService.deliverMessage(message))
                .thenReturn(Mono.just(message));
        System.out.println("isCurrentServer result: " + distributedMessageDeliveryService.isCurrentServer("server-1"));
        distributedMessageDeliveryService.deliverMessage(message).block();

        verify(webSocketMessageDeliveryService).deliverMessage(message);
    }
}

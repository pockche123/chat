package com.example.chatapp.integration;

import com.example.chatapp.integration.config.CassandraTestConfig;
import com.example.chatapp.integration.config.RedisTestConfig;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.LocalOnlineUserService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Testcontainers
public class LocalOnlineUserServiceIntegrationTest {

    @Container
    static final GenericContainer<?> redis = RedisTestConfig.createRedisContainer();

    @Container
    static final CassandraContainer<?> cassandra = CassandraTestConfig.createCassandraContainer();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        RedisTestConfig.configureRedis(registry, redis);
        CassandraTestConfig.configureCassandra(registry, cassandra);
    }



    @Autowired
    private LocalOnlineUserService localOnlineUserService;

    @Autowired
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;


    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @AfterEach
    void cleanupTestData() {
        // Clear Cassandra tables
        chatMessageRepository.deleteAll().block();
        // Clear Redis
    }


    @Test
    void shouldDeliverUndeliveredMessagesWhenUserComesOnline(){
        // Wait for Cassandra to be ready with more robust check
        UUID userId = UUID.randomUUID();
        ChatMessage undeliveredMessage= createUndeliveredMessage(userId);
        chatMessageRepository.save(undeliveredMessage).block();


        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.send(any())).thenReturn(Mono.empty());
        when(mockSession.textMessage(anyString())).thenReturn(mock(WebSocketMessage.class));
        webSocketMessageDeliveryService.registerSession(userId, mockSession);

//       act
        localOnlineUserService.markUserOnline(userId).block();
//        assert
        ChatMessage deliveredMessage = chatMessageRepository.findByMessageId(undeliveredMessage.getMessageId())
                .block();
        assertEquals(MessageStatus.DELIVERED, deliveredMessage.getStatus());
    }

    private ChatMessage createUndeliveredMessage(UUID receiverId){
        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());
        message.setConversationId(UUID.randomUUID());
        message.setReceiverId(receiverId);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));
        message.setSenderId(UUID.randomUUID());
        message.setStatus(MessageStatus.SENT);
        message.setContent("test message");
        return message;
    }

}

package com.example.chatapp.integration;

import com.example.chatapp.integration.config.CassandraTestConfig;
import com.example.chatapp.integration.config.RedisTestConfig;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.ChatMessageListener;
import com.example.chatapp.service.DistributedOnlineUserService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;



import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Testcontainers
@Slf4j
public class ChatMessageListenerIntegrationTest {

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
    private DistributedOnlineUserService DistributedOnlineUserService;

    @Autowired
    private ChatMessageListener chatMessageListener;

    @Autowired
    private ChatMessageRepository chatMessageRepository;


    @Autowired
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    @Test
    void test_chatMessageListener_deliversMessage(){
        UUID userId = UUID.randomUUID();
        ChatMessage message = createMessage(userId);

        redisTemplate.opsForValue()
                .set("user:server:" + userId, "localhost:8080")
                .block();


        chatMessageRepository.save(message).block();

        DistributedOnlineUserService.markUserOnline(userId).block();
        WebSocketSession mockSession = createSession();



        webSocketMessageDeliveryService.registerSession(userId, mockSession);

        // Act - trigger message processing (now synchronous)
        chatMessageListener.handleKafkaMessage(message);

        // Assert - delivery should be complete
        assertTrue(DistributedOnlineUserService.isUserOnline(userId));
        
        ChatMessage deliveredMessage = chatMessageRepository.findByMessageId(message.getMessageId())
                .blockFirst();
        assertEquals(MessageStatus.DELIVERED, deliveredMessage.getStatus());

    }

    @Test
    void test_chatMessageListener_handlesDeliveryError(){
        UUID userId = UUID.randomUUID();
        ChatMessage message = createMessage(userId);
        chatMessageRepository.save(message).block();

        DistributedOnlineUserService.markUserOnline(userId).block();

        // Create a mock session that will cause delivery to fail
        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.send(any())).thenReturn(Mono.error(new RuntimeException("WebSocket connection failed")));
        when(mockSession.textMessage(anyString())).thenReturn(mock(WebSocketMessage.class));
        webSocketMessageDeliveryService.registerSession(userId, mockSession);

        // Message should remain in SENT status since delivery failed
        ChatMessage failedMessage = chatMessageRepository.findByMessageId(message.getMessageId())
                .blockFirst();
        assertEquals(MessageStatus.SENT, failedMessage.getStatus());
    }

    private ChatMessage createMessage(UUID receiverId){
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

    private WebSocketSession createSession() {
        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.send(any())).thenReturn(Mono.empty());
        when(mockSession.textMessage(anyString())).thenReturn(mock(WebSocketMessage.class));
        return mockSession;
    }

}

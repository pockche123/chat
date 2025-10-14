package com.example.chatapp.integration;

import com.example.chatapp.integration.config.CassandraTestConfig;
import com.example.chatapp.integration.config.RedisTestConfig;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.DistributedMessageDeliveryService;
import com.example.chatapp.service.RedisServerRegistryService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 16, topics = {"chat-messages"})
@Testcontainers
@Slf4j
public class DistributedMessageDeliveryServiceTest {

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
    private ChatMessageRepository chatMessageRepository;


    @Autowired
    private RedisServerRegistryService redisServerRegistryService;

    @MockitoBean
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;


    @Autowired
    private DistributedMessageDeliveryService distributedMessageDeliveryService;

    @Test
    void distributedMessageFlow_routesMessageCorrectlyToLocal(){
        UUID messageId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String serveraddress = "localhost:8080";

        WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.textMessage(anyString())).thenReturn(mock(WebSocketMessage.class));
        when(mockSession.send(any())).thenReturn(Mono.empty());

        ChatMessage message = createUndeliveredMessage(messageId, senderId, receiverId);

        webSocketMessageDeliveryService.registerSession(receiverId, mockSession);
        redisServerRegistryService.registerUserServer(receiverId,  serveraddress).block();

        distributedMessageDeliveryService.deliverMessage(message).block();

        ChatMessage updatedMessage = chatMessageRepository .findByMessageId(messageId)
                .block();

        assertNotNull(updatedMessage);
        assertEquals(MessageStatus.DELIVERED, updatedMessage.getStatus());
    }

    @Test
    void distributedMessageFlow_routesMessagesCorrectlyToDistributed(){
        UUID messageId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        ChatMessage message = createUndeliveredMessage(messageId, senderId, receiverId);
        String serveraddress = "localhost:8082";


        redisServerRegistryService.registerUserServer(receiverId,  serveraddress).block();
        distributedMessageDeliveryService.deliverMessage(message).block();

        verify(kafkaTemplate).send(eq("chat-messages"), anyInt(), eq(receiverId.toString()), eq(message));
    }



    private ChatMessage createUndeliveredMessage(UUID messageId, UUID senderId, UUID receiverId){
        ChatMessage message = new ChatMessage();
        message.setMessageId(messageId);
        message.setConversationId(UUID.randomUUID());
        message.setReceiverId(receiverId);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));
        message.setSenderId(senderId);
        message.setStatus(MessageStatus.SENT);
        message.setContent("test message");
        return message;
    }


}

package com.example.chatapp.unit.respository;



import com.example.chatapp.integration.config.CassandraTestConfig;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@DataCassandraTest
@Testcontainers
public class ChatMessageRepositoryTest {

    @Container
    static final CassandraContainer<?> cassandra = CassandraTestConfig.createCassandraContainer();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        CassandraTestConfig.configureCassandra(registry, cassandra);
    }

    @Autowired
    private ChatMessageRepository chatMessageRepository;


    @Test
    void test_findbyConversationId_from_repo(){
//        Arrange
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        ChatMessage msg = new ChatMessage(messageId, new Timestamp(System.currentTimeMillis()), conversationId, "Hello", senderId, receiverId, MessageStatus.SENT);

//        ACT
        chatMessageRepository.save(msg).block();
       ChatMessage message= chatMessageRepository
                .findByMessageId(messageId)
                .block();
//      ASSERT

        assertEquals(msg.getContent(), message.getContent());

    }
}
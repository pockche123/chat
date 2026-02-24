package com.example.chatapp.integration;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.integration.config.CassandraTestConfig;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.DirectConversation;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.repository.DirectConversationRepository;
import com.example.chatapp.repository.GroupRepository;
import com.example.chatapp.service.ChatMessageService;
import com.example.chatapp.service.DirectConversationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Testcontainers
@Slf4j
public class ChatMessageServiceIntegrationTest {

    @Container
    static final CassandraContainer<?> cassandra = CassandraTestConfig.createCassandraContainer();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        CassandraTestConfig.configureCassandra(registry, cassandra);
    }

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageService chatMessageService;



    @Test
    void processIncomingMessage_handles_read_receipt(){

        UUID receiverId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();
        incomingMessageDTO.setContent("Hello World!");
        incomingMessageDTO.setType("read_receipt");
        incomingMessageDTO.setMessageId(messageId);
        incomingMessageDTO.setConversationId(conversationId);

        UUID senderId = UUID.randomUUID();

        ChatMessage messageDelivered = new ChatMessage();
        messageDelivered.setMessageId(messageId);
        messageDelivered.setStatus(MessageStatus.DELIVERED);
        messageDelivered.setConversationId(conversationId);
        messageDelivered.setContent("Hello World!");
        messageDelivered.setReceiverId(receiverId);
        messageDelivered.setSenderId(senderId);
        messageDelivered.setTimestamp(new Timestamp(System.currentTimeMillis()));

        chatMessageRepository.save(messageDelivered).block();

        ChatMessage message = chatMessageService.processIncomingMessage(receiverId, incomingMessageDTO).blockFirst();

        assertNotNull(message);
        assertEquals(MessageStatus.READ, message.getStatus());

    }


}

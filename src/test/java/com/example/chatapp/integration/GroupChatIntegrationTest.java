package com.example.chatapp.integration;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.integration.config.CassandraTestConfig;
import com.example.chatapp.integration.config.RedisTestConfig;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.Group;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.GroupRepository;
import com.example.chatapp.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Testcontainers
@Slf4j
public class GroupChatIntegrationTest {


    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    void sendsMessagesToAllPeopleInTheGroup(){
        String content = "Hello";
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId1 = UUID.randomUUID();
        UUID receiverId2 = UUID.randomUUID();
        List<UUID> uuids = Arrays.asList(receiverId1, receiverId2);

        IncomingMessageDTO incomingMessageDTO= new IncomingMessageDTO("message", content, conversationId, messageId);

        Group group = new Group();
        group.setConversationId(conversationId);
        group.setMemberIds(uuids);
        groupRepository.save(group).block();



        StepVerifier.create(chatMessageService.processIncomingMessage(senderId, incomingMessageDTO))
                .recordWith(ArrayList::new)
                .expectNextCount(2)
                .consumeRecordedWith(messages -> {
                    messages.forEach(message -> {
                        assertTrue(uuids.contains(message.getReceiverId()));
                        assertEquals(MessageStatus.SENT, message.getStatus());
                        assertEquals("Hello", message.getContent());
                    });
                })
          .verifyComplete();


    }

}

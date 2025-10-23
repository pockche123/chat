package com.example.chatapp.integration;

import com.example.chatapp.model.DirectConversation;
import com.example.chatapp.repository.DirectConversationRepository;
import com.example.chatapp.service.ConversationService;
import com.example.chatapp.service.DirectConversationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Slf4j
public class ConversationServiceIntegrationTest {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private DirectConversationRepository directConversationRepository;


    @Test
    void should_returnReceiverFromDirect_whenConversationIsNotInGroup(){
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String content = "Hello world!";
        UUID messageId = UUID.randomUUID();

        DirectConversation direct = new DirectConversation();
        direct.setConversationId(conversationId);
        direct.setParticipant1(senderId);
        direct.setParticipant2(receiverId);
        directConversationRepository.save(direct).block();

        List<UUID> receivers = conversationService.getReceivers(conversationId, senderId).block();

        assertEquals(1, receivers.size());
        assertEquals(receiverId, receivers.get(0));
    }
}

package com.example.chatapp.integration;

import com.example.chatapp.dto.IncomingMessageDTO;
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
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Slf4j
public class ChatMessageServiceIntegrationTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private DirectConversationRepository directConversationRepository;


//    @Test
//    void processIncomingMessage_handles_read_receipt(){
//
//        UUID receiverId = UUID.randomUUID();
//        UUID messageId = UUID.randomUUID();
//        UUID conversationId = UUID.randomUUID();
//
//        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();
//        incomingMessageDTO.setContent("Hello World!");
//        incomingMessageDTO.setType("read_receipt");
//        incomingMessageDTO.setReceiverId(receiverId);
//        incomingMessageDTO.setMessageId(messageId);
//        incomingMessageDTO.setConversationId(conversationId);
//
//        UUID senderId = UUID.randomUUID();
//
//        ChatMessage messageDelivered = new ChatMessage();
//        messageDelivered.setMessageId(messageId);
//        messageDelivered.setStatus(MessageStatus.DELIVERED);
//        messageDelivered.setConversationId(conversationId);
//        messageDelivered.setContent("Hello World!");
//        messageDelivered.setReceiverId(receiverId);
//        messageDelivered.setSenderId(senderId);
//        messageDelivered.setTimestamp(new Timestamp(System.currentTimeMillis()));
//
//        chatMessageRepository.save(messageDelivered).block();
//
//        ChatMessage message = chatMessageService.processIncomingMessage(senderId, incomingMessageDTO).blockFirst();
//
//        assertNotNull(message);
//        assertEquals(MessageStatus.READ, message.getStatus());
//
//    }

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

        List<UUID> receivers = chatMessageService.getReceivers(conversationId, senderId).block();

        assertEquals(1, receivers.size());
        assertEquals(receiverId, receivers.get(0));
    }
}

package com.example.chatapp.integration;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.OnlineUserService;
import com.example.chatapp.service.UndeliveredMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.sql.Timestamp;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class OnlineUserServiceIntegrationTest {

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private UndeliveredMessageService  undeliveredMessageService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void shouldDeliverUndeliveredMessagesWhenUserComesOnline(){
        UUID userId = UUID.randomUUID();
        ChatMessage undeliveredMessage= createUndeliveredMessage(userId);
        chatMessageRepository.save(undeliveredMessage).block();

        onlineUserService.markUserOnline(userId);


        StepVerifier.create(undeliveredMessageService.deliverUndeliveredMessage(userId))
                .expectNextCount(1)
                .verifyComplete();
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

package com.example.chatapp.unit.respository;



import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@DataCassandraTest
public class ChatMessageRepositoryTest {

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
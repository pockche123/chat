package com.example.chatapp.respository;


import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        ChatMessage msg = new ChatMessage(messageId, conversationId, new Timestamp(System.currentTimeMillis()), "Hello", senderId, receiverId);

//        ACT
        chatMessageRepository.save(msg).block();
        List<ChatMessage> messages = chatMessageRepository
                .findByMessageId(messageId)
                .collectList()
                .block();
//      ASSERT
        assertEquals(1, messages.size());
//        assertEquals(msg.getContent(), messages.getFirst().getContent());

    }
}

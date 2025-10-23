package com.example.chatapp.unit.service;

import com.example.chatapp.model.DirectConversation;
import com.example.chatapp.repository.DirectConversationRepository;
import com.example.chatapp.service.DirectConversationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DirectConversationServiceTest {

    @Mock
    private DirectConversationRepository directConversationRepository;

    @InjectMocks
    private DirectConversationService directConversationService;

    @Test
    void test_getOtherParticipant_returnsUUID(){
        UUID convoId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        DirectConversation conversation = new DirectConversation();
        conversation.setConversationId(convoId);
        conversation.setParticipant1(senderId);
        conversation.setParticipant2(receiverId);
        when(directConversationRepository.findById(convoId)).thenReturn(Mono.just(conversation));

        UUID actualId = directConversationService.getOtherParticipant(convoId, senderId).block();

        assertEquals(receiverId, actualId);
    }
}

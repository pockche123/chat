package com.example.chatapp.unit.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.Group;
import com.example.chatapp.repository.GroupRepository;
import com.example.chatapp.service.ConversationService;
import com.example.chatapp.service.DirectConversationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConversationServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private DirectConversationService directConversationService;

    @InjectMocks
    private ConversationService conversationService;


    @Test
    void should_returnReceiversFromGroup_whenConversationIsGroup(){
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        String content = "Hello world!";
        UUID messageId = UUID.randomUUID();

        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();
        incomingMessageDTO.setConversationId(conversationId);
        incomingMessageDTO.setMessageId(messageId);
        incomingMessageDTO.setContent(content);
        UUID mockReceiverId = UUID.randomUUID();
        UUID mockReceiverId2 = UUID.randomUUID();

        Group mockGroup = new Group();
        mockGroup.setMemberIds(List.of(senderId, mockReceiverId, mockReceiverId2));
        mockGroup.setConversationId(conversationId);

        when(groupRepository.findById(conversationId)).thenReturn(Mono.just(mockGroup));
        when(directConversationService.getOtherParticipant(any(), any()))
                .thenReturn(Mono.empty());


        Mono<List<UUID>> actual= conversationService.getReceivers(conversationId, senderId);
        List<UUID> actualList = actual.block();
        assertNotNull(actual);
        assertEquals(2, actualList.size());
        assertTrue(actualList.contains(mockReceiverId));
        assertTrue(actualList.contains(mockReceiverId2));
    }

    @Test
    void should_makeCallToDirectConversationService_whenGroupFindByIdIsNull(){
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        when(directConversationService.getOtherParticipant(conversationId, senderId)).thenReturn(Mono.just(receiverId));

        when(groupRepository.findById(conversationId)).thenReturn(Mono.empty());

        List<UUID> actual= conversationService.getReceivers(conversationId, senderId).block();
        assertNotNull(actual);
        verify(directConversationService).getOtherParticipant(conversationId, senderId);

    }




}

package com.example.chatapp.unit.service;

import com.example.chatapp.dto.GroupRequestDTO;
import com.example.chatapp.dto.GroupResponseDTO;
import com.example.chatapp.model.Group;
import com.example.chatapp.repository.GroupRepository;
import com.example.chatapp.service.GroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {
    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    @Test
    void createGroup_shouldReturnGroupObjectWithIdandMetadata(){
        UUID convoId = UUID.randomUUID();
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();
        UUID member3 = UUID.randomUUID();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Group group = new Group();
        group.setConversationId(convoId);
        group.setMemberIds(Arrays.asList(member1, member2, member3));
        group.setName("Monkey boys");
        group.setAdminId(member1);
        group.setCreatedAt(now);

        GroupRequestDTO groupRequestDTO = new GroupRequestDTO( Arrays.asList(member1, member2, member3), "Monkey boys", member1);


        Mockito.when(groupRepository.save(any())).thenReturn(Mono.just(group));

        GroupResponseDTO actual = groupService.createGroup(groupRequestDTO).block();


        assertNotNull(actual);
    }

    @Test
    void addMemberToGroup_addsItToGroup(){

        UUID convoId = UUID.randomUUID();
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();
        UUID member3 = UUID.randomUUID();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Group group = new Group();
        group.setConversationId(convoId);
        group.setMemberIds(new ArrayList<>(Arrays.asList(member1, member2)));
        group.setName("Monkey boys");
        group.setAdminId(member1);
        group.setCreatedAt(now);

        Mockito.when(groupRepository.findById(convoId)).thenReturn(Mono.just(group));
        Mockito.when(groupRepository.save(any())).thenReturn(Mono.just(group));

        StepVerifier.create(groupService.addMemberToGroup(convoId, member3)).verifyComplete();

        assertTrue(group.getMemberIds().contains(member3));
    }



    @Test
    void removeMemberFromGroup_removesItFromGroup(){

        UUID convoId = UUID.randomUUID();
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();
        UUID member3 = UUID.randomUUID();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Group group = new Group();
        group.setConversationId(convoId);
        group.setMemberIds(new ArrayList<>(Arrays.asList(member1, member2, member3)));
        group.setName("Monkey boys");
        group.setAdminId(member1);
        group.setCreatedAt(now);

        Mockito.when(groupRepository.findById(convoId)).thenReturn(Mono.just(group));
        Mockito.when(groupRepository.save(any())).thenReturn(Mono.just(group));

        StepVerifier.create(groupService.removeMemberFromGroup(convoId, member3)).verifyComplete();

        assertFalse(group.getMemberIds().contains(member3));
    }

}

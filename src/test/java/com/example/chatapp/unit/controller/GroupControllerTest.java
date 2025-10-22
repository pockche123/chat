package com.example.chatapp.unit.controller;

import com.example.chatapp.controller.GroupController;
import com.example.chatapp.dto.GroupRequestDTO;
import com.example.chatapp.dto.GroupResponseDTO;
import com.example.chatapp.model.Group;
import com.example.chatapp.service.GroupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    @Test
    void postGroup_addsToGroup(){
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


        GroupResponseDTO groupResponseDTO = new GroupResponseDTO(convoId, Arrays.asList(member1, member2, member3), "Monkey boys", member1);

        when(groupService.createGroup(groupRequestDTO)).thenReturn(Mono.just(groupResponseDTO));


        ResponseEntity<GroupResponseDTO> result = groupController.createGroup(groupRequestDTO).block();
        assertEquals(HttpStatus.CREATED, result.getStatusCode());

    }
}

package com.example.chatapp.controller;

import com.example.chatapp.dto.GroupRequestDTO;
import com.example.chatapp.dto.GroupResponseDTO;
import com.example.chatapp.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public Mono<ResponseEntity<GroupResponseDTO>> createGroup(@RequestBody GroupRequestDTO request) {
        return groupService.createGroup(request)
                .map(group -> ResponseEntity.status(HttpStatus.CREATED).body(group));
    }
}

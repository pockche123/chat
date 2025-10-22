package com.example.chatapp.service;

import com.example.chatapp.dto.GroupDTO;
import com.example.chatapp.model.Group;
import com.example.chatapp.repository.GroupRepository;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class GroupService {
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Mono<Group> createGroup(GroupDTO groupDTO) {

        Group group = new Group();
        group.setConversationId(UUID.randomUUID());
        group.setMemberIds(groupDTO.getMemberIds());
        group.setAdminId(groupDTO.getAdminId());
        group.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        return groupRepository.save(group);
    }

    public Mono<Void> addMemberToGroup(UUID id, UUID member3) {
        return groupRepository.findById(id)
                .flatMap(group -> {
                    group.getMemberIds().add(member3);
                    return groupRepository.save(group);
                }).then();
    }

    public Mono<Void> removeMemberFromGroup(UUID id, UUID member3) {
        return groupRepository.findById(id)
                .flatMap(group -> {
                    group.getMemberIds().remove(member3);
                    return groupRepository.save(group);
                }).then();
    }
}

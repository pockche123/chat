package com.example.chatapp.service;

import com.example.chatapp.repository.GroupRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final GroupRepository groupRepository;
    private final DirectConversationService directConversationService;

    public ConversationService(GroupRepository groupRepository, DirectConversationService directConversationService) {
        this.groupRepository = groupRepository;
        this.directConversationService = directConversationService;
    }


    public Mono<List<UUID>> getReceivers(UUID conversationId, UUID currentUserId) {
        return groupRepository.findById(conversationId)
                .map(group ->
                        group.getMemberIds().stream()
                                .filter(memberId -> !memberId.equals(currentUserId))
                                .collect(Collectors.toList())
                )
                .switchIfEmpty(
                        directConversationService.getOtherParticipant(conversationId, currentUserId)
                                .map(List::of)
                );
    }

}

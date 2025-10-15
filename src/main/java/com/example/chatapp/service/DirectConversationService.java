package com.example.chatapp.service;

import com.example.chatapp.repository.DirectConversationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class DirectConversationService {
    private final DirectConversationRepository directConversationRepository;

    public DirectConversationService(DirectConversationRepository directConversationRepository) {
        this.directConversationRepository = directConversationRepository;
    }

    public Mono<UUID> getOtherParticipant(UUID conversationId, UUID senderId) {
                return directConversationRepository.findById(conversationId)
                        .map(convo -> convo.getParticipant1().equals(senderId)? convo.getParticipant2():  convo.getParticipant1());
    }
}

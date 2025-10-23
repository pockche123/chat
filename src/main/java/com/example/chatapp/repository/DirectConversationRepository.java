package com.example.chatapp.repository;

import com.example.chatapp.model.DirectConversation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

public interface DirectConversationRepository extends ReactiveMongoRepository<DirectConversation, UUID> {
}

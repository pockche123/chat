package com.example.chatapp.repository;

import com.example.chatapp.model.Group;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Repository
public interface GroupRepository extends ReactiveMongoRepository<Group, UUID> {



}

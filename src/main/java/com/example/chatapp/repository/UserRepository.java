package com.example.chatapp.repository;

import com.example.chatapp.model.User;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, UUID> {
    Mono<User> findByUsername(String username);

    Mono<User> findByProviderAndProviderId(String provider, String providerId);

}

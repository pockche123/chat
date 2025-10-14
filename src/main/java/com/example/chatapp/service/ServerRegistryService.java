package com.example.chatapp.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ServerRegistryService {

    Mono<Void> registerUserServer(UUID userId, String serverAddress);
    Mono<Void> unregisterUser(UUID userId);
    Mono<String> findUserServer(UUID userId);
}

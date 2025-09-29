package com.example.chatapp.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ServerRegistryService {
    Mono<String> findUserServer(UUID userId);
}

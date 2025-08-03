package com.example.chatapp.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RedisServerRegistryService implements  ServerRegistryService {
    @Override
    public Mono<String> findUserServer(UUID userId) {
        return null;
    }
}

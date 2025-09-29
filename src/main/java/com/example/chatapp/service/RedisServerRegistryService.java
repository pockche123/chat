package com.example.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RedisServerRegistryService implements  ServerRegistryService {
    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;
    @Override
    public Mono<String> findUserServer(UUID userId) {
        return redisTemplate.opsForValue()
                .get("user:server:" + userId)
                .switchIfEmpty(Mono.just("localhost:8080"));
    }
}

package com.example.chatapp.service;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class RedisServerRegistryService implements  ServerRegistryService {
    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> registerUserServer(UUID userId, String serverAddress) {
        return  redisTemplate.opsForValue()
                .set("user:server:" + userId, serverAddress)
                .doOnSuccess(ignored -> log.info("Registered user: {} ", userId))
                .then();
    }

    @Override
    public Mono<Void> unregisterUser(UUID userId) {
        return redisTemplate.delete("user:server:" + userId)
                .doOnSuccess(ignored -> log.info("Unregistered user: {} ", userId))
                .then();
    }

    @Override
    public Mono<String> findUserServer(UUID userId) {
        return redisTemplate.opsForValue()
                .get("user:server:" + userId)
                .switchIfEmpty(Mono.just("localhost:8080"));
    }
}

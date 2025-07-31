package com.example.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class DistributedOnlineUserService implements OnlineUserService{


    private final ReactiveStringRedisTemplate sharedRedisTemplate;


    private final UndeliveredMessageService undeliveredMessageService;

    public DistributedOnlineUserService(ReactiveStringRedisTemplate sharedRedisTemplate, UndeliveredMessageService undeliveredMessageService) {
        this.sharedRedisTemplate = sharedRedisTemplate;
        this.undeliveredMessageService = undeliveredMessageService;
    }

    @Override
    public Mono<Void> markUserOnline(UUID userId) {
        return isUserOnlineReactive(userId)
                .flatMap(wasOnline -> {
                    return sharedRedisTemplate.opsForSet()
                            .add("online_users", userId.toString())
                            .then(!wasOnline ? undeliveredMessageService.deliverUndeliveredMessages(userId).then() : Mono.empty());
                        });
    }

    @Override
    public boolean isUserOnline(UUID userId) {
        return isUserOnlineReactive(userId).block();
    }

    @Override
    public void markUserOffline(UUID userId) {

    }

    public Mono<Boolean> isUserOnlineReactive(UUID userId){
        return sharedRedisTemplate.opsForSet()
                .isMember("online_users", userId.toString());
    }
}

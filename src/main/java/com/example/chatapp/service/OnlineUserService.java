package com.example.chatapp.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OnlineUserService {
     Mono<Void> markUserOnline(UUID userId);
     boolean isUserOnline(UUID userId);
    void markUserOffline(UUID userId);


}

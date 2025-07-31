package com.example.chatapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OnlineUserService {

    private final Set<UUID> onlineChecker = ConcurrentHashMap.newKeySet();

    @Autowired
    private UndeliveredMessageService undeliveredMessageService;

    /**
     * Mark a user as online and trigger delivery of any unread messages
     */
    public Mono<Void> markUserOnline(UUID userId) {
        boolean wasOffline = !isUserOnline(userId);
        onlineChecker.add(userId);
        log.info("User was offline?: {}", wasOffline);
        // If the user was previously offline, deliver any unread messages
        return wasOffline
                ? undeliveredMessageService.deliverUndeliveredMessages(userId).then()
                : Mono.empty();
    }

    public boolean isUserOnline(UUID userId) {
        return onlineChecker.contains(userId);
    }

    public void markUserOffline(UUID userId) {
        onlineChecker.remove(userId);
    }
}

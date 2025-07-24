package com.example.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    private final Set<UUID> onlineChecker = ConcurrentHashMap.newKeySet();

    @Autowired
    private UndeliveredMessageService undeliveredMessageService;

    /**
     * Mark a user as online and trigger delivery of any unread messages
     */
    public void markUserOnline(UUID userId) {
        boolean wasOffline = !isUserOnline(userId);
        onlineChecker.add(userId);

        // If the user was previously offline, deliver any unread messages
        if (wasOffline) {
            undeliveredMessageService.deliverUndeliveredMessage(userId);
        }
    }

    public boolean isUserOnline(UUID userId) {
        return onlineChecker.contains(userId);
    }

    public void markUserOffline(UUID userId) {
        onlineChecker.remove(userId);
    }
}

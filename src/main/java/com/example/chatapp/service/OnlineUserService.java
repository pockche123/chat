package com.example.chatapp.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {

    private final Set<UUID> onlineChecker = ConcurrentHashMap.newKeySet();

    public void markUserOnline(UUID userId) {
        onlineChecker.add(userId);

    }

    public boolean isUserOnline(UUID userId) {
        return onlineChecker.contains(userId);
    }

    public void markUserOffline(UUID userId) {
        onlineChecker.remove(userId);
    }
}

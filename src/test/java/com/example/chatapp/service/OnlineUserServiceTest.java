package com.example.chatapp.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.UUID;

public class OnlineUserServiceTest {

    @InjectMocks
    private OnlineUserService onlineUserService;

    @Test
    void test_shouldMarkUserOnlineWhenConnected(){
//        Given
        UUID userId  = UUID.randomUUID();

        //WHen
        onlineUserService.markUserOnline(userId);
    }
}

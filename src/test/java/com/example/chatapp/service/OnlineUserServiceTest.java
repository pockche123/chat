package com.example.chatapp.service;

import com.example.chatapp.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class OnlineUserServiceTest {

    @InjectMocks
    private OnlineUserService onlineUserService;

    @Test
    void test_shouldMarkUserOnlineWhenConnected(){
//        Given
        UUID userId  = UUID.randomUUID();
        User testUser = new User();
        testUser.setUserId(userId);

        //WHen
        onlineUserService.markUserOnline(userId);

//    Then
        assertTrue(onlineUserService.isUserOnline(userId));
    }

    @Test
    void test_shouldMarkUserOfflineWhenConnected(){
        //        Given
        UUID userId  = UUID.randomUUID();
        User testUser = new User();
        testUser.setUserId(userId);

        //WHen
        onlineUserService.markUserOffline(userId);

//    Then
        assertFalse(onlineUserService.isUserOnline(userId));
    }
}

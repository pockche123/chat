package com.example.chatapp.unit.service;

import com.example.chatapp.model.User;
import com.example.chatapp.service.OnlineUserService;
import com.example.chatapp.service.UndeliveredMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OnlineUserServiceTest {

    @InjectMocks
    private OnlineUserService onlineUserService;

    @Mock
    private UndeliveredMessageService undeliveredMessageService;


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
        verify(undeliveredMessageService).deliverUndeliveredMessage(userId);
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

    @Test
    void test_whenUserOnline_shouldTriggerHandleUndeliveredMessage(){
        //        Given
        UUID userId  = UUID.randomUUID();
        User testUser = new User();
        testUser.setUserId(userId);

        //WHen
        onlineUserService.markUserOnline(userId);

        verify(undeliveredMessageService).deliverUndeliveredMessage(userId);
    }
}

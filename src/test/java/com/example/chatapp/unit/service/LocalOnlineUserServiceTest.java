package com.example.chatapp.unit.service;

import com.example.chatapp.model.User;
import com.example.chatapp.service.LocalOnlineUserService;
import com.example.chatapp.service.UndeliveredMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocalOnlineUserServiceTest {

    @InjectMocks
    private LocalOnlineUserService LocalOnlineUserService;

    @Mock
    private UndeliveredMessageService undeliveredMessageService;


    @Test
    void test_shouldMarkUserOnlineWhenConnected(){
//        Given
        UUID userId  = UUID.randomUUID();
        User testUser = new User();
        testUser.setUserId(userId);

        when(undeliveredMessageService.deliverUndeliveredMessages(userId))
                .thenReturn(Flux.empty());

        //WHen
        LocalOnlineUserService.markUserOnline(userId).block();

//    Then
        assertTrue(LocalOnlineUserService.isUserOnline(userId));
        verify(undeliveredMessageService).deliverUndeliveredMessages(userId);
    }

    @Test
    void test_shouldMarkUserOfflineWhenConnected(){
        //        Given
        UUID userId  = UUID.randomUUID();
        User testUser = new User();
        testUser.setUserId(userId);

        //WHen
        LocalOnlineUserService.markUserOffline(userId);

//    Then
        assertFalse(LocalOnlineUserService.isUserOnline(userId));
    }

    @Test
    void test_whenUserOnline_shouldTriggerHandleUndeliveredMessage(){
        //        Given
        UUID userId  = UUID.randomUUID();
        User testUser = new User();
        testUser.setUserId(userId);
        when(undeliveredMessageService.deliverUndeliveredMessages(userId))
                .thenReturn(Flux.empty());

        //WHen
        LocalOnlineUserService.markUserOnline(userId).block();

        verify(undeliveredMessageService).deliverUndeliveredMessages(userId);
    }
}

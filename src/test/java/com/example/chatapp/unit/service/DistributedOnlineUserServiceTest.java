package com.example.chatapp.unit.service;

import com.example.chatapp.service.DistributedOnlineUserService;
import com.example.chatapp.service.UndeliveredMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DistributedOnlineUserServiceTest {

//    @Mock
//    private ReactiveStringRedisTemplate sharedRedisTemplate;
//
//    @Mock
//    private UndeliveredMessageService undeliveredMessageService;
//
//    @InjectMocks
//    private DistributedOnlineUserService distributedOnlineUserService;

    @Test
    void should_share_online_status_with_distributed_service(){
        ReactiveStringRedisTemplate sharedRedisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveSetOperations<String, String> setOps = mock(ReactiveSetOperations.class);
        UndeliveredMessageService undeliveredMessageService = mock(UndeliveredMessageService.class);

        UUID userId = UUID.randomUUID();

        when(sharedRedisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember("online_users", userId.toString())).thenReturn(Mono.just(false), Mono.just(true));
//        1L means added successfully. 0L means already exists.
        when(setOps.add("online_users", userId.toString())).thenReturn(Mono.just(1L));
        when(undeliveredMessageService.deliverUndeliveredMessages(any())).thenReturn(Flux.empty());

        DistributedOnlineUserService server1 = new DistributedOnlineUserService(sharedRedisTemplate, undeliveredMessageService);
        DistributedOnlineUserService server2 = new DistributedOnlineUserService(sharedRedisTemplate, undeliveredMessageService);

//        Act- Server 1 marks user online
        server1.markUserOnline(userId).block();

        assertTrue(server2.isUserOnline(userId));
    }

    @Test
    void should_mark_user_offline_across_servers(){
        ReactiveStringRedisTemplate sharedRedisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveSetOperations<String, String> setOps = mock(ReactiveSetOperations.class);
        UndeliveredMessageService undeliveredService = mock(UndeliveredMessageService.class);

        UUID userId = UUID.randomUUID();

        when(sharedRedisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.remove("online_users", userId.toString())).thenReturn(Mono.just(1L));
        when(setOps.isMember("online_users", userId.toString())).thenReturn(Mono.just(false));

        DistributedOnlineUserService server1 = new DistributedOnlineUserService(sharedRedisTemplate, undeliveredService);
        DistributedOnlineUserService server2 = new DistributedOnlineUserService(sharedRedisTemplate, undeliveredService);

        server1.markUserOffline(userId);
        assertFalse(server2.isUserOnline(userId));


    }

}

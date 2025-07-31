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

}

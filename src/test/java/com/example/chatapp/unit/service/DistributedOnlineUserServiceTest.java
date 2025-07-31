package com.example.chatapp.unit.service;

import com.example.chatapp.service.UndeliveredMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DistributedOnlineUserServiceTest {

    @Test
    void should_share_online_status_with_distributed_service(){
        ReactiveStringRedisTemplate sharedRedisTemplate = mock(ReactiveStringRedisTemplate.class);
        ReactiveSetOperations<String, String> setOps = mock(ReactiveSetOperations.class);
        UndeliveredMessageService undeliveredService = mock(UndeliveredMessageService.class);

        UUID userId = UUID.randomUUID();

        when(sharedRedisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember("online_users", userId.toString())).thenReturn(Mono.just(false), Mono.just(true));


    }

}

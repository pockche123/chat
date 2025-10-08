package com.example.chatapp.unit.service;

import com.example.chatapp.service.RedisServerRegistryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisServerRegistryServiceTest {
    @Mock
    private ReactiveStringRedisTemplate  redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    @InjectMocks
    private RedisServerRegistryService redisServerRegistryService;

    @Test
    void registerUserServer_storesUserServerMapping(){
        // Given
        UUID userId = UUID.randomUUID();
        String serverAddress = "server1:8080";

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.set(anyString(), anyString())).thenReturn(Mono.just(true));

        // When
        StepVerifier.create(redisServerRegistryService.registerUserServer(userId, serverAddress))
                .verifyComplete();

        // Then
        verify(valueOps).set("user:server:" + userId, serverAddress);
    }

    @Test
    void findUserServer_retrievesUserServerAddress(){
        // Given
        UUID userId = UUID.randomUUID();
        String serverAddress = "server1:8080";

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(Mono.just(serverAddress));

        // When
        String actual = redisServerRegistryService.findUserServer(userId).block();

        // Then
        verify(valueOps).get("user:server:" + userId);
        assertEquals(serverAddress, actual);
    }

    @Test
    void unregisterUser_removesUserFromServer(){

        UUID userId = UUID.randomUUID();
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));

        StepVerifier.create(redisServerRegistryService.unregisterUser(userId))
                .verifyComplete();

        verify(redisTemplate).delete("user:server:" + userId);

    }


}

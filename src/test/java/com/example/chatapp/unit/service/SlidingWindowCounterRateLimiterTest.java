package com.example.chatapp.unit.service;

import com.example.chatapp.service.SlidingWindowCounterRateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SlidingWindowCounterRateLimiterTest {

    @Mock
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;


    @InjectMocks
    private SlidingWindowCounterRateLimiter rateLimiter;

    @Test
    void isAllowed_returnsFalse_whenEstimatedCountIsLessThanMaxRequests(){
        String key = "testKey";
        int maxRequests = 5;
        Duration windowSize = Duration.ofHours(1);

        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);

        when(valueOps.get(anyString()))
                .thenReturn(Mono.just("1"))  // First call (current window)
                .thenReturn(Mono.just("5"));  // Second call (previous window)



        StepVerifier.create(rateLimiter.isAllowed(key, maxRequests, windowSize))
                .expectNext(false)
                .verifyComplete();




    }
}

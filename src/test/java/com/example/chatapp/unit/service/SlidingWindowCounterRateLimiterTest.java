package com.example.chatapp.unit.service;

import com.example.chatapp.service.SlidingWindowCounterRateLimiter;
import org.junit.jupiter.api.BeforeEach;
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


    @BeforeEach
    void setUp() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void isAllowed_returnsFalse_whenRateLimitExceeded() {
        String key = "test-key";
        int maxRequests = 10;
        Duration windowSize = Duration.ofMinutes(1);

        when(valueOps.get(anyString())).thenReturn(Mono.just("8"));

        StepVerifier.create(rateLimiter.isAllowed(key, maxRequests, windowSize))
                .expectNext(false)
                .verifyComplete();
    }



    @Test
    void isAllowed_returnsTrue_whenEstimatedCountIsGreaterThanMaxRequests(){
        String key = "testKey";
        int maxRequests = 5;
        Duration windowSize = Duration.ofHours(1);

        when(valueOps.get(anyString()))
                .thenReturn(Mono.just("1"))  // First call (current window)
                .thenReturn(Mono.just("4"));

        when(valueOps.increment(anyString()))
                .thenReturn(Mono.just(1L));

        when(reactiveRedisTemplate.expire(anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(rateLimiter.isAllowed(key, maxRequests, windowSize))
                .expectNext(true)
                .verifyComplete();
    }
}

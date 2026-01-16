package com.example.chatapp.service;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
public class SlidingWindowCounterRateLimiter {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public SlidingWindowCounterRateLimiter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isAllowed(String key, int maxRequests, Duration windowSize) {
        long now = Instant.now().toEpochMilli();
        long windowSizeMs = windowSize.toMillis();

        long currentWindow = now / windowSizeMs;
        long previousWindow = currentWindow - 1;

        String currentKey = key + ":" + currentWindow;
        String previousKey = key + ":" + previousWindow;

        return Mono.zip(
                redisTemplate.opsForValue().get(currentKey).defaultIfEmpty("0"),
                redisTemplate.opsForValue().get(previousKey).defaultIfEmpty("0")
        ).flatMap(tuple -> {
            int currentCount = Integer.parseInt(tuple.getT1());
            int previousCount = Integer.parseInt(tuple.getT2());

            double overlap = (double)(now % windowSizeMs) / windowSizeMs;
            double estimatedCount = currentCount + (previousCount * (1 - overlap));

            if (estimatedCount < maxRequests) {
                return redisTemplate.opsForValue()
                        .increment(currentKey)
                        .then(redisTemplate.expire(currentKey, windowSize.plusSeconds(60)))
                        .thenReturn(true);
            }
            return Mono.just(false);
        });
    }
}

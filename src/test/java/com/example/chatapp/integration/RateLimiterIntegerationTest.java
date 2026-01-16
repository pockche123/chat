package com.example.chatapp.integration;

import com.example.chatapp.integration.config.RedisTestConfig;
import com.example.chatapp.service.SlidingWindowCounterRateLimiter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Testcontainers
public class RateLimiterIntegerationTest {

    @Container
    static GenericContainer<?> redis = RedisTestConfig.createRedisContainer();

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        RedisTestConfig.configureRedis(registry, redis);
    }

    @Autowired
    private SlidingWindowCounterRateLimiter rateLimiter;

    @Test
    void rateLimiter_allowsRequestsUnderLimit(){
        String key = "test:user:" + UUID.randomUUID();

        for(int i=0; i< 5; i++){
            StepVerifier.create(rateLimiter.isAllowed(key, 5, Duration.ofMinutes(1)))
                    .expectNext(true)
                    .verifyComplete();
        }

        // 6th request should fail
        StepVerifier.create(rateLimiter.isAllowed(key, 5, Duration.ofMinutes(1)))
                .expectNext(false)
                .verifyComplete();

    }



}

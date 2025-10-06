package com.example.chatapp.integration.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import java.time.Duration;

public class RedisTestConfig {
    
    public static GenericContainer<?> createRedisContainer() {
        return new GenericContainer<>("redis:alpine")
                .withExposedPorts(6379)
                .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2));
    }
    
    public static void configureRedis(DynamicPropertyRegistry registry, GenericContainer<?> redis) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
}

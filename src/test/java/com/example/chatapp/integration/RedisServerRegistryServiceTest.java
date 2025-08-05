package com.example.chatapp.integration;

import com.example.chatapp.service.RedisServerRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
@Testcontainers
public class RedisServerRegistryServiceTest {


    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:alpine")
            .withExposedPorts(6379);


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private RedisServerRegistryService serverRegistryService;

    @Test
    void findUserServer_returnsMonoWithServerInstance() {
        UUID userId = UUID.randomUUID();

        StepVerifier.create(serverRegistryService.findUserServer(userId))
                .expectNext("localhost:8080")
                .verifyComplete();
    }

}

package com.example.chatapp.integration;

import com.example.chatapp.service.RedisServerRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
@Testcontainers
public class RedisServerRegistryServiceTest extends BaseIntegrationTest{

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

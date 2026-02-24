package com.example.chatapp.integration;

import com.example.chatapp.dto.OAuthCallbackRequest;
import com.example.chatapp.dto.OAuthUserInfo;
import com.example.chatapp.integration.config.CassandraTestConfig;
import com.example.chatapp.repository.AuditLogRepository;
import com.example.chatapp.service.GoogleOAuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Testcontainers
@Slf4j
public class AuditAspectIntegrationTest {


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private GoogleOAuthService googleOAuthService;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll().block();
    }

    @Container
    static final CassandraContainer<?> cassandra = CassandraTestConfig.createCassandraContainer();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        CassandraTestConfig.configureCassandra(registry, cassandra);
    }


    @Test
    void shouldCreateAuditLogOnSuccessfulLogin() {

        // Mock the OAuth provider
        OAuthUserInfo userInfo = new OAuthUserInfo("testId", "test@example.com", "testUsername");
        when(googleOAuthService.getUserInfo("test-code")).thenReturn(Mono.just(userInfo));

        OAuthCallbackRequest request = new OAuthCallbackRequest("test-code");

        webTestClient.post()
                .uri("/api/v1/auth/oauth/google/callback")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> log.info("Response: {}", new String(response.getResponseBody())));

        // Verify audit log was created
        StepVerifier.create(auditLogRepository.findAll())
                .expectNextMatches(log ->
                        log.getAction().equals("USER_LOGIN") &&
                                log.getStatus().equals("SUCCESS"))
                .verifyComplete();

    }

    @Test
    void shouldCreateAuditLogOnFailedLogin() {
        // Mock the OAuth provider to return an error
        when(googleOAuthService.getUserInfo("invalid-code"))
                .thenReturn(Mono.error(new RuntimeException("Invalid OAuth code")));

        OAuthCallbackRequest request = new OAuthCallbackRequest("invalid-code");

        webTestClient.post()
                .uri("/api/v1/auth/oauth/google/callback")
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();

        // Verify audit log was created with FAILURE status
        StepVerifier.create(auditLogRepository.findAll())
                .expectNextMatches(log ->
                        log.getAction().equals("USER_LOGIN") &&
                                log.getStatus().equals("FAILURE"))
                .verifyComplete();
    }

}

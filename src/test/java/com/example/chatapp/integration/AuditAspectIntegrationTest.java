package com.example.chatapp.integration;

import com.example.chatapp.dto.OAuthCallbackRequest;
import com.example.chatapp.dto.OAuthUserInfo;
import com.example.chatapp.integration.config.CassandraTestConfig;
import com.example.chatapp.repository.AuditLogRepository;
import com.example.chatapp.service.GoogleOAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"chat-messages"})
@Testcontainers
@Slf4j
public class AuditAspectIntegrationTest {

    @Container
    static final CassandraContainer<?> cassandra = CassandraTestConfig.createCassandraContainer();

    @DynamicPropertySource
    static void configureCassandra(DynamicPropertyRegistry registry) {
        CassandraTestConfig.configureCassandra(registry, cassandra);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private GoogleOAuthService googleOAuthService;


    @Test
    void shouldCreateAuditLogOnSuccessfulLogin() throws JsonProcessingException {

        // Mock the OAuth provider
        OAuthUserInfo userInfo = new OAuthUserInfo("testId", "test@example.com", "testUsername");
        when(googleOAuthService.getUserInfo("test-code")).thenReturn(userInfo);
        log.info("Mocked OAuth service");

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

}

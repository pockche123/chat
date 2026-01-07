package com.example.chatapp.integration;

import com.example.chatapp.dto.OAuthUserInfo;
import com.example.chatapp.factory.OAuthProviderFactory;
import com.example.chatapp.service.GoogleOAuthService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 16, topics = {"chat-messages"})
@Testcontainers
@Slf4j
public class GoogleOAuthServiceIntegrationTest {
    @Autowired
    private OAuthProviderFactory oAuthProviderFactory;

    @Autowired
    private GoogleOAuthService googleOAuthService;  // Uses test properties

    @Test
    void test_getUserInfo() throws IOException {
        try (MockWebServer mockServer = new MockWebServer()) {
            mockServer.start(54321);  // Start on port 54321 to match properties

            mockServer.enqueue(new MockResponse()
                    .setBody("{\"access_token\":\"token123\"}"));
            mockServer.enqueue(new MockResponse()
                    .setBody("{\"id\":\"123\",\"email\":\"test@gmail.com\"}"));

            // Test with real service (but it calls MockWebServer due to test properties)
            OAuthUserInfo result = googleOAuthService.getUserInfo("code123");

            assertNotNull(result);
        }
    }
}

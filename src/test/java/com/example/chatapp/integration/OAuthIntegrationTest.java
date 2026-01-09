package com.example.chatapp.integration;

import com.example.chatapp.controller.OAuthController;
import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.dto.OAuthCallbackRequest;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 16, topics = {"chat-messages"})
@Testcontainers
@Slf4j
public class OAuthIntegrationTest {

    @Autowired
    private OAuthController oAuthController;

    @Autowired
    private UserRepository userRepository;

    @Test
    void test_completeOAuthFlow() throws IOException {
        try(MockWebServer mockServer = new MockWebServer()){
            mockServer.start(54321);

            // Mock Google responses
            mockServer.enqueue(new MockResponse()
                    .setBody("{\"access_token\":\"token123\"}"));
            mockServer.enqueue(new MockResponse()
                    .setBody("{\"id\":\"google123\",\"email\":\"test@gmail.com\"}"));

          // Test the complete flow
            String provider = "google";
            OAuthCallbackRequest request = new OAuthCallbackRequest();
            request.setCode("auth_code_123");

            ResponseEntity<AuthDTO> response = oAuthController.handleOAuthCallback(provider, request);

          // Verify response
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getToken());  // JWT token exists

            Optional<User> user = userRepository.findByProviderAndProviderId("google", "google123");
            assertTrue(user.isPresent());
            assertEquals("test@gmail.com", user.get().getUsername());
            assertEquals("google", user.get().getProvider());
            assertEquals("google123", user.get().getProviderId());
        }
    }
}

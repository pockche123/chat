package com.example.chatapp.integration;

import com.example.chatapp.dto.UserDeviceRequest;
import com.example.chatapp.model.UserDevice;
import com.example.chatapp.repository.UserDeviceRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationIntegrationTest extends BaseIntegrationTest{
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserDeviceRepository userDeviceRepository;


    @Test
    @Order(1)
    void should_registerDeviceToken() {
        String token = "token";
        UUID userId = UUID.randomUUID();
        String deviceType = "apple";

        UserDeviceRequest userDeviceRequest = new UserDeviceRequest(token, deviceType, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDeviceRequest> request = new HttpEntity<>(userDeviceRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/devices", request, String.class);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Optional<UserDevice> savedDevice = userDeviceRepository.findUserDeviceByUserId(userId);
        assertNotNull(savedDevice);
        assertEquals("token", savedDevice.get().getDeviceToken());
        assertEquals("apple", savedDevice.get().getDeviceType());
    }


}

package com.example.chatapp.integration;

import com.example.chatapp.dto.UserDeviceRequest;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserDevice;
import com.example.chatapp.repository.UserDeviceRepository;
import com.example.chatapp.service.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.example.chatapp.service.DistributedOnlineUserService;


import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationIntegrationTest extends BaseIntegrationTest{
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private KafkaMessageQueueService kafkaMessageQueueService;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

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

    @Test
    @Order(2)
    void shouldSendNotification_whenUserOfflineAndMessageReceived() throws FirebaseMessagingException, InterruptedException {
        UUID userId = UUID.randomUUID();
        User offlineUser = new User();
        offlineUser.setUserId(userId);
        UUID senderId = UUID.randomUUID();

        // Register device for the user who will receive the message
        UserDeviceRequest deviceRequest = new UserDeviceRequest("test-token", "ios", userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDeviceRequest> request = new HttpEntity<>(deviceRequest, headers);
        restTemplate.postForEntity("/api/v1/devices", request, String.class);

        ChatMessage message = createChatMessage(senderId, userId);
        kafkaMessageQueueService.enqueueMessage(message);

        verify(firebaseMessaging, timeout(8000)).send(any(Message.class));
    }

    private ChatMessage createChatMessage(UUID senderId, UUID receiverId){
        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());
        message.setConversationId(UUID.randomUUID());
        message.setReceiverId(receiverId);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));
        message.setSenderId(senderId);
        message.setStatus(MessageStatus.SENT);
        message.setContent("test message");
        return message;
    }



}

package com.example.chatapp.unit.controller;

import com.example.chatapp.controller.UserDeviceController;
import com.example.chatapp.dto.UserDeviceDTO;
import com.example.chatapp.dto.UserDeviceRequest;
import com.example.chatapp.service.UserDeviceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDeviceControllerTest {

    @Mock
    private UserDeviceService userDeviceService;

    @InjectMocks
    private UserDeviceController userDeviceController;

    @Test
    void createUserDevice_through_POST(){
        String token = "token";
        String deviceType = "apple";
        UUID userId = UUID.randomUUID();

        UserDeviceDTO userDeviceDTO = new UserDeviceDTO(token, deviceType, userId);
        UserDeviceRequest userDeviceRequest = new UserDeviceRequest(token, deviceType, userId);
        when(userDeviceService.saveUserDevice(token, deviceType, userId)).thenReturn(userDeviceDTO);

        ResponseEntity<UserDeviceDTO> response = userDeviceController.createUserDevice(userDeviceRequest);
        UserDeviceDTO device = response.getBody();

        assertNotNull(response);
        assertNotNull(device);
        assertEquals(token, device.getDeviceToken());
        assertEquals(deviceType, device.getDeviceType());
        assertEquals(userId, device.getUserId());

    }

}

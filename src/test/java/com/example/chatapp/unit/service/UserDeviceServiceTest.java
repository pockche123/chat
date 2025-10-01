package com.example.chatapp.unit.service;

import com.example.chatapp.dto.UserDeviceDTO;
import com.example.chatapp.model.UserDevice;
import com.example.chatapp.repository.UserDeviceRepository;
import com.example.chatapp.service.UserDeviceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDeviceServiceTest {
    @Mock
    private UserDeviceRepository userDeviceRepository;

    @InjectMocks
    private UserDeviceService userDeviceService;

    @Test
    void shouldSaveUserDevice() {
        // given
        String deviceToken = "token";
        String deviceType = "apple";
        UUID userId = UUID.randomUUID();
        UserDevice userDevice = new UserDevice();
        userDevice.setDeviceToken(deviceToken);
        userDevice.setDeviceType(deviceType);
        userDevice.setUserId(userId);
        when(userDeviceRepository.save(any(UserDevice.class))).thenReturn(userDevice);


        // when
        UserDeviceDTO userDeviceDTO = userDeviceService.saveUserDevice(deviceToken, deviceType, userId);

        // then
        Assertions.assertNotNull(userDeviceDTO);
        assertEquals(deviceToken, userDeviceDTO.getDeviceToken());
        assertEquals(deviceType, userDeviceDTO.getDeviceType());
        assertEquals(userId, userDeviceDTO.getUserId());

    }
}

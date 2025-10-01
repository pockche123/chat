package com.example.chatapp.service;

import com.example.chatapp.dto.UserDeviceDTO;
import com.example.chatapp.model.UserDevice;
import com.example.chatapp.repository.UserDeviceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;

    public UserDeviceService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    public UserDeviceDTO saveUserDevice(String deviceToken, String deviceType, UUID userId) {
        UserDevice userDevice = new UserDevice(deviceToken, deviceType, userId);
        userDeviceRepository.save(userDevice);
        return new UserDeviceDTO(deviceToken, deviceType, userId);
    }
}

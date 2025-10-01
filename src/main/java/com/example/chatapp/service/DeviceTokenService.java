package com.example.chatapp.service;

import com.example.chatapp.model.UserDevice;
import com.example.chatapp.repository.UserDeviceRepository;

import java.util.Optional;
import java.util.UUID;

public class DeviceTokenService {

    private final UserDeviceRepository userDeviceRepository;

    public DeviceTokenService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

/// do  a catch here??
    public String getDeviceToken(UUID userId) {
        Optional<UserDevice> device =  userDeviceRepository.findUserDeviceByUserId(userId);
        return device.map(UserDevice::getDeviceToken).orElse(null);
    }
}

package com.example.chatapp.controller;

import com.example.chatapp.dto.UserDeviceDTO;
import com.example.chatapp.dto.UserDeviceRequest;
import com.example.chatapp.service.UserDeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserDeviceController {
    private final UserDeviceService userDeviceService;

    public UserDeviceController(UserDeviceService userDeviceService) {
        this.userDeviceService = userDeviceService;
    }

    @PostMapping("/api/v1/devices")
    public ResponseEntity<UserDeviceDTO> createUserDevice(@RequestBody UserDeviceRequest userDeviceRequest) {
        return ResponseEntity.ok(userDeviceService.saveUserDevice(userDeviceRequest.getDeviceToken(), userDeviceRequest.getDeviceType(), userDeviceRequest.getUserId()));
    }
}

package com.example.chatapp.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDeviceRequest {
    private String deviceToken;
    private String deviceType;
    private UUID userId;
}

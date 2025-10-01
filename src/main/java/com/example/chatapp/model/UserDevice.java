package com.example.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDevice {
    private String deviceToken;
    private String deviceType;
    private UUID userId;

}

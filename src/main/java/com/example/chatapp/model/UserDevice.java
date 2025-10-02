package com.example.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDevice {
    @Id
    private String deviceToken;
    private String deviceType;
    private UUID userId;

}

package com.example.chatapp.dto;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDeviceDTO {
    private String deviceToken;
    private String deviceType;
    private UUID userId;

}

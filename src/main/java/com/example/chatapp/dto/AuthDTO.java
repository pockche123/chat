package com.example.chatapp.dto;

import com.example.chatapp.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthDTO {
    UUID userId;
    String username;
    UserStatus userStatus;
    String token;
}

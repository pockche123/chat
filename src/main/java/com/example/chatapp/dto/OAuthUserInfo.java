package com.example.chatapp.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserInfo {
    private String id;
    private String email;
    private String username;
}

package com.example.chatapp.service;

import com.example.chatapp.dto.OAuthUserInfo;
import org.springframework.stereotype.Service;

@Service
public class GoogleOAuthService implements OAuthProviderService{
    @Override
    public OAuthUserInfo getUserInfo(String code) {
        return null;
    }
}

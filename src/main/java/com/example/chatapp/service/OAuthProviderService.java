package com.example.chatapp.service;

import com.example.chatapp.dto.OAuthUserInfo;

public interface OAuthProviderService {

    OAuthUserInfo getUserInfo(String code);
}

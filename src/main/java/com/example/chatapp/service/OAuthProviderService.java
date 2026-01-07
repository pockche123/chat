package com.example.chatapp.service;

import com.example.chatapp.dto.OAuthUserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface OAuthProviderService {

    OAuthUserInfo getUserInfo(String code) throws JsonProcessingException;
}

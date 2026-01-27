package com.example.chatapp.service;

import com.example.chatapp.dto.OAuthUserInfo;
import reactor.core.publisher.Mono;

public interface OAuthProviderService {

    Mono<OAuthUserInfo> getUserInfo(String code);
}

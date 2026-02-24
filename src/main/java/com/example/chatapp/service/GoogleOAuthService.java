package com.example.chatapp.service;

import com.example.chatapp.dto.OAuthUserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GoogleOAuthService implements OAuthProviderService{
    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${google.oauth.token-uri}")
    private String tokenUri;

    @Value("${google.oauth.userinfo-uri}")
    private String userinfoUri;


    public GoogleOAuthService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<OAuthUserInfo> getUserInfo(String code) {
        return exchangeCodeForAccessToken(code)
                .flatMap(this::fetchUserInfoFromGoogle);
    }

    private Mono<String> exchangeCodeForAccessToken(String code) {
        return webClient.post()
                .uri(tokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&code=" + code +
                        "&grant_type=authorization_code" +
                        "&redirect_uri=" + redirectUri)
                .retrieve()
                .bodyToMono(String.class)
                .map(tokenResponse -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(tokenResponse);
                        return jsonNode.get("access_token").asText();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to parse token response", e);
                    }
                });
    }

    private Mono<OAuthUserInfo> fetchUserInfoFromGoogle(String accessToken) {
        return webClient.get()
                .uri(userinfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .map(userResponse -> {
                    try {
                        JsonNode userNode = objectMapper.readTree(userResponse);
                        return OAuthUserInfo.builder()
                                .id(userNode.get("id").asText())
                                .email(userNode.get("email").asText())
                                .username(userNode.get("email").asText())
                                .build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to parse user info response", e);
                    }
                });
    }

}

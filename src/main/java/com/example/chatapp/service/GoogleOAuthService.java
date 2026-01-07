package com.example.chatapp.service;

import com.example.chatapp.dto.OAuthUserInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    public OAuthUserInfo getUserInfo(String code) throws JsonProcessingException {
        String accessToken = exchangeCodeForAccessToken(code);
        return fetchUserInfoFromGoogle(accessToken);

    }

    private String exchangeCodeForAccessToken(String code) throws JsonProcessingException {
        String tokenResponse = webClient.post()
                .uri(tokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&code=" + code +
                        "&grant_type=authorization_code" +
                        "&redirect_uri=" + redirectUri)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse JSON response to extract access_token
        JsonNode jsonNode = objectMapper.readTree(tokenResponse);
        return jsonNode.get("access_token").asText();
    }

    private OAuthUserInfo fetchUserInfoFromGoogle(String accessToken) throws JsonProcessingException {
        String userResponse = webClient.get()
                .uri(userinfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse JSON response to create OAuthUserInfo
        JsonNode userNode = objectMapper.readTree(userResponse);
        return OAuthUserInfo.builder()
                .id(userNode.get("id").asText())
                .email(userNode.get("email").asText())
                .username(userNode.get("email").asText())
                .build();
    }

}

package com.example.chatapp.service;

import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.dto.OAuthUserInfo;
import com.example.chatapp.factory.OAuthProviderFactory;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserStatus;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final OAuthProviderFactory oAuthProviderFactory;

    public OAuthService(UserRepository userRepository, JwtUtil jwtUtil, OAuthProviderFactory oAuthProviderFactory) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.oAuthProviderFactory = oAuthProviderFactory;
    }


    public Mono<AuthDTO> findOrCreateOAuthUser(String providerId, String provider, String email) {
       return  userRepository.findByProviderAndProviderId(provider, providerId)
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User();
                    newUser.setUserId(UUID.randomUUID());
                    newUser.setUsername(email);
                    newUser.setProvider(provider);
                    newUser.setProviderId(providerId);
                    newUser.setUserStatus(UserStatus.ONLINE);
                    newUser.setTier("free");
                    return userRepository.save(newUser);
                }))
                .map(user -> {
                    String jwt = jwtUtil.generateToken(user);
                    return new AuthDTO(user.getUserId(), user.getUsername(), user.getUserStatus(), jwt);
                });

    }

    public Mono<AuthDTO> handleOAuth(String provider, String code) throws JsonProcessingException {
        OAuthProviderService providerService = oAuthProviderFactory.getProvider(provider);
        OAuthUserInfo userInfo = providerService.getUserInfo(code);
        return findOrCreateOAuthUser(userInfo.getId(), provider, userInfo.getEmail());
    }
}

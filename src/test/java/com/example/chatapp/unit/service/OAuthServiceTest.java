package com.example.chatapp.unit.service;

import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.dto.OAuthUserInfo;
import com.example.chatapp.factory.OAuthProviderFactory;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.GoogleOAuthService;
import com.example.chatapp.service.OAuthProviderService;
import com.example.chatapp.service.OAuthService;
import com.example.chatapp.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OAuthServiceTest {

    @InjectMocks
    private OAuthService oAuthService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OAuthProviderFactory providerFactory;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @Mock
    private OAuthProviderService providerService;


    @Test
    public void test_findOrCreateOAuthUser_whenUserIsNotPresent_returnsAuthDTO(){
        String provider = "google";
        String providerId = "googleId";
        String email = "test@gmail.com";


        when(userRepository.findByProviderAndProviderId(provider, providerId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("token");

        AuthDTO authDTO = oAuthService.findOrCreateOAuthUser(providerId, provider,email);
        assertNotNull(authDTO);

    }

    @Test
    public void test_handleAuth_returnsAuthDTO() throws JsonProcessingException {
        String provider = "google";
        String providerId = "googleId";
        String email = "test@gmail.com";
        String code = "googleCode";

        // Fix: Create userInfo with actual data
        OAuthUserInfo userInfo = OAuthUserInfo.builder()
                .id(providerId)
                .email(email)
                .build();

        when(providerFactory.getProvider(provider)).thenReturn(googleOAuthService);
        when(googleOAuthService.getUserInfo(code)).thenReturn(userInfo);
        when(userRepository.findByProviderAndProviderId(provider, providerId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("token");

        AuthDTO actual = oAuthService.handleOAuth(provider, code);

        assertNotNull(actual);
    }
}

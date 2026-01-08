package com.example.chatapp.unit.controller;

import com.example.chatapp.controller.OAuthController;
import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.dto.OAuthCallbackRequest;
import com.example.chatapp.service.OAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OAuthControllerTest {

    @InjectMocks
    private OAuthController oAuthController;

    @Mock
    private OAuthService oAuthService;

    @Test
    void test_handleOAuthCallback_returnsAuthDTO() throws JsonProcessingException {
        String provider = "google";
        String code = "mockGoogleCode";
        OAuthCallbackRequest callbackRequest = new OAuthCallbackRequest(code);

        AuthDTO expectedAuthDTO = new AuthDTO();
        when(oAuthService.handleOAuth(provider, code)).thenReturn(expectedAuthDTO);

        ResponseEntity<AuthDTO> actual = oAuthController.handleOAuthCallback(provider, callbackRequest);

        assertNotNull(actual);
        assertEquals(expectedAuthDTO, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }
}

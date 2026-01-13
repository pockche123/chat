package com.example.chatapp.controller;

import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.dto.OAuthCallbackRequest;
import com.example.chatapp.service.OAuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/oauth")
public class OAuthController {
    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @PostMapping("/{provider}/callback")
    public ResponseEntity<AuthDTO> handleOAuthCallback( @PathVariable String provider,
                                                        @RequestBody OAuthCallbackRequest request) throws JsonProcessingException {
        String code = request.getCode();
        AuthDTO authDTO = oAuthService.handleOAuth(provider, code);
        return ResponseEntity.ok(authDTO);
    }
}

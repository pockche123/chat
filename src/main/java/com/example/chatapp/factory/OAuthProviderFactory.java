package com.example.chatapp.factory;

import com.example.chatapp.service.GoogleOAuthService;
import com.example.chatapp.service.OAuthProviderService;
import org.springframework.stereotype.Component;

@Component
public class OAuthProviderFactory {

    private final GoogleOAuthService googleOAuthService;

    public OAuthProviderFactory(GoogleOAuthService googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    public OAuthProviderService getProvider(String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return googleOAuthService;
            default:
                throw new IllegalArgumentException("Invalid OAuth provider type: " + provider);
        }
    }
}

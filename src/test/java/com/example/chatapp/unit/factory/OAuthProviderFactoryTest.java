package com.example.chatapp.unit.factory;

import com.example.chatapp.factory.OAuthProviderFactory;
import com.example.chatapp.service.GoogleOAuthService;
import com.example.chatapp.service.OAuthProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class OAuthProviderFactoryTest {
    @InjectMocks
    private OAuthProviderFactory oAuthProviderFactory;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @Test
    void withGoogleProvider_ReturnGoogleOAuthService(){
         String provider = "google";

        OAuthProviderService actual = oAuthProviderFactory.getProvider(provider);

        assertEquals(googleOAuthService, actual);
    }

    @Test
    void invalidProvider_ThrowIllegalArgumentException(){
        String provider = "invalid";

        assertThrows(IllegalArgumentException.class, () ->  oAuthProviderFactory.getProvider(provider));
    }

}

package com.example.chatapp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ClassPathResource("firebase-service-account.json").getInputStream());

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp app;
        try {
            app = FirebaseApp.getInstance("chat-app");
        } catch (IllegalStateException e) {
            // App doesn't exist, create it
            app = FirebaseApp.initializeApp(firebaseOptions, "chat-app");
        }

        return FirebaseMessaging.getInstance(app);
    }
}

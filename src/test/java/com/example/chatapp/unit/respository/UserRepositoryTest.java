package com.example.chatapp.unit.respository;

import com.example.chatapp.model.User;
import com.example.chatapp.model.UserStatus;
import com.example.chatapp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    public void tearDown() {
        mongoTemplate.dropCollection(User.class);
    }


    @TestConfiguration
    static class TestConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }


    @Test
    public void testSaveUser() {
        String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);
        user.setUsername(uniqueUsername);
        user.setPassword(passwordEncoder.encode("testPassword"));
        user.setUserStatus(UserStatus.ONLINE);

        StepVerifier.create(userRepository.save(user))
                .assertNext(savedUser -> {
             assertNotNull(savedUser.getUserId());
             assertEquals(uniqueUsername, savedUser.getUsername());
             assertEquals(UserStatus.ONLINE, savedUser.getUserStatus());
             assertTrue(passwordEncoder.matches("testPassword", savedUser.getPassword()));
         }).verifyComplete();


    }

    @Test
    public void test_findBYUsername_getUser(){
        String uniqueUsername = "testUser_" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);
        user.setUsername(uniqueUsername);
        user.setPassword(passwordEncoder.encode("testPassword"));
        user.setUserStatus(UserStatus.ONLINE);
        userRepository.save(user);

        StepVerifier.create(userRepository.findByUsername(uniqueUsername))
                .assertNext(savedUser -> {
                    assertNotNull(savedUser.getUserId());
                    assertEquals(uniqueUsername, savedUser.getUsername());
                    assertEquals(UserStatus.ONLINE, savedUser.getUserStatus());
                    assertTrue(passwordEncoder.matches("testPassword", savedUser.getPassword()));
                });
    }
}

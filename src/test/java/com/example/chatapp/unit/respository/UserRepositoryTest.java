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

import java.util.Optional;
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
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);
        user.setUsername("testUser");
        user.setPassword(passwordEncoder.encode("testPassword"));
        user.setUserStatus(UserStatus.ONLINE);
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getUserId());
        assertEquals("testUser", savedUser.getUsername());
        assertEquals(UserStatus.ONLINE, savedUser.getUserStatus());
        assertTrue(passwordEncoder.matches("testPassword", savedUser.getPassword()));
    }

    @Test
    public void test_findBYUsername_getUser(){
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);
        user.setUsername("testUser");
        user.setPassword(passwordEncoder.encode("testPassword"));
        user.setUserStatus(UserStatus.ONLINE);
        userRepository.save(user);

        Optional<User> optionalUser = userRepository.findByUsername("testUser");
        User savedUser = optionalUser.get();


        assertNotNull(savedUser.getUserId());
        assertEquals("testUser", savedUser.getUsername());
        assertEquals(UserStatus.ONLINE, savedUser.getUserStatus());
        assertTrue(passwordEncoder.matches("testPassword", savedUser.getPassword()));
    }
}

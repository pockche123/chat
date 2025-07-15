package com.example.chatapp.respository;

import com.example.chatapp.model.User;
import com.example.chatapp.model.UserStatus;
import com.example.chatapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Import(UserRepository.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        assertTrue(passwordEncoder.matches("testPassword", savedUser.getPassword()));
    }
}

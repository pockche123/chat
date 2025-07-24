package com.example.chatapp.unit.service;

import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserStatus;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.UserService;
import com.example.chatapp.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;



    @Test
    public void test_registerUser_registersUser(){
        String username = "testUser";
        String password = "testPassword";
        String encodedPassword = "encodedPassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        UserDTO actual = userService.registerUser(username, password);

        assertNotNull(actual);
        assertEquals(UserStatus.OFFLINE, actual.getUserStatus());
        assertEquals(username, actual.getUsername());
    }

    @Test
    public void test_registerUser_registersAlreadyExistingUser_throwsException(){

        String username = "existingUser";
        User user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> userService.registerUser(username, "password"));

        assertEquals("User already exists.", exception.getMessage());

    }


    @Test
    public void test_loginUser_throwsUserNotFoundException(){
        String username = "testUser";
        String password = "password";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> userService.loginUser(username, password)
        );

        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    public void test_loginUser_throwsWrongPasswordException(){
        String username = "XXXXXXXX";
        String password = "XXXXXXXX";

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> userService.loginUser(username, password)
        );

        assertEquals("Invalid password.", exception.getMessage());
    }



    @Test
    public void test_loginUser_success(){
        String username = "XXXXXXXX";
        String password = "XXXXXXXX";


        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("token");

        AuthDTO authDTO = userService.loginUser(username, password);

        assertNotNull(authDTO);
        assertEquals(username, authDTO.getUsername());
        assertEquals(UserStatus.ONLINE, authDTO.getUserStatus());
    }

}

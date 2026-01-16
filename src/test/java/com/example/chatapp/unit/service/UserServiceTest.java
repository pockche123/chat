package com.example.chatapp.unit.service;


import com.example.chatapp.exception.RateLimitExceededException;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserStatus;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.SlidingWindowCounterRateLimiter;
import com.example.chatapp.service.UserService;
import com.example.chatapp.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SlidingWindowCounterRateLimiter rateLimiter;

    @InjectMocks
    private UserService userService;


    @Test
    public void test_registerUser_registersUser(){
        String username = "testUser";
        String password = "testPassword";
        String encodedPassword = "encodedPassword";

        User savedUser = new User();
        savedUser.setUserStatus(UserStatus.OFFLINE);
        savedUser.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        StepVerifier.create(userService.registerUser(username, password))
                        .assertNext(actual -> {
                            assertNotNull(actual);
                            assertEquals(UserStatus.OFFLINE, actual.getUserStatus());
                            assertEquals(username, actual.getUsername());
                        })
                .verifyComplete();
    }

    @Test
    public void test_registerUser_registersAlreadyExistingUser_throwsException(){

        String username = "existingUser";
        User user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        StepVerifier.create(userService.registerUser(username, "password"))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("User already exists")
                )
                .verify();

    }


    @Test
    public void test_loginUser_throwsUserNotFoundException(){
        String username = "testUser";
        String password = "password";

        when(userRepository.findByUsername(username)).thenReturn(Mono.empty());

        when(rateLimiter.isAllowed(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(true));


        StepVerifier.create(userService.loginUser(username, password))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("User not found")
                )
                .verify();
    }

    @Test
    public void test_loginUser_throwsWrongPasswordException(){
        String username = "XXXXXXXX";
        String password = "XXXXXXXX";

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(rateLimiter.isAllowed(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(true));
        when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        StepVerifier.create(userService.loginUser(username, password))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Invalid password")
                )
                .verify();

    }



    @Test
    public void test_loginUser_success(){
        String username = "XXXXXXXX";
        String password = "XXXXXXXX";


        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("token");
        when(userRepository.save(user)).thenReturn(Mono.just(user));

        when(rateLimiter.isAllowed(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(true));


        StepVerifier.create(userService.loginUser(username, password))
                .assertNext(authDTO ->{
                    assertNotNull(authDTO);
                    assertEquals(username, authDTO.getUsername());
                    assertEquals(UserStatus.ONLINE, authDTO.getUserStatus());
                } )
                .verifyComplete();

    }

    @Test
    public void test_loginUser_throwsRateLimitExceededException(){
        String username = "mockUsername";
        String password = "mockPassword";

        when(rateLimiter.isAllowed(anyString(), anyInt(), any(Duration.class))).thenReturn(Mono.just(false));

        StepVerifier.create(userService.loginUser(username, password))
                .expectError(RateLimitExceededException.class)
                .verify();

    }



}

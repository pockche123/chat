package com.example.chatapp.service;

import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.exception.RateLimitExceededException;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserStatus;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;


    private final PasswordEncoder passwordEncoder;


    private final JwtUtil jwtUtil;

    private final SlidingWindowCounterRateLimiter rateLimiter;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, SlidingWindowCounterRateLimiter ratelLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.rateLimiter = ratelLimiter;
    }


    public Mono<UserDTO> registerUser(String username, String password){
        return userRepository.findByUsername(username)
                .hasElement()
                .flatMap(exists -> {
                    if(exists){
                        return Mono.error(new RuntimeException("User already exists"));
                    }
                    User user = new User();
                    user.setUserId(UUID.randomUUID());
                    user.setUserStatus(UserStatus.OFFLINE);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setUsername(username);

                    return userRepository.save(user)
                            .map(savedUser -> new UserDTO(savedUser.getUsername(), savedUser.getUserStatus()));
                });



    }


    public Mono<AuthDTO> loginUser(String username, String password) {
        String key = "login:username:"+  username;

        return rateLimiter.isAllowed(key, 5, Duration.ofMinutes(15))
                .flatMap(allowed -> {
                    if (!allowed) {
                        return Mono.error(new RateLimitExceededException("Too many login attempts. Try again later."));
                    }

                    return userRepository.findByUsername(username)
                            .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                            .flatMap(user -> {
                                if (!passwordEncoder.matches(password, user.getPassword())) {
                                    return Mono.error(new RuntimeException("Invalid password"));
                                }

                                String token = jwtUtil.generateToken(user);
                                user.setUserStatus(UserStatus.ONLINE);

                                return userRepository.save(user)
                                        .map(savedUser -> new AuthDTO(
                                                savedUser.getUserId(),
                                                savedUser.getUsername(),
                                                savedUser.getUserStatus(),
                                                token
                                        ));
                            });
                });
    }

}

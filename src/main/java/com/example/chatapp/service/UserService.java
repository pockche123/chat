package com.example.chatapp.service;

import com.example.chatapp.dto.AuthDTO;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserStatus;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

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
    }

}

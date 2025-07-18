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
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public UserDTO registerUser(String username, String password){
        if(userRepository.findByUsername(username).isPresent()){
            throw new RuntimeException("User already exists.");
        }
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserStatus(UserStatus.OFFLINE);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(username);

        userRepository.save(user);
        return new UserDTO(user.getUsername(), user.getUserStatus());
    }


    public AuthDTO loginUser(String username, String password){

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found."));

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Invalid password.");
        }

        String token = jwtUtil.generateToken(user);

        user.setUserStatus(UserStatus.ONLINE);
        userRepository.save(user);

        return new AuthDTO(user.getUserId(), user.getUsername(), user.getUserStatus(), token);
    }
}

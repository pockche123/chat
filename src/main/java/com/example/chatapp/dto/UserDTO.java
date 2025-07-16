package com.example.chatapp.dto;

import com.example.chatapp.model.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    private UserStatus userStatus;
}

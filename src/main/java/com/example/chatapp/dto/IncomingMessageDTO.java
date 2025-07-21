package com.example.chatapp.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncomingMessageDTO {

    private UUID receiverId;
    private String content;
}

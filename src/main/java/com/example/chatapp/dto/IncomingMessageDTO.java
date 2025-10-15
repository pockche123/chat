package com.example.chatapp.dto;



import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IncomingMessageDTO {

    private String type = "message";
    private UUID receiverId;
    private String content;
    private UUID conversationId;
    private UUID messageId;
}

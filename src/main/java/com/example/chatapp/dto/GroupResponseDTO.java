package com.example.chatapp.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponseDTO {
    private UUID conversationId;
    private List<UUID> memberIds;
    private String name;
    private UUID adminId;

}
package com.example.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("direct_conversations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DirectConversation {
    @Id
    private UUID conversationId;
    private UUID participant1;
    private UUID participant2;

}

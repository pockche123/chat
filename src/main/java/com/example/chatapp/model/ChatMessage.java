package com.example.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.sql.Timestamp;
import java.util.UUID;

@Table("chat_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    @PrimaryKeyColumn(name = "message_id", type= PrimaryKeyType.PARTITIONED)
    private UUID messageId;
    private Timestamp timestamp;
    @Indexed
    @Column("conversation_id")
    private UUID conversationId;
    private String content;
    @Column("sender_id")
    private UUID senderId;
    @Column("receiver_id")
    @Indexed
    private UUID receiverId;
    @Column("status")
    @Indexed
    private MessageStatus status = MessageStatus.CREATED;


}
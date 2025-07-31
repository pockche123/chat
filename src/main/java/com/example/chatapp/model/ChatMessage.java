package com.example.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.Ordering;
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
    @PrimaryKeyColumn(name = "conversation_id", type= PrimaryKeyType.PARTITIONED)
    private UUID conversationId;
    @PrimaryKeyColumn(name = "timestamp", type=PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Timestamp timestamp;
    @PrimaryKeyColumn(name = "message_id", type=PrimaryKeyType.CLUSTERED)
    private UUID messageId;

    private String content;
    @Column("sender_id")
    private UUID senderId;
    @Column("receiver_id")
    private UUID receiverId;
    @Column("status")
    private MessageStatus status = MessageStatus.CREATED;


}

package com.example.chatapp.model;


import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.sql.Timestamp;
import java.util.UUID;

@PrimaryKeyClass
public class ChatMessageKey {
    @PrimaryKeyColumn(name = "conversation_id", type = PrimaryKeyType.PARTITIONED)
    private UUID conversationId;

    @PrimaryKeyColumn(name = "timestamp", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Timestamp timestamp;

    @PrimaryKeyColumn(name = "message_id", type = PrimaryKeyType.CLUSTERED)
    private UUID messageId;
}
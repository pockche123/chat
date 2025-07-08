USE my_chat;

CREATE TABLE IF NOT Exists chat_messages
(
    conversation_id uuid,
    timestamp       timestamp,
    sender_id       uuid,
    receiver_id     uuid,
    content         text,
    PRIMARY KEY ((conversation_id), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);
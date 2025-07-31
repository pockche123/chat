USE my_chat;
DROP TABlE IF EXISTS chat_messages;
CREATE TABLE IF NOT Exists chat_messages
(
    message_id uuid,
    conversation_id uuid,
    timestamp       timestamp,
    sender_id       uuid,
    receiver_id     uuid,
    content         text,
    status          text,
    PRIMARY KEY ((conversation_id), timestamp, message_id)
) WITH CLUSTERING ORDER BY (timestamp DESC);
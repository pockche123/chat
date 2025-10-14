USE my_chat;
DROP TABlE IF EXISTS chat_messages;
CREATE TABLE IF NOT Exists chat_messages
(
    message_id uuid,
    timestamp       timestamp,
    conversation_id uuid,
    sender_id       uuid,
    receiver_id     uuid,
    content         text,
    status          text,
    PRIMARY KEY (message_id)
);
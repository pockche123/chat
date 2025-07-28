USE my_chat;
CREATE INDEX IF NOT EXISTS idx_status
    ON my_chat.chat_messages(status);
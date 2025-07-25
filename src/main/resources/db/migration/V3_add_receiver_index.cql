USE my_chat;
CREATE INDEX IF NOT EXISTS idx_receiver_id
ON my_chat.chat_messages(receiver_id);
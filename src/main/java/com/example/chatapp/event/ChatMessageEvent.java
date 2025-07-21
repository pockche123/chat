package com.example.chatapp.event;

import com.example.chatapp.model.ChatMessage;

/**
 * Event class for chat messages
 */
public class ChatMessageEvent {
    private final ChatMessage message;
    
    public ChatMessageEvent(ChatMessage message) {
        this.message = message;
    }
    
    public ChatMessage getMessage() {
        return message;
    }
}
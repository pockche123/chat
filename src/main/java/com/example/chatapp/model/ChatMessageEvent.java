package com.example.chatapp.model;

//wrapper class good for events?
public class ChatMessageEvent {
    private final ChatMessage message;

    public ChatMessageEvent(ChatMessage message){
        this.message = message;
    }

    public ChatMessage getMessage(){
        return message;
    }
}

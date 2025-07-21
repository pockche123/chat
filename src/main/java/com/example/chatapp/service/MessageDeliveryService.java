package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;

public interface MessageDeliveryService {

    public void deliverMessage(ChatMessage message);
}

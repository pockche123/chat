package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;

public interface MessageDeliveryService {

   void deliverMessage(ChatMessage message);
}

package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import reactor.core.publisher.Mono;

public interface MessageDeliveryService {

   Mono<ChatMessage> deliverMessage(ChatMessage message);
}

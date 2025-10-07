package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MessageDeliveryService {

   Mono<ChatMessage> deliverMessage(ChatMessage message);


   void registerSession(UUID userId, WebSocketSession session);

   void removeSession(UUID userId);

}

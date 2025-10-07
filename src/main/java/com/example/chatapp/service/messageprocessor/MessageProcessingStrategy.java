package com.example.chatapp.service.messageprocessor;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MessageProcessingStrategy  {

    Mono<ChatMessage> processMessage(UUID senderId, IncomingMessageDTO incomingMessageDTO);
    boolean canHandle(String messageType);

}

package com.example.chatapp.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public Mono<ChatMessage> processIncomingMessage(IncomingMessageDTO incomingMessageDTO) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageId(UUID.randomUUID());
        if(chatMessage.getConversationId() == null) {
            chatMessage.setConversationId(generateConversationId(incomingMessageDTO.getSenderId(), incomingMessageDTO.getReceiverId()));
        }
        chatMessage.setContent(incomingMessageDTO.getContent());
        chatMessage.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return chatMessageRepository.save(chatMessage);
    }

    private UUID generateConversationId(UUID senderId, UUID receiverId) {
        return UUID.nameUUIDFromBytes((senderId.toString() + receiverId.toString()).getBytes());
    }
}

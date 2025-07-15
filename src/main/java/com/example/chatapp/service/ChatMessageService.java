package com.example.chatapp.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

@Slf4j
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
        chatMessage.setSenderId(incomingMessageDTO.getSenderId());
        chatMessage.setReceiverId(incomingMessageDTO.getReceiverId());
        chatMessage.setContent(incomingMessageDTO.getContent());
        chatMessage.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return chatMessageRepository.save(chatMessage).doOnSuccess(saved -> log.info("Saved chat message: {}", saved));
    }

    private UUID generateConversationId(UUID senderId, UUID receiverId) {
        String combined  = senderId.compareTo(receiverId) < 0 ? senderId.toString() + receiverId.toString() : receiverId.toString() + senderId.toString();
        return UUID.nameUUIDFromBytes(combined.getBytes());
    }
}

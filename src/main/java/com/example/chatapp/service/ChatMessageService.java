package com.example.chatapp.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.repository.DirectConversationRepository;
import com.example.chatapp.repository.GroupRepository;
import com.example.chatapp.service.messageprocessor.MessageProcessingStrategy;
import com.example.chatapp.service.messageprocessor.MessageProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MessageProcessorFactory messageProcessorFactory;


    public ChatMessageService(ChatMessageRepository chatMessageRepository, MessageProcessorFactory messageProcessorFactory) {
        this.chatMessageRepository = chatMessageRepository;
        this.messageProcessorFactory = messageProcessorFactory;

    }

    public Flux<ChatMessage> processIncomingMessage(UUID currentUserId, IncomingMessageDTO incomingMessageDTO) {
        MessageProcessingStrategy strategy = messageProcessorFactory.getProcessor(incomingMessageDTO.getType());
        return strategy.processMessages(currentUserId, incomingMessageDTO);
    }

    public Flux<ChatMessage> markDeliveredMessagesAsRead(UUID conversationId, UUID receiverId) {
        return chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED)
                .flatMap(message -> {
                    message.setStatus(MessageStatus.READ);
                    return chatMessageRepository.save(message);
                });
    }


}

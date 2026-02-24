package com.example.chatapp.service;

import com.example.chatapp.annotation.Audited;
import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.messageprocessor.MessageProcessingStrategy;
import com.example.chatapp.service.messageprocessor.MessageProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MessageProcessorFactory messageProcessorFactory;


    public ChatMessageService(ChatMessageRepository chatMessageRepository, MessageProcessorFactory messageProcessorFactory) {
        this.chatMessageRepository = chatMessageRepository;
        this.messageProcessorFactory = messageProcessorFactory;

    }

    @Audited(action = "MESSAGE_SENT")
    public Flux<ChatMessage> processIncomingMessage(UUID currentUserId, IncomingMessageDTO incomingMessageDTO) {
        MessageProcessingStrategy strategy = messageProcessorFactory.getProcessor(incomingMessageDTO.getType());
        return strategy.processMessages(currentUserId, incomingMessageDTO);
    }

    @Audited(action = "READ_RECEIPT")
    public Flux<ChatMessage> markDeliveredMessagesAsRead(UUID conversationId, UUID receiverId) {
        return chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED)
                .flatMap(message -> {
                    message.setStatus(MessageStatus.READ);
                    return chatMessageRepository.save(message);
                });
    }


}

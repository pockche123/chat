package com.example.chatapp.service.messageprocessor;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class ReadMessageProcessor implements MessageProcessingStrategy {

    private final ChatMessageRepository chatMessageRepository;

    public ReadMessageProcessor(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public Flux<ChatMessage> processMessages(UUID receiverId, IncomingMessageDTO incomingMessageDTO) {
        return chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(incomingMessageDTO.getConversationId(), receiverId, MessageStatus.DELIVERED)
                .flatMap(message -> {
                   message.setStatus(MessageStatus.READ);
                   return chatMessageRepository.save(message);
                });
    }

    @Override
    public boolean canHandle(String messageType) {
        return messageType.equalsIgnoreCase("read_receipt");
    }
}

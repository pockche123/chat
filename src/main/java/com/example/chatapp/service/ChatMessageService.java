package com.example.chatapp.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

@Slf4j
@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final KafkaMessageQueueService messageQueueService;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, KafkaMessageQueueService messageQueueService) {
        this.chatMessageRepository = chatMessageRepository;
        this.messageQueueService = messageQueueService;
    }

    public Mono<ChatMessage> processIncomingMessage(UUID senderId, IncomingMessageDTO incomingMessageDTO) {
        log.info("[THREAD: {}] Processing message from {} to {}",
                Thread.currentThread().getName(),
                senderId,
                incomingMessageDTO.getReceiverId());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageId(UUID.randomUUID());
        if(chatMessage.getConversationId() == null) {
            chatMessage.setConversationId(generateConversationId(senderId, incomingMessageDTO.getReceiverId()));
        }
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(incomingMessageDTO.getReceiverId());
        chatMessage.setContent(incomingMessageDTO.getContent());
        chatMessage.setTimestamp(new Timestamp(System.currentTimeMillis()));
        chatMessage.setStatus(MessageStatus.SENT);

        log.info("[THREAD: {}] Saving message with ID: {}",
                Thread.currentThread().getName(), chatMessage.getMessageId());

        return chatMessageRepository.save(chatMessage)
                .doOnSuccess(saved -> {
                    log.info("[THREAD: {}] Saved chat message: {}",
                            Thread.currentThread().getName(), saved.getMessageId());

                    // Step 3: Send to message sync queue);
                    messageQueueService.enqueueMessage(saved);
                });
    }

    private UUID generateConversationId(UUID senderId, UUID receiverId) {
        String combined  = senderId.compareTo(receiverId) < 0 ? senderId.toString() + receiverId.toString() : receiverId.toString() + senderId.toString();
        return UUID.nameUUIDFromBytes(combined.getBytes());
    }

    public Flux<ChatMessage> markDeliveredMessagesAsRead(UUID conversationId, UUID receiverId) {
        return chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED)
                .flatMap(message -> {
                    message.setStatus(MessageStatus.READ);
                    return chatMessageRepository.save(message);
                });
    }
}

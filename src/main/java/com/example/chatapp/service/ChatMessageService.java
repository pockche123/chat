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
    private final KafkaMessageQueueService messageQueueService;
    private final MessageProcessorFactory messageProcessorFactory;
    private final GroupRepository groupRepository;
    private final DirectConversationService directConversationService;
    private final DirectConversationRepository directConversationRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, KafkaMessageQueueService messageQueueService, MessageProcessorFactory messageProcessorFactory, GroupRepository groupRepository, DirectConversationService directConversationService, DirectConversationRepository directConversationRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.messageQueueService = messageQueueService;
        this.messageProcessorFactory = messageProcessorFactory;
        this.groupRepository = groupRepository;

        this.directConversationService = directConversationService;
        this.directConversationRepository = directConversationRepository;
    }

    public Flux<ChatMessage> processIncomingMessage(UUID senderId, IncomingMessageDTO incomingMessageDTO) {
        MessageProcessingStrategy strategy = messageProcessorFactory.getProcessor(incomingMessageDTO.getType());
        return strategy.processMessages(senderId, incomingMessageDTO);
    }

    public Flux<ChatMessage> markDeliveredMessagesAsRead(UUID conversationId, UUID receiverId) {
        return chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED)
                .flatMap(message -> {
                    message.setStatus(MessageStatus.READ);
                    return chatMessageRepository.save(message);
                });
    }

    public Mono<List<UUID>> getReceivers(UUID conversationId, UUID senderId) {
        return groupRepository.findById(conversationId)
                .map(group ->
                        group.getMemberIds().stream()
                                .filter(memberId -> !memberId.equals(senderId))
                                .collect(Collectors.toList())
                        )
                .switchIfEmpty(
                        directConversationService.getOtherParticipant(conversationId, senderId)
                                .map(List::of)
                );
    }
}

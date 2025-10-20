package com.example.chatapp.service.messageprocessor;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.ConversationService;
import com.example.chatapp.service.KafkaMessageQueueService;
import com.example.chatapp.service.MessageQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

@Slf4j
@Component
public class TextMessageProcessor implements MessageProcessingStrategy{

    private final ChatMessageRepository chatMessageRepository;
    private final MessageQueueService messageQueueService;
    private final ConversationService conversationService;

    public TextMessageProcessor(ChatMessageRepository chatMessageRepository, MessageQueueService messageQueueService, ConversationService conversationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.messageQueueService = messageQueueService;
        this.conversationService = conversationService;
    }


//    This needs to change to get GetReceivers() because we there could be many receivers for group chat.
    @Override
    public Flux<ChatMessage> processMessages(UUID currentUserId, IncomingMessageDTO incomingMessageDTO) {

        return conversationService.getReceivers(incomingMessageDTO.getConversationId(), currentUserId)
                        .flatMapMany(receivers -> Flux.fromIterable(receivers)
                                .map(receiverId -> createMessage(currentUserId, receiverId, incomingMessageDTO))
                                .flatMap(chatMessageRepository::save)
                                .doOnNext(messageQueueService::enqueueMessage)
                        );
    }

    private ChatMessage createMessage(UUID senderId, UUID receiverId, IncomingMessageDTO incomingMessageDTO ){
        log.info("[THREAD: {}] Processing message from {} to {}",
                Thread.currentThread().getName(),
                senderId,
                receiverId);
        ChatMessage chatMessage = new ChatMessage();

        if(incomingMessageDTO.getConversationId() == null) {
            chatMessage.setConversationId(generateConversationId(senderId, receiverId));
        } else{
            chatMessage.setConversationId(incomingMessageDTO.getConversationId());
        }
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setContent(incomingMessageDTO.getContent());
        chatMessage.setMessageId(UUID.randomUUID());
        chatMessage.setTimestamp(new Timestamp(System.currentTimeMillis()));
        chatMessage.setStatus(MessageStatus.SENT);

        log.info("[THREAD: {}] Saving message with ID: {}",
                Thread.currentThread().getName(), chatMessage.getMessageId());
        return chatMessage;
    }


    private UUID generateConversationId(UUID senderId, UUID receiverId) {
        String combined  = senderId.compareTo(receiverId) < 0 ? senderId.toString() + receiverId.toString() : receiverId.toString() + senderId.toString();
        return UUID.nameUUIDFromBytes(combined.getBytes());
    }

    @Override
    public boolean canHandle(String messageType) {
        return messageType.equalsIgnoreCase("message");
    }
}

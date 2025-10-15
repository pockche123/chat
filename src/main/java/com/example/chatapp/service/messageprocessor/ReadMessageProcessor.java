package com.example.chatapp.service.messageprocessor;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ReadMessageProcessor implements MessageProcessingStrategy {

    private final ChatMessageRepository chatMessageRepository;

    public ReadMessageProcessor(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public Flux<ChatMessage> processMessages(UUID senderId, IncomingMessageDTO incomingMessageDTO) {
        return null;
//        return chatMessageRepository.findByMessageId(incomingMessageDTO.getMessageId())
//                .flatMap(message -> {
//                    if(message.getStatus() == MessageStatus.DELIVERED) {
//                        message.setStatus(MessageStatus.READ);
//                        return chatMessageRepository.save(message);
//                    } else if(message.getStatus() == MessageStatus.READ) {
//                        return Mono.just(message);
//                    } else{
//                        return Mono.error(new RuntimeException("Message is not delivered yet"));
//                    }
//                });
    }

    @Override
    public boolean canHandle(String messageType) {
        return messageType.equalsIgnoreCase("read_receipt");
    }
}

package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class UndeliveredMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    MessageDeliveryService messageDeliveryService;

    public Flux<ChatMessage> deliverUndeliveredMessage(UUID receiverId){

        return chatMessageRepository.findByReceiverIdAndStatus(receiverId, MessageStatus.SENT.toString())
                .doOnNext(message ->   messageDeliveryService.deliverMessage(message));

    }
}

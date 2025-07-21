package com.example.chatapp.service;


import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.ChatMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;


@Service
public class MessageQueueService {

    @Autowired
    private  ApplicationEventPublisher eventPublisher;

    public void enqueueMessage(ChatMessage message){
        eventPublisher.publishEvent(new ChatMessageEvent(message));
    }

}

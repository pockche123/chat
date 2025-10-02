package com.example.chatapp.service.messageprocessor;

import org.springframework.stereotype.Component;


public class TextMessageProcessor implements MessageProcessingStrategy{
    @Override
    public boolean canHandle(String messageType) {
        return messageType.equalsIgnoreCase("message");
    }
}

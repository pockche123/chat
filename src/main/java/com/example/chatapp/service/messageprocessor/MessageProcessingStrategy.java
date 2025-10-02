package com.example.chatapp.service.messageprocessor;

public interface MessageProcessingStrategy  {
    boolean canHandle(String messageType);

}

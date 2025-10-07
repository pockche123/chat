package com.example.chatapp.service.messageprocessor;

import com.example.chatapp.dto.IncomingMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MessageProcessorFactory {


    private final List<MessageProcessingStrategy> processors;

    public MessageProcessorFactory(List<MessageProcessingStrategy> processors) {
        this.processors = processors;
    }


    public MessageProcessingStrategy getProcessor(String inputType){
        return processors.stream()
                .filter(strategy -> strategy.canHandle(inputType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No strategy found for input type: " + inputType));
    }
}

package com.example.chatapp.unit.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.service.messageprocessor.MessageProcessingStrategy;
import com.example.chatapp.service.messageprocessor.MessageProcessorFactory;
import com.example.chatapp.service.messageprocessor.TextMessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageProcessorFactoryTest {

    @Mock
    private TextMessageProcessor textMessageProcessor;

    @InjectMocks
    private MessageProcessorFactory messageProcessorFactory;

    @BeforeEach
    public void setUp() {
        List<MessageProcessingStrategy> processors = List.of(textMessageProcessor);
        messageProcessorFactory = new MessageProcessorFactory(processors);
    }

    @Test
    void getMessageProcessorStrategy_returnsTextMessageProcessor_whenValidStringInput(){
        String inputType = "message";
        when(textMessageProcessor.canHandle(inputType)).thenReturn(true);

        MessageProcessingStrategy strategy = messageProcessorFactory.getProcessor(inputType);

        assertEquals(strategy.getClass(), TextMessageProcessor.class);

    }

    @Test
    void getMessageProcessorStrategy_throwsException_whenInvalidStringInput(){
        String inputType = "invalid";

        when(textMessageProcessor.canHandle(inputType)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> messageProcessorFactory.getProcessor(inputType));
    }
}

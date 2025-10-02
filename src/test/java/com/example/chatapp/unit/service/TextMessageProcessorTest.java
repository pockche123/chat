package com.example.chatapp.unit.service;

import com.example.chatapp.service.messageprocessor.TextMessageProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TextMessageProcessorTest {

    @InjectMocks
    private TextMessageProcessor textMessageProcessor;

    @Test
    void canHandle_returnsTrueForCorrect_andFalseForIncorrect(){

        assertTrue(textMessageProcessor.canHandle("message"));
        assertFalse(textMessageProcessor.canHandle("read_receipt"));
    }


}

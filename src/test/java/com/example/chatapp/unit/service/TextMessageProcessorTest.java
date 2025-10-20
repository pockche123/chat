package com.example.chatapp.unit.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.ConversationService;
import com.example.chatapp.service.KafkaMessageQueueService;
import com.example.chatapp.service.messageprocessor.TextMessageProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.when;  // CHANGE THIS LINE!



import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class TextMessageProcessorTest {

    @Mock
    private KafkaMessageQueueService kafkaMessageQueueService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private TextMessageProcessor textMessageProcessor;

    @Test
    void canHandle_returnsTrueForCorrect_andFalseForIncorrect(){

        assertTrue(textMessageProcessor.canHandle("message"));
        assertFalse(textMessageProcessor.canHandle("read_receipt"));
    }

    @Test
    void  processMessages_returnsFluxOfChatMessage(){
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();
        incomingMessageDTO.setReceiverId(receiverId);
        incomingMessageDTO.setConversationId(conversationId);
        incomingMessageDTO.setContent("Hello world!");

        List<UUID> uuids = List.of(receiverId);
        when(conversationService.getReceivers(conversationId, senderId)).thenReturn(Mono.just(uuids));


        Mockito.when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        ChatMessage actual = textMessageProcessor.processMessages(senderId, incomingMessageDTO).blockFirst();

        assertNotNull(actual);
        verify(kafkaMessageQueueService).enqueueMessage(actual);
    }




}

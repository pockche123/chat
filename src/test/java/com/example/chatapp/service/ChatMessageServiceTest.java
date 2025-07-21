package com.example.chatapp.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {
    @Mock
    private ChatMessageRepository processIncomingMessageDTORepository;

    @InjectMocks
    private ChatMessageService processIncomingMessageDTOService;

    @Test
    public void test_processIncomingMessage_savestheMessage() {
        IncomingMessageDTO processIncomingMessageDTO = new IncomingMessageDTO();
        processIncomingMessageDTO.setContent("Hello World!");
   ;
        processIncomingMessageDTO.setReceiverId(UUID.randomUUID());
        UUID senderId = UUID.randomUUID();

        when(processIncomingMessageDTORepository.save(any(ChatMessage.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<ChatMessage> result = processIncomingMessageDTOService.processIncomingMessage(senderId, processIncomingMessageDTO);

        assertNotNull(result);
        StepVerifier.create(processIncomingMessageDTOService.processIncomingMessage(senderId, processIncomingMessageDTO))
                .assertNext(saved -> {
                    assertNotNull(saved.getMessageId());
                    assertNotNull(saved.getConversationId());
                    assertNotNull(saved.getTimestamp());
                })
                .verifyComplete();

    }



}

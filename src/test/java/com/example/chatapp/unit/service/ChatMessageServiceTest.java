package com.example.chatapp.unit.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.ChatMessageService;
import com.example.chatapp.service.MessageQueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {
    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Mock
    private MessageQueueService messageQueueService;

    @Test
    public void test_processIncomingMessage_savesAndEnqueuesMessage() {
        // Given
        IncomingMessageDTO processIncomingMessageDTO = new IncomingMessageDTO();
        processIncomingMessageDTO.setContent("Hello World!");
        processIncomingMessageDTO.setReceiverId(UUID.randomUUID());
        UUID senderId = UUID.randomUUID();

        // Mock repository to return the saved message
        when(chatMessageRepository.save(any(ChatMessage.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When
        ChatMessage savedMessage = chatMessageService
            .processIncomingMessage(senderId, processIncomingMessageDTO)
            .block(); // Block to get the actual saved message

        // Then
        assertNotNull(savedMessage);
        assertNotNull(savedMessage.getMessageId());
        assertNotNull(savedMessage.getConversationId());
        assertNotNull(savedMessage.getTimestamp());
        
        // Verify message was enqueued
        verify(messageQueueService).enqueueMessage(savedMessage);
    }

    @Test
    void should_mark_delivered_one_message_as_read(){

        UUID messageId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        ChatMessage deliveredMessage = new ChatMessage();
        deliveredMessage.setMessageId(messageId);
        deliveredMessage.setReceiverId(receiverId);
        deliveredMessage.setStatus(MessageStatus.DELIVERED);

        when(chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED)).thenReturn(Flux.just(deliveredMessage));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(chatMessageService.markDeliveredMessageAsRead(conversationId, receiverId))
                .assertNext(message -> {
                    assertNotNull(message);
                    assertEquals(MessageStatus.READ, message.getStatus());
                }).verifyComplete();
    }



}

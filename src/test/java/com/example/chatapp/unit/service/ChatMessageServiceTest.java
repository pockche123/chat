package com.example.chatapp.unit.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.DirectConversation;
import com.example.chatapp.model.Group;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.repository.DirectConversationRepository;
import com.example.chatapp.repository.GroupRepository;
import com.example.chatapp.service.ChatMessageService;
import com.example.chatapp.service.DirectConversationService;
import com.example.chatapp.service.KafkaMessageQueueService;
import com.example.chatapp.service.MessageQueueService;
import com.example.chatapp.service.messageprocessor.MessageProcessorFactory;
import com.example.chatapp.service.messageprocessor.TextMessageProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {
    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private MessageProcessorFactory messageProcessorFactory;

    @Mock
    private TextMessageProcessor textMessageProcessor;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private DirectConversationRepository directConversationRepository;

    @Mock
    private DirectConversationService directConversationService;

    @InjectMocks
    private ChatMessageService chatMessageService;



    @Test
    public void test_processIncomingMessage_savesAndEnqueuesMessage() {
        // Given
        IncomingMessageDTO processIncomingMessageDTO = new IncomingMessageDTO();
        processIncomingMessageDTO.setContent("Hello World!");

        UUID senderId = UUID.randomUUID();


        // When
        ChatMessage mockSavedMessage = new ChatMessage();
        mockSavedMessage.setMessageId(UUID.randomUUID());

        when(messageProcessorFactory.getProcessor(processIncomingMessageDTO.getType())).thenReturn(textMessageProcessor);
        when(textMessageProcessor.processMessages(senderId, processIncomingMessageDTO)).thenReturn(Flux.just(mockSavedMessage));

        ChatMessage savedMessage = chatMessageService.processIncomingMessage(senderId, processIncomingMessageDTO).blockFirst();

        // Then
        assertNotNull(savedMessage);
        assertEquals(mockSavedMessage.getMessageId(), savedMessage.getMessageId());
        
        // Verify message was enqueued
        verify(messageProcessorFactory).getProcessor(processIncomingMessageDTO.getType());
        verify(textMessageProcessor).processMessages(senderId, processIncomingMessageDTO);
    }

    @Test
    void should_mark_delivered_one_message_as_read(){

        UUID receiverId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        ChatMessage deliveredMessage = createDeliveredMessage(conversationId, receiverId);
        when(chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED)).thenReturn(Flux.just(deliveredMessage));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(chatMessageService.markDeliveredMessagesAsRead(conversationId, receiverId))
                .assertNext(message -> {
                    assertNotNull(message);
                    assertEquals(MessageStatus.READ, message.getStatus());
                }).verifyComplete();
    }


    @Test
    void should_mark_delivered_many_messages_as_read(){

        UUID receiverId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        ChatMessage deliveredMessage = createDeliveredMessage(conversationId, receiverId);
        ChatMessage deliveredMessage2 = createDeliveredMessage(conversationId, receiverId);

        when(chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED)).thenReturn(Flux.just(deliveredMessage, deliveredMessage2));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(chatMessageService.markDeliveredMessagesAsRead(conversationId, receiverId))
                .expectNextCount(2)
                .verifyComplete();

        assertEquals(MessageStatus.READ, deliveredMessage2.getStatus());
        assertEquals(MessageStatus.READ, deliveredMessage.getStatus());
    }






    private ChatMessage createDeliveredMessage(UUID conversationId, UUID receiverId) {
        ChatMessage message = new ChatMessage();
        message.setMessageId(UUID.randomUUID());
        message.setConversationId(conversationId);
        message.setReceiverId(receiverId);
        message.setStatus(MessageStatus.DELIVERED);
        return message;
    }




}

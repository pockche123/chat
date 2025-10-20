package com.example.chatapp.unit.service;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.messageprocessor.ReadMessageProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ReadMessageProcessorTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;



    @InjectMocks
    private ReadMessageProcessor readMessageProcessor;

    @Test
    void canHandleReturnsTrueAndFalse(){
        assertFalse(readMessageProcessor.canHandle("message"));
        assertTrue(readMessageProcessor.canHandle("read_receipt"));
    }

    @Test
    void processMessage_throwsRuntimeException_whenMessageIsNotFound(){
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        IncomingMessageDTO incomingMessageDTO= new IncomingMessageDTO();
        incomingMessageDTO.setType("read_receipt");
        incomingMessageDTO.setConversationId(conversationId);
        incomingMessageDTO.setMessageId(messageId);

//        when(chatMessageRepository.findByConversationIdAndMessageId(conversationId, messageId))
//                .thenReturn(Mono.error(new RuntimeException("Message is not delivered yet")));

        assertThrows(RuntimeException.class, () -> {
            readMessageProcessor.processMessages(senderId, incomingMessageDTO).blockFirst();
        });
    }



    @Test
    void processMessage_throwsRuntimeException_whenMessageIsNotDelivered(){

        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        IncomingMessageDTO incomingMessageDTO= new IncomingMessageDTO();
        incomingMessageDTO.setType("read_receipt");
        incomingMessageDTO.setConversationId(conversationId);
        incomingMessageDTO.setMessageId(messageId);

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setMessageId(messageId);
        message.setReceiverId(receiverId);



        assertThrows(RuntimeException.class, () -> {
            readMessageProcessor.processMessages(senderId, incomingMessageDTO).blockFirst();
        });

    }

    @Test
    void processMessages_changesChatMessageToRead(){
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        ChatMessage mockMessage = new ChatMessage();
        mockMessage.setConversationId(conversationId);
        mockMessage.setMessageId(messageId);
        mockMessage.setReceiverId(receiverId);
        mockMessage.setStatus(MessageStatus.DELIVERED);



        IncomingMessageDTO incomingMessageDTO= new IncomingMessageDTO();
        incomingMessageDTO.setType("read_receipt");
        incomingMessageDTO.setConversationId(conversationId);
        incomingMessageDTO.setMessageId(messageId);

        when(chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED)).thenReturn(Flux.just(mockMessage));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));


        StepVerifier.create(readMessageProcessor.processMessages(receiverId, incomingMessageDTO))
                .assertNext(chatMessage -> {
                    assertEquals(MessageStatus.READ, chatMessage.getStatus());
                        }
                ).verifyComplete();

    }

    @Test
    void processMessages_throwsError_whenRepositoryFails(){
        UUID conversationId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();
        incomingMessageDTO.setType("read_receipt");
        incomingMessageDTO.setConversationId(conversationId);

        when(chatMessageRepository.findByConversationIdAndReceiverIdAndStatus(conversationId, receiverId, MessageStatus.DELIVERED))
                .thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(readMessageProcessor.processMessages(receiverId, incomingMessageDTO))
                .verifyError(RuntimeException.class);
    }
}

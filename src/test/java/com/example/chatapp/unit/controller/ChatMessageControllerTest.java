package com.example.chatapp.unit.controller;

import com.example.chatapp.controller.ChatMessageController;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.MessageStatus;
import com.example.chatapp.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatMessageControllerTest {
    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMessageController chatMessageController;

    @Test
    public void test_getAllMessages_returns_FluxOfMessages(){
        ChatMessage chatMessage = new ChatMessage(
                UUID.fromString("8f1d1e5d-7a22-4d24-92d3-da8147d18ad6"),
                new Timestamp(System.currentTimeMillis()),
                UUID.fromString("a7654562-7ba2-4990-ac41-28d09238852b"),

                "Hello",
                UUID.fromString("ab662615-678b-4399-9a32-8ca8447469c2"),
                UUID.fromString("208f1421-8f36-4f1f-b075-270ea149bb6f"),
                MessageStatus.SENT
        );


        ChatMessage chatMessage2 = new ChatMessage(
                UUID.fromString("8f1d1e5d-7a22-4d24-92d3-da8147d18ad6"),
                new Timestamp(System.currentTimeMillis()),
                UUID.fromString("a7654562-7ba2-4990-ac41-28d09238852b"),

                "How's it going?",
                UUID.fromString("ab662615-678b-4399-9a32-8ca8447469c2"),
                UUID.fromString("208f1421-8f36-4f1f-b075-270ea149bb6f"),
                MessageStatus.SENT
        );




        Mono<ChatMessage> message1 = Mono.just(chatMessage);
        Mono<ChatMessage> message2 = Mono.just(chatMessage2);

        Flux<ChatMessage> expectedMessages = Flux.concat(message1, message2);
        when(chatMessageRepository.findAll()).thenReturn(expectedMessages);

        Flux<ChatMessage> actualMessages = chatMessageController.getAllMessages();
        Flux<String> contents = actualMessages.map(ChatMessage::getContent);

        assertNotNull(actualMessages);
        assertEquals(expectedMessages, actualMessages);
        assertEquals("How's it going?", contents.elementAt(1).block());


    }
}

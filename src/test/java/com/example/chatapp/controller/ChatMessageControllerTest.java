package com.example.chatapp.controller;

import com.example.chatapp.model.ChatMessage;
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
                UUID.fromString("a7654562-7ba2-4990-ac41-28d09238852b"),
                new Timestamp(System.currentTimeMillis()),
                "Hello",
                UUID.fromString("ab662615-678b-4399-9a32-8ca8447469c2"),
                UUID.fromString("208f1421-8f36-4f1f-b075-270ea149bb6f")
        );


        ChatMessage chatMessage2 = new ChatMessage(
                UUID.fromString("8f1d1e5d-7a22-4d24-92d3-da8147d18ad6"),
                UUID.fromString("a7654562-7ba2-4990-ac41-28d09238852b"),
                new Timestamp(System.currentTimeMillis()),
                "How's it going?",
                UUID.fromString("ab662615-678b-4399-9a32-8ca8447469c2"),
                UUID.fromString("208f1421-8f36-4f1f-b075-270ea149bb6f")
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

package com.example.chatapp.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatWebSocketHandlerTests {

    @InjectMocks
    private ChatWebSocketHandler chatWebSocketHandler;

    @Mock
    private WebSocketSession session;

    @Mock
    private WebSocketMessage message;

    @BeforeEach
    public void setUp() {
        chatWebSocketHandler = new ChatWebSocketHandler();
    }

    @Test
    public void basic_testHandle_echoesMessages(){
//        Arrange
        WebSocketSession session = mock(WebSocketSession.class);
        WebSocketMessage incomingMessage = mock(WebSocketMessage.class);
        WebSocketMessage outgoingMessage = mock(WebSocketMessage.class);

//        Pretend client sends one message: "hello"
        when(incomingMessage.getPayloadAsText()).thenReturn("hello");
        when(session.receive()).thenReturn(Flux.just(incomingMessage));
        when(session.textMessage("Echo: hello")).thenReturn(outgoingMessage);

//        "Hey mock session: when someone calls your send(...) method with any input, just act like you sent something and completed successfully — don’t throw an error or hang."
        when(session.send(any())).thenReturn(Mono.empty());

//        Capture what the handler tries to send
        ArgumentCaptor<Flux<WebSocketMessage>> captor = ArgumentCaptor.forClass(Flux.class);
        when(session.send(captor.capture())).thenReturn(Mono.empty());

//        Act
        ChatWebSocketHandler chatWebSocketHandler = new ChatWebSocketHandler();
//        block() is a subscriber but subscribe is asynchronous because starts the stream and waits to finish . you can be sure that everything has run before the test ends.
        chatWebSocketHandler.handle(session).block();

//        Assert
        Flux<WebSocketMessage> sentFlux = captor.getValue();
        WebSocketMessage sent = sentFlux.blockFirst(); // Just take the first message
        assertEquals(outgoingMessage, sent);
    }




}

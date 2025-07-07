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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatWebSocketHandlerTests {

    @InjectMocks
    private ChatWebSocketHandler chatWebSocketHandler;

    @Mock
    private WebSocketSession session;

    @Mock
    private WebSocketMessage incomingMessage;

    @BeforeEach
    public void setUp() {
        chatWebSocketHandler = new ChatWebSocketHandler();
        incomingMessage = mock(WebSocketMessage.class);
        session = mock(WebSocketSession.class);
    }

    @Test
    public void basic_testHandle_echoesMessages(){
//        Arrange
//        WebSocketMessage incomingMessage = mock(WebSocketMessage.class);
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
//        block() is a subscriber but subscribe is asynchronous because starts the stream and waits to finish . you can be sure that everything has run before the test ends.
        chatWebSocketHandler.handle(session).block();

//        Assert
        Flux<WebSocketMessage> sentFlux = captor.getValue();
        WebSocketMessage sent = sentFlux.blockFirst(); // Just take the first message
        assertEquals(outgoingMessage, sent);
    }

    @Test
    public void testHandle_echoesMultipleMessages(){

//        Mock incoming messages
      WebSocketMessage incomingMessage1 = mock(WebSocketMessage.class);
      WebSocketMessage incomingMessage2 = mock(WebSocketMessage.class);
      when(incomingMessage1.getPayloadAsText()).thenReturn("hello");
      when(incomingMessage2.getPayloadAsText()).thenReturn("world");
      when(session.receive()).thenReturn(Flux.just(incomingMessage1, incomingMessage2));

//      Mock outgoing message dymanically
        when(session.textMessage(anyString())).thenAnswer(invocation -> {
            String payload = invocation.getArgument(0);
            WebSocketMessage outgoingMessage = mock(WebSocketMessage.class);
            when(outgoingMessage.getPayloadAsText()).thenReturn(payload);
            return outgoingMessage;
        });

//        Capture the outgoing Flux passed to send using stubbing
        ArgumentCaptor<Flux<WebSocketMessage>> captor = ArgumentCaptor.forClass(Flux.class);
        when(session.send(captor.capture())).thenReturn(Mono.empty());

        Mono<Void> result = chatWebSocketHandler.handle(session);


        // Create + assert in one chained call
        StepVerifier.create(result).verifyComplete();

//       Verify that the correct messages were sent
        Flux<WebSocketMessage> sent = captor.getValue();
        StepVerifier.create(sent.map(WebSocketMessage::getPayloadAsText))
                .expectNext("Echo: hello")
                .expectNext("Echo: world")
                .verifyComplete();


    }




}

package com.example.chatapp.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


//handler manages the persistent WebSocket connection with User B, sending and receiving messages reactively. 
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
//        receives messages from the client
        Flux<WebSocketMessage> incomingMessages = session.receive();
        Flux<WebSocketMessage> outgoingMessages = incomingMessages
                .map(msg -> session.textMessage("Echo: " + msg.getPayloadAsText()));
        return session.send(outgoingMessages);
    }
}

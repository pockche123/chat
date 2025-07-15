package com.example.chatapp.handler;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
//handler manages the persistent WebSocket connection with User B, sending and receiving messages reactively. 
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ChatMessageService chatMessageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

//    @Override
//    public Mono<Void> handle(WebSocketSession session) {
//        log.info("WebSocket connection established");
//        return session.send(
//                Mono.just(session.textMessage("Welcome to WebSocket!"))
//        );
//    }


//
//    @Override
//    public Mono<Void> handle(WebSocketSession session) {
////        receives messages from the client
//        Flux<WebSocketMessage> incomingMessages = session.receive();
//        Flux<WebSocketMessage> outgoingMessages = incomingMessages
//                .map(msg -> session.textMessage("Echo: " + msg.getPayloadAsText()));
//        return session.send(outgoingMessages);
//    }
//

    @Override
    public Mono<Void> handle(WebSocketSession session) {


        log.info("Hitting here!!!");
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(json -> {
                    log.info("Received JSON: {}", json);
                    try {
                        IncomingMessageDTO msg = objectMapper.readValue(json, IncomingMessageDTO.class);
                        return chatMessageService.processIncomingMessage(msg);
                    } catch (Exception e) {
                        log.error("Failed to parse JSON: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Invalid message format", e));
                    }
                })
                .then();
    }
}

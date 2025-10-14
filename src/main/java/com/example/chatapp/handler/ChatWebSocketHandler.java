package com.example.chatapp.handler;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.ChatMessageService;
import com.example.chatapp.service.OnlineUserService;
import com.example.chatapp.service.MessageDeliveryService;
import com.example.chatapp.service.ServerRegistryService;
import com.example.chatapp.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Slf4j
//handler manages the persistent WebSocket connection with User B, sending and receiving messages reactively. 
@Component
public class ChatWebSocketHandler implements WebSocketHandler {


    private final ChatMessageService chatMessageService;


    private final ObjectMapper objectMapper;

    private final JwtUtil jwtUtil;

    private final OnlineUserService onlineUserService;

    private final MessageDeliveryService messageDeliverService;

    public ChatWebSocketHandler(ChatMessageService chatMessageService, ObjectMapper objectMapper, JwtUtil jwtUtil, OnlineUserService onlineUserService, MessageDeliveryService messageDeliveryService) {
        this.chatMessageService = chatMessageService;
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.onlineUserService = onlineUserService;
        this.messageDeliverService = messageDeliveryService;
    }

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

     return authenticateSession(session)
                .flatMap(senderId -> registerSession(senderId, session)
                        .thenMany(session.receive()
                                .map(WebSocketMessage::getPayloadAsText)
                                .flatMap(json -> processIncomingMessage(json, senderId)))
                        .doFinally(signal -> cleanUp(senderId))
                        .then())
                .onErrorResume(error -> {
                    log.error("Error processing message: {}", error.getMessage());
                    if (error.getMessage().equals("Unauthorized")) {
                        return session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
                    }
                    return Mono.error(error);

                        });
    }

    public Mono<UUID> authenticateSession(WebSocketSession session) {
        String token = session.getHandshakeInfo()
                .getHeaders()
                .getFirst("Authorization");
        if (!jwtUtil.validateToken(token)){
            log.error("Failed to validate token: {}", token);
            return Mono.error(new RuntimeException("Unauthorized"));
        }
        return Mono.just(jwtUtil.getUserIdFromToken(token));
    }


    public Mono<Void> registerSession(UUID senderId, WebSocketSession session) {
        return onlineUserService.markUserOnline(senderId)
                .doOnSuccess((ignored) -> {  messageDeliverService.registerSession(senderId, session);});

    }

    public Mono<ChatMessage> processIncomingMessage(String json, UUID senderId){
        try {
            IncomingMessageDTO msg = objectMapper.readValue(json, IncomingMessageDTO.class);
            return chatMessageService.processIncomingMessage(senderId, msg);
        } catch (Exception e) {
            log.error("[THREAD: {}] failed to parse JSON: {}", Thread.currentThread().getName(),  e.getMessage());
            return Mono.error(new RuntimeException("Invalid message format", e));
        }
    }

    public void cleanUp(UUID senderId) {
        onlineUserService.markUserOffline(senderId);
        messageDeliverService.removeSession(senderId);
    }
}



//    @Override
//    public Mono<Void> handle(WebSocketSession session) {
//        HandshakeInfo info = session.getHandshakeInfo();
//        HttpHeaders headers = info.getHeaders();
//
//        String token = headers.getFirst("Authorization");
//        if (!isValidToken(token)) {
//            return session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
//        }
//
//        // Optional: extract user info from token
//        String userId = extractUserIdFromToken(token);
//        session.getAttributes().put("userId", userId);
//
//        // Handle messaging here
//        return session.receive()
//                .map(WebSocketMessage::getPayloadAsText)
//                .flatMap(message -> handleMessage(userId, message, session))
//                .then();
//    }
//
//    private boolean isValidToken(String token) {
//        // Add real JWT validation here
//        return token != null && token.startsWith("Bearer ");
//    }
//
//    private String extractUserIdFromToken(String token) {
//        // Decode and return userId
//        return "user123"; // mock
//    }
//
//    private Mono<Void> handleMessage(String userId, String message, WebSocketSession session) {
//        // Save message, send to broker, etc.
//        return Mono.empty();
//    }



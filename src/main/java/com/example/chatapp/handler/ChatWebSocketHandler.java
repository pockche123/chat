package com.example.chatapp.handler;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.service.ChatMessageService;
import com.example.chatapp.service.OnlineUserService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import com.example.chatapp.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Slf4j
//handler manages the persistent WebSocket connection with User B, sending and receiving messages reactively. 
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private  ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;

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

        String token = session.getHandshakeInfo()
                .getHeaders()
                .getFirst("Authorization");

        if (!jwtUtil.validateToken(token)) {
            log.error("[THREAD: {}]  failed to validate token: {}",
                    Thread.currentThread().getName(), token);
            return session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
        }

        UUID senderId = jwtUtil.getUserIdFromToken(token);

        
        return onlineUserService.markUserOnline(senderId)
                        .doOnSuccess((ignored) -> {  webSocketMessageDeliveryService.registerSession(senderId, session);})
                                .thenMany(
                session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(json -> {
                    try {
                        IncomingMessageDTO msg = objectMapper.readValue(json, IncomingMessageDTO.class);
                        if(msg.getType().equals("read_receipt")){
                            return chatMessageService.markDeliveredMessagesAsRead(msg.getConversationId(), msg.getReceiverId());
                        }

                        return chatMessageService.processIncomingMessage(senderId, msg);
                    } catch (Exception e) {
                        log.error("[THREAD: {}] failed to parse JSON: {}",
                                Thread.currentThread().getName(),  e.getMessage());
                        return Mono.error(new RuntimeException("Invalid message format", e));
                    }
                }))
                .doFinally(signal -> {
                    onlineUserService.markUserOffline(senderId);
                    webSocketMessageDeliveryService.removeSession(senderId);

                })
                .then();
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
}

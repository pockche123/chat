package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class DistributedMessageDeliveryService implements  MessageDeliveryService{

    private final ServerRegistryService serverRegistry;
    private final String currentServerId;
    private final WebSocketMessageDeliveryService webSocketMessageDeliveryService;
    private final KafkaTemplate kafkaTemplate;
    private final String serverAddress;

    public DistributedMessageDeliveryService(ServerRegistryService serverRegistry, @Value("${server.id:default-server}") String serverId, WebSocketMessageDeliveryService webSocketMessageDeliveryService, KafkaTemplate kafkaTemplate, @Value("${chat.server.address:localhost:8080}") String serverAddress){
        this.serverRegistry = serverRegistry;
        this.currentServerId = serverId;
        this.webSocketMessageDeliveryService = webSocketMessageDeliveryService;
        this.kafkaTemplate = kafkaTemplate;
        this.serverAddress = serverAddress;
    }

    @Override
    public Mono<ChatMessage> deliverMessage(ChatMessage message) {
        return serverRegistry.findUserServer(message.getReceiverId())
                .flatMap(serverId -> {
                    if(isCurrentServer(serverId)){
                        return webSocketMessageDeliveryService.deliverMessage(message);
                    } else{
                        return forwardToKafka(message, serverId);
                    }
                });

    }

    @Override
    public void registerSession(UUID userId, WebSocketSession session) {
        serverRegistry.registerUserServer(userId, serverAddress).subscribe();
        webSocketMessageDeliveryService.registerSession(userId, session);
    }

    @Override
    public void removeSession(UUID userId) {
        serverRegistry.unregisterUser(userId).subscribe();
        webSocketMessageDeliveryService.removeSession(userId);
    }

    public boolean isCurrentServer(String serverId){
        return serverId.equals(currentServerId);
    }

    private Mono<ChatMessage> forwardToKafka(ChatMessage message, String serverId){
        return Mono.fromCallable(() -> {
            kafkaTemplate.send("chat-messages", message.getReceiverId().toString(), message);
            log.info("Forwarded message {} via chat-messages topic", message.getMessageId());
            return message;
        });
    }

    public String getCurrentServerId(){
        return currentServerId;
    }

}

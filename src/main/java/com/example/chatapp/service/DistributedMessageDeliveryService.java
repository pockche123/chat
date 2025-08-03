package com.example.chatapp.service;

import com.example.chatapp.model.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DistributedMessageDeliveryService implements  MessageDeliveryService{

    private final ServerRegistryService serverRegistry;
    private final String currentServerId;
    private final WebSocketMessageDeliveryService webSocketMessageDeliveryService;

    public DistributedMessageDeliveryService(ServerRegistryService serverRegistry, @Value("${server.id:default-server}") String serverId, WebSocketMessageDeliveryService webSocketMessageDeliveryService){
        this.serverRegistry = serverRegistry;
        this.currentServerId = serverId;
        this.webSocketMessageDeliveryService = webSocketMessageDeliveryService;
    }

    @Override
    public Mono<ChatMessage> deliverMessage(ChatMessage message) {
        return serverRegistry.findUserServer(message.getReceiverId())
                .flatMap(serverId -> {
                    if(isCurrentServer(serverId)){
                        return webSocketMessageDeliveryService.deliverMessage(message);
                    } else{
                        return Mono.just(message);
                    }
                });

    }

    public boolean isCurrentServer(String serverId){
        return serverId.equals(currentServerId);
    }

}

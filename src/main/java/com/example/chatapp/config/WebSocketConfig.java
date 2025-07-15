package com.example.chatapp.config;

import com.example.chatapp.handler.ChatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.Map;


@Configuration
public class WebSocketConfig {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;


    @Bean
    public HandlerMapping webSocketMapping(){
        return new SimpleUrlHandlerMapping(
                Map.of("/ws/chat", chatWebSocketHandler),
                -1
        );
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter(){
        return new WebSocketHandlerAdapter();
    }


}

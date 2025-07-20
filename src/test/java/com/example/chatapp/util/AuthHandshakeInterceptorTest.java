package com.example.chatapp.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthHandshakeInterceptorTest {
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private WebSocketHandler wsHandler;

    @Mock
    private ServerHttpResponse response;

//
//
//
//    @InjectMocks
//    private AuthHandshakeInterceptor interceptor;
//
//    @Test
//    public void beforeHandshake_ValidToken_SetUsernameAttribute() throws Exception {
////        Setup
//        String validToken = "validToken";
//        String username = "testUser";
//        Map<String, Object> attributes = new HashMap<>();
//        URI uri = new URI("ws://localhost/ws?token=" + validToken);
//
//        when(request.getURI()).thenReturn(uri);
//        when(jwtUtil.validateToken(validToken)).thenReturn(true);
//        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(username);
//
////        Actual
//        boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);
//
////        Assert
//        assertTrue(result);
//    }
}

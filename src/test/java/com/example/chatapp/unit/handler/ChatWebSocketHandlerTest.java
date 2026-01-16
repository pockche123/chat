package com.example.chatapp.unit.handler;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.exception.RateLimitExceededException;
import com.example.chatapp.handler.ChatWebSocketHandler;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.RateLimitTier;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.service.*;
import com.example.chatapp.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatWebSocketHandlerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LocalOnlineUserService localOnlineUserService;

    @Mock
    private WebSocketMessageDeliveryService webSocketMessageDeliveryService;

    @Mock
    private SlidingWindowCounterRateLimiter rateLimiter;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RateLimitService rateLimitService;



    @InjectMocks
    private ChatWebSocketHandler chatWebSocketHandler;

    @BeforeEach
    public void setUp() {
        reset(objectMapper, chatMessageService,  rateLimiter);

    }



    @Test
    void test_handle_validJson_callsService(){
//        Given
        WebSocketSession session = mock(WebSocketSession.class);
        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();


        incomingMessageDTO.setContent("Hello World!");
        UUID senderId = UUID.randomUUID();
        
        // Mock handshake info
        when(session.getHandshakeInfo()).thenReturn(mock(org.springframework.web.reactive.socket.HandshakeInfo.class));
        when(session.getHandshakeInfo().getHeaders()).thenReturn(mock(org.springframework.http.HttpHeaders.class));
        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(senderId);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);


        User user = new User();
        user.setTier("free");
        RateLimitTier tierLimit = new RateLimitTier("free", 20, 1);

        when(userRepository.findById(senderId)).thenReturn(Mono.just(user));
        when(rateLimitService.getRateLimit("free")).thenReturn(Mono.just(tierLimit));
        when(rateLimiter.isAllowed(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(true)); // Allow rate limit to pass




        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setContent(incomingMessageDTO.getContent());

        String json = "{\"receiverId: mockID, content\":\"Hello World!\"}";

        WebSocketMessage webSocketMessage = mock(WebSocketMessage.class);
        when(webSocketMessage.getPayloadAsText()).thenReturn(json);

        Flux<WebSocketMessage> input = Flux.just(webSocketMessage);
        when(session.receive()).thenReturn(input);

        // Mock the ObjectMapper to return our DTO when parsing the JSON
        try {
            when(objectMapper.readValue(json, IncomingMessageDTO.class)).thenReturn(incomingMessageDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(chatMessageService.processIncomingMessage(any(UUID.class), any(IncomingMessageDTO.class))).thenReturn(Flux.just(chatMessage));
        when(localOnlineUserService.markUserOnline(any(UUID.class))).thenReturn(Mono.empty());


//        When
        Mono<Void> result = chatWebSocketHandler.handle(session);

//        Then
        StepVerifier.create(result).verifyComplete();
        verify(chatMessageService, times(1)).processIncomingMessage(any(UUID.class), any(IncomingMessageDTO.class));
    }

    @Test
    void test_handle_invalidJson_callsService(){
        // Given
        WebSocketSession session = mock(WebSocketSession.class);
        
        // Mock handshake info
        when(session.getHandshakeInfo()).thenReturn(mock(HandshakeInfo.class));
        when(session.getHandshakeInfo().getHeaders()).thenReturn(mock(HttpHeaders.class));
        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(UUID.randomUUID());
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(localOnlineUserService.markUserOnline(any(UUID.class))).thenReturn(Mono.empty());


        WebSocketMessage webSocketMessage = mock(WebSocketMessage.class);
        when(webSocketMessage.getPayloadAsText()).thenReturn("{invalid_json}");

        // When
        when(session.receive()).thenReturn(Flux.just(webSocketMessage));
        
        // Act
        Mono<Void> result = chatWebSocketHandler.handle(session);
        
        // Verify
        StepVerifier.create(result).expectError(RuntimeException.class).verify();
    }

    @Test
    void test_handle_invalidToken(){
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getHandshakeInfo()).thenReturn(mock(HandshakeInfo.class));
        when(session.getHandshakeInfo().getHeaders()).thenReturn(mock(HttpHeaders.class));
        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn(null);
        when(jwtUtil.validateToken(null)).thenReturn(false);
        when(session.close(any(CloseStatus.class))).thenReturn(Mono.empty());



        Mono<Void> result = chatWebSocketHandler.handle(session);

        StepVerifier.create(result).verifyComplete();
        verify(session).close(any(CloseStatus.class));

    }



    @Test
    void should_MarkUserOffline_whenDisconnected(){
        WebSocketSession session = mock(WebSocketSession.class, RETURNS_DEEP_STUBS);
        UUID userId = UUID.randomUUID();

//        Mock JWT validation
        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(userId);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(localOnlineUserService.markUserOnline(any(UUID.class))).thenReturn(Mono.empty());


        // Mock session.receive() to complete immediately
        when(session.receive()).thenReturn(Flux.empty());
        chatWebSocketHandler.handle(session).block();


        verify(localOnlineUserService).markUserOnline(userId);
        verify(localOnlineUserService).markUserOffline(userId);

    }

    @Test
    void should_register_session_when_connected(){
        WebSocketSession session = mock(WebSocketSession.class, RETURNS_DEEP_STUBS);
        UUID userId = UUID.randomUUID();

        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(userId);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(session.receive()).thenReturn(Flux.empty());

        when(localOnlineUserService.markUserOnline(any(UUID.class))).thenReturn(Mono.empty());



        chatWebSocketHandler.handle(session).block();

        verify(webSocketMessageDeliveryService).registerSession(userId, session);
//        verify(webSocketMessageDeliveryService, never()).removeSession(userId);

    }

    @Test
    void should_remove_session_when_disconnected(){
        WebSocketSession session = mock(WebSocketSession.class, RETURNS_DEEP_STUBS);
        UUID userId = UUID.randomUUID();

//        Mock JWT validation
        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(userId);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(localOnlineUserService.markUserOnline(any(UUID.class))).thenReturn(Mono.empty());


        // Mock session.receive() to complete immediately
        when(session.receive()).thenReturn(Flux.empty());
        chatWebSocketHandler.handle(session).block();

        verify(webSocketMessageDeliveryService).registerSession(userId, session);
        verify(webSocketMessageDeliveryService).removeSession(userId);
    }

    @Test
    void authenticateSession_rejectsInvalidToken(){
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getHandshakeInfo()).thenReturn(mock(HandshakeInfo.class));
        when(session.getHandshakeInfo().getHeaders()).thenReturn(mock(HttpHeaders.class));
        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("invalidToken");

        when(jwtUtil.validateToken("falseToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> chatWebSocketHandler.authenticateSession(session).block());
    }

    @Test
    void authenticateSession_acceptsValidToken(){
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getHandshakeInfo()).thenReturn(mock(HandshakeInfo.class));
        when(session.getHandshakeInfo().getHeaders()).thenReturn(mock(HttpHeaders.class));
        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("validToken");

        UUID sessionID = UUID.randomUUID();
        when(jwtUtil.validateToken("validToken")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("validToken")).thenReturn(UUID.randomUUID());
        when(jwtUtil.getUserIdFromToken("validToken")).thenReturn(sessionID);

        UUID actualSessionId = chatWebSocketHandler.authenticateSession(session).block();

        assertEquals(sessionID, actualSessionId);
    }

    @Test
    void registerSession_registersSession(){
        WebSocketSession session = mock(WebSocketSession.class);

        UUID senderId = UUID.randomUUID();
        when(localOnlineUserService.markUserOnline(senderId)).thenReturn(Mono.empty());

        chatWebSocketHandler.registerSession(senderId, session).block();

        verify(webSocketMessageDeliveryService).registerSession(senderId, session);
    }


    @Test
    void processIncomingMessage_processesMessage() throws JsonProcessingException {
        UUID senderId = UUID.randomUUID();
        User user = new User();
        user.setTier("free");
        RateLimitTier tierLimit = new RateLimitTier("free", 20, 1);

        when(userRepository.findById(senderId)).thenReturn(Mono.just(user));
        when(rateLimitService.getRateLimit("free")).thenReturn(Mono.just(tierLimit));
        when(rateLimiter.isAllowed(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(true)); // Allow rate limit to pass

        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();
        String json = "{}";
        ChatMessage expectedMessage = new ChatMessage();
        when(objectMapper.readValue(anyString(), eq(IncomingMessageDTO.class))).thenReturn(incomingMessageDTO);
        when(chatMessageService.processIncomingMessage(senderId, incomingMessageDTO)).thenReturn(Flux.just(expectedMessage));


        Mono<ChatMessage> result = chatWebSocketHandler.processIncomingMessage(json, senderId).next();

        StepVerifier.create(result)
                .expectNext(expectedMessage)
                .verifyComplete();
    }

    @Test
    void  processIncomingMessage_throwsError() throws JsonProcessingException {
        UUID senderId = UUID.randomUUID();
        User user = new User();
        user.setTier("free");
        RateLimitTier tierLimit = new RateLimitTier("free", 20, 1);

        when(userRepository.findById(senderId)).thenReturn(Mono.just(user));
        when(rateLimitService.getRateLimit("free")).thenReturn(Mono.just(tierLimit));
        when(rateLimiter.isAllowed(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(true)); // Allow rate limit to pass


        when(objectMapper.readValue(anyString(), eq(IncomingMessageDTO.class))).thenThrow(new RuntimeException("Invalid JSON"));

        StepVerifier.create(chatWebSocketHandler.processIncomingMessage("invalidJson", senderId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void cleanUp_marksUserOffline_andRemovesSession(){

        UUID senderId = UUID.randomUUID();

        chatWebSocketHandler.cleanUp(senderId);
        verify(localOnlineUserService).markUserOffline(senderId);
        verify(webSocketMessageDeliveryService).removeSession(senderId);
    }

    @Test
    void handle_unauthorisedError_closesSession(){
        WebSocketSession session = mock(WebSocketSession.class, RETURNS_DEEP_STUBS);

        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("invalid");
        when(jwtUtil.validateToken("invalid")).thenReturn(false);
        when(session.close(any(CloseStatus.class))).thenReturn(Mono.empty());

        StepVerifier.create(chatWebSocketHandler.handle(session))
                .verifyComplete();

        verify(session).close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));

    }

    @Test
    void handle_otherError_propagatesError(){
        WebSocketSession session = mock(WebSocketSession.class, RETURNS_DEEP_STUBS);
        RuntimeException exception = new RuntimeException("Some error");

        UUID userId = UUID.randomUUID();

        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("valid");
        when(jwtUtil.validateToken("valid")).thenReturn(true);
        when(jwtUtil.getUserIdFromToken("valid")).thenReturn(userId);
        when(localOnlineUserService.markUserOnline(any(UUID.class))).thenReturn(Mono.error(exception));


        StepVerifier.create(chatWebSocketHandler.handle(session))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void processIncomingMessage_throwsRateLimitException_whenLimitExceeded(){
        UUID userId = UUID.randomUUID();
        String json = "{\"recipientId\":\"123\",\"content\":\"test\"}";

        User user = new User();


        when(rateLimiter.isAllowed(anyString(), anyInt(), any(Duration.class)))
                .thenReturn(Mono.just(false));

        when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        when(rateLimitService.getRateLimit(user.getTier())).thenReturn(Mono.just(new RateLimitTier()));


        StepVerifier.create(chatWebSocketHandler.processIncomingMessage(json, userId))
                .expectError(RateLimitExceededException.class)
                .verify();

    }
}
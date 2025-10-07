package com.example.chatapp.unit.handler;

import com.example.chatapp.dto.IncomingMessageDTO;
import com.example.chatapp.handler.ChatWebSocketHandler;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.ChatMessageService;
import com.example.chatapp.service.LocalOnlineUserService;
import com.example.chatapp.service.WebSocketMessageDeliveryService;
import com.example.chatapp.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @InjectMocks
    private ChatWebSocketHandler chatWebSocketHandler;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.reset(jwtUtil);
    }


    @Test
    void test_handle_validJson_callsService(){
//        Given
        WebSocketSession session = mock(WebSocketSession.class);
        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();

        incomingMessageDTO.setReceiverId(UUID.randomUUID());
        incomingMessageDTO.setContent("Hello World!");
        UUID senderId = UUID.randomUUID();
        
        // Mock handshake info
        when(session.getHandshakeInfo()).thenReturn(mock(org.springframework.web.reactive.socket.HandshakeInfo.class));
        when(session.getHandshakeInfo().getHeaders()).thenReturn(mock(org.springframework.http.HttpHeaders.class));
        when(session.getHandshakeInfo().getHeaders().getFirst("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(senderId);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);



        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(incomingMessageDTO.getReceiverId());
        chatMessage.setContent(incomingMessageDTO.getContent());

        String json = "{\"receiverId\":\"" + incomingMessageDTO.getReceiverId() + "\",\"content\":\"Hello World!\"}";

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

        when(chatMessageService.processIncomingMessage(any(UUID.class), any(IncomingMessageDTO.class))).thenReturn(Mono.just(chatMessage));
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
        IncomingMessageDTO incomingMessageDTO = new IncomingMessageDTO();
        String json = "{}";
        UUID senderId = UUID.randomUUID();
        ChatMessage expectedMessage = new ChatMessage();
        when(objectMapper.readValue(anyString(), eq(IncomingMessageDTO.class))).thenReturn(incomingMessageDTO);
        when(chatMessageService.processIncomingMessage(senderId, incomingMessageDTO)).thenReturn(Mono.just(expectedMessage));


        Mono<ChatMessage> result = chatWebSocketHandler.processIncomingMessage(json, senderId);

        StepVerifier.create(result)
                .expectNext(expectedMessage)
                .verifyComplete();
    }

    @Test
    void  processIncomingMessage_throwsError() throws JsonProcessingException {

        when(objectMapper.readValue(anyString(), eq(IncomingMessageDTO.class))).thenThrow(new RuntimeException("Invalid JSON"));
        UUID senderId = UUID.randomUUID();

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






//    @InjectMocks
//    private ChatWebSocketHandler chatWebSocketHandler;
//
//    @Mock
//    private WebSocketSession session;
//
//    @Mock
//    private WebSocketMessage incomingMessage;
//
//    @BeforeEach
//    public void setUp() {
//        chatWebSocketHandler = new ChatWebSocketHandler();
//        incomingMessage = mock(WebSocketMessage.class);
//        session = mock(WebSocketSession.class);
//    }
//
//    @Test
//    public void basic_testHandle_echoesMessages(){
////        Arrange
////        WebSocketMessage incomingMessage = mock(WebSocketMessage.class);
//        WebSocketMessage outgoingMessage = mock(WebSocketMessage.class);
//
////        Pretend client sends one message: "hello"
//        when(incomingMessage.getPayloadAsText()).thenReturn("hello");
//        when(session.receive()).thenReturn(Flux.just(incomingMessage));
//        when(session.textMessage("Echo: hello")).thenReturn(outgoingMessage);
//
////        "Hey mock session: when someone calls your send(...) method with any input, just act like you sent something and completed successfully — don’t throw an error or hang."
//        when(session.send(any())).thenReturn(Mono.empty());
//
////        Capture what the handler tries to send
//        ArgumentCaptor<Flux<WebSocketMessage>> captor = ArgumentCaptor.forClass(Flux.class);
//        when(session.send(captor.capture())).thenReturn(Mono.empty());
//
////        Act
////        block() is a subscriber but subscribe is asynchronous because starts the stream and waits to finish . you can be sure that everything has run before the test ends.
//        chatWebSocketHandler.handle(session).block();
//
////        Assert
//        Flux<WebSocketMessage> sentFlux = captor.getValue();
//        WebSocketMessage sent = sentFlux.blockFirst(); // Just take the first message
//        assertEquals(outgoingMessage, sent);
//    }
//
//    @Test
//    public void testHandle_echoesMultipleMessages(){
//
////        Mock incoming messages
//      WebSocketMessage incomingMessage1 = mock(WebSocketMessage.class);
//      WebSocketMessage incomingMessage2 = mock(WebSocketMessage.class);
//      when(incomingMessage1.getPayloadAsText()).thenReturn("hello");
//      when(incomingMessage2.getPayloadAsText()).thenReturn("world");
//      when(session.receive()).thenReturn(Flux.just(incomingMessage1, incomingMessage2));
//
////      Mock outgoing message dymanically
//        when(session.textMessage(anyString())).thenAnswer(invocation -> {
//            String payload = invocation.getArgument(0);
//            WebSocketMessage outgoingMessage = mock(WebSocketMessage.class);
//            when(outgoingMessage.getPayloadAsText()).thenReturn(payload);
//            return outgoingMessage;
//        });
//
////        Capture the outgoing Flux passed to send using stubbing
//        ArgumentCaptor<Flux<WebSocketMessage>> captor = ArgumentCaptor.forClass(Flux.class);
//        when(session.send(captor.capture())).thenReturn(Mono.empty());
//
//        Mono<Void> result = chatWebSocketHandler.handle(session);
//
//
//        // Create + assert in one chained call
//        StepVerifier.create(result).verifyComplete();
//
////       Verify that the correct messages were sent
//        Flux<WebSocketMessage> sent = captor.getValue();
//        StepVerifier.create(sent.map(WebSocketMessage::getPayloadAsText))
//                .expectNext("Echo: hello")
//                .expectNext("Echo: world")
//                .verifyComplete();
//    }
}

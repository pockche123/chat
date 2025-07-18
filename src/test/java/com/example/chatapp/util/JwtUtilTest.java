package com.example.chatapp.util;

import com.example.chatapp.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {


    private final String SECRET_KEY = "01234567890123456789012345678901";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    private final JwtUtil jwtUtil = new JwtUtil(SECRET_KEY);


    @Test
    public void test_generateToken_returnsValidToken(){
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");

        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        assertEquals("username", claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }
}

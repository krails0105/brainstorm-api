package com.brainstorm.brainstorm_api.config.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final SecretKey secret;

    private final long expiration;

    public JwtProvider(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        this.secret = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    public String createToken(UUID userId) {
        Date date = new Date();
        Date expirationDate = new Date(date.getTime() + expiration);

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .signWith(this.secret)
            .issuedAt(date)
            .expiration(expirationDate)
            .compact();
    }

    public UUID getUserIdFromToken(String token) {
        String subject = Jwts.parser()
            .verifyWith(this.secret)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();

        return UUID.fromString(subject);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(this.secret)
                .build()
                .parseSignedClaims(token);
            return true;
        }
        catch (JwtException e) {
            return false;
        }
    }

}

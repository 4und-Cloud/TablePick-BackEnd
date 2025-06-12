package com.goorm.tablepick.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 1000 * 60 * 60 * 24; // access - 24 hours
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 1000 * 60 * 60 * 24 * 7; // refresh - 1 week

    private Key key;

    @PostConstruct
    public void init() {
        // Generate a secure key for HS512
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    public String createAccessToken(Long userId, String email) {
        return createToken(userId, email, ACCESS_TOKEN_EXPIRATION_MS);
    }

    public String createRefreshToken(Long userId, String email) {
        return createToken(userId, email, REFRESH_TOKEN_EXPIRATION_MS);
    }

    private String createToken(Long userId, String email, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            return Instant.now().isBefore(expiration.toInstant());
        } catch (ExpiredJwtException e) {
            // Token expired, return false
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Return claims even if token is expired
            return e.getClaims();
        }
    }

    // Extract email claim from JWT
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("email", String.class);
    }
}
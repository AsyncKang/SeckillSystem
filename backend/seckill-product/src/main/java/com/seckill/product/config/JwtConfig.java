package com.seckill.product.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtConfig {

    private final SecretKey key;

    public JwtConfig(@Value("${app.jwt.secret:seckill-jwt-secret-change-in-production-min-256bits}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getUserIdFromToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }
        String token = bearerToken.substring(7);
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean isAdmin(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) return false;
        String token = bearerToken.substring(7);
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Object admin = claims.get("admin");
        if (admin instanceof Boolean b) return b;
        if (admin instanceof Number n) return n.intValue() == 1;
        if (admin instanceof String s) return "true".equalsIgnoreCase(s) || "1".equals(s);
        return false;
    }
}

package com.seckill.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "seckill-jwt-secret-change-in-production-min-256bits";
    private long expirationMs = 86400_000L; // 24h
    private String header = "Authorization";
    private String prefix = "Bearer ";
}

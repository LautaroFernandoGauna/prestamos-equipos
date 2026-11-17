package com.proyecto.prestamos.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    //genera un jwt unico
    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    
    public Claims getClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        if (claims.getId() == null || claims.getId().isBlank()
                || claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getExpiration() == null) {
            throw new IllegalArgumentException("JWT sin datos obligatorios");
        }
        return claims;
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getJtiFromToken(String token) {
        return getClaims(token).getId();
    }

    public boolean validateJwtToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

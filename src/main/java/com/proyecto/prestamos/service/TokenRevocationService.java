package com.proyecto.prestamos.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.prestamos.model.RevokedToken;
import com.proyecto.prestamos.repository.RevokedTokenRepository;
import com.proyecto.prestamos.security.JwtUtil;

import io.jsonwebtoken.Claims;

@Service
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokens;
    private final JwtUtil jwtUtil;

    public TokenRevocationService(RevokedTokenRepository revokedTokens, JwtUtil jwtUtil) {
        this.revokedTokens = revokedTokens;
        this.jwtUtil = jwtUtil;
    }

    public boolean isRevoked(String jti) {
        return jti == null || jti.isBlank() || revokedTokens.existsById(jti);
    }

    @Transactional
    public void revoke(String jwt) {
        // compruebo si se puede revocar 
        Claims claims = jwtUtil.getClaims(jwt);
        String jti = claims.getId();
        if (jti == null || jti.isBlank() || isRevoked(jti)) {
            throw new IllegalArgumentException("JWT inválido o previamente revocado");
        }

        revokedTokens.save(new RevokedToken(jti, claims.getExpiration().toInstant()));

        // busca los tokens vencidos y los borra 
        revokedTokens.deleteByExpiresAtBefore(Instant.now());
    }
}

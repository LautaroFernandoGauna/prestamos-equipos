package com.proyecto.prestamos.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.prestamos.model.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    // elimina los tokens revocados q expiraron
    long deleteByExpiresAtBefore(Instant instant);
}

package com.proyecto.prestamos.model;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//Registra los identificadores (jti) de los JWT que dejaron de ser válidos.
//No almacenamos el token completo por motivos de seguridad.
 
@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    @Column(name = "jti", nullable = false, length = 36)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected RevokedToken() {
    
    }

    public RevokedToken(String jti, Instant expiresAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
    }

    public String getJti() {
        return jti;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}

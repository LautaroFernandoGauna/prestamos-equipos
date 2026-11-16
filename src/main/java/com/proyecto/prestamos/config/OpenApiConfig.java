package com.proyecto.prestamos.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API de Préstamos de Equipos",
        version = "1.0",
        description = "API para gestionar equipos, solicitantes, préstamos y reportes."))

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingresá el token JWT para acceder a las operaciones protegidas.")
public class OpenApiConfig {
}

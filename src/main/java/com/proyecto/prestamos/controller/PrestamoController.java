package com.proyecto.prestamos.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.prestamos.dto.PrestamoDTO;
import com.proyecto.prestamos.dto.PrestamoRequest;
import com.proyecto.prestamos.service.PrestamoService;

@RestController 
@RequestMapping ("/prestamos")
@Tag(name = "Préstamos", description = "Solicitud, consulta y devolución de equipos.")
@SecurityRequirement(name = "bearerAuth")
public class PrestamoController {
    private final PrestamoService servicio;

    public PrestamoController(PrestamoService servicio) { this.servicio = servicio; }

    // prestamos activos los puede ver el admin y encargado
    @GetMapping
    @Operation(summary = "Listar préstamos activos", description = "Devuelve los préstamos que todavía no se han cerrado.")
    @ApiResponse(responseCode = "200", description = "Listado de préstamos activos.")
    public List<PrestamoDTO> activos() { return servicio.activos(); }

    // los usuarios ven sus prestamos
    @GetMapping("/mios")
    @Operation(summary = "Consultar mis préstamos", description = "Devuelve los préstamos asociados al usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Listado de préstamos del usuario.")
    
    public List<PrestamoDTO> mios(Authentication auth) { 
        return servicio.mios(auth.getName()); 
    }

    
    @PostMapping("/solicitar") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicitar un préstamo", description = "Registra una solicitud para prestar un equipo a un solicitante.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Préstamo creado."),
            @ApiResponse(responseCode = "400", description = "Faltan datos o los identificadores no son válidos."),
            @ApiResponse(responseCode = "409", description = "El equipo no está disponible.")
    })
    public PrestamoDTO prestar(@RequestBody PrestamoRequest datos) {
         return servicio.prestar(datos); 
        }

    // La multa la calcula el servidor según el perfil; el cliente no envía importes.
    @PostMapping("/{id}/devolver")
    @Operation(summary = "Registrar la devolución", description = "Cierra el préstamo; cualquier multa aplicable se calcula en el servidor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolución registrada."),
            @ApiResponse(responseCode = "404", description = "No existe un préstamo con ese identificador."),
            @ApiResponse(responseCode = "409", description = "El préstamo ya fue devuelto.")
    })
    public PrestamoDTO devolver(@Parameter(description = "Identificador del préstamo.", example = "1") @PathVariable Long id) {
        return servicio.devolver(id);
    }
}

package com.proyecto.prestamos.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.prestamos.dto.AsignarPerfilRequest;
import com.proyecto.prestamos.dto.SolicitanteDTO;
import com.proyecto.prestamos.service.SolicitanteService;

@RestController 
@RequestMapping ("/solicitantes")
@Tag(name = "Solicitantes", description = "Consulta y administración de perfiles de solicitantes.")
@SecurityRequirement(name = "bearerAuth")
public class SolicitanteController {

    private final SolicitanteService servicio;

    public SolicitanteController(SolicitanteService servicio) { this.servicio = servicio; }

    // cada cuenta ve únicamente su propio perfil
    @GetMapping("/me")
    @Operation(summary = "Consultar mi perfil", description = "Devuelve el perfil asociado al usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Perfil del usuario.")
    public SolicitanteDTO miPerfil(Authentication auth) { return servicio.miPerfil(auth.getName()); }

    // es el listado y busqueda de los perfiles de los solicitantes, el admin puede ver todos los perfiles 
    //el encargado solo de los alumnos
    @GetMapping
    @Operation(summary = "Listar solicitantes", description = "Devuelve los perfiles de solicitantes registrados.")
    @ApiResponse(responseCode = "200", description = "Listado de solicitantes.")
    public List<SolicitanteDTO> listar() { 
        return servicio.listar(); 
    }
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un solicitante", description = "Busca un perfil por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitante encontrado."),
            @ApiResponse(responseCode = "404", description = "No existe un solicitante con ese identificador.")
    })
    public SolicitanteDTO buscar(
            @Parameter(description = "Identificador del solicitante.", example = "1") @PathVariable Long id) {
        return servicio.buscar(id);
    }

    // el admin puede asignar un perfil al solicitante
    @PutMapping("/{id}/perfil")
    @Operation(summary = "Asignar un perfil", description = "Actualiza el perfil del solicitante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado."),
            @ApiResponse(responseCode = "400", description = "El perfil enviado no es válido."),
            @ApiResponse(responseCode = "404", description = "No existe un solicitante con ese identificador.")
    })
    public SolicitanteDTO asignarPerfil(
            @Parameter(description = "Identificador del solicitante.", example = "1") @PathVariable Long id,
            @RequestBody AsignarPerfilRequest datos) {
        return servicio.asignarPerfil(id, datos == null ? null : datos.perfil());
    }
}

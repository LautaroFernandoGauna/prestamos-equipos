package com.proyecto.prestamos.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.prestamos.dto.EquipoDTO;
import com.proyecto.prestamos.dto.EquipoRequest;
import com.proyecto.prestamos.dto.EstadoEquipoRequest;
import com.proyecto.prestamos.service.EquipoService;

@RestController 
@RequestMapping ("/equipos")
@Tag(name = "Equipos", description = "Consulta y administración del inventario de equipos.")
@SecurityRequirement(name = "bearerAuth")
public class EquipoController {

    private final EquipoService servicio;

    public EquipoController(EquipoService servicio) { this.servicio = servicio; }

    // cualquiera puede ver los equipos pero solo el admin puede crear nuevos equipos
    @GetMapping
    @Operation(summary = "Listar equipos", description = "Lista todos los equipos o filtra únicamente los disponibles.")
    @ApiResponse(responseCode = "200", description = "Listado de equipos.")
    public List<EquipoDTO> listar(@RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return soloDisponibles ? servicio.disponibles() : servicio.listar();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un equipo", description = "Busca un equipo por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo encontrado."),
            @ApiResponse(responseCode = "404", description = "No existe un equipo con ese identificador.")
    })
    public EquipoDTO buscar(@Parameter(description = "Identificador del equipo.", example = "1") @PathVariable Long id) {
        return servicio.buscar(id);
    }

    // altas y bajas de equipos es solo para el admin
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un equipo", description = "Agrega un equipo al inventario.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipo registrado."),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos."),
            @ApiResponse(responseCode = "409", description = "Ya existe un equipo con ese código.")
    })
    public EquipoDTO crear(@RequestBody EquipoRequest datos) { 
        return servicio.crear(datos); 
    }

    //el admin cambia de estado del equipo
    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de un equipo", description = "Actualiza el estado del equipo, por ejemplo a DISPONIBLE o EN_REPARACION.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado."),
            @ApiResponse(responseCode = "400", description = "El estado enviado no es válido."),
            @ApiResponse(responseCode = "404", description = "No existe un equipo con ese identificador."),
            @ApiResponse(responseCode = "409", description = "No se puede modificar un equipo prestado.")
    })
    public EquipoDTO cambiarEstado(
            @Parameter(description = "Identificador del equipo.", example = "1") @PathVariable Long id,
            @RequestBody EstadoEquipoRequest datos) {
        return servicio.cambiarEstado(id, datos == null ? null : datos.estado());
    }
    //admin cambia datos del equipo
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un equipo", description = "Elimina el equipo si no tiene un préstamo activo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipo eliminado."),
            @ApiResponse(responseCode = "404", description = "No existe un equipo con ese identificador."),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar el equipo en su estado actual.")
    })
    public void eliminar(@Parameter(description = "Identificador del equipo.", example = "1") @PathVariable Long id) {
        servicio.eliminar(id);
    }
}

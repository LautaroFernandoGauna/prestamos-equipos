package com.proyecto.prestamos.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.proyecto.prestamos.dto.EquipoDTO;
import com.proyecto.prestamos.dto.EquipoRequest;
import com.proyecto.prestamos.model.Equipo;
import com.proyecto.prestamos.model.EstadoEquipo;
import com.proyecto.prestamos.repository.EquipoRepository;

@Service 

public class EquipoService {
    private final EquipoRepository repo;

    public EquipoService(EquipoRepository repo) {
        this.repo = repo;
    }

    public List<EquipoDTO> listar() {
        return repo.findAllByOrderByCodigoAsc().stream().map(EquipoDTO::de).toList();
    }

    public List<EquipoDTO> disponibles() {
        return repo.findByEstadoOrderByCodigoAsc(EstadoEquipo.DISPONIBLE).stream().map(EquipoDTO::de).toList();
    }

    public EquipoDTO buscar(Long id) {
        return EquipoDTO.de(entidad(id));
    }

    public EquipoDTO crear(EquipoRequest datos) {
        if (datos == null || datos.tipo() == null || vacio(datos.codigo())
                || vacio(datos.marca()) || vacio(datos.modelo())) {
            throw new IllegalArgumentException("Indicá tipo (NOTEBOOK, PROYECTOR o TABLET), código, marca y modelo.");
        }
        String codigo = datos.codigo().trim().toUpperCase();
        if (repo.existsByCodigo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un equipo con el código " + codigo);
        }
        Equipo equipo = new Equipo(codigo, datos.marca().trim(), datos.modelo().trim(),
                datos.tipo(), datos.especificacion());
        return EquipoDTO.de(repo.save(equipo));
    }

    public EquipoDTO cambiarEstado(Long id, EstadoEquipo nuevo) {
        Equipo e = entidad(id);
        if (nuevo == null || nuevo == EstadoEquipo.PRESTADO) {
            throw new IllegalArgumentException("Solo se puede pasar a DISPONIBLE o EN_REPARACION.");
        }
        if (e.getEstado() == EstadoEquipo.PRESTADO) {
            throw new IllegalStateException("No se puede modificar un equipo que está prestado.");
        }
        e.setEstado(nuevo);
        return EquipoDTO.de(repo.save(e));
    }

    public void eliminar(Long id) {
        Equipo e = entidad(id);
        if (e.getEstado() == EstadoEquipo.PRESTADO) {
            throw new IllegalStateException("No se puede eliminar un equipo que está prestado.");
        }
        repo.delete(e);
        repo.flush(); // si tiene historial de prestamos falla aca y se informa como conflicto
    }

    private Equipo entidad(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo inexistente."));
    }

    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
}

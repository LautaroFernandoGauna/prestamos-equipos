package com.proyecto.prestamos.service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.proyecto.prestamos.dto.RegistroRequest;
import com.proyecto.prestamos.dto.SolicitanteDTO;
import com.proyecto.prestamos.model.Perfil;
import com.proyecto.prestamos.model.Solicitante;
import com.proyecto.prestamos.model.User;
import com.proyecto.prestamos.repository.SolicitanteRepository;
import com.proyecto.prestamos.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service 
@Transactional
public class SolicitanteService {
    private final SolicitanteRepository solicitantes;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public SolicitanteService(SolicitanteRepository solicitantes, UserRepository users, PasswordEncoder encoder) {
        this.solicitantes = solicitantes;
        this.users = users;
        this.encoder = encoder;
    }

    public SolicitanteDTO registrar(RegistroRequest datos) {
        if (datos == null || vacio(datos.username()) || vacio(datos.nombre()) || vacio(datos.email())
                || datos.password() == null || datos.password().length() < 8
                || datos.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indicá usuario, nombre, email y contraseña de 8 caracteres como mínimo (máximo 72 bytes).");
        }
        if (users.existsByUsername(datos.username().trim()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe.");
        if (solicitantes.existsByEmailIgnoreCase(datos.email().trim()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado.");
        // el perfil por defecto es alumno
        User user = users.save(new User(null, datos.username().trim(), encoder.encode(datos.password()), "USER"));
        Solicitante s = new Solicitante(datos.nombre().trim(),
                datos.apellido() == null ? "" : datos.apellido().trim(), datos.email().trim(), user);
        return SolicitanteDTO.de(solicitantes.save(s));
    }

    public List<SolicitanteDTO> listar() {
        return solicitantes.findAll().stream().map(SolicitanteDTO::de).toList();
    }

    public SolicitanteDTO buscar(Long id) { return SolicitanteDTO.de(entidad(id)); }

    public SolicitanteDTO miPerfil(String username) {
        return SolicitanteDTO.de(solicitantes.findByUsuarioUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay solicitante para esta cuenta.")));
    }

    public SolicitanteDTO asignarPerfil(Long id, Perfil perfil) {
        if (perfil == null) throw new IllegalArgumentException("Elegí un perfil válido (ALUMNO, DOCENTE o STAFF).");
        Solicitante s = entidad(id);
        s.setPerfil(perfil);
        return SolicitanteDTO.de(solicitantes.save(s));
    }

    private Solicitante entidad(Long id) {
        return solicitantes.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitante inexistente."));
    }

    private boolean vacio(String v) { return v == null || v.isBlank(); }
}

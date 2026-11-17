package com.proyecto.prestamos.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.proyecto.prestamos.dto.PrestamoDTO;
import com.proyecto.prestamos.dto.PrestamoRequest;
import com.proyecto.prestamos.model.Equipo;
import com.proyecto.prestamos.model.EstadoEquipo;
import com.proyecto.prestamos.model.EstadoPrestamo;
import com.proyecto.prestamos.model.Prestamo;
import com.proyecto.prestamos.model.Solicitante;
import com.proyecto.prestamos.repository.EquipoRepository;
import com.proyecto.prestamos.repository.PrestamoRepository;
import com.proyecto.prestamos.repository.SolicitanteRepository;
import com.proyecto.prestamos.strategy.PoliticaPrestamo;
import com.proyecto.prestamos.strategy.PoliticaPrestamoResolver;

@Service 
public class PrestamoService {
    private final PrestamoRepository prestamoRepo;
    private final SolicitanteRepository solicitanteRepo;
    private final EquipoRepository equipoRepo;
    private final PoliticaPrestamoResolver politicas;

    public PrestamoService(PrestamoRepository prestamoRepo, SolicitanteRepository solicitanteRepo,
                           EquipoRepository equipoRepo, PoliticaPrestamoResolver politicas) {
        this.prestamoRepo = prestamoRepo;
        this.solicitanteRepo = solicitanteRepo;
        this.equipoRepo = equipoRepo;
        this.politicas = politicas;
    }

    public List<PrestamoDTO> activos() {
        return prestamoRepo.findByEstadoOrderByFechaDevolucionPrevistaAsc(EstadoPrestamo.ACTIVO)
                .stream().map(PrestamoDTO::de).toList();
    }

    public List<PrestamoDTO> mios(String username) {
        return prestamoRepo.findBySolicitanteUsuarioUsernameOrderByFechaPrestamoDesc(username)
                .stream().map(PrestamoDTO::de).toList();
    }

    //regla clave no se presta un equipo que no está disponible. 
    public PrestamoDTO prestar(PrestamoRequest datos) {
        if (datos == null || datos.solicitanteId() == null || datos.equipoId() == null) {
            throw new IllegalArgumentException("Indicá el solicitante y el equipo.");
        }
        Solicitante solicitante = solicitanteRepo.findById(datos.solicitanteId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitante inexistente."));
        Equipo equipo = equipoRepo.findById(datos.equipoId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo inexistente."));

        if (equipo.getEstado() != EstadoEquipo.DISPONIBLE) {
            throw new IllegalStateException("El equipo " + equipo.getCodigo() + " no está disponible.");
        }

        //el plazo depende del perfil del solicitante.
        PoliticaPrestamo politica = politicas.para(solicitante.getPerfil());

        equipo.setEstado(EstadoEquipo.PRESTADO);

        Prestamo p = new Prestamo();
        p.setSolicitante(solicitante);
        p.setEquipo(equipo);
        p.setFechaPrestamo(LocalDate.now());
        p.setFechaDevolucionPrevista(LocalDate.now().plusDays(politica.plazoDias()));
        p.setEstado(EstadoPrestamo.ACTIVO);
        return PrestamoDTO.de(prestamoRepo.save(p));
    }

    //registra la devolución y calcula la multa con la estrategia del perfil
    public PrestamoDTO devolver(Long prestamoId) {
        Prestamo p = prestamoRepo.findById(prestamoId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Préstamo inexistente."));
        if (p.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new IllegalStateException("Ese préstamo ya fue devuelto.");
        }

        LocalDate hoy = LocalDate.now();
        long retraso = Math.max(0, ChronoUnit.DAYS.between(p.getFechaDevolucionPrevista(), hoy));
        p.setMulta(politicas.para(p.getSolicitante().getPerfil()).calcularMulta(retraso));

        p.getEquipo().setEstado(EstadoEquipo.DISPONIBLE);
        p.setFechaDevolucionReal(hoy);
        p.setEstado(EstadoPrestamo.DEVUELTO);
        return PrestamoDTO.de(prestamoRepo.save(p));
    }
}

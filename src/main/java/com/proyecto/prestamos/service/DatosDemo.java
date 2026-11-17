package com.proyecto.prestamos.service;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.proyecto.prestamos.dto.EquipoRequest;
import com.proyecto.prestamos.dto.RegistroRequest;
import com.proyecto.prestamos.dto.SolicitanteDTO;
import com.proyecto.prestamos.model.Equipo;
import com.proyecto.prestamos.model.EstadoEquipo;
import com.proyecto.prestamos.model.Perfil;
import com.proyecto.prestamos.model.Prestamo;
import com.proyecto.prestamos.model.Solicitante;
import com.proyecto.prestamos.model.TipoEquipo;
import com.proyecto.prestamos.repository.EquipoRepository;
import com.proyecto.prestamos.repository.PrestamoRepository;
import com.proyecto.prestamos.repository.SolicitanteRepository;

import jakarta.transaction.Transactional;

@Component 
public class DatosDemo implements CommandLineRunner {
    private final EquipoRepository equipoRepo;
    private final EquipoService equipoService;
    private final SolicitanteService solicitanteService;
    private final SolicitanteRepository solicitanteRepo;
    private final PrestamoRepository prestamoRepo;

    public DatosDemo(EquipoRepository equipoRepo, EquipoService equipoService,
                     SolicitanteService solicitanteService, SolicitanteRepository solicitanteRepo,
                     PrestamoRepository prestamoRepo) {
        this.equipoRepo = equipoRepo;
        this.equipoService = equipoService;
        this.solicitanteService = solicitanteService;
        this.solicitanteRepo = solicitanteRepo;
        this.prestamoRepo = prestamoRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (equipoRepo.count() > 0) return;

        solicitanteService.registrar(new RegistroRequest("luciaPerez", "alumno123", "Lucía", "Perez", "luciaPerez@gmail.com"));
        SolicitanteDTO docente = solicitanteService.registrar(new RegistroRequest("docente", "martinRodri", "Martín", "Rodriguez", "martinprofe@gmail.com"));
        solicitanteService.asignarPerfil(docente.idSolicitante(), Perfil.DOCENTE);

        equipoService.crear(new EquipoRequest(TipoEquipo.NOTEBOOK, "N001", "Dell", "Latitude ", "16 GB RAM"));
        equipoService.crear(new EquipoRequest(TipoEquipo.NOTEBOOK, "N002", "Lenovo", "ThinkPad ", "8 GB RAM"));
        equipoService.crear(new EquipoRequest(TipoEquipo.NOTEBOOK, "N003", "HP", "Pavilion", "8 GB RAM"));
        equipoService.crear(new EquipoRequest(TipoEquipo.PROYECTOR, "P001", "Fika", "FIKP8", "HD"));
        equipoService.crear(new EquipoRequest(TipoEquipo.TABLET, "T001", "Samsung", "Galaxy A11", "8 pulgadas"));

        
        Equipo net3 = equipoRepo.findAllByOrderByCodigoAsc().stream()
                .filter(e -> e.getCodigo().equals("N003")).findFirst().orElseThrow();
        Solicitante alumno = solicitanteRepo.findByUsuarioUsername("luciaPerez").orElseThrow();
        Prestamo vencido = new Prestamo();
        vencido.setSolicitante(alumno);
        vencido.setEquipo(net3);
        vencido.setFechaPrestamo(LocalDate.now().minusDays(8));
        vencido.setFechaDevolucionPrevista(LocalDate.now().minusDays(5));
        net3.setEstado(EstadoEquipo.PRESTADO);
        equipoRepo.save(net3);
        prestamoRepo.save(vencido);
    }
}

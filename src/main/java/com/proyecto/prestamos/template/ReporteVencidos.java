package com.proyecto.prestamos.template;

import java.util.List;

import org.springframework.stereotype.Component;

import com.proyecto.prestamos.model.EstadoPrestamo;
import com.proyecto.prestamos.model.Prestamo;
import com.proyecto.prestamos.repository.PrestamoRepository;

@Component 
public class ReporteVencidos extends ReporteTemplate {
    private final PrestamoRepository repo;

    public ReporteVencidos(PrestamoRepository repo) { this.repo = repo; }

    @Override public String clave() { return "vencidos"; }
    @Override protected String titulo() { return "Prestamos vencidos sin devolver"; }
    @Override protected List<Prestamo> obtenerDatos() {
        return repo.findByEstadoOrderByFechaDevolucionPrevistaAsc(EstadoPrestamo.ACTIVO);
    }
    @Override protected List<Prestamo> filtrar(List<Prestamo> datos) {
        return datos.stream().filter(Prestamo::isVencido).toList();
    }
}

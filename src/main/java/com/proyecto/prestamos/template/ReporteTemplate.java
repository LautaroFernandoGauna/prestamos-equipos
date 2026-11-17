package com.proyecto.prestamos.template;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.proyecto.prestamos.model.Prestamo;

public abstract class ReporteTemplate {
     private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // metodo plantilla: no se puede sobrescribir. 
    public final Reporte generar() {
        List<Prestamo> datos = obtenerDatos();
        List<Prestamo> filtrados = filtrar(datos);
        List<String[]> filas = filtrados.stream().map(this::fila).toList();
        return new Reporte(titulo(), encabezados(), filas);
    }

    public final String aCsv(Reporte r) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(String.join(";", r.getEncabezados())).append("\n");
        for (String[] fila : r.getFilas()) {
            sb.append(java.util.Arrays.stream(fila)
                    .map(c -> "\"" + (c == null ? "" : c.replace("\"", "\"\"")) + "\"")
                    .collect(Collectors.joining(";"))).append("\n");
        }
        return sb.toString();
    }

    public abstract String clave();
    protected abstract String titulo();
    protected abstract List<Prestamo> obtenerDatos();

    protected List<Prestamo> filtrar(List<Prestamo> datos) { return datos; }

    protected String[] encabezados() {
        return new String[]{"N:", "Usuario:", "Equipo:", "Prestado:", "Vence:", "Devuelto:", "Multa:"};
    }

    protected String[] fila(Prestamo p) {
        return new String[]{
                String.valueOf(p.getId()),
                p.getSolicitante().getNombre() + " " + p.getSolicitante().getApellido(),
                p.getEquipo().getCodigo() + " - " + p.getEquipo().getMarca() + " " + p.getEquipo().getModelo(),
                fmt(p.getFechaPrestamo()),
                fmt(p.getFechaDevolucionPrevista()),
                p.getFechaDevolucionReal() == null ? "-" : fmt(p.getFechaDevolucionReal()),
                "$" + p.getMulta()
        };
    }

    private String fmt(LocalDate f) { return f == null ? "" : f.format(FECHA); }
}

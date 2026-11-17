package com.proyecto.prestamos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity 
public class Equipo {
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String codigo;
    private String marca;
    private String modelo;

    @Enumerated(EnumType.STRING)
    private TipoEquipo tipo;

    /** Dato propio del equipo: RAM, lúmenes, pulgadas... */
    private String especificacion;

    @Enumerated(EnumType.STRING)
    private EstadoEquipo estado = EstadoEquipo.DISPONIBLE;

     public Equipo() {}

    public Equipo(String codigo, String marca, String modelo, TipoEquipo tipo, String especificacion) {
        this.codigo = codigo;
        this.marca = marca;
        this.modelo = modelo;
        this.tipo = tipo;
        this.especificacion = especificacion;
        this.estado = EstadoEquipo.DISPONIBLE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public TipoEquipo getTipo() {
        return tipo;
    }

    public void setTipo(TipoEquipo tipo) {
        this.tipo = tipo;
    }

    public String getEspecificacion() {
        return especificacion;
    }

    public void setEspecificacion(String especificacion) {
        this.especificacion = especificacion;
    }

    public EstadoEquipo getEstado() {
        return estado;
    }

    public void setEstado(EstadoEquipo estado) {
        this.estado = estado;
    }


}

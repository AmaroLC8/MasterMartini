package com.mastermartini.cursos.model;

import jakarta.persistence.*;

// Entidad JPA que representa un curso de Master Martini en la base de datos.
@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String insumo;

    @Column(nullable = false)
    private String chef;

    @Column(nullable = false)
    private String duracion;

    @Column(nullable = false)
    private String nivel;

    @Column(nullable = false)
    private String urlVideo;

    @Column(nullable = false)
    private boolean soloPro = false;

    public Curso() {
    }

    public Curso(String titulo, String categoria, String insumo, String chef,
                 String duracion, String nivel, String urlVideo, boolean soloPro) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.insumo = insumo;
        this.chef = chef;
        this.duracion = duracion;
        this.nivel = nivel;
        this.urlVideo = urlVideo;
        this.soloPro = soloPro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getInsumo() {
        return insumo;
    }

    public void setInsumo(String insumo) {
        this.insumo = insumo;
    }

    public String getChef() {
        return chef;
    }

    public void setChef(String chef) {
        this.chef = chef;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public boolean isSoloPro() {
        return soloPro;
    }

    public void setSoloPro(boolean soloPro) {
        this.soloPro = soloPro;
    }
}

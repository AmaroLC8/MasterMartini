package com.mastermartini.interacciones.model;

import jakarta.persistence.*;

// Entidad JPA que representa la interacción de un usuario con un curso.
@Entity
@Table(name = "interacciones")
public class Interaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Long cursoId;

    private boolean favorito = false;

    private boolean completado = false;

    @Column(length = 500)
    private String notaPersonal;

    public Interaccion() {
    }

    public Interaccion(Long usuarioId, Long cursoId, boolean favorito, boolean completado, String notaPersonal) {
        this.usuarioId = usuarioId;
        this.cursoId = cursoId;
        this.favorito = favorito;
        this.completado = completado;
        this.notaPersonal = notaPersonal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public String getNotaPersonal() {
        return notaPersonal;
    }

    public void setNotaPersonal(String notaPersonal) {
        this.notaPersonal = notaPersonal;
    }
}

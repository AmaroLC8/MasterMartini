package com.mastermartini.interacciones.dto;

import com.mastermartini.interacciones.model.Interaccion;

// DTO de salida que expone los datos de una interacción hacia los clientes de la API.
public class InteraccionResponse {

    private Long id;
    private Long usuarioId;
    private Long cursoId;
    private boolean favorito;
    private boolean completado;
    private String notaPersonal;

    public InteraccionResponse() {
    }

    public InteraccionResponse(Long id, Long usuarioId, Long cursoId,
                               boolean favorito, boolean completado, String notaPersonal) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.cursoId = cursoId;
        this.favorito = favorito;
        this.completado = completado;
        this.notaPersonal = notaPersonal;
    }

    public static InteraccionResponse fromEntity(Interaccion interaccion) {
        return new InteraccionResponse(
                interaccion.getId(),
                interaccion.getUsuarioId(),
                interaccion.getCursoId(),
                interaccion.isFavorito(),
                interaccion.isCompletado(),
                interaccion.getNotaPersonal()
        );
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

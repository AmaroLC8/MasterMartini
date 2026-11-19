package com.mastermartini.cursos.dto;

import jakarta.validation.constraints.NotBlank;

// DTO de entrada con los datos validados para crear o actualizar un curso.
public class CursoRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotBlank(message = "El insumo es obligatorio")
    private String insumo;

    @NotBlank(message = "El chef es obligatorio")
    private String chef;

    @NotBlank(message = "La duración es obligatoria")
    private String duracion;

    @NotBlank(message = "El nivel es obligatorio")
    private String nivel;

    @NotBlank(message = "La URL del video es obligatoria")
    private String urlVideo;

    private boolean soloPro = false;

    public CursoRequest() {
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

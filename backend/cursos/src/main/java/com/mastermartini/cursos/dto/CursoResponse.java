package com.mastermartini.cursos.dto;

import com.mastermartini.cursos.model.Curso;

// DTO de salida que expone los datos de un curso hacia los clientes de la API.
public class CursoResponse {

    private Long id;
    private String titulo;
    private String categoria;
    private String insumo;
    private String chef;
    private String duracion;
    private String nivel;
    private String urlVideo;
    private boolean soloPro;

    public CursoResponse() {
    }

    public CursoResponse(Long id, String titulo, String categoria, String insumo, String chef,
                         String duracion, String nivel, String urlVideo, boolean soloPro) {
        this.id = id;
        this.titulo = titulo;
        this.categoria = categoria;
        this.insumo = insumo;
        this.chef = chef;
        this.duracion = duracion;
        this.nivel = nivel;
        this.urlVideo = urlVideo;
        this.soloPro = soloPro;
    }

    public static CursoResponse fromEntity(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getTitulo(),
                curso.getCategoria(),
                curso.getInsumo(),
                curso.getChef(),
                curso.getDuracion(),
                curso.getNivel(),
                curso.getUrlVideo(),
                curso.isSoloPro()
        );
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

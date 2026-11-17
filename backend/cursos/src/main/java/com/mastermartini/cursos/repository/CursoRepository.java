package com.mastermartini.cursos.repository;

import com.mastermartini.cursos.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repositorio JPA para acceder y consultar los cursos almacenados en la base de datos.
public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findByCategoriaIgnoreCase(String categoria);

    List<Curso> findBySoloProFalse();
}

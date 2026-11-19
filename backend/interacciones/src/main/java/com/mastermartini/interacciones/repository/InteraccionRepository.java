package com.mastermartini.interacciones.repository;

import com.mastermartini.interacciones.model.Interaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repositorio JPA para consultar y persistir las interacciones de usuarios con cursos.
public interface InteraccionRepository extends JpaRepository<Interaccion, Long> {

    // Obtiene todas las interacciones registradas para un usuario específico.
    List<Interaccion> findByUsuarioId(Long usuarioId);

    // Obtiene los cursos marcados como favoritos por un usuario.
    List<Interaccion> findByUsuarioIdAndFavoritoTrue(Long usuarioId);

    // Busca la interacción única entre un usuario y un curso determinado.
    Optional<Interaccion> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);
}

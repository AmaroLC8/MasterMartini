package com.mastermartini.interacciones.service;

import com.mastermartini.interacciones.dto.InteraccionRequest;
import com.mastermartini.interacciones.dto.InteraccionResponse;
import com.mastermartini.interacciones.exception.ApiException;
import com.mastermartini.interacciones.model.Interaccion;
import com.mastermartini.interacciones.repository.InteraccionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

// Servicio que gestiona la lógica de negocio de las interacciones de usuarios con cursos.
@Service
public class InteraccionService {

    private final InteraccionRepository interaccionRepository;

    public InteraccionService(InteraccionRepository interaccionRepository) {
        this.interaccionRepository = interaccionRepository;
    }

    // Lista todas las interacciones registradas para un usuario específico.
    public List<InteraccionResponse> listarPorUsuario(Long usuarioId) {
        return interaccionRepository.findByUsuarioId(usuarioId).stream()
                .map(InteraccionResponse::fromEntity)
                .toList();
    }

    // Lista únicamente las interacciones marcadas como favoritas de un usuario.
    public List<InteraccionResponse> listarFavoritosPorUsuario(Long usuarioId) {
        return interaccionRepository.findByUsuarioIdAndFavoritoTrue(usuarioId).stream()
                .map(InteraccionResponse::fromEntity)
                .toList();
    }

    // Crea una interacción nueva o actualiza favorito, completado y nota si ya existe para usuario y curso.
    public InteraccionResponse guardarOActualizar(InteraccionRequest req) {
        Interaccion interaccion = interaccionRepository
                .findByUsuarioIdAndCursoId(req.getUsuarioId(), req.getCursoId())
                .orElseGet(() -> new Interaccion(
                        req.getUsuarioId(),
                        req.getCursoId(),
                        req.isFavorito(),
                        req.isCompletado(),
                        req.getNotaPersonal()
                ));

        interaccion.setFavorito(req.isFavorito());
        interaccion.setCompletado(req.isCompletado());
        interaccion.setNotaPersonal(req.getNotaPersonal());

        return InteraccionResponse.fromEntity(interaccionRepository.save(interaccion));
    }

    // Actualiza favorito, completado y nota personal de una interacción existente o lanza 404 si no existe.
    public InteraccionResponse actualizarPorId(Long id, InteraccionRequest req) {
        Interaccion interaccion = interaccionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Interacción no encontrada"));

        interaccion.setFavorito(req.isFavorito());
        interaccion.setCompletado(req.isCompletado());
        interaccion.setNotaPersonal(req.getNotaPersonal());

        return InteraccionResponse.fromEntity(interaccionRepository.save(interaccion));
    }

    // Elimina una interacción por su ID verificando primero que exista en la base de datos.
    public void eliminar(Long id) {
        if (!interaccionRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Interacción no encontrada");
        }
        interaccionRepository.deleteById(id);
    }
}

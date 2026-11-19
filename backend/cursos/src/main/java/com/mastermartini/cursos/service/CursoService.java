package com.mastermartini.cursos.service;

import com.mastermartini.cursos.dto.CursoRequest;
import com.mastermartini.cursos.dto.CursoResponse;
import com.mastermartini.cursos.exception.ApiException;
import com.mastermartini.cursos.model.Curso;
import com.mastermartini.cursos.repository.CursoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

// Servicio que gestiona la lógica de negocio de los cursos de Master Martini.
@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    // Lista todos los cursos registrados en la base de datos.
    public List<CursoResponse> listarTodos() {
        return cursoRepository.findAll().stream()
                .map(CursoResponse::fromEntity)
                .toList();
    }

    // Lista los cursos filtrados por categoría sin distinguir mayúsculas o minúsculas.
    public List<CursoResponse> listarPorCategoria(String categoria) {
        return cursoRepository.findByCategoriaIgnoreCase(categoria).stream()
                .map(CursoResponse::fromEntity)
                .toList();
    }

    // Busca un curso por su ID o lanza 404 si no existe.
    public CursoResponse obtenerPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Curso no encontrado"));
        return CursoResponse.fromEntity(curso);
    }

    // Crea y guarda un nuevo curso a partir de los datos de entrada.
    public CursoResponse crear(CursoRequest req) {
        Curso nuevo = new Curso(
                req.getTitulo(),
                req.getCategoria(),
                req.getInsumo(),
                req.getChef(),
                req.getDuracion(),
                req.getNivel(),
                req.getUrlVideo(),
                req.isSoloPro()
        );
        return CursoResponse.fromEntity(cursoRepository.save(nuevo));
    }

    // Actualiza todos los campos de un curso existente o lanza 404 si no se encuentra.
    public CursoResponse actualizar(Long id, CursoRequest req) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Curso no encontrado"));

        curso.setTitulo(req.getTitulo());
        curso.setCategoria(req.getCategoria());
        curso.setInsumo(req.getInsumo());
        curso.setChef(req.getChef());
        curso.setDuracion(req.getDuracion());
        curso.setNivel(req.getNivel());
        curso.setUrlVideo(req.getUrlVideo());
        curso.setSoloPro(req.isSoloPro());

        return CursoResponse.fromEntity(cursoRepository.save(curso));
    }

    // Elimina un curso por su ID verificando primero que exista en la base de datos.
    public void eliminar(Long id) {
        if (!cursoRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Curso no encontrado");
        }
        cursoRepository.deleteById(id);
    }
}

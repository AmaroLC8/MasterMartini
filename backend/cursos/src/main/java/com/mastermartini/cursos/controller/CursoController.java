package com.mastermartini.cursos.controller;

import com.mastermartini.cursos.dto.CursoRequest;
import com.mastermartini.cursos.dto.CursoResponse;
import com.mastermartini.cursos.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST que expone los endpoints CRUD para los cursos.
@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // Lista todos los cursos o filtra por categoría si se recibe el parámetro opcional.
    @GetMapping
    public ResponseEntity<List<CursoResponse>> listar(@RequestParam(required = false) String categoria) {
        List<CursoResponse> cursos = (categoria != null && !categoria.isBlank())
                ? cursoService.listarPorCategoria(categoria)
                : cursoService.listarTodos();
        return ResponseEntity.ok(cursos);
    }

    // Obtiene el detalle de un curso específico por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<CursoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cursoService.obtenerPorId(id));
    }

    // Crea un nuevo curso validando los datos de entrada y retorna estado 201 CREATED.
    @PostMapping
    public ResponseEntity<CursoResponse> crear(@Valid @RequestBody CursoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cursoService.crear(req));
    }

    // Actualiza un curso existente por su ID y retorna el resultado con estado 200 OK.
    @PutMapping("/{id}")
    public ResponseEntity<CursoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest req) {
        return ResponseEntity.ok(cursoService.actualizar(id, req));
    }

    // Elimina un curso por su ID y retorna estado 204 NO CONTENT.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

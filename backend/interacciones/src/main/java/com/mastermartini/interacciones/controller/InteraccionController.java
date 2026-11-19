package com.mastermartini.interacciones.controller;

import com.mastermartini.interacciones.dto.InteraccionRequest;
import com.mastermartini.interacciones.dto.InteraccionResponse;
import com.mastermartini.interacciones.service.InteraccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST que expone los endpoints para gestionar interacciones de usuarios con cursos.
@RestController
@RequestMapping("/api/interacciones")
@CrossOrigin(origins = "*")
public class InteraccionController {

    private final InteraccionService interaccionService;

    public InteraccionController(InteraccionService interaccionService) {
        this.interaccionService = interaccionService;
    }

    // Obtiene todas las interacciones registradas para un usuario por su ID.
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<InteraccionResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(interaccionService.listarPorUsuario(usuarioId));
    }

    // Obtiene los cursos marcados como favoritos de un usuario por su ID.
    @GetMapping("/usuario/{usuarioId}/favoritos")
    public ResponseEntity<List<InteraccionResponse>> listarFavoritosPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(interaccionService.listarFavoritosPorUsuario(usuarioId));
    }

    // Crea o actualiza la interacción de un usuario con un curso y retorna estado 201 CREATED.
    @PostMapping
    public ResponseEntity<InteraccionResponse> guardarOActualizar(@Valid @RequestBody InteraccionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interaccionService.guardarOActualizar(req));
    }

    // Actualiza una interacción existente por su ID y retorna el resultado con estado 200 OK.
    @PutMapping("/{id}")
    public ResponseEntity<InteraccionResponse> actualizarPorId(
            @PathVariable Long id,
            @Valid @RequestBody InteraccionRequest req) {
        return ResponseEntity.ok(interaccionService.actualizarPorId(id, req));
    }

    // Elimina una interacción por su ID y retorna estado 204 NO CONTENT.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        interaccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

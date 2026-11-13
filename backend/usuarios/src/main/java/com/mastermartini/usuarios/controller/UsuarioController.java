package com.mastermartini.usuarios.controller;

import com.mastermartini.usuarios.dto.*;
import com.mastermartini.usuarios.model.Rol;
import com.mastermartini.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Registra un nuevo usuario cliente y retorna sus datos con estado 201 CREATED.
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(req));
    }

    // Autentica un usuario por correo o RUT y retorna sus datos con estado 200 OK.
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(usuarioService.login(req));
    }

    // Solicita el envío de un código de recuperación de contraseña para el correo indicado.
    @PostMapping("/recuperar")
    public ResponseEntity<Map<String, Object>> recuperar(@Valid @RequestBody RecuperarRequest req) {
        usuarioService.recuperar(req);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo existe, se envió un código de recuperación",
                "estado", 200
        ));
    }

    // Restablece la contraseña del usuario validando el código de recuperación.
    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, Object>> restablecer(@Valid @RequestBody RestablecerRequest req) {
        usuarioService.restablecer(req);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Contraseña actualizada correctamente",
                "estado", 200
        ));
    }

    // Obtiene la información de un usuario específico por su ID.
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id, solicitanteId));
    }

    // Actualiza los datos del perfil de un usuario existente.
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPerfilRequest req,
            @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(id, req, solicitanteId));
    }

    // Lista todos los usuarios o los filtra opcionalmente por rol.
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(required = false) Rol rol,
            @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return ResponseEntity.ok(usuarioService.listar(rol, solicitanteId));
    }

    // Activa o desactiva el Plan Pro de un usuario.
    @PutMapping("/{id}/plan-pro")
    public ResponseEntity<UsuarioResponse> cambiarPlanPro(
            @PathVariable Long id,
            @Valid @RequestBody PlanProRequest req,
            @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return ResponseEntity.ok(usuarioService.cambiarPlanPro(id, req, solicitanteId));
    }

    // Cambia el rol asignado a un usuario.
    @PutMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @PathVariable Long id,
            @Valid @RequestBody CambiarRolRequest req,
            @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, req, solicitanteId));
    }
}

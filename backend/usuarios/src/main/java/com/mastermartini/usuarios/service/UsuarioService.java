package com.mastermartini.usuarios.service;

import com.mastermartini.usuarios.dto.*;
import com.mastermartini.usuarios.exception.ApiException;
import com.mastermartini.usuarios.model.Rol;
import com.mastermartini.usuarios.model.Usuario;
import com.mastermartini.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public UsuarioResponse registrar(RegistroRequest req){
        if(usuarioRepository.existsByCorreo(req.correo())){
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya esta registrado");
        }
        if(usuarioRepository.existsByRut(req.rut())){
            throw new ApiException(HttpStatus.CONFLICT, "El rut ya esta registrado");
        }
        Usuario nuevo = new Usuario(
                req.nombre(),
                req.rut(),
                req.correo(),
                req.telefono(),
                req.rubro(),
                req.personasNegocio(),
                passwordEncoder.encode(req.password()),
                Rol.CLIENTE,
                false
        );
        Usuario guardado = usuarioRepository.save(nuevo);
        return UsuarioResponse.desdeEntidad(guardado);
    }
    public UsuarioResponse login(LoginRequest req) {

        Usuario usuario = usuarioRepository.findByCorreo(req.usuario())
                .or(() -> usuarioRepository.findByRut(req.usuario()))
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED, "RUT/correo o contraseña incorrectos"
                ));


        if (!passwordEncoder.matches(req.password(), usuario.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "RUT/correo o contraseña incorrectos");
        }

        return UsuarioResponse.desdeEntidad(usuario);
    }

    // Genera y registra un código de recuperación de 6 dígitos si el correo existe; no requiere autenticación.
    public void recuperar(RecuperarRequest req) {
        usuarioRepository.findByCorreo(req.correo()).ifPresent(usuario -> {
            String codigo = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            usuario.setCodigoRecuperacion(codigo);
            usuario.setCodigoExpira(LocalDateTime.now().plusMinutes(10));
            usuarioRepository.save(usuario);
            log.info("Código de recuperación para {}: {}", req.correo(), codigo);
        });
    }

    // Valida el código de recuperación y su vigencia para restablecer la contraseña del usuario; no requiere autenticación.
    public void restablecer(RestablecerRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.correo())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getCodigoRecuperacion() == null || !usuario.getCodigoRecuperacion().equals(req.codigo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Código de recuperación inválido");
        }
        if (usuario.getCodigoExpira() == null || usuario.getCodigoExpira().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El código de recuperación ha expirado");
        }

        usuario.setPassword(passwordEncoder.encode(req.nuevaPassword()));
        usuario.setCodigoRecuperacion(null);
        usuario.setCodigoExpira(null);
        usuarioRepository.save(usuario);
    }

    // Obtiene los datos de un usuario por su ID; solo permitido para el propio usuario, VENTAS o ADMINISTRADOR.
    public UsuarioResponse obtenerPorId(Long id, Long solicitanteId) {
        Usuario usuario = buscarUsuarioPorId(id);
        if (!id.equals(solicitanteId)) {
            verificarRol(solicitanteId, Rol.VENTAS, Rol.ADMINISTRADOR);
        }
        return UsuarioResponse.desdeEntidad(usuario);
    }

    // Actualiza nombre, teléfono, rubro y personas del negocio de un usuario; solo permitido para el propio usuario o ADMINISTRADOR.
    public UsuarioResponse actualizarPerfil(Long id, ActualizarPerfilRequest req, Long solicitanteId) {
        Usuario usuario = buscarUsuarioPorId(id);
        if (!id.equals(solicitanteId)) {
            verificarRol(solicitanteId, Rol.ADMINISTRADOR);
        }

        usuario.setNombre(req.nombre());
        usuario.setTelefono(req.telefono());
        usuario.setRubro(req.rubro());
        usuario.setPersonasNegocio(req.personasNegocio());

        return UsuarioResponse.desdeEntidad(usuarioRepository.save(usuario));
    }

    // Lista todos los usuarios o los filtra por rol; solo permitido para VENTAS o ADMINISTRADOR.
    public List<UsuarioResponse> listar(Rol rolFiltro, Long solicitanteId) {
        verificarRol(solicitanteId, Rol.VENTAS, Rol.ADMINISTRADOR);
        List<Usuario> usuarios = (rolFiltro == null)
                ? usuarioRepository.findAll()
                : usuarioRepository.findByRol(rolFiltro);
        return usuarios.stream().map(UsuarioResponse::desdeEntidad).toList();
    }

    // Activa o desactiva el Plan Pro de un usuario; solo permitido para VENTAS o ADMINISTRADOR.
    public UsuarioResponse cambiarPlanPro(Long id, PlanProRequest req, Long solicitanteId) {
        verificarRol(solicitanteId, Rol.VENTAS, Rol.ADMINISTRADOR);
        Usuario usuario = buscarUsuarioPorId(id);
        usuario.setPlanPro(req.activo());
        return UsuarioResponse.desdeEntidad(usuarioRepository.save(usuario));
    }

    // Cambia el rol de un usuario; solo permitido para ADMINISTRADOR.
    public UsuarioResponse cambiarRol(Long id, CambiarRolRequest req, Long solicitanteId) {
        verificarRol(solicitanteId, Rol.ADMINISTRADOR);
        Usuario usuario = buscarUsuarioPorId(id);
        usuario.setRol(req.rol());
        return UsuarioResponse.desdeEntidad(usuarioRepository.save(usuario));
    }

    private Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void verificarRol(Long solicitanteId, Rol... rolesPermitidos) {
        if (solicitanteId == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
        }
        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción"));
        boolean autorizado = Arrays.asList(rolesPermitidos).contains(solicitante.getRol());
        if (!autorizado) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
        }
    }
}
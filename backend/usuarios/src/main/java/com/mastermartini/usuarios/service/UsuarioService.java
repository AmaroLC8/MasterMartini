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
        if(usuarioRepository.existByCorreo(req.correo())){
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya esta registrado");
        }
        if(usuarioRepository.existByRut(req.rut())){
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
}
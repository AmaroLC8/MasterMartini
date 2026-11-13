package com.mastermartini.usuarios.repository;

import com.mastermartini.usuarios.model.Rol;
import com.mastermartini.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    Optional<Usuario> findByCorreo(String Correo);
    Optional<Usuario> findByRut(String rut);

    boolean existsByCorreo(String correo);
    boolean existsByRut(String rut);

    List<Usuario> findByRol(Rol rol);
}

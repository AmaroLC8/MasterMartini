package com.mastermartini.usuarios.dto;

import com.mastermartini.usuarios.model.Rol;
import com.mastermartini.usuarios.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String rut,
        String correo,
        String telefono,
        String rubro,
        String personasNegocio,
        Rol rol,
        boolean planPro
) {
    public static UsuarioResponse desdeEntidad(Usuario u){
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getRut(),
                u.getCorreo(),
                u.getTelefono(),
                u.getRubro(),
                u.getPersonasNegocio(),
                u.getRol(),
                u.isPlanPro()
        );
    }
}

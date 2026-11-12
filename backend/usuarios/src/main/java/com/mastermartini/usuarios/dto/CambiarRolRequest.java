package com.mastermartini.usuarios.dto;

import com.mastermartini.usuarios.model.Rol;
import jakarta.validation.constraints.NotNull;

public record CambiarRolRequest(
        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {}
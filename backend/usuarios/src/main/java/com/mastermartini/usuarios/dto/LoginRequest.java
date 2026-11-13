package com.mastermartini.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Debes ingresar tu RUT o correo")
        String usuario,

        @NotBlank(message = "Debes ingresar tu contraseña")
        String password
) {}
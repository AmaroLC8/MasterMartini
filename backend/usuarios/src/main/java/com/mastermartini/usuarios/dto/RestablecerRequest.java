package com.mastermartini.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        String correo,

        @NotBlank(message = "El código de recuperación es obligatorio")
        String codigo,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
        String nuevaPassword
) {}
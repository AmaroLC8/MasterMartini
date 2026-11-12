package com.mastermartini.usuarios.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El RUT es obligatorio")
        @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "Formato de RUT inválido (ej: 12345678-9)")
        String rut,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^9\\d{8}$", message = "El teléfono debe tener 9 dígitos y empezar con 9")
        String telefono,

        @NotBlank(message = "El rubro es obligatorio")
        String rubro,

        @NotBlank(message = "La cantidad de personas es obligatoria")
        String personasNegocio,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
        String password
) {}
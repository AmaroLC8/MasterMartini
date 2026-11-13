package com.mastermartini.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ActualizarPerfilRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^9\\d{8}$", message = "El teléfono debe tener 9 dígitos y empezar con 9")
        String telefono,

        @NotBlank(message = "El rubro es obligatorio")
        String rubro,

        @NotBlank(message = "La cantidad de personas es obligatoria")
        String personasNegocio
) {}
package com.mastermartini.usuarios.dto;

import jakarta.validation.constraints.NotNull;

public record PlanProRequest(
        @NotNull(message = "Debes indicar si el Plan Pro queda activo (true/false)")
        Boolean activo
) {}
package com.mastermartini.cursos.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

// Estructura estándar de respuesta para errores HTTP y fallos de validación.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String mensaje,
        int estado,
        Map<String, String> campos
) {

    public ErrorResponse(String mensaje, int estado) {
        this(mensaje, estado, null);
    }
}

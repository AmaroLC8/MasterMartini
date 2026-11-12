package com.mastermartini.usuarios.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

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
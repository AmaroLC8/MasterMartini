package com.mastermartini.interacciones.exception;

import org.springframework.http.HttpStatus;

// Excepción de negocio que asocia un código HttpStatus a un mensaje de error.
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

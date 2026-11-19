package com.mastermartini.cursos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// Manejador global que intercepta excepciones de negocio y errores de validación @Valid.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> manejarApiException(ApiException ex) {
        ErrorResponse body = new ErrorResponse(ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> erroresCampos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                erroresCampos.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse body = new ErrorResponse("Datos inválidos", HttpStatus.BAD_REQUEST.value(), erroresCampos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}

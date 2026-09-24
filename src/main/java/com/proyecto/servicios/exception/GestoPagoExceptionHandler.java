package com.proyecto.servicios.exception;

import com.proyecto.servicios.controller.GestoPagoProductController;
import com.proyecto.servicios.model.GenericResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte los errores de integración en la respuesta estándar del proyecto
 * (GenericResponse).
 * Solo aplica a los controllers de GestoPago para no alterar los demás
 * endpoints.
 */
@RestControllerAdvice(assignableTypes = GestoPagoProductController.class)
public class GestoPagoExceptionHandler {

    private static final int CODIGO_ERROR = 1;

    @ExceptionHandler(GestoPagoIntegrationException.class)
    public ResponseEntity<GenericResponse> handleIntegrationError(GestoPagoIntegrationException ex) {
        GenericResponse response = new GenericResponse();
        response.setCodigo(CODIGO_ERROR);
        response.setMensaje(ex.getMessage());
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }
}
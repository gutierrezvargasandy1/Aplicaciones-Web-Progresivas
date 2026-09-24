package com.proyecto.servicios.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base de la integración con GestoPago.
 * Sus mensajes nunca contienen tokens, credenciales ni el cuerpo de la
 * respuesta del proveedor.
 */
@Getter
public class GestoPagoIntegrationException extends RuntimeException {

    private final HttpStatus httpStatus;

    public GestoPagoIntegrationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public GestoPagoIntegrationException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }
}
package com.proyecto.servicios.exception;

import org.springframework.http.HttpStatus;

/** No fue posible comunicarse con GestoPago (red, DNS, conexión rechazada). */
public class GestoPagoCommunicationException extends GestoPagoIntegrationException {

    public GestoPagoCommunicationException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}
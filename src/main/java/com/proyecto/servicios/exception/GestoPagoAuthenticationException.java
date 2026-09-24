package com.proyecto.servicios.exception;

import org.springframework.http.HttpStatus;

/** Token rechazado o expirado (401/403), o token no configurado. */
public class GestoPagoAuthenticationException extends GestoPagoIntegrationException {

    public GestoPagoAuthenticationException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
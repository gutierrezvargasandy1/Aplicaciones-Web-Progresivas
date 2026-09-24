package com.proyecto.servicios.exception;

import org.springframework.http.HttpStatus;

/** GestoPago no respondió dentro del tiempo configurado. */
public class GestoPagoTimeoutException extends GestoPagoIntegrationException {

    public GestoPagoTimeoutException(String message, Throwable cause) {
        super(message, HttpStatus.GATEWAY_TIMEOUT, cause);
    }
}
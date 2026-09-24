package com.proyecto.servicios.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * GestoPago respondió, pero con un status HTTP no exitoso, un CODIGO distinto
 * de "01" o un XML inválido.
 */
@Getter
public class GestoPagoUnsuccessfulResponseException extends GestoPagoIntegrationException {

    /**
     * Status HTTP devuelto por GestoPago; null si el HTTP fue 200 pero el contenido
     * indica error.
     */
    private final Integer providerStatus;

    public GestoPagoUnsuccessfulResponseException(String message, Integer providerStatus) {
        super(message, HttpStatus.BAD_GATEWAY);
        this.providerStatus = providerStatus;
    }

    public GestoPagoUnsuccessfulResponseException(String message, Integer providerStatus, Throwable cause) {
        super(message, HttpStatus.BAD_GATEWAY, cause);
        this.providerStatus = providerStatus;
    }
}
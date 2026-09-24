package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.exception.GestoPagoUnsuccessfulResponseException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

/**
 * Convierte respuestas HTTP no exitosas en excepciones de dominio.
 * El cuerpo de la respuesta no se lee ni se propaga, para no exponer
 * información sensible.
 */
public class GestoPagoErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            return new GestoPagoAuthenticationException("GestoPago rechazó el token (inválido o expirado)");
        }
        if (status == HttpStatus.REQUEST_TIMEOUT.value() || status == HttpStatus.GATEWAY_TIMEOUT.value()) {
            return new GestoPagoTimeoutException("GestoPago no respondió a tiempo", null);
        }
        return new GestoPagoUnsuccessfulResponseException(
                "GestoPago respondió con un estado no exitoso: " + status, status);
    }
}
package com.proyecto.servicios.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Configuración exclusiva del cliente Feign "gestoPagoProduct".
 * NO lleva @Configuration a propósito: así el interceptor del Bearer no se
 * aplica
 * a GestoPagoAuthClient ni a otros clientes Feign.
 */
public class GestoPagoProductClientConfig {

    @Bean
    public RequestInterceptor gestoPagoBearerTokenInterceptor(
            @Value("${gestopago.product.token}") String token,
            @Value("${gestopago.product.api-key:}") String apiKey) {
        return new GestoPagoBearerTokenInterceptor(token, apiKey);
    }

    @Bean
    public ErrorDecoder gestoPagoErrorDecoder() {
        return new GestoPagoErrorDecoder();
    }
}
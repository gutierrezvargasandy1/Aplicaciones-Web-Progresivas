package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

/**
 * Agrega "Authorization: Bearer {token}" y, si está configurada, "X-API-Key".
 * El token viene de la configuración (gestopago.product.token) y nunca se
 * registra en logs.
 */
public class GestoPagoBearerTokenInterceptor implements RequestInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String API_KEY_HEADER = "X-API-Key";

    private final String token;
    private final String apiKey;

    public GestoPagoBearerTokenInterceptor(String token, String apiKey) {
        this.token = token;
        this.apiKey = apiKey;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!StringUtils.hasText(token)) {
            throw new GestoPagoAuthenticationException("El token de GestoPago no está configurado");
        }
        template.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token.trim());
        if (StringUtils.hasText(apiKey)) {
            template.header(API_KEY_HEADER, apiKey.trim());
        }
    }
}
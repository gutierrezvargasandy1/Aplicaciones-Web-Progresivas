package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GestoPagoBearerTokenInterceptorTest {

    @Test
    void apply_agregaBearerYApiKey() {
        RequestTemplate template = new RequestTemplate();

        new GestoPagoBearerTokenInterceptor("abc123", "key-1").apply(template);

        assertThat(template.headers().get("Authorization")).containsExactly("Bearer abc123");
        assertThat(template.headers().get("X-API-Key")).containsExactly("key-1");
    }

    @Test
    void apply_sinApiKey_noAgregaHeader() {
        RequestTemplate template = new RequestTemplate();

        new GestoPagoBearerTokenInterceptor("abc123", "").apply(template);

        assertThat(template.headers()).doesNotContainKey("X-API-Key");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void apply_sinToken_lanzaErrorAutenticacion(String token) {
        GestoPagoBearerTokenInterceptor interceptor = new GestoPagoBearerTokenInterceptor(token, null);

        assertThatThrownBy(() -> interceptor.apply(new RequestTemplate()))
                .isInstanceOf(GestoPagoAuthenticationException.class);
    }
}
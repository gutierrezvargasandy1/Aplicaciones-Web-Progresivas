package com.proyecto.servicios.config;

import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.exception.GestoPagoUnsuccessfulResponseException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GestoPagoErrorDecoderTest {

    private final GestoPagoErrorDecoder decoder = new GestoPagoErrorDecoder();

    @ParameterizedTest
    @ValueSource(ints = { 401, 403 })
    void decode_errorAutenticacion(int status) {
        assertThat(decoder.decode("getProductList", respuesta(status)))
                .isInstanceOf(GestoPagoAuthenticationException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = { 408, 504 })
    void decode_timeout(int status) {
        assertThat(decoder.decode("getProductList", respuesta(status)))
                .isInstanceOf(GestoPagoTimeoutException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 404, 500, 503 })
    void decode_respuestaNoExitosa(int status) {
        Exception ex = decoder.decode("getProductList", respuesta(status));

        assertThat(ex).isInstanceOf(GestoPagoUnsuccessfulResponseException.class);
        assertThat(((GestoPagoUnsuccessfulResponseException) ex).getProviderStatus()).isEqualTo(status);
        assertThat(ex.getMessage()).doesNotContain("EXPIRED");
    }

    private static Response respuesta(int status) {
        Request request = Request.create(Request.HttpMethod.GET,
                "https://gestopago.portalventas.net/sistema/service/getProductList.do",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return Response.builder()
                .status(status)
                .reason("reason")
                .request(request)
                .headers(Collections.emptyMap())
                .body("{\"success\":false,\"token\":\"EXPIRED\"}", StandardCharsets.UTF_8)
                .build();
    }
}
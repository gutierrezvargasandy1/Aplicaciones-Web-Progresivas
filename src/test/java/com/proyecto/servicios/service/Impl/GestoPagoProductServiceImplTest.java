package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.client.GestoPagoXmlParser;
import com.proyecto.servicios.exception.GestoPagoAuthenticationException;
import com.proyecto.servicios.exception.GestoPagoCommunicationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.exception.GestoPagoUnsuccessfulResponseException;
import com.proyecto.servicios.mapper.GestoPagoProductMapper;
import com.proyecto.servicios.model.gestopago.ProductoGestoPago;
import com.proyecto.servicios.model.gestopago.ProductosGestoPagoResponse;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestoPagoProductServiceImplTest {

    /** Respuesta basada en el ejemplo de la documentación de GestoPago. */
    private static final String XML_EXITO = """
            <?xml version='1.0' encoding='UTF-8'?>
            <RESPONSE>
                <MENSAJE>
                    <CODIGO>01</CODIGO>
                    <TEXTO>Operacion realizada con exito</TEXTO>
                </MENSAJE>
                <PRODUCTOS>
                    <producto servicio='AGUAKAN' producto='Agua Cancun' idServicio='56' idProducto='185'
                              idCatTipoServicio='15' tipoFront='2' precio='10.0' tipoReferencia='c'>
                        <legend><![CDATA[Pago de servicio de agua]]></legend>
                    </producto>
                    <producto servicio='TELCEL' producto='Telcel 50' idServicio='133' idProducto='582'
                              idCatTipoServicio='1' tipoFront='1' precio='50.0' tipoReferencia='n'/>
                </PRODUCTOS>
            </RESPONSE>
            """;

    private static final String XML_SIN_PRODUCTOS = """
            <?xml version='1.0' encoding='UTF-8'?>
            <RESPONSE>
                <MENSAJE><CODIGO>01</CODIGO><TEXTO>Operacion realizada con exito</TEXTO></MENSAJE>
                <PRODUCTOS/>
            </RESPONSE>
            """;

    private static final String XML_CODIGO_ERROR = """
            <?xml version='1.0' encoding='UTF-8'?>
            <RESPONSE>
                <MENSAJE><CODIGO>03</CODIGO><TEXTO>Dispositivo no identificado</TEXTO></MENSAJE>
            </RESPONSE>
            """;

    @Mock
    private GestoPagoProductClient productClient;

    private GestoPagoProductServiceImpl service;

    @BeforeEach
    void setUp() {
        GestoPagoProductMapper mapper = Mappers.getMapper(GestoPagoProductMapper.class);
        service = new GestoPagoProductServiceImpl(productClient, new GestoPagoXmlParser(), mapper);
    }

    // ------------------------------------------------------------ escenarios
    // exitosos

    @Test
    @DisplayName("Respuesta exitosa: interpreta el XML y mapea los productos")
    void obtenerProductos_respuestaExitosa() {
        when(productClient.getProductList()).thenReturn(bytes(XML_EXITO));

        ProductosGestoPagoResponse resultado = service.obtenerProductos();

        assertThat(resultado.getCodigo()).isZero();
        assertThat(resultado.getMensaje()).isEqualTo("Exito");
        assertThat(resultado.getTotal()).isEqualTo(2);

        ProductoGestoPago primero = resultado.getProductos().get(0);
        assertThat(primero.getIdProducto()).isEqualTo(185);
        assertThat(primero.getIdServicio()).isEqualTo(56);
        assertThat(primero.getNombre()).isEqualTo("Agua Cancun");
        assertThat(primero.getServicio()).isEqualTo("AGUAKAN");
        assertThat(primero.getIdCatTipoServicio()).isEqualTo(15);
        assertThat(primero.getTipoFront()).isEqualTo(2);
        assertThat(primero.getPrecio()).isEqualByComparingTo("10.0");
        assertThat(primero.getTipoReferencia()).isEqualTo("c");
        assertThat(primero.getLeyenda()).isEqualTo("Pago de servicio de agua");
        verify(productClient).getProductList();
    }

    @Test
    @DisplayName("Respuesta exitosa sin productos: devuelve lista vacía")
    void obtenerProductos_sinProductos() {
        when(productClient.getProductList()).thenReturn(bytes(XML_SIN_PRODUCTOS));

        ProductosGestoPagoResponse resultado = service.obtenerProductos();

        assertThat(resultado.getTotal()).isZero();
        assertThat(resultado.getProductos()).isEmpty();
    }

    // ------------------------------------------------------------ respuestas no
    // exitosas

    @Test
    @DisplayName("CODIGO distinto de 01: respuesta no exitosa")
    void obtenerProductos_codigoNoExitoso() {
        when(productClient.getProductList()).thenReturn(bytes(XML_CODIGO_ERROR));

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOf(GestoPagoUnsuccessfulResponseException.class)
                .hasMessageContaining("03");
    }

    @Test
    @DisplayName("Cuerpo vacío: respuesta no exitosa")
    void obtenerProductos_cuerpoVacio() {
        when(productClient.getProductList()).thenReturn(new byte[0]);

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOf(GestoPagoUnsuccessfulResponseException.class)
                .hasMessageContaining("vacía");
    }

    @Test
    @DisplayName("XML mal formado: respuesta no exitosa")
    void obtenerProductos_xmlInvalido() {
        when(productClient.getProductList()).thenReturn(bytes("<RESPONSE><MENSAJE>"));

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOf(GestoPagoUnsuccessfulResponseException.class)
                .hasMessageContaining("formato");
    }

    @Test
    @DisplayName("HTTP 500 del proveedor: se propaga como respuesta no exitosa")
    void obtenerProductos_http500() {
        when(productClient.getProductList())
                .thenThrow(new GestoPagoUnsuccessfulResponseException(
                        "GestoPago respondió con un estado no exitoso: 500", 500));

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOfSatisfying(GestoPagoUnsuccessfulResponseException.class, e -> {
                    assertThat(e.getProviderStatus()).isEqualTo(500);
                    assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                });
    }

    // ------------------------------------------------------------ autenticación

    @Test
    @DisplayName("Token expirado (403): error de autenticación sin exponer el token")
    void obtenerProductos_tokenExpirado() {
        when(productClient.getProductList())
                .thenThrow(new GestoPagoAuthenticationException("GestoPago rechazó el token (inválido o expirado)"));

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOf(GestoPagoAuthenticationException.class)
                .hasMessageNotContaining("Bearer");
    }

    // ------------------------------------------------------------ timeouts y
    // comunicación

    @Test
    @DisplayName("Read timeout: GestoPagoTimeoutException")
    void obtenerProductos_timeout() {
        RetryableException error = errorDeRed(new SocketTimeoutException("Read timed out"));
        when(productClient.getProductList()).thenThrow(error);

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOfSatisfying(GestoPagoTimeoutException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT));
    }

    @Test
    @DisplayName("Timeout anidado en la cadena de causas: también se detecta")
    void obtenerProductos_timeoutAnidado() {
        RetryableException error = errorDeRed(
                new IOException("error", new SocketTimeoutException("connect timed out")));
        when(productClient.getProductList()).thenThrow(error);

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOf(GestoPagoTimeoutException.class);
    }

    @Test
    @DisplayName("Conexión rechazada: GestoPagoCommunicationException")
    void obtenerProductos_conexionRechazada() {
        RetryableException error = errorDeRed(new ConnectException("Connection refused"));
        when(productClient.getProductList()).thenThrow(error);

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOfSatisfying(GestoPagoCommunicationException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    @DisplayName("Otro error de Feign: GestoPagoCommunicationException")
    void obtenerProductos_errorFeignGenerico() {
        FeignException error = mock(FeignException.class);
        when(productClient.getProductList()).thenThrow(error);

        assertThatThrownBy(() -> service.obtenerProductos())
                .isInstanceOf(GestoPagoCommunicationException.class);
    }

    // ------------------------------------------------------------ utilidades

    private static byte[] bytes(String xml) {
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Simula el error que lanza Feign cuando falla la red (timeout, conexión
     * rechazada, etc.).
     */
    private static RetryableException errorDeRed(Throwable causa) {
        RetryableException error = mock(RetryableException.class);
        when(error.getCause()).thenReturn(causa);
        return error;
    }
}
package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.client.GestoPagoXmlParser;
import com.proyecto.servicios.exception.GestoPagoCommunicationException;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.exception.GestoPagoTimeoutException;
import com.proyecto.servicios.exception.GestoPagoUnsuccessfulResponseException;
import com.proyecto.servicios.mapper.GestoPagoProductMapper;
import com.proyecto.servicios.model.gestopago.GestoPagoMensaje;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import com.proyecto.servicios.model.gestopago.ProductoGestoPago;
import com.proyecto.servicios.model.gestopago.ProductosGestoPagoResponse;
import com.proyecto.servicios.service.GestoPagoProductService;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GestoPagoProductServiceImpl implements GestoPagoProductService {

    public static final String CACHE_PRODUCTOS = "gestopagoProductos";

    private static final String OPERACION = "getProductList";
    private static final String CODIGO_EXITO_PROVEEDOR = "01";
    private static final int CODIGO_EXITO = 0;
    private static final String MENSAJE_EXITO = "Exito";

    private final GestoPagoProductClient productClient;
    private final GestoPagoXmlParser xmlParser;
    private final GestoPagoProductMapper productMapper;

    public GestoPagoProductServiceImpl(GestoPagoProductClient productClient,
            GestoPagoXmlParser xmlParser,
            GestoPagoProductMapper productMapper) {
        this.productClient = productClient;
        this.xmlParser = xmlParser;
        this.productMapper = productMapper;
    }

    /**
     * GestoPago permite máximo 3 consultas diarias a getProductList,
     * por eso la respuesta exitosa se guarda en caché (los errores no se guardan).
     */
    @Override
    @Cacheable(CACHE_PRODUCTOS)
    public ProductosGestoPagoResponse obtenerProductos() {
        long inicio = System.currentTimeMillis();
        log.info("Inicio invocación GestoPago operacion={}", OPERACION);
        try {
            GestoPagoProductListResponse respuesta = xmlParser.parsear(invocarProveedor());
            validarCodigoProveedor(respuesta.getMensaje());
            ProductosGestoPagoResponse resultado = construirRespuesta(respuesta);
            log.info("Fin invocación GestoPago operacion={} resultado=OK productos={} duracionMs={}",
                    OPERACION, resultado.getTotal(), duracion(inicio));
            return resultado;
        } catch (GestoPagoIntegrationException e) {
            log.error("Fin invocación GestoPago operacion={} resultado=ERROR tipo={} detalle={} duracionMs={}",
                    OPERACION, e.getClass().getSimpleName(), e.getMessage(), duracion(inicio));
            throw e;
        }
    }

    /**
     * Limpia la caché una vez al día para volver a consultar el catálogo
     * actualizado.
     */
    @Scheduled(cron = "${gestopago.product.cache-refresh-cron:0 0 3 * * *}")
    @CacheEvict(value = CACHE_PRODUCTOS, allEntries = true)
    public void limpiarCacheProductos() {
        log.info("Se limpia la caché del catálogo de productos GestoPago");
    }

    /**
     * Invoca al cliente y traduce las excepciones técnicas de Feign a excepciones
     * de dominio.
     */
    private byte[] invocarProveedor() {
        try {
            return productClient.getProductList();
        } catch (GestoPagoIntegrationException e) {
            throw e;
        } catch (RetryableException e) {
            if (esTimeout(e)) {
                throw new GestoPagoTimeoutException("GestoPago no respondió a tiempo", e);
            }
            throw new GestoPagoCommunicationException("No fue posible comunicarse con GestoPago", e);
        } catch (FeignException e) {
            throw new GestoPagoCommunicationException("Error de comunicación con GestoPago", e);
        }
    }

    private static boolean esTimeout(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private static void validarCodigoProveedor(GestoPagoMensaje mensaje) {
        String codigo = mensaje == null || mensaje.getCodigo() == null ? null : mensaje.getCodigo().trim();
        if (!CODIGO_EXITO_PROVEEDOR.equals(codigo)) {
            throw new GestoPagoUnsuccessfulResponseException(
                    "GestoPago respondió con código no exitoso: " + codigo, null);
        }
    }

    private ProductosGestoPagoResponse construirRespuesta(GestoPagoProductListResponse respuesta) {
        List<ProductoGestoPago> productos = respuesta.getProductos() == null
                ? Collections.emptyList()
                : productMapper.toProductos(respuesta.getProductos());

        ProductosGestoPagoResponse resultado = new ProductosGestoPagoResponse();
        resultado.setCodigo(CODIGO_EXITO);
        resultado.setMensaje(MENSAJE_EXITO);
        resultado.setProductos(productos);
        resultado.setTotal(productos.size());
        return resultado;
    }

    private static long duracion(long inicio) {
        return System.currentTimeMillis() - inicio;
    }
}
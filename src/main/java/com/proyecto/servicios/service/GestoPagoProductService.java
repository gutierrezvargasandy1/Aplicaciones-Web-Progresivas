package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.ProductosGestoPagoResponse;

public interface GestoPagoProductService {

    /**
     * Consulta el catálogo de productos en GestoPago.
     *
     * @throws com.proyecto.servicios.exception.GestoPagoIntegrationException si la
     *                                                                        integración
     *                                                                        falla
     */
    ProductosGestoPagoResponse obtenerProductos();
}
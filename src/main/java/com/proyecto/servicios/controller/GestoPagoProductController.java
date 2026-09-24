package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.gestopago.ProductosGestoPagoResponse;
import com.proyecto.servicios.service.GestoPagoProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GestoPagoProductController {

    private final GestoPagoProductService productService;

    public GestoPagoProductController(GestoPagoProductService productService) {
        this.productService = productService;
    }

    @GetMapping(value = "/gestopago/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductosGestoPagoResponse> obtenerProductos() {
        return new ResponseEntity<>(productService.obtenerProductos(), HttpStatus.OK);
    }
}
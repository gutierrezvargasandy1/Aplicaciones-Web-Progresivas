package com.proyecto.servicios.model.gestopago;

import com.proyecto.servicios.model.GenericResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductosGestoPagoResponse extends GenericResponse {
    private Integer total;
    private List<ProductoGestoPago> productos = new ArrayList<>();
}
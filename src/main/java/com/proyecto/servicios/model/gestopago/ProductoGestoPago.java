package com.proyecto.servicios.model.gestopago;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductoGestoPago {
    private Integer idProducto;
    private Integer idServicio;
    private String nombre;
    private String servicio;
    private Integer idCatTipoServicio;
    private Integer tipoFront;
    private BigDecimal precio;
    private String tipoReferencia;
    private String leyenda;
}
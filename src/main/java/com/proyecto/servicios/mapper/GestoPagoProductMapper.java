package com.proyecto.servicios.mapper;

import com.proyecto.servicios.model.gestopago.GestoPagoProductDto;
import com.proyecto.servicios.model.gestopago.ProductoGestoPago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GestoPagoProductMapper {

    @Mapping(target = "nombre", source = "producto")
    @Mapping(target = "leyenda", source = "legend")
    ProductoGestoPago toProducto(GestoPagoProductDto dto);

    List<ProductoGestoPago> toProductos(List<GestoPagoProductDto> dtos);
}
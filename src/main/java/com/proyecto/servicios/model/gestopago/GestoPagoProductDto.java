package com.proyecto.servicios.model.gestopago;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Elemento <producto> de getProductList.do; sus datos vienen como atributos.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class GestoPagoProductDto {

    @XmlAttribute
    private Integer idProducto;

    @XmlAttribute
    private Integer idServicio;

    @XmlAttribute
    private String servicio;

    @XmlAttribute
    private String producto;

    @XmlAttribute
    private Integer idCatTipoServicio;

    @XmlAttribute
    private Integer tipoFront;

    @XmlAttribute
    private BigDecimal precio;

    @XmlAttribute
    private String tipoReferencia;

    @XmlElement
    private String legend;
}
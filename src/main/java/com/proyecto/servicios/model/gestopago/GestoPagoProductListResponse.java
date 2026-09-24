package com.proyecto.servicios.model.gestopago;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.List;

/** Respuesta de GET /sistema/service/getProductList.do (raíz <RESPONSE>). */
@Data
@XmlRootElement(name = "RESPONSE")
@XmlAccessorType(XmlAccessType.FIELD)
public class GestoPagoProductListResponse {

    @XmlElement(name = "MENSAJE")
    private GestoPagoMensaje mensaje;

    @XmlElementWrapper(name = "PRODUCTOS")
    @XmlElement(name = "producto")
    private List<GestoPagoProductDto> productos;
}
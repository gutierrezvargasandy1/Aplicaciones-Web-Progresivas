package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoProductClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "gestoPagoProduct", url = "${gestopago.product.url}", configuration = GestoPagoProductClientConfig.class)
public interface GestoPagoProductClient {

    /**
     * Devuelve el XML crudo; GestoPagoXmlParser lo interpreta respetando su
     * encoding.
     */
    @GetMapping("/sistema/service/getProductList.do")
    byte[] getProductList();
}
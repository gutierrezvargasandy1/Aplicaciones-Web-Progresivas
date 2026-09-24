package com.proyecto.servicios.client;

import com.proyecto.servicios.exception.GestoPagoUnsuccessfulResponseException;
import com.proyecto.servicios.model.gestopago.GestoPagoProductListResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

/**
 * Convierte el XML de GestoPago en DTOs usando JAXB (DTD y entidades externas
 * deshabilitadas).
 */
@Component
public class GestoPagoXmlParser {

    private final JAXBContext context;
    private final XMLInputFactory inputFactory;

    public GestoPagoXmlParser() {
        this.context = crearContexto();
        this.inputFactory = XMLInputFactory.newFactory();
        this.inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        this.inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    public GestoPagoProductListResponse parsear(byte[] xml) {
        if (xml == null || xml.length == 0) {
            throw new GestoPagoUnsuccessfulResponseException("GestoPago devolvió una respuesta vacía", null);
        }
        XMLStreamReader reader = null;
        try {
            reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(xml));
            return context.createUnmarshaller()
                    .unmarshal(reader, GestoPagoProductListResponse.class)
                    .getValue();
        } catch (JAXBException | XMLStreamException e) {
            throw new GestoPagoUnsuccessfulResponseException(
                    "La respuesta de GestoPago no tiene el formato XML esperado", null, e);
        } finally {
            cerrar(reader);
        }
    }

    private static JAXBContext crearContexto() {
        try {
            return JAXBContext.newInstance(GestoPagoProductListResponse.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("No fue posible inicializar el parser XML de GestoPago", e);
        }
    }

    private static void cerrar(XMLStreamReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (XMLStreamException ignored) {
            // El reader ya no se usa; no hay nada más que liberar.
        }
    }
}
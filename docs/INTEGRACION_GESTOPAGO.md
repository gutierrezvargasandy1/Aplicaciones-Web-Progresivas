# Integración GestoPago – Catálogo de productos

## Solución
Se integró el endpoint `GET /sistema/service/getProductList.do` de GestoPago y se expone como
`GET /gestopago/productos`, respetando las capas existentes del proyecto:

Controller (GestoPagoProductController)
  → Service (GestoPagoProductServiceImpl)
    → Client (GestoPagoProductClient – Feign + GestoPagoXmlParser)
      → GestoPago

## Configuración
Propiedades `gestopago.product.*` (misma convención que `gestopago.auth.*`): URL base, token,
API key y timeouts. El token y la API key se leen de variables de entorno
(`GESTOPAGO_PRODUCT_TOKEN`, `GESTOPAGO_API_KEY`); no están en el código ni en el repositorio.
La ruta del endpoint vive en la anotación del cliente Feign, igual que en GestoPagoAuthClient.

## Autenticación
`GestoPagoBearerTokenInterceptor` agrega `Authorization: Bearer <token>` (y `X-API-Key` si existe).
`GestoPagoProductClientConfig` no lleva `@Configuration`, para que el interceptor solo aplique a
este cliente y no al cliente de login.

## Manejo de errores
| Escenario | Excepción | HTTP de nuestra API |
|---|---|---|
| Token vacío, 401 o 403 (token expirado) | GestoPagoAuthenticationException | 502 |
| Timeout (conexión/lectura, 408, 504) | GestoPagoTimeoutException | 504 |
| Error de red (conexión rechazada, DNS) | GestoPagoCommunicationException | 503 |
| HTTP no 2xx, CODIGO ≠ "01", XML vacío o inválido | GestoPagoUnsuccessfulResponseException | 502 |

`GestoPagoExceptionHandler` convierte estas excepciones en el `GenericResponse` del proyecto.

## Logs
Se registran el inicio y el fin de cada invocación (resultado, número de productos y duración).
En los errores solo se registra el tipo de excepción y un mensaje controlado. El token, los headers
y el cuerpo de la respuesta del proveedor nunca se registran (Feign en nivel `basic`).

## Decisiones técnicas
- **OpenFeign:** el proyecto ya lo usa (`@EnableFeignClients`, GestoPagoAuthClient).
- **XML con JAXB:** GestoPago responde en XML con atributos; JAXB ya estaba en las dependencias.
  El cliente recibe `byte[]` para respetar el encoding del XML, y el parser deshabilita DTD y
  entidades externas (prevención de XXE).
- **Validación del CODIGO:** GestoPago puede responder HTTP 200 con un error; solo "01" es éxito.
- **Caché diaria:** GestoPago permite máximo 3 llamadas al día a getProductList, así que se usa
  `@Cacheable` y un `@Scheduled` limpia la caché diariamente. Los errores no se guardan en caché.
- **DTO externo separado del modelo interno** (MapStruct), para que un cambio del proveedor no
  afecte a nuestra API.
- **Inyección por constructor**, igual que en GestoPagoTokenServiceImpl.
- **Refresh del token cada 24 h** (`gestopago.auth.refresh-rate-ms=86400000`), porque la
  documentación prohíbe pedir tokens de más.

## Pruebas
`GestoPagoProductServiceImplTest` (JUnit 5 + Mockito, sin levantar Spring) cubre: éxito, catálogo
vacío, CODIGO no exitoso, cuerpo vacío, XML inválido, HTTP 500, token expirado, timeout, timeout
anidado, conexión rechazada y error genérico de Feign. Además hay pruebas del ErrorDecoder y del
interceptor del token.

## Mejora futura
Obtener el token del que ya renueva `GestoPagoTokenService` y guarda en la base de datos, en lugar
de tomarlo de la configuración.
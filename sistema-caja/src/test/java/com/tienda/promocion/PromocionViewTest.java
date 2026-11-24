package com.tienda.promocion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

/**
 * Prueba de caracterización de la captura de datos de promociones (ISS-32).
 */
class PromocionViewTest {

    /**
     * Comprueba que la vista recorta la descripción y convierte el porcentaje.
     */
    @Test
    void pedirDatosPromocion_entradaValida_devuelvePromocionCapturada() {
        // RF-15: registrar, editar y eliminar promociones y descuentos para productos específicos.
        String entrada = "  Promoción de temporada  \n15.5\n";

        try (Scanner scanner = new Scanner(
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8)) {
            PromocionModel promocion = new PromocionView().pedirDatosPromocion(scanner);

            assertNotNull(promocion);
            assertEquals("Promoción de temporada", promocion.getDescripcion());
            assertEquals(15.5, promocion.getPorcentajeDescuento(), 0.0001);
        }
    }
}

package com.tienda.promocion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

/**
 * Prueba de caracterización de la captura de datos de promociones (ISS-32).
 */
class PromocionViewTest {

    /**
     * Comprueba la captura válida y el comportamiento ante un porcentaje no numérico.
     */
    @Test
    void pedirDatosPromocion_entradaValida_devuelvePromocionCapturada() {
        // RF-15: registrar, editar y eliminar promociones y descuentos para productos específicos.
        String entradaValida = "  Promoción de temporada  \n15.5\n";

        try (Scanner scanner = crearScanner(entradaValida)) {
            PromocionModel promocion =
                    new PromocionView().pedirDatosPromocion(scanner);

            assertNotNull(promocion);
            assertEquals("Promoción de temporada", promocion.getDescripcion());
            assertEquals(15.5, promocion.getPorcentajeDescuento(), 0.0001);
        }

        // Caso de error: actualmente un porcentaje no numérico cancela la captura y devuelve null.
        String entradaInvalida = "Promoción con porcentaje inválido\nabc\n";

        try (Scanner scanner = crearScanner(entradaInvalida)) {
            PromocionModel promocion =
                    new PromocionView().pedirDatosPromocion(scanner);

            assertNull(promocion,
                    "La vista debe devolver null cuando el porcentaje no es numérico");
        }
    }

    /**
     * Construye un Scanner con una entrada simulada.
     *
     * @param entrada texto que recibirá la vista
     * @return scanner preparado para la prueba
     */
    private Scanner crearScanner(String entrada) {
        return new Scanner(
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }
}
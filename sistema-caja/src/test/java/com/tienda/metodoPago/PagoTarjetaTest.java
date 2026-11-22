package com.tienda.metodoPago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de caracterización de {@link PagoTarjeta} (ISS-28).
 *
 * <p>El pago con tarjeta es simulado: solo se valida que el número no esté
 * vacío (ERS, RF-06).</p>
 */
class PagoTarjetaTest {

    private PrintStream salidaOriginal;
    private ByteArrayOutputStream salida;
    private PagoTarjeta pago;

    @BeforeEach
    void preparar() {
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
        pago = new PagoTarjeta();
    }

    @AfterEach
    void restaurar() {
        System.setOut(salidaOriginal);
    }

    private String salida() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    // ===================== getNumTarjeta / setNumTarjeta =====================

    @Test
    void numTarjeta_nuevoPago_esNull() {
        // RF-06
        assertNull(pago.getNumTarjeta());
    }

    @Test
    void setNumTarjeta_guardaElTextoSinCambiarlo() {
        // RF-06: no se quitan espacios ni se valida el formato
        pago.setNumTarjeta(" 4111 1111 ");

        assertEquals(" 4111 1111 ", pago.getNumTarjeta());
    }

    // ===================== procesar =====================

    @Test
    void procesar_conNumero_apruebaEImprimePago() {
        // RF-06
        pago.setNumTarjeta("4111111111111111");

        assertTrue(pago.procesar(25.5));
        assertEquals("Pago con tarjeta procesado: $25.5" + System.lineSeparator(), salida());
    }

    @Test
    void procesar_sinNumero_rechazaEImprimeInvalido() {
        // RF-06: el número es null si nunca se asignó
        assertFalse(pago.procesar(10.0));
        assertEquals("Número de tarjeta inválido" + System.lineSeparator(), salida());
    }

    @Test
    void procesar_numeroVacio_rechaza() {
        // RF-06
        pago.setNumTarjeta("");

        assertFalse(pago.procesar(10.0));
        assertTrue(salida().contains("Número de tarjeta inválido"));
    }

    @Test
    void procesar_numeroSoloConEspacios_seAprueba() {
        // RF-06
        // B-08: se usa isEmpty() y no isBlank(); "   " se acepta como número de tarjeta
        pago.setNumTarjeta("   ");

        assertTrue(pago.procesar(10.0));
    }

    @Test
    void procesar_numeroConLetras_seAprueba() {
        // RF-06: el pago es simulado y no se valida el formato del número
        pago.setNumTarjeta("abc");

        assertTrue(pago.procesar(10.0));
    }

    @Test
    void procesar_montoCeroONegativo_seApruebaConNumero() {
        // RF-06: el monto no se valida
        pago.setNumTarjeta("1234");

        assertTrue(pago.procesar(0.0));
        assertTrue(pago.procesar(-5.0));
        assertTrue(salida().contains("Pago con tarjeta procesado: $-5.0"));
    }
}

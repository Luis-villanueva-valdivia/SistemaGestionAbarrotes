package com.tienda.metodoPago;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de caracterización de {@link PagoEfectivo} (ISS-28).
 *
 * <p>No usan base de datos. {@code procesar} imprime mensajes, así que se
 * captura {@code System.out} para comprobar el texto exacto.</p>
 */
class PagoEfectivoTest {

    private static final double DELTA = 0.0001;

    private PrintStream salidaOriginal;
    private ByteArrayOutputStream salida;
    private PagoEfectivo pago;

    @BeforeEach
    void preparar() {
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
        pago = new PagoEfectivo();
    }

    @AfterEach
    void restaurar() {
        System.setOut(salidaOriginal);
    }

    private String salida() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    // ===================== getMontoRecibido / setMontoRecibido =====================

    @Test
    void montoRecibido_nuevoPago_empiezaEnCero() {
        // RF-06
        assertEquals(0.0, pago.getMontoRecibido(), DELTA);
    }

    @Test
    void setMontoRecibido_guardaElValorTalCual() {
        // RF-06
        pago.setMontoRecibido(150.75);

        assertEquals(150.75, pago.getMontoRecibido(), DELTA);
    }

    // ===================== procesar =====================

    @Test
    void procesar_montoMayorAlTotal_apruebaEImprimePagoYCambio() {
        // RF-06: el monto recibido debe cubrir el total y se calcula el cambio
        pago.setMontoRecibido(50.0);

        assertTrue(pago.procesar(20.0));
        assertEquals("Pago en efectivo procesado: $20.0" + System.lineSeparator()
                + "   Cambio: $30.0" + System.lineSeparator(), salida());
    }

    @Test
    void procesar_montoIgualAlTotal_apruebaConCambioCero() {
        // RF-06: la comparación es "menor que", así que el monto exacto se acepta
        pago.setMontoRecibido(20.0);

        assertTrue(pago.procesar(20.0));
        assertTrue(salida().contains("   Cambio: $0.0"));
    }

    @Test
    void procesar_montoMenorAlTotal_rechazaEImprimeInsuficiente() {
        // RF-06
        pago.setMontoRecibido(19.99);

        assertFalse(pago.procesar(20.0));
        assertEquals("Efectivo insuficiente" + System.lineSeparator(), salida());
    }

    @Test
    void procesar_sinMontoRecibido_rechazaCualquierTotalPositivo() {
        // RF-06: el monto recibido empieza en 0
        assertFalse(pago.procesar(0.01));
    }

    @Test
    void procesar_totalCero_seApruebaAunqueNoSeRecibaDinero() {
        // RF-06
        assertTrue(pago.procesar(0.0));
    }

    @Test
    void procesar_totalNegativo_seAprueba() {
        // RF-06
        // B-07: no se valida que el total ni el monto sean positivos;
        // un total de -10 con 0 recibido se aprueba y da cambio de $10.0
        assertTrue(pago.procesar(-10.0));
        assertTrue(salida().contains("   Cambio: $10.0"));
    }

    @Test
    void procesar_imprimeLosMontosSinFormatoDeDosDecimales() {
        // RF-06: el mensaje concatena el double, no usa String.format
        pago.setMontoRecibido(100.0);

        pago.procesar(33.333);

        assertTrue(salida().contains("Pago en efectivo procesado: $33.333"));
    }

    // ===================== calcularCambio =====================

    @Test
    void calcularCambio_restaElTotalAlMontoRecibido() {
        // RF-06
        pago.setMontoRecibido(100.0);

        assertEquals(35.5, pago.calcularCambio(64.5), DELTA);
    }

    @Test
    void calcularCambio_montoInsuficiente_devuelveCambioNegativo() {
        // RF-06: calcularCambio no valida; quien llama debe usar antes procesar
        pago.setMontoRecibido(10.0);

        assertEquals(-5.0, pago.calcularCambio(15.0), DELTA);
    }

    @Test
    void calcularCambio_noImprimeNada() {
        // RF-06
        pago.setMontoRecibido(10.0);

        pago.calcularCambio(5.0);

        assertEquals("", salida());
    }
}

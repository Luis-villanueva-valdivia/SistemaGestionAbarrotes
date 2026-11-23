package com.tienda.reporte;

import static com.tienda.reporte.DatosPruebaReporte.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de caracterización de {@link ReporteView#mostrarMenu}: se simula la
 * entrada del usuario y se captura lo que se imprime en consola.
 */
class ReporteViewTest {

    private DatosPruebaReporte datos;

    @BeforeEach
    void prepararDatos() throws SQLException {
        datos = new DatosPruebaReporte();
        datos.insertar();
    }

    @AfterEach
    void borrarDatos() throws SQLException {
        datos.limpiar();
    }

    @Test
    void mostrarMenu_opcionCero_muestraLasCincoOpcionesYRegresa() {
        // RF-16, RF-17, RF-18
        String salida = ejecutarMenu("0");

        assertTrue(salida.contains("--- Reportes ---"));
        assertTrue(salida.contains("1) Ventas (diario/semanal/mensual/anual)"));
        assertTrue(salida.contains("2) Inventario"));
        assertTrue(salida.contains("3) Producto vendido por ID"));
        assertTrue(salida.contains("4) Productos vendidos por categoría"));
        assertTrue(salida.contains("0) Volver"));
    }

    @Test
    void mostrarMenu_opcionUnoDiario_imprimeUnaLineaPorPeriodo() {
        // RF-16
        String salida = ejecutarMenu("1", "diario", "0");

        assertTrue(salida.contains("Resultados para: diario"));
        assertTrue(salida.contains("Periodo=" + DIA + " | Cantidad=" + VENTAS_DEL_DIA + " | Total=" + TOTAL_DEL_DIA));
    }

    @Test
    void mostrarMenu_opcionUnoTipoDesconocido_muestraElReporteDiarioConElTextoEscrito() {
        // RF-16: caso límite
        String salida = ejecutarMenu("1", "quincenal", "0");

        assertTrue(salida.contains("Resultados para: quincenal"));
        assertTrue(salida.contains("Periodo=" + DIA));
    }

    @Test
    void mostrarMenu_opcionDos_imprimeElInventarioConNombreYCategoriaCruzados() {
        // RF-17 (la ERS ya lo describe). B-XX: la cabecera dice "ID | Nombre | Cantidad | Categoria",
        // pero se imprime la categoría donde va el nombre y viceversa
        String salida = ejecutarMenu("2", "0");

        assertTrue(salida.contains("Inventario (ID | Nombre | Cantidad | Categoria):"));
        assertTrue(salida.contains(datos.idProductoA + " | " + CATEGORIA + " | " + STOCK_A + " | " + NOMBRE_A));
    }

    @Test
    void mostrarMenu_opcionTresConVentas_muestraLaCategoriaEnLugarDelNombre() {
        // RF-18. B-XX: "Producto:" muestra la categoría, no el nombre del producto
        String salida = ejecutarMenu("3", String.valueOf(datos.idProductoA), "0");

        assertTrue(salida.contains("Producto: " + CATEGORIA + " | Total vendido: " + VENDIDO_A
                + " | Total ventas: " + TOTAL_A));
        assertFalse(salida.contains("Producto: " + NOMBRE_A));
    }

    @Test
    void mostrarMenu_opcionTresSinVentas_avisaQueNoHayVentas() {
        // RF-18: caso límite, el producto existe pero no se ha vendido
        String salida = ejecutarMenu("3", String.valueOf(datos.idProductoC), "0");

        assertTrue(salida.contains("No hay ventas para ese producto o no existe."));
    }

    @Test
    void mostrarMenu_opcionTresIdQueNoExiste_avisaQueNoHayVentas() {
        // RF-18: caso de error
        String salida = ejecutarMenu("3", String.valueOf(ID_INEXISTENTE), "0");

        assertTrue(salida.contains("No hay ventas para ese producto o no existe."));
    }

    @Test
    void mostrarMenu_opcionTresIdNoNumerico_muestraIdInvalido() {
        // RF-18: caso de error
        String salida = ejecutarMenu("3", "abc", "0");

        assertTrue(salida.contains("ID inválido"));
    }

    @Test
    void mostrarMenu_opcionCuatro_imprimeLosVendidosConLaCategoriaEnLaColumnaNombre() {
        // RF-18. B-XX: la columna "Nombre" muestra la categoría
        String salida = ejecutarMenu("4", CATEGORIA, "0");

        assertTrue(salida.contains("Productos vendidos en categoría (ID | Nombre | Cantidad | Total):"));
        assertTrue(salida.contains(datos.idProductoA + " | " + CATEGORIA + " | " + VENDIDO_A + " | " + TOTAL_A));
        assertTrue(salida.contains(datos.idProductoB + " | " + CATEGORIA + " | " + VENDIDO_B + " | " + TOTAL_B));
    }

    @Test
    void mostrarMenu_opcionCuatroCategoriaQueNoExiste_soloImprimeLaCabecera() {
        // RF-18: caso de error, la lista viene vacía y no hay mensaje de aviso
        String salida = ejecutarMenu("4", "CategoriaQueNoExiste31", "0");

        assertTrue(salida.contains("Productos vendidos en categoría (ID | Nombre | Cantidad | Total):"));
        assertFalse(salida.contains(" | " + CATEGORIA + " | "));
    }

    @Test
    void mostrarMenu_opcionQueNoExiste_muestraOpcionInvalidaYVuelveAPreguntar() {
        // RF-16: caso de error
        String salida = ejecutarMenu("9", "0");

        assertTrue(salida.contains("Opción inválida"));
        assertTrue(salida.indexOf("--- Reportes ---") != salida.lastIndexOf("--- Reportes ---"),
                "El menú se debe mostrar otra vez");
    }

    /**
     * Ejecuta el menú con las líneas indicadas como entrada y devuelve todo lo
     * que se imprimió. Restaura System.in y System.out al terminar.
     */
    private static String ejecutarMenu(String... lineas) {
        InputStream entradaOriginal = System.in;
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salidaCapturada = new ByteArrayOutputStream();
        byte[] entrada = (String.join("\n", lineas) + "\n").getBytes(StandardCharsets.UTF_8);

        try (PrintStream salidaTemporal = new PrintStream(salidaCapturada, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(entrada));
            System.setOut(salidaTemporal);
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            new ReporteView().mostrarMenu(scanner, new ReporteController());
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(salidaOriginal);
        }
        return salidaCapturada.toString(StandardCharsets.UTF_8);
    }
}

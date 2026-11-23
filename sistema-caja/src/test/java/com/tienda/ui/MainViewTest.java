package com.tienda.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.util.ConexionDB;

/**
 * Pruebas de caracterización de {@link MainView}, el menú de consola (ISS-28).
 *
 * <p>{@code MainView} crea su {@code Scanner} sobre {@code System.in} al
 * construirse, así que la entrada simulada se pone <b>antes</b> de crear la
 * vista. Las ventas que se registran desde el menú quedan en la base de
 * pruebas y se borran al terminar cada prueba.</p>
 *
 * <p>De los submenús de otros módulos solo se prueba que el menú principal
 * entra y regresa; su contenido lo prueba cada responsable.</p>
 */
class MainViewTest {

    private static final String N = System.lineSeparator();
    /** MainView escribe un "\n" literal antes de cada encabezado (no usa el separador del sistema). */
    private static final String SALTO = "\n";
    private static final String MENU_PRINCIPAL = SALTO
            + "=== SISTEMA DE CAJA - MENU PRINCIPAL ===" + N
            + "1) Clientes" + N
            + "2) Inventario" + N
            + "3) Ventas" + N
            + "4) Promociones" + N
            + "5) Reportes" + N
            + "0) Salir" + N
            + "Elija una opción: ";
    private static final String MENU_VENTAS = SALTO
            + "--- Ventas ---" + N
            + "1) Registrar venta" + N
            + "2) Listar ventas" + N
            + "0) Volver" + N
            + "Opción: ";

    private static Locale localeOriginal;

    private InputStream entradaOriginal;
    private PrintStream salidaOriginal;
    private ByteArrayOutputStream salida;

    private int maxVentaAntes;
    private int maxItemAntes;
    private int maxMovimientoAntes;
    private Map<String, Integer> secuenciasAntes;
    private Map<Integer, Integer> existenciasAntes;

    @BeforeAll
    static void prepararTodo() {
        localeOriginal = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("es-MX"));
        ConexionDB.getInstancia().getConexion();
    }

    @AfterAll
    static void restaurarTodo() {
        Locale.setDefault(localeOriginal);
    }

    @BeforeEach
    void preparar() throws SQLException {
        entradaOriginal = System.in;
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
        tomarFotoDeLaBase();
    }

    @AfterEach
    void restaurar() throws SQLException {
        System.setIn(entradaOriginal);
        System.setOut(salidaOriginal);
        limpiarBase();
    }

    /** Pone la entrada simulada, crea la vista y corre el menú hasta que termine. */
    private String ejecutar(String... lineas) {
        simularEntrada(lineas);
        new MainView().mostrarMenu();
        return salida.toString(StandardCharsets.UTF_8);
    }

    private void simularEntrada(String... lineas) {
        String texto = String.join("\n", lineas) + "\n";
        System.setIn(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
    }

    private static int veces(String texto, String buscado) {
        return texto.split(java.util.regex.Pattern.quote(buscado), -1).length - 1;
    }

    // ===================== Menú principal =====================

    @Test
    void mostrarMenu_opcionTres_abreElSubmenuDeVentasYLaCeroSale() {
        // RF-06, RF-10: la opción "3) Ventas" abre el submenú con "Registrar venta" (RF-06)
        // y "Listar ventas" (RF-10); "0) Volver" regresa y "0) Salir" termina
        // (ERS, Apéndice A, figura A.6)
        assertEquals(MENU_PRINCIPAL + MENU_VENTAS + "Volviendo al menú principal..." + N
                + MENU_PRINCIPAL + "Saliendo..." + N, ejecutar("3", "0", "0"));
    }

    @Test
    void mostrarMenu_opcionInvalida_avisaYPermiteSeguirAListarVentas() throws SQLException {
        // RF-10: después de opciones inválidas, el menú sigue llevando a "Listar ventas"
        String texto = ejecutar("9", "abc", "", "3", "2", "0");

        assertEquals(3, veces(texto, "Opción inválida"));
        assertTrue(texto.contains("===== HISTORIAL DE VENTAS ====="));
        assertEquals(contar("SELECT COUNT(*) FROM venta"), veces(texto, "ID:"));
        assertTrue(texto.endsWith("Saliendo..." + N));
    }

    @Test
    void mostrarMenu_opcionesConEspacios_seRecortanYListanLasVentas() {
        // RF-10: "  3  " y "  2  " se recortan y llegan a "Listar ventas"
        String texto = ejecutar("  3  ", "  2  ", "  0  ");

        assertTrue(texto.contains("===== HISTORIAL DE VENTAS ====="));
        assertFalse(texto.contains("Opción inválida"));
        assertTrue(texto.endsWith("Saliendo..." + N));
    }

    @Test
    void mostrarMenu_seAcabaLaEntradaAlRegistrarVenta_lanzaExcepcionYNoGuarda() throws SQLException {
        // RF-06: si la entrada se cierra a media venta, se lanza NoSuchElementException
        // y no se guarda ninguna venta
        simularEntrada("3", "1", "", "1");
        MainView vista = new MainView();

        assertThrows(NoSuchElementException.class, vista::mostrarMenu);
        assertEquals(0, contar("SELECT COUNT(*) FROM venta WHERE id > " + maxVentaAntes));
    }

    @Test
    void main_creaLaVistaYPermiteListarLasVentas() {
        // RF-10: el punto de entrada de consola llega a "Listar ventas"
        simularEntrada("3", "2", "0");

        MainView.main(new String[0]);

        String texto = salida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.startsWith(MENU_PRINCIPAL + MENU_VENTAS + "===== HISTORIAL DE VENTAS ====="));
        assertTrue(texto.endsWith("Saliendo..." + N));
    }

    // ===================== Submenú de ventas =====================

    @Test
    void menuVentas_opcionInvalida_tambienRegresa() {
        // RF-06, RF-10: cualquier opción distinta de 1 y 2 regresa sin avisar del error
        String texto = ejecutar("3", "7", "0");

        assertTrue(texto.contains("Volviendo al menú principal..."));
        assertFalse(texto.contains("Opción inválida"));
    }

    @Test
    void menuVentas_listarVentas_muestraElHistorialDeLaBase() throws SQLException {
        // RF-10
        String texto = ejecutar("3", "2", "0");

        assertTrue(texto.contains("===== HISTORIAL DE VENTAS ====="));
        assertEquals(contar("SELECT COUNT(*) FROM venta"), veces(texto, "ID:"));
    }

    @Test
    void menuVentas_registrarSinProductos_noGuardaNada() throws SQLException {
        // RF-06
        String texto = ejecutar("3", "1", "", "", "0");

        assertTrue(texto.contains("No se puede registrar una venta sin productos."));
        assertFalse(texto.contains("Venta registrada con ID"));
        assertEquals(0, contar("SELECT COUNT(*) FROM venta WHERE id > " + maxVentaAntes));
    }

    @Test
    void menuVentas_registrarEnEfectivo_guardaVentaMuestraTicketYDescuentaInventario() throws SQLException {
        // RF-06, RF-08, RF-09
        int cocaAntes = existencias(1);

        String texto = ejecutar("3", "1", "", "1", "2", "", "1", "50", "0");

        int id = ultimaVenta();
        assertTrue(id > maxVentaAntes, "Debe guardarse una venta nueva");
        assertTrue(texto.contains("===== TICKET =====" + N + "ID: " + id + N));
        assertTrue(texto.contains(" - Coca Cola x2  @ $10.00  = $20.00" + N + "Total: $20.00" + N));
        assertTrue(texto.contains("==================" + N + "Venta registrada con ID: " + id + N));
        // El ticket de consola no muestra el método de pago ni el cambio (no usa generarTicket)
        assertFalse(texto.contains("Cambio: $30.00"));
        assertEquals("20.0|null|Efectivo|null", filaVenta(id));
        assertEquals(1, contar("SELECT COUNT(*) FROM venta_item WHERE venta_id = " + id));
        assertEquals(1, contar("SELECT COUNT(*) FROM movimiento WHERE id > " + maxMovimientoAntes
                + " AND producto_id = 1 AND tipo = 'SALIDA' AND cantidad = 2"));
        assertEquals(cocaAntes - 2, existencias(1));
    }

    @Test
    void menuVentas_registrarConTarjeta_guardaElNumero() throws SQLException {
        // RF-06
        ejecutar("3", "1", "", "2", "1", "", "2", "4111", "0");

        assertEquals("10.5|null|Tarjeta|4111", filaVenta(ultimaVenta()));
    }

    @Test
    void menuVentas_registrarConClienteVerificado_guardaLaVentaSinCliente() throws SQLException {
        // RF-06
        // B-05: el cliente se verifica por teléfono, pero la venta queda con cliente_id NULL
        String texto = ejecutar("3", "1", "5512345678", "1", "1", "", "2", "4111", "0");

        assertTrue(texto.contains("Cliente verificado: Juan Pérez"));
        assertTrue(texto.contains("Cliente: Juan Pérez"));
        assertEquals("10.0|null|Tarjeta|4111", filaVenta(ultimaVenta()));
    }

    // ===================== Otros submenús: solo entrar y regresar =====================

    @Test
    void menuClientes_opcionCero_muestraSusOpcionesYRegresa() {
        // RF-12, RF-14
        String texto = ejecutar("1", "0", "0");

        assertTrue(texto.contains(SALTO + "--- Clientes ---" + N
                + "1) Buscar cliente por ID" + N
                + "2) Listar clientes frecuentes" + N
                + "3) Registrar cliente" + N
                + "0) Volver" + N
                + "Opción: Volviendo al menú principal..." + N));
    }

    @Test
    void menuInventario_opcionCero_muestraSusOpcionesYRegresa() {
        // RF-01, RF-02, RF-03, RF-11
        String texto = ejecutar("2", "0", "0");

        assertTrue(texto.contains(SALTO + "--- Inventario ---" + N
                + "1) Listar productos" + N
                + "2) Agregar producto" + N
                + "3) Editar producto" + N
                + "4) Eliminar producto" + N
                + "5) Buscar producto" + N
                + "6) Registrar movimiento" + N
                + "0) Volver" + N
                + "Opción: Volviendo al menú principal..." + N));
    }

    @Test
    void menuPromociones_opcionCero_muestraSusOpcionesYRegresa() {
        // RF-15
        String texto = ejecutar("4", "0", "0");

        assertTrue(texto.contains(SALTO + "--- Promociones ---" + N
                + "1) Listar promociones" + N
                + "2) Agregar promocion" + N
                + "3) Editar promocion" + N
                + "4) Eliminar promocion" + N
                + "0) Volver" + N
                + "Opción: Volviendo al menú principal..." + N));
    }

    @Test
    void menuReportes_opcionCero_entraAlMenuDeReportesYRegresa() {
        // RF-16, RF-17, RF-18: el submenú lo imprime ReporteView
        String texto = ejecutar("5", "0", "0");

        assertTrue(texto.contains("--- Reportes ---"));
        assertEquals(2, veces(texto, "=== SISTEMA DE CAJA - MENU PRINCIPAL ==="));
        assertTrue(texto.endsWith("Saliendo..." + N));
    }

    // ===================== Ayudas de base de datos =====================

    private static Connection conexion() {
        return ConexionDB.getInstancia().getConexion();
    }

    private static int contar(String sql) throws SQLException {
        try (Statement st = conexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static int ultimaVenta() throws SQLException {
        return contar("SELECT MAX(id) FROM venta");
    }

    private static int existencias(int productoId) throws SQLException {
        return contar("SELECT cantidad FROM producto WHERE id = " + productoId);
    }

    /** total|cliente_id|metodo_pago|numero_tarjeta de una venta. */
    private static String filaVenta(int id) throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement(
                "SELECT total, cliente_id, metodo_pago, numero_tarjeta FROM venta WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1) + "|" + rs.getObject(2) + "|" + rs.getString(3) + "|" + rs.getString(4);
            }
        }
    }

    private void tomarFotoDeLaBase() throws SQLException {
        maxVentaAntes = contar("SELECT COALESCE(MAX(id), 0) FROM venta");
        maxItemAntes = contar("SELECT COALESCE(MAX(id), 0) FROM venta_item");
        maxMovimientoAntes = contar("SELECT COALESCE(MAX(id), 0) FROM movimiento");
        secuenciasAntes = new HashMap<>();
        try (Statement st = conexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT name, seq FROM sqlite_sequence")) {
            while (rs.next()) {
                secuenciasAntes.put(rs.getString(1), rs.getInt(2));
            }
        }
        existenciasAntes = new HashMap<>();
        try (Statement st = conexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT id, cantidad FROM producto")) {
            while (rs.next()) {
                existenciasAntes.put(rs.getInt(1), rs.getInt(2));
            }
        }
    }

    private void limpiarBase() throws SQLException {
        try (Statement st = conexion().createStatement()) {
            st.executeUpdate("DELETE FROM venta_item WHERE id > " + maxItemAntes);
            st.executeUpdate("DELETE FROM venta WHERE id > " + maxVentaAntes);
            st.executeUpdate("DELETE FROM movimiento WHERE id > " + maxMovimientoAntes);
        }
        try (PreparedStatement ps = conexion().prepareStatement("UPDATE sqlite_sequence SET seq = ? WHERE name = ?")) {
            for (Map.Entry<String, Integer> e : secuenciasAntes.entrySet()) {
                ps.setInt(1, e.getValue());
                ps.setString(2, e.getKey());
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = conexion().prepareStatement("UPDATE producto SET cantidad = ? WHERE id = ?")) {
            for (Map.Entry<Integer, Integer> e : existenciasAntes.entrySet()) {
                ps.setInt(1, e.getValue());
                ps.setInt(2, e.getKey());
                ps.executeUpdate();
            }
        }
    }
}

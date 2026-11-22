package com.tienda.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.producto.ProductoModel;
import com.tienda.reporte.ReporteModel;
import com.tienda.util.ConexionDB;

/**
 * Pruebas de caracterización de {@link VentaController} (ISS-28).
 *
 * <p>El controlador recibe su DAO por el constructor, así que se usa un DAO
 * falso. La excepción es {@code registrarVenta}: crea un {@code MovimientoDAO}
 * real dentro del método (LLA-11), así que esas pruebas escriben en la base de
 * pruebas aislada y la dejan como estaba al terminar.</p>
 */
class VentaControllerTest {

    private static final double DELTA = 0.0001;
    private static Locale localeOriginal;

    private VentaDAOFalso daoFalso;
    private VentaController controller;
    private PrintStream salidaOriginal;
    private ByteArrayOutputStream salida;

    private int maxMovimientoAntes;
    private int secuenciaMovimientoAntes;
    private Map<Integer, Integer> existenciasAntes;

    @BeforeAll
    static void prepararTodo() {
        // String.format usa el locale; se fija es-MX (punto decimal) para que
        // el ticket se compare igual en cualquier computadora del equipo.
        localeOriginal = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("es-MX"));
    }

    @AfterAll
    static void restaurarTodo() {
        Locale.setDefault(localeOriginal);
    }

    @BeforeEach
    void preparar() {
        daoFalso = new VentaDAOFalso();
        controller = new VentaController(daoFalso);
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
    }

    @AfterEach
    void restaurar() throws SQLException {
        System.setOut(salidaOriginal);
        if (existenciasAntes != null) {
            limpiarBase();
        }
    }

    private void capturarSalida() {
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
    }

    private String salida() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    // ===================== calcularTotal =====================

    @Test
    void calcularTotal_sumaPrecioPorCantidadDeCadaProducto() {
        // RF-07
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 2), producto(2, "Pepsi", 10.5, 3));

        assertEquals(51.5, controller.calcularTotal(v), DELTA);
    }

    @Test
    void calcularTotal_sinProductos_devuelveCero() {
        // RF-07: getProductos() devuelve un arreglo vacío si no se asignaron
        assertEquals(0.0, controller.calcularTotal(new VentaModel()), DELTA);
    }

    @Test
    void calcularTotal_ignoraElTotalYElDescuentoGuardadosEnLaVenta() {
        // RF-07: las promociones y el descuento de la venta no se aplican al calcular
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 2));
        v.setTotal(999.0);
        v.setDescuento(50.0);

        assertEquals(20.0, controller.calcularTotal(v), DELTA);
    }

    @Test
    void calcularTotal_cantidadCero_noSumaEseProducto() {
        // RF-07
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 0), producto(2, "Pepsi", 10.5, 1));

        assertEquals(10.5, controller.calcularTotal(v), DELTA);
    }

    @Test
    void calcularTotal_cantidadNegativa_restaDelTotal() {
        // RF-07
        // B-04: no se validan cantidades en 0 o negativas; el total puede quedar negativo
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, -3));

        assertEquals(-30.0, controller.calcularTotal(v), DELTA);
    }

    @Test
    void calcularTotal_noLlamaAlDao() {
        // RF-07
        controller.calcularTotal(venta(producto(1, "Coca Cola", 10.0, 1)));

        assertTrue(daoFalso.llamadas.isEmpty());
    }

    // ===================== aplicarDescuento =====================

    @Test
    void aplicarDescuento_restaElPorcentajeAlTotal() {
        // RF-07
        assertEquals(90.0, controller.aplicarDescuento(100.0, 10.0), DELTA);
        assertEquals(95.0, controller.aplicarDescuento(100.0, 5.0), DELTA);
    }

    @Test
    void aplicarDescuento_ceroPorCiento_dejaElTotalIgual() {
        // RF-07
        assertEquals(100.0, controller.aplicarDescuento(100.0, 0.0), DELTA);
    }

    @Test
    void aplicarDescuento_cienPorCiento_dejaElTotalEnCero() {
        // RF-07
        assertEquals(0.0, controller.aplicarDescuento(100.0, 100.0), DELTA);
    }

    @Test
    void aplicarDescuento_masDeCienPorCiento_dejaElTotalNegativo() {
        // RF-07
        // B-06: el porcentaje no se limita a 0-100; 150 % da un total negativo
        assertEquals(-50.0, controller.aplicarDescuento(100.0, 150.0), DELTA);
    }

    @Test
    void aplicarDescuento_porcentajeNegativo_aumentaElTotal() {
        // RF-07
        // B-06: un descuento de -10 % sube el total
        assertEquals(110.0, controller.aplicarDescuento(100.0, -10.0), DELTA);
    }

    @Test
    void aplicarDescuento_totalCero_devuelveCero() {
        // RF-07
        assertEquals(0.0, controller.aplicarDescuento(0.0, 20.0), DELTA);
    }

    // ===================== generarTicket =====================

    @Test
    void generarTicket_pagoEnEfectivo_imprimeTicketCompleto() {
        // RF-08
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 2), producto(2, "Pepsi", 10.5, 1));
        v.setCliente("Juan Pérez");
        v.setMetodoPago("Efectivo");
        v.setMontoPagado(50.0);
        v.setCambio(19.5);

        capturarSalida();
        controller.generarTicket(v);

        String n = System.lineSeparator();
        assertEquals("===== TICKET =====" + n
                + "Cliente: Juan Pérez" + n
                + "Coca Cola x2 = $20.00" + n
                + "Pepsi x1 = $10.50" + n
                + "Total: $30.50" + n
                + "Método de pago: Efectivo" + n
                + "Monto recibido: $50.00" + n
                + "Cambio: $19.50" + n
                + "==================" + n, salida());
    }

    @Test
    void generarTicket_pagoConTarjeta_imprimeElNumeroDeTarjeta() {
        // RF-08
        // B-09: el ticket muestra el número de tarjeta completo, sin enmascarar
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 1));
        v.setCliente("");
        v.setMetodoPago("Tarjeta");
        v.setNumeroTarjeta("4111111111111111");

        capturarSalida();
        controller.generarTicket(v);

        String n = System.lineSeparator();
        assertEquals("===== TICKET =====" + n
                + "Cliente: " + n
                + "Coca Cola x1 = $10.00" + n
                + "Total: $10.00" + n
                + "Método de pago: Tarjeta" + n
                + "Tarjeta: 4111111111111111" + n
                + "==================" + n, salida());
    }

    @Test
    void generarTicket_metodoEnMinusculas_seReconoce() {
        // RF-08: la comparación ignora mayúsculas y minúsculas
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 1));
        v.setMetodoPago("efectivo");
        v.setMontoPagado(10.0);

        capturarSalida();
        controller.generarTicket(v);

        assertTrue(salida().contains("Método de pago: efectivo"));
        assertTrue(salida().contains("Cambio: $0.00"));
    }

    @Test
    void generarTicket_sinMetodoDePago_omiteLasLineasDePago() {
        // RF-08
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 1));
        v.setCliente("Ana");

        capturarSalida();
        controller.generarTicket(v);

        assertFalse(salida().contains("Método de pago"));
        assertTrue(salida().contains("Total: $10.00"));
    }

    @Test
    void generarTicket_metodoDesconocido_soloImprimeElNombreDelMetodo() {
        // RF-08
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 1));
        v.setMetodoPago("Vales");

        capturarSalida();
        controller.generarTicket(v);

        assertTrue(salida().contains("Método de pago: Vales"));
        assertFalse(salida().contains("Monto recibido"));
        assertFalse(salida().contains("Tarjeta:"));
    }

    @Test
    void generarTicket_clienteNull_imprimeLaPalabraNull() {
        // RF-08: no se sustituye el null por texto vacío
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 1));

        capturarSalida();
        controller.generarTicket(v);

        assertTrue(salida().contains("Cliente: null"));
    }

    @Test
    void generarTicket_sinProductos_imprimeTotalCero() {
        // RF-08
        capturarSalida();
        controller.generarTicket(new VentaModel());

        String n = System.lineSeparator();
        assertEquals("===== TICKET =====" + n
                + "Cliente: null" + n
                + "Total: $0.00" + n
                + "==================" + n, salida());
    }

    @Test
    void generarTicket_recalculaElTotalEnVezDeUsarElGuardado() {
        // RF-08: el ticket usa calcularTotal, no v.getTotal()
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 3));
        v.setTotal(1.0);

        capturarSalida();
        controller.generarTicket(v);

        assertTrue(salida().contains("Total: $30.00"));
    }

    // ===================== registrarVenta (usa la base de pruebas) =====================

    @Test
    void registrarVenta_delegaEnRegistrarVentaDelDao() throws SQLException {
        // RF-06: llama a registrarVenta, no al alias registrar
        tomarFotoDeLaBase();
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 1));

        controller.registrarVenta(v);

        assertEquals(List.of("registrarVenta"), daoFalso.llamadas);
        assertSame(v, daoFalso.ventasRegistradas.get(0));
    }

    @Test
    void registrarVenta_registraUnaSalidaPorProductoYDescuentaExistencias() throws SQLException {
        // RF-09: un movimiento SALIDA por producto y se descuenta el inventario
        tomarFotoDeLaBase();
        int cocaAntes = existencias(1);
        int pepsiAntes = existencias(2);
        VentaModel v = venta(producto(1, "Coca Cola", 10.0, 2), producto(2, "Pepsi", 10.5, 3));

        controller.registrarVenta(v);

        List<String[]> movimientos = movimientosNuevos();
        assertEquals(2, movimientos.size());
        assertEquals("1|SALIDA|2", String.join("|", movimientos.get(0)));
        assertEquals("2|SALIDA|3", String.join("|", movimientos.get(1)));
        assertEquals(cocaAntes - 2, existencias(1));
        assertEquals(pepsiAntes - 3, existencias(2));
    }

    @Test
    void registrarVenta_cantidadMayorAlStock_dejaExistenciaEnCeroYGuardaLaCantidadPedida() throws SQLException {
        // RF-09: la existencia no baja de 0 y el movimiento conserva la cantidad pedida
        tomarFotoDeLaBase();
        int pedida = existencias(5) + 100;

        controller.registrarVenta(venta(producto(5, "Doritos pizzerola", 20.0, pedida)));

        assertEquals(0, existencias(5));
        assertEquals("5|SALIDA|" + pedida, String.join("|", movimientosNuevos().get(0)));
    }

    @Test
    void registrarVenta_cantidadNegativa_seRegistraComoSalidaYTambienDescuenta() throws SQLException {
        // RF-09
        // B-04: la SALIDA usa Math.abs, así que vender -3 descuenta 3 del inventario
        tomarFotoDeLaBase();
        int antes = existencias(4);

        controller.registrarVenta(venta(producto(4, "Sabritas", 10.0, -3)));

        assertEquals("4|SALIDA|-3", String.join("|", movimientosNuevos().get(0)));
        assertEquals(antes - 3, existencias(4));
    }

    @Test
    void registrarVenta_sinProductos_noRegistraMovimientos() throws SQLException {
        // RF-09
        tomarFotoDeLaBase();

        controller.registrarVenta(new VentaModel());

        assertEquals(List.of("registrarVenta"), daoFalso.llamadas);
        assertTrue(movimientosNuevos().isEmpty());
    }

    // ===================== Ayudas =====================

    private static VentaModel venta(ProductoModel... productos) {
        VentaModel v = new VentaModel();
        v.setProductos(productos);
        return v;
    }

    private static ProductoModel producto(int id, String nombre, double precio, int cantidad) {
        ProductoModel p = new ProductoModel();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setCantidad(cantidad);
        return p;
    }

    private static Connection conexion() {
        return ConexionDB.getInstancia().getConexion();
    }

    /** Guarda el último movimiento y las existencias para dejar la base igual al terminar. */
    private void tomarFotoDeLaBase() throws SQLException {
        try (Statement st = conexion().createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) FROM movimiento")) {
                rs.next();
                maxMovimientoAntes = rs.getInt(1);
            }
            try (ResultSet rs = st.executeQuery("SELECT seq FROM sqlite_sequence WHERE name = 'movimiento'")) {
                secuenciaMovimientoAntes = rs.next() ? rs.getInt(1) : 0;
            }
            existenciasAntes = new HashMap<>();
            try (ResultSet rs = st.executeQuery("SELECT id, cantidad FROM producto")) {
                while (rs.next()) {
                    existenciasAntes.put(rs.getInt(1), rs.getInt(2));
                }
            }
        }
    }

    private void limpiarBase() throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement("DELETE FROM movimiento WHERE id > ?")) {
            ps.setInt(1, maxMovimientoAntes);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conexion().prepareStatement("UPDATE producto SET cantidad = ? WHERE id = ?")) {
            for (Map.Entry<Integer, Integer> e : existenciasAntes.entrySet()) {
                ps.setInt(1, e.getValue());
                ps.setInt(2, e.getKey());
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = conexion().prepareStatement(
                "UPDATE sqlite_sequence SET seq = ? WHERE name = 'movimiento'")) {
            ps.setInt(1, secuenciaMovimientoAntes);
            ps.executeUpdate();
        }
        existenciasAntes = null;
    }

    private int existencias(int productoId) throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement("SELECT cantidad FROM producto WHERE id = ?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Movimientos creados por la prueba: producto_id, tipo y cantidad. */
    private List<String[]> movimientosNuevos() throws SQLException {
        List<String[]> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion().prepareStatement(
                "SELECT producto_id, tipo, cantidad FROM movimiento WHERE id > ? ORDER BY id")) {
            ps.setInt(1, maxMovimientoAntes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[] { rs.getString(1), rs.getString(2), rs.getString(3) });
                }
            }
        }
        return lista;
    }

    /** DAO falso: anota qué método se llamó y no toca la base. */
    private static class VentaDAOFalso implements IVentaDAO {
        private final List<String> llamadas = new ArrayList<>();
        private final List<VentaModel> ventasRegistradas = new ArrayList<>();

        @Override
        public void registrar(VentaModel v) {
            llamadas.add("registrar");
        }

        @Override
        public void historial() {
            llamadas.add("historial");
        }

        @Override
        public ReporteModel generarReporte(String periodo) {
            llamadas.add("generarReporte");
            return null;
        }

        @Override
        public void historialPorCliente(int clienteId) {
            llamadas.add("historialPorCliente");
        }

        @Override
        public void registrarVenta(VentaModel v) {
            llamadas.add("registrarVenta");
            ventasRegistradas.add(v);
        }
    }
}

package com.tienda.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.producto.ProductoModel;
import com.tienda.util.ConexionDB;

/**
 * Pruebas de caracterización de {@link VentaDAO} (ISS-28).
 *
 * <p>Trabajan contra la base de pruebas aislada ({@code target/test-run/data/tienda.db}).
 * Cada prueba borra al terminar las ventas y renglones que creó.</p>
 */
class VentaDAOTest {

    private static final double DELTA = 0.0001;
    private static Locale localeOriginal;

    private VentaDAO dao;
    private int maxVentaAntes;
    private int maxItemAntes;
    private int secuenciaVentaAntes;
    private int secuenciaItemAntes;
    private PrintStream salidaOriginal;
    private ByteArrayOutputStream salida;

    @BeforeAll
    static void prepararTodo() {
        // printf usa el locale; es-MX usa punto decimal
        localeOriginal = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("es-MX"));
    }

    @AfterAll
    static void restaurarTodo() {
        Locale.setDefault(localeOriginal);
    }

    @BeforeEach
    void preparar() throws SQLException {
        dao = new VentaDAO();
        maxVentaAntes = maximo("venta");
        maxItemAntes = maximo("venta_item");
        secuenciaVentaAntes = secuencia("venta");
        secuenciaItemAntes = secuencia("venta_item");
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
    }

    @AfterEach
    void limpiar() throws SQLException {
        System.setOut(salidaOriginal);
        Connection c = conexion();
        if (!c.getAutoCommit()) {
            c.rollback();
            c.setAutoCommit(true);
        }
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM venta_item WHERE id > ?")) {
            ps.setInt(1, maxItemAntes);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM venta WHERE id > ?")) {
            ps.setInt(1, maxVentaAntes);
            ps.executeUpdate();
        }
        restaurarSecuencia("venta", secuenciaVentaAntes);
        restaurarSecuencia("venta_item", secuenciaItemAntes);
    }

    private void capturarSalida() {
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
    }

    private String salida() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    // ===================== registrarVenta =====================

    @Test
    void registrarVenta_guardaEncabezadoYRenglonesYAsignaElId() throws Exception {
        // RF-06
        VentaModel v = venta(producto(1, 10.0, 2), producto(2, 10.5, 1));
        v.setMetodoPago("Tarjeta");
        v.setNumeroTarjeta("1234");
        v.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-11-22 10:30:00"));

        dao.registrarVenta(v);

        assertTrue(v.getId() > maxVentaAntes, "Debe asignar el id generado por la base");
        assertEquals("2026-11-22 10:30:00|30.5|null|Tarjeta|1234", filaVenta(v.getId()));
        assertEquals(List.of("1|2|10.0|0.0", "2|1|10.5|0.0"), renglones(v.getId()));
    }

    @Test
    void registrarVenta_recalculaElTotalConLosRenglonesYLoGuardaEnLaVenta() {
        // RF-06, RF-07: el total que trae la venta se ignora y se sobrescribe
        VentaModel v = venta(producto(1, 10.0, 3));
        v.setTotal(999.0);

        dao.registrarVenta(v);

        assertEquals(30.0, v.getTotal(), DELTA);
    }

    @Test
    void registrarVenta_noAplicaElDescuentoDeLaVenta() throws SQLException {
        // RF-07: en los renglones el descuento siempre se guarda en 0
        VentaModel v = venta(producto(1, 10.0, 1));
        v.setDescuento(50.0);

        dao.registrarVenta(v);

        assertEquals(10.0, v.getTotal(), DELTA);
        assertEquals(List.of("1|1|10.0|0.0"), renglones(v.getId()));
    }

    @Test
    void registrarVenta_sinFecha_usaLaFechaActual() throws Exception {
        // RF-06
        long antes = System.currentTimeMillis() / 1000 * 1000;
        VentaModel v = venta(producto(1, 10.0, 1));

        dao.registrarVenta(v);

        String fecha = filaVenta(v.getId()).split("\\|")[0];
        long guardada = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fecha).getTime();
        assertTrue(guardada >= antes && guardada <= System.currentTimeMillis(),
                "La fecha guardada debe ser la del momento de registrar");
        assertNull(v.getFecha(), "La fecha del objeto no se actualiza");
    }

    @Test
    void registrarVenta_conClienteId_loGuarda() throws SQLException {
        // RF-06
        VentaModel v = venta(producto(1, 10.0, 1));
        v.setClienteId(2);
        v.setMetodoPago("Efectivo");

        dao.registrarVenta(v);

        assertEquals("2|Efectivo|null", filaVenta(v.getId()).split("\\|", 3)[2]);
    }

    @Test
    void registrarVenta_sinProductos_guardaVentaConTotalCeroYSinRenglones() throws SQLException {
        // RF-06: el DAO no valida que haya productos (la consola sí lo hace antes)
        VentaModel v = new VentaModel();

        dao.registrarVenta(v);

        assertTrue(v.getId() > maxVentaAntes, "Debe asignar el id generado por la base");
        assertEquals(0.0, v.getTotal(), DELTA);
        assertTrue(renglones(v.getId()).isEmpty());
    }

    @Test
    void registrarVenta_productoQueNoExiste_seGuardaIgual() throws SQLException {
        // RF-06
        // B-01: la conexión no activa PRAGMA foreign_keys; el renglón apunta a un producto inexistente
        VentaModel v = venta(producto(99999, 5.0, 1));

        dao.registrarVenta(v);

        assertEquals(List.of("99999|1|5.0|0.0"), renglones(v.getId()));
    }

    @Test
    void registrarVenta_noModificaLasExistencias() throws SQLException {
        // RF-09: el inventario lo descuenta VentaController, no el DAO
        int antes = existencias(1);

        dao.registrarVenta(venta(producto(1, 10.0, 2)));

        assertEquals(antes, existencias(1));
    }

    @Test
    void registrarVenta_dejaLaConexionEnAutoCommit() throws SQLException {
        // RF-06
        dao.registrarVenta(venta(producto(1, 10.0, 1)));

        assertTrue(conexion().getAutoCommit());
    }

    // ===================== registrar =====================

    @Test
    void registrar_haceLoMismoQueRegistrarVenta() throws SQLException {
        // RF-06: registrar solo delega en registrarVenta (LLA-02)
        VentaModel v = venta(producto(2, 10.5, 2));

        dao.registrar(v);

        assertTrue(v.getId() > maxVentaAntes, "Debe asignar el id generado por la base");
        assertEquals(21.0, v.getTotal(), DELTA);
        assertEquals(List.of("2|2|10.5|0.0"), renglones(v.getId()));
    }

    // ===================== listarTodos =====================

    @Test
    void listarTodos_devuelveTodasLasVentasDeLaMasNuevaALaMasVieja() throws SQLException {
        // RF-10
        List<VentaModel> lista = dao.listarTodos();

        assertEquals(contar("venta"), lista.size());
        for (int i = 1; i < lista.size(); i++) {
            assertTrue(lista.get(i - 1).getId() > lista.get(i).getId());
        }
    }

    @Test
    void listarTodos_cargaIdFechaTotalYNombreDelCliente() throws Exception {
        // RF-10: no carga los productos ni el método de pago
        VentaModel v = venta(producto(1, 10.0, 1));
        v.setClienteId(1);
        v.setMetodoPago("Efectivo");
        v.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-11-22 09:15:00"));
        dao.registrarVenta(v);

        VentaModel leida = dao.listarTodos().get(0);

        assertEquals(v.getId(), leida.getId());
        assertEquals(v.getFecha(), leida.getFecha());
        assertEquals(10.0, leida.getTotal(), DELTA);
        assertEquals(nombreCliente(1), leida.getCliente());
        assertEquals(0, leida.getProductos().length);
        assertNull(leida.getMetodoPago());
        assertNull(leida.getClienteId());
    }

    @Test
    void listarTodos_ventaSinCliente_ponePalabraVacia() {
        // RF-10
        dao.registrarVenta(venta(producto(1, 10.0, 1)));

        assertEquals("", dao.listarTodos().get(0).getCliente());
    }

    @Test
    void listarTodos_fechaNull_ponLaFechaActual() throws SQLException {
        // RF-10: el NullPointerException de parse se atrapa y se usa new Date() (LLA-05)
        insertarVentaConFechaNull();
        long antes = System.currentTimeMillis();

        Date fecha = dao.listarTodos().get(0).getFecha();

        assertNotNull(fecha);
        assertTrue(fecha.getTime() >= antes);
    }

    @Test
    void listarTodos_sinVentas_devuelveListaVacia() throws SQLException {
        // RF-10: se borran las ventas dentro de una transacción que se deshace al final
        conexion().setAutoCommit(false);
        try (Statement st = conexion().createStatement()) {
            st.executeUpdate("DELETE FROM venta");
        }

        assertTrue(dao.listarTodos().isEmpty());
    }

    // ===================== historial =====================

    @Test
    void historial_imprimeUnaLineaPorVentaConSuFormato() throws Exception {
        // RF-10
        VentaModel v = venta(producto(1, 10.0, 2));
        v.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-11-22 08:00:00"));
        dao.registrarVenta(v);

        capturarSalida();
        dao.historial();

        String[] lineas = salida().split(System.lineSeparator());
        assertEquals(contar("venta"), lineas.length);
        assertEquals(String.format("Venta ID:%d Fecha:%s Total:20.00 Cliente:", v.getId(), v.getFecha()),
                lineas[0]);
    }

    @Test
    void historial_sinVentas_imprimeAviso() throws SQLException {
        // RF-10
        conexion().setAutoCommit(false);
        try (Statement st = conexion().createStatement()) {
            st.executeUpdate("DELETE FROM venta");
        }

        capturarSalida();
        dao.historial();

        assertEquals("No hay ventas registradas." + System.lineSeparator(), salida());
    }

    // ===================== historialPorCliente =====================

    @Test
    void historialPorCliente_imprimeSoloLasVentasDeEseCliente() throws Exception {
        // RF-13
        VentaModel v = venta(producto(1, 10.0, 1));
        v.setClienteId(2);
        v.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2099-01-01 12:00:00"));
        dao.registrarVenta(v);

        capturarSalida();
        dao.historialPorCliente(2);

        String[] lineas = salida().split(System.lineSeparator());
        assertEquals(contarDeCliente(2), lineas.length);
        assertEquals(String.format("ID:%d Fecha:2099-01-01 12:00:00 Total:10.00 Cliente:%s",
                v.getId(), nombreCliente(2)), lineas[0]);
    }

    @Test
    void historialPorCliente_clienteSinVentasOQueNoExiste_noImprimeNada() {
        // RF-13: no hay mensaje de "sin ventas"
        capturarSalida();
        dao.historialPorCliente(99999);
        dao.historialPorCliente(0);
        dao.historialPorCliente(-1);

        assertEquals("", salida());
    }

    // ===================== generarReporte =====================

    @Test
    void generarReporte_siempreDevuelveNull() {
        // RF-16: el método no está implementado en VentaDAO
        assertNull(dao.generarReporte("diario"));
        assertNull(dao.generarReporte("mensual"));
        assertNull(dao.generarReporte(""));
        assertNull(dao.generarReporte(null));
    }

    // ===================== Ayudas =====================

    private static VentaModel venta(ProductoModel... productos) {
        VentaModel v = new VentaModel();
        v.setProductos(productos);
        return v;
    }

    private static ProductoModel producto(int id, double precio, int cantidad) {
        ProductoModel p = new ProductoModel();
        p.setId(id);
        p.setPrecio(precio);
        p.setCantidad(cantidad);
        return p;
    }

    private static Connection conexion() {
        return ConexionDB.getInstancia().getConexion();
    }

    private static int maximo(String tabla) throws SQLException {
        try (Statement st = conexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Valor de AUTOINCREMENT guardado en sqlite_sequence (0 si la tabla no tiene). */
    private static int secuencia(String tabla) throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement("SELECT seq FROM sqlite_sequence WHERE name = ?")) {
            ps.setString(1, tabla);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static void restaurarSecuencia(String tabla, int valor) throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement("UPDATE sqlite_sequence SET seq = ? WHERE name = ?")) {
            ps.setInt(1, valor);
            ps.setString(2, tabla);
            ps.executeUpdate();
        }
    }

    private static int contar(String tabla) throws SQLException {
        try (Statement st = conexion().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static int contarDeCliente(int clienteId) throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement("SELECT COUNT(*) FROM venta WHERE cliente_id = ?")) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static String nombreCliente(int id) throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement("SELECT nombre FROM cliente WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    private static int existencias(int productoId) throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement("SELECT cantidad FROM producto WHERE id = ?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** fecha|total|cliente_id|metodo_pago|numero_tarjeta de una venta. */
    private static String filaVenta(int id) throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement(
                "SELECT fecha, total, cliente_id, metodo_pago, numero_tarjeta FROM venta WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1) + "|" + rs.getDouble(2) + "|" + rs.getObject(3) + "|"
                        + rs.getString(4) + "|" + rs.getString(5);
            }
        }
    }

    /** producto_id|cantidad|precio_unitario|descuento de cada renglón, en orden. */
    private static List<String> renglones(int ventaId) throws SQLException {
        List<String> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion().prepareStatement(
                "SELECT producto_id, cantidad, precio_unitario, descuento FROM venta_item WHERE venta_id = ? ORDER BY id")) {
            ps.setInt(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getInt(1) + "|" + rs.getInt(2) + "|" + rs.getDouble(3) + "|" + rs.getDouble(4));
                }
            }
        }
        return lista;
    }

    private static void insertarVentaConFechaNull() throws SQLException {
        try (Statement st = conexion().createStatement()) {
            st.executeUpdate("INSERT INTO venta (fecha, total) VALUES (NULL, 1.0)");
        }
    }
}

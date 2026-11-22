package com.tienda.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.producto.ProductoDAO;
import com.tienda.producto.ProductoModel;
import com.tienda.util.ConexionDB;

/**
 * Pruebas de caracterización de {@link MovimientoDAO} (ISS-30).
 *
 * <p>Corren contra la copia de la base en {@code target/test-run/data/tienda.db}.
 * Cada prueba crea un producto {@code ISS30...} con existencia inicial de 10
 * y borra el producto y sus movimientos al terminar.</p>
 */
class MovimientoDAOTest {

    private static final int EXISTENCIA_INICIAL = 10;
    private static final int PRODUCTO_INEXISTENTE = 999999;

    private Connection conn;
    private MovimientoDAO dao;
    private int productoId;

    @BeforeEach
    void preparar() throws SQLException {
        conn = ConexionDB.getInstancia().getConexion();
        limpiar();
        dao = new MovimientoDAO();
        ProductoModel p = new ProductoModel();
        p.setNombre("ISS30 Producto movimientos");
        p.setCodigo("ISS30-MOV-001");
        p.setPrecio(10.0);
        p.setCantidad(EXISTENCIA_INICIAL);
        new ProductoDAO().agregar(p);
        productoId = p.getId();
        assertTrue(productoId > 0, "El producto de prueba debe quedar guardado");
    }

    @AfterEach
    void limpiarDatos() throws SQLException {
        limpiar();
    }

    // ===================== registrar: ajuste de existencias =====================

    @Test
    void registrar_entrada_sumaALaExistencia() throws SQLException {
        // RF-02
        dao.registrar(movimiento(productoId, "ENTRADA", 5));

        assertEquals(15, existencia(productoId));
    }

    @Test
    void registrar_salida_restaALaExistencia() throws SQLException {
        // RF-02
        dao.registrar(movimiento(productoId, "SALIDA", 4));

        assertEquals(6, existencia(productoId));
    }

    @Test
    void registrar_salidaMayorQueLaExistencia_dejaExistenciaEnCeroYGuardaCantidadPedida() throws SQLException {
        // RF-02, RF-09: la existencia no baja de cero, el movimiento conserva la cantidad pedida
        dao.registrar(movimiento(productoId, "SALIDA", 25));

        assertEquals(0, existencia(productoId));
        List<MovimientoInventarioModel> movs = dao.listarPorProducto(productoId);
        assertEquals(1, movs.size());
        assertEquals(25, movs.get(0).getCantidad());
    }

    @Test
    void registrar_salidaIgualALaExistencia_dejaExistenciaEnCero() throws SQLException {
        // RF-02
        dao.registrar(movimiento(productoId, "SALIDA", EXISTENCIA_INICIAL));

        assertEquals(0, existencia(productoId));
    }

    @Test
    void registrar_ajustePositivo_sumaLaCantidad() throws SQLException {
        // RF-02: AJUSTE usa la cantidad tal cual
        dao.registrar(movimiento(productoId, "AJUSTE", 3));

        assertEquals(13, existencia(productoId));
    }

    @Test
    void registrar_ajusteNegativo_restaLaCantidad() throws SQLException {
        // RF-02: AJUSTE permite negativos
        dao.registrar(movimiento(productoId, "AJUSTE", -4));

        assertEquals(6, existencia(productoId));
    }

    @Test
    void registrar_ajusteNegativoMayorQueLaExistencia_dejaExistenciaEnCero() throws SQLException {
        // RF-02
        dao.registrar(movimiento(productoId, "AJUSTE", -50));

        assertEquals(0, existencia(productoId));
    }

    @Test
    void registrar_entradaConCantidadNegativa_sumaElValorAbsoluto() throws SQLException {
        // RF-02
        // B-02: ENTRADA y SALIDA usan Math.abs, así que una ENTRADA de -5 suma 5
        // y una SALIDA de -5 resta 5; no se rechazan cantidades negativas.
        dao.registrar(movimiento(productoId, "ENTRADA", -5));

        assertEquals(15, existencia(productoId));
    }

    @Test
    void registrar_salidaConCantidadNegativa_restaElValorAbsoluto() throws SQLException {
        // RF-02
        // B-02: ver registrar_entradaConCantidadNegativa_sumaElValorAbsoluto
        dao.registrar(movimiento(productoId, "SALIDA", -5));

        assertEquals(5, existencia(productoId));
        assertEquals(-5, dao.listarPorProducto(productoId).get(0).getCantidad());
    }

    @Test
    void registrar_cantidadCero_guardaMovimientoSinCambiarExistencia() throws SQLException {
        // RF-02
        dao.registrar(movimiento(productoId, "ENTRADA", 0));

        assertEquals(EXISTENCIA_INICIAL, existencia(productoId));
        assertEquals(1, dao.listarPorProducto(productoId).size());
    }

    @Test
    void registrar_tipoEnMinusculas_seReconoce() throws SQLException {
        // RF-02: la comparación del tipo no distingue mayúsculas
        dao.registrar(movimiento(productoId, "salida", 2));

        assertEquals(8, existencia(productoId));
        assertEquals("salida", dao.listarPorProducto(productoId).get(0).getTipo());
    }

    @Test
    void registrar_tipoDesconocido_seTrataComoAjuste() throws SQLException {
        // RF-02: cualquier tipo que no sea ENTRADA ni SALIDA usa la cantidad tal cual
        dao.registrar(movimiento(productoId, "OTRO", -3));

        assertEquals(7, existencia(productoId));
    }

    @Test
    void registrar_guardaFechaTipoYCantidadSinNota() throws SQLException {
        // RF-02, RF-05
        dao.registrar(movimiento(productoId, "ENTRADA", 2));

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT fecha, tipo, cantidad, nota FROM movimiento WHERE producto_id = ?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("2026-11-22T10:00", rs.getString("fecha"));
                assertEquals("ENTRADA", rs.getString("tipo"));
                assertEquals(2, rs.getInt("cantidad"));
                assertNull(rs.getString("nota"));
            }
        }
    }

    @Test
    void registrar_productoInexistente_guardaMovimientoSinProducto() throws SQLException {
        // RF-02
        // B-03: no se valida que el producto exista; como la conexión no activa
        // PRAGMA foreign_keys, el movimiento se guarda apuntando a un producto que no existe.
        dao.registrar(movimiento(PRODUCTO_INEXISTENTE, "ENTRADA", 5));

        assertEquals(1, dao.listarPorProducto(PRODUCTO_INEXISTENTE).size());
    }

    @Test
    void registrar_sinTipo_noGuardaNadaNiCambiaExistencia() throws SQLException {
        // RF-02: tipo es NOT NULL; el error solo se imprime
        dao.registrar(movimiento(productoId, null, 5));

        assertEquals(EXISTENCIA_INICIAL, existencia(productoId));
        assertTrue(dao.listarPorProducto(productoId).isEmpty());
    }

    // ===================== listarPorProducto =====================

    @Test
    void listarPorProducto_devuelveMovimientosDelProductoOrdenadosPorFechaDescendente() {
        // RF-05
        dao.registrar(movimiento(productoId, "ENTRADA", 1, "2026-11-20T09:00"));
        dao.registrar(movimiento(productoId, "SALIDA", 2, "2026-11-22T09:00"));
        dao.registrar(movimiento(productoId, "AJUSTE", 3, "2026-11-21T09:00"));

        List<MovimientoInventarioModel> movs = dao.listarPorProducto(productoId);

        assertEquals(3, movs.size());
        assertEquals("2026-11-22T09:00", movs.get(0).getFecha());
        assertEquals("SALIDA", movs.get(0).getTipo());
        assertEquals(2, movs.get(0).getCantidad());
        assertEquals("2026-11-21T09:00", movs.get(1).getFecha());
        assertEquals("2026-11-20T09:00", movs.get(2).getFecha());
        movs.forEach(m -> {
            assertEquals(productoId, m.getProductoId());
            assertTrue(m.getId() > 0);
        });
    }

    @Test
    void listarPorProducto_sinMovimientos_devuelveListaVacia() {
        // RF-05
        assertTrue(dao.listarPorProducto(productoId).isEmpty());
    }

    @Test
    void listarPorProducto_idCeroYNegativo_devuelveListaVacia() {
        // RF-05
        assertTrue(dao.listarPorProducto(0).isEmpty());
        assertTrue(dao.listarPorProducto(-1).isEmpty());
    }

    // ===================== Utilidades =====================

    private static MovimientoInventarioModel movimiento(int productoId, String tipo, int cantidad) {
        return movimiento(productoId, tipo, cantidad, "2026-11-22T10:00");
    }

    private static MovimientoInventarioModel movimiento(int productoId, String tipo, int cantidad, String fecha) {
        MovimientoInventarioModel m = new MovimientoInventarioModel();
        m.setProductoId(productoId);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setFecha(fecha);
        return m;
    }

    private int existencia(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT cantidad FROM producto WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "El producto de prueba debe existir");
                return rs.getInt(1);
            }
        }
    }

    private void limpiar() throws SQLException {
        try (PreparedStatement mov = conn.prepareStatement(
                "DELETE FROM movimiento WHERE producto_id = " + PRODUCTO_INEXISTENTE + " OR producto_id IN "
                        + "(SELECT id FROM producto WHERE nombre LIKE 'ISS30%' OR codigo LIKE 'ISS30-%')");
             PreparedStatement prod = conn.prepareStatement(
                     "DELETE FROM producto WHERE nombre LIKE 'ISS30%' OR codigo LIKE 'ISS30-%'")) {
            mov.executeUpdate();
            prod.executeUpdate();
        }
    }
}

package com.tienda.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Prueba de caracterización de {@link MovimientoDAO} (ISS-30).
 *
 * <p>Corre contra la copia de la base en {@code target/test-run/data/tienda.db}.
 * Crea un producto {@code ISS30...} con existencia inicial de 10
 * y borra el producto y sus movimientos al terminar.</p>
 */
class MovimientoDAOTest {

    private static final int EXISTENCIA_INICIAL = 10;

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

    @Test
    void registrar_salidaMayorQueLaExistencia_dejaExistenciaEnCeroYGuardaCantidadPedida() throws SQLException {
        // RF-02, RF-09: la existencia no baja de cero, el movimiento conserva la cantidad pedida
        MovimientoInventarioModel m = new MovimientoInventarioModel();
        m.setProductoId(productoId);
        m.setTipo("SALIDA");
        m.setCantidad(25);
        m.setFecha("2026-11-22T10:00");

        dao.registrar(m);

        assertEquals(0, existencia(productoId));
        List<MovimientoInventarioModel> movs = dao.listarPorProducto(productoId);
        assertEquals(1, movs.size());
        assertEquals(25, movs.get(0).getCantidad());
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
                "DELETE FROM movimiento WHERE producto_id IN "
                        + "(SELECT id FROM producto WHERE nombre LIKE 'ISS30%' OR codigo LIKE 'ISS30-%')");
             PreparedStatement prod = conn.prepareStatement(
                     "DELETE FROM producto WHERE nombre LIKE 'ISS30%' OR codigo LIKE 'ISS30-%'")) {
            mov.executeUpdate();
            prod.executeUpdate();
        }
    }
}

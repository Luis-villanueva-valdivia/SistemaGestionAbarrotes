package com.tienda.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.producto.ProductoModel;
import com.tienda.util.ConexionDB;

/**
 * Prueba de caracterización de {@link VentaDAO} (ISS-28).
 *
 * <p>Trabaja contra la base de pruebas aislada ({@code target/test-run/data/tienda.db}).
 * Al terminar borra la venta y los renglones que creó y regresa los contadores
 * de {@code sqlite_sequence} a como estaban.</p>
 */
class VentaDAOTest {

    private static final double DELTA = 0.0001;

    private int maxVentaAntes;
    private int maxItemAntes;
    private int secuenciaVentaAntes;
    private int secuenciaItemAntes;

    @BeforeEach
    void tomarFotoDeLaBase() throws SQLException {
        maxVentaAntes = maximo("venta");
        maxItemAntes = maximo("venta_item");
        secuenciaVentaAntes = secuencia("venta");
        secuenciaItemAntes = secuencia("venta_item");
    }

    @AfterEach
    void limpiarBase() throws SQLException {
        try (PreparedStatement ps = conexion().prepareStatement("DELETE FROM venta_item WHERE id > ?")) {
            ps.setInt(1, maxItemAntes);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conexion().prepareStatement("DELETE FROM venta WHERE id > ?")) {
            ps.setInt(1, maxVentaAntes);
            ps.executeUpdate();
        }
        restaurarSecuencia("venta", secuenciaVentaAntes);
        restaurarSecuencia("venta_item", secuenciaItemAntes);
    }

    @Test
    void registrarVenta_guardaVentaYRenglonesRecalculandoElTotal() throws Exception {
        // RF-06: se guarda la venta con su método de pago y un renglón por producto;
        // el total se recalcula con los renglones y se asigna el id generado
        VentaModel venta = new VentaModel();
        venta.setProductos(new ProductoModel[] { producto(1, 10.0, 2), producto(2, 10.5, 1) });
        venta.setTotal(999.0);
        venta.setMetodoPago("Tarjeta");
        venta.setNumeroTarjeta("1234");
        venta.setFecha(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-11-22 10:30:00"));

        new VentaDAO().registrarVenta(venta);

        assertTrue(venta.getId() > maxVentaAntes, "Debe asignar el id generado por la base");
        assertEquals(30.5, venta.getTotal(), DELTA);
        assertEquals("2026-11-22 10:30:00|30.5|null|Tarjeta|1234", filaVenta(venta.getId()));
        assertEquals(List.of("1|2|10.0|0.0", "2|1|10.5|0.0"), renglones(venta.getId()));
    }

    // ===================== Ayudas =====================

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
}

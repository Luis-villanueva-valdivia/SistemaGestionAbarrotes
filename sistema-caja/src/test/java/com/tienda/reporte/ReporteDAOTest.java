package com.tienda.reporte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.util.ConexionDB;

/**
 * Pruebas de caracterización de {@link ReporteDAO} contra la base de pruebas
 * (ISS-22). Registran lo que el DAO hace hoy, aunque esté mal.
 *
 * <p>Antes de cada prueba se insertan estos datos, con una fecha de 1999 que no
 * existe en los datos del sistema, y al terminar se borran:</p>
 * <ul>
 *   <li>Un producto con ventas: 3 piezas a $10 y 1 pieza a $15 (4 piezas, $45).</li>
 *   <li>Un producto sin ventas.</li>
 *   <li>Dos ventas el 1999-03-10: una de $30 y otra de $15 (total del día: $45).</li>
 * </ul>
 */
class ReporteDAOTest {

    private static final double DELTA = 0.0001;
    private static final String DIA = "1999-03-10";
    private static final String NOMBRE_CON_VENTAS = "ProdPruebaReporte31A";
    private static final int ID_INEXISTENTE = 999_999;

    private final Connection conn = ConexionDB.getInstancia().getConexion();
    private final ReporteDAO dao = new ReporteDAO();

    private int idProductoConVentas;
    private int idProductoSinVentas;
    private int idVenta1;
    private int idVenta2;

    @BeforeEach
    void insertarDatosDePrueba() throws SQLException {
        idProductoConVentas = insertarProducto(NOMBRE_CON_VENTAS, "REP31-A");
        idProductoSinVentas = insertarProducto("ProdPruebaReporte31C", "REP31-C");

        idVenta1 = insertarVenta(DIA + " 10:00:00", 30.0);
        insertarRenglon(idVenta1, idProductoConVentas, 3, 10.0);

        idVenta2 = insertarVenta(DIA + " 18:00:00", 15.0);
        insertarRenglon(idVenta2, idProductoConVentas, 1, 15.0);
    }

    @AfterEach
    void borrarDatosDePrueba() throws SQLException {
        ejecutar("DELETE FROM venta_item WHERE venta_id IN (?, ?)", idVenta1, idVenta2);
        ejecutar("DELETE FROM venta WHERE id IN (?, ?)", idVenta1, idVenta2);
        ejecutar("DELETE FROM producto WHERE id IN (?, ?)", idProductoConVentas, idProductoSinVentas);
    }

    @Test
    void reporteVentas_agrupaPorDia_tipoVacioUsaDiario_tipoNuloLanzaExcepcion() {
        // RF-16: reportes de ventas por periodo

        // Caso normal: las dos ventas del mismo día se juntan en un solo periodo
        ReporteModel dia = buscarPeriodo(dao.reporteVentas("diario"), DIA);
        assertEquals(2, dia.getCantidadVentas());
        assertEquals(45.0, dia.getTotalVentas(), DELTA);
        assertEquals("dia", dia.getCategoria());

        // Caso límite: un tipo vacío (o que no reconoce) usa el reporte diario
        ReporteModel vacio = buscarPeriodo(dao.reporteVentas(""), DIA);
        assertEquals("dia", vacio.getCategoria());
        assertEquals(2, vacio.getCantidadVentas());

        // Caso de error. B-10 (#50): no se valida el tipo; tipo.toLowerCase() falla con null
        assertThrows(NullPointerException.class, () -> dao.reporteVentas(null));
    }

    @Test
    void productoVendidoPorId_sumaSusVentas_sinVentasOIdInexistenteDevuelveNull() {
        // RF-18: análisis de ventas por producto

        // Caso normal: suma las piezas y el importe de todas las ventas del producto
        ReporteModel r = dao.productoVendidoPorId(idProductoConVentas);
        assertNotNull(r);
        assertEquals(4, r.getCantidadVentas());
        assertEquals(45.0, r.getTotalVentas(), DELTA);
        assertEquals(NOMBRE_CON_VENTAS, r.getPeriodo()); // el nombre se guarda en "periodo" (MOV-06)

        // Caso límite: el producto existe pero nunca se ha vendido
        assertNull(dao.productoVendidoPorId(idProductoSinVentas));

        // Caso de error: un id que no existe
        assertNull(dao.productoVendidoPorId(ID_INEXISTENTE));
    }

    // ---------- auxiliares ----------

    private static ReporteModel buscarPeriodo(List<ReporteModel> lista, String periodo) {
        return lista.stream()
                .filter(r -> periodo.equals(r.getPeriodo()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el periodo " + periodo));
    }

    private int insertarProducto(String nombre, String codigo) throws SQLException {
        String sql = "INSERT INTO producto (nombre, precio, cantidad, categoria, codigo) "
                + "VALUES (?, 10.0, 5, 'CatPruebaReporte31', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, codigo);
            ps.executeUpdate();
            return idGenerado(ps);
        }
    }

    private int insertarVenta(String fecha, double total) throws SQLException {
        String sql = "INSERT INTO venta (fecha, total, metodo_pago) VALUES (?, ?, 'Efectivo')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fecha);
            ps.setDouble(2, total);
            ps.executeUpdate();
            return idGenerado(ps);
        }
    }

    private void insertarRenglon(int ventaId, int productoId, int cantidad, double precio) throws SQLException {
        String sql = "INSERT INTO venta_item (venta_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ventaId);
            ps.setInt(2, productoId);
            ps.setInt(3, cantidad);
            ps.setDouble(4, precio);
            ps.executeUpdate();
        }
    }

    private void ejecutar(String sql, int... ids) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                ps.setInt(i + 1, ids[i]);
            }
            ps.executeUpdate();
        }
    }

    private static int idGenerado(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            keys.next();
            return keys.getInt(1);
        }
    }
}

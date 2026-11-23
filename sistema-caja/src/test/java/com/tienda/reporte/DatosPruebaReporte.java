package com.tienda.reporte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.tienda.util.ConexionDB;

/**
 * Datos de prueba del módulo de reportes (ISS-31).
 *
 * Inserta en la base de pruebas tres productos de una categoría propia y dos
 * ventas con una fecha de 1999, que no existe en los datos del sistema. Así cada
 * prueba puede buscar exactamente sus filas sin depender de lo que ya hay en la
 * base. {@link #limpiar()} borra todo lo insertado.
 *
 * Resumen de los datos:
 * <ul>
 *   <li>Producto A: 7 en stock; vendido 3 x $10 y 1 x $15 (4 piezas, $45).</li>
 *   <li>Producto B: 0 en stock; vendido 1 x $20 (1 pieza, $20).</li>
 *   <li>Producto C: 5 en stock; sin ventas.</li>
 *   <li>Venta 1: 1999-03-10 10:00, total $50. Venta 2: 1999-03-10 18:00, total $15.</li>
 * </ul>
 */
final class DatosPruebaReporte {

    static final String CATEGORIA = "CatPruebaReporte31";
    static final String NOMBRE_A = "ProdPruebaReporte31A";
    static final String NOMBRE_B = "ProdPruebaReporte31B";
    static final String NOMBRE_C = "ProdPruebaReporte31C";
    static final int STOCK_A = 7;
    static final int STOCK_B = 0;
    static final int STOCK_C = 5;

    static final String DIA = "1999-03-10";
    static final String SEMANA = "1999-10";
    static final String MES = "1999-03";
    static final String ANIO = "1999";
    static final int VENTAS_DEL_DIA = 2;
    static final double TOTAL_DEL_DIA = 65.0;

    static final int VENDIDO_A = 4;
    static final double TOTAL_A = 45.0;
    static final int VENDIDO_B = 1;
    static final double TOTAL_B = 20.0;

    /** Un id que no existe en la tabla producto. */
    static final int ID_INEXISTENTE = 999_999;

    int idProductoA;
    int idProductoB;
    int idProductoC;
    private int idVenta1;
    private int idVenta2;

    private final Connection conn = ConexionDB.getInstancia().getConexion();

    /**
     * Inserta los productos, las ventas y sus renglones.
     *
     * @throws SQLException si falla alguna inserción
     */
    void insertar() throws SQLException {
        idProductoA = insertarProducto(NOMBRE_A, STOCK_A, "REP31-A");
        idProductoB = insertarProducto(NOMBRE_B, STOCK_B, "REP31-B");
        idProductoC = insertarProducto(NOMBRE_C, STOCK_C, "REP31-C");

        idVenta1 = insertarVenta(DIA + " 10:00:00", 50.0);
        insertarRenglon(idVenta1, idProductoA, 3, 10.0);
        insertarRenglon(idVenta1, idProductoB, 1, 20.0);

        idVenta2 = insertarVenta(DIA + " 18:00:00", 15.0);
        insertarRenglon(idVenta2, idProductoA, 1, 15.0);
    }

    /**
     * Borra todo lo que insertó {@link #insertar()}, en orden para respetar
     * las llaves foráneas.
     *
     * @throws SQLException si falla algún borrado
     */
    void limpiar() throws SQLException {
        ejecutar("DELETE FROM venta_item WHERE venta_id IN (?, ?)", idVenta1, idVenta2);
        ejecutar("DELETE FROM venta WHERE id IN (?, ?)", idVenta1, idVenta2);
        ejecutar("DELETE FROM producto WHERE id IN (?, ?, ?)", idProductoA, idProductoB, idProductoC);
    }

    private int insertarProducto(String nombre, int cantidad, String codigo) throws SQLException {
        String sql = "INSERT INTO producto (nombre, precio, cantidad, categoria, codigo) VALUES (?, 10.0, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setInt(2, cantidad);
            ps.setString(3, CATEGORIA);
            ps.setString(4, codigo);
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

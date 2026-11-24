package com.tienda.promocion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.util.ConexionDB;

/**
 * Prueba de caracterización del acceso a datos de promociones (ISS-32).
 *
 * <p>Utiliza la copia aislada de SQLite preparada por Maven en
 * {@code target/test-run/data/tienda.db} y elimina los datos creados.</p>
 */
class PromocionDAOTest {

    private static final String DESCRIPCION = "ISS32_PROMOCION_CARACTERIZACION";

    private Connection conexion;
    private int secuenciaAntes;

    /**
     * Guarda la secuencia original y elimina residuos de una ejecución anterior.
     *
     * @throws SQLException si falla el acceso a la base de pruebas
     */
    @BeforeEach
    void prepararBase() throws SQLException {
        conexion = ConexionDB.getInstancia().getConexion();

        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM promocion WHERE descripcion = ?")) {
            ps.setString(1, DESCRIPCION);
            ps.executeUpdate();
        }

        secuenciaAntes = obtenerSecuencia();
    }

    /**
     * Elimina la promoción de prueba y restaura el contador AUTOINCREMENT.
     *
     * @throws SQLException si falla la limpieza de la base de pruebas
     */
    @AfterEach
    void limpiarBase() throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM promocion WHERE descripcion = ?")) {
            ps.setString(1, DESCRIPCION);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conexion.prepareStatement(
                "UPDATE sqlite_sequence SET seq = ? WHERE name = 'promocion'")) {
            ps.setInt(1, secuenciaAntes);
            ps.executeUpdate();
        }
    }

    /**
     * Comprueba que agregar persiste la promoción y asigna su identificador.
     *
     * @throws SQLException si falla la consulta de verificación
     */
    @Test
    void agregar_promocionValida_persisteDatosYAsignaId() throws SQLException {
        // RF-15: registrar, editar y eliminar promociones y descuentos para productos específicos.
        PromocionModel promocion = new PromocionModel();
        promocion.setDescripcion(DESCRIPCION);
        promocion.setPorcentajeDescuento(15.5);

        new PromocionDAO().agregar(promocion);

        assertTrue(promocion.getId() > 0, "Debe asignarse el ID generado por SQLite");

        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT descripcion, porcentajeDescuento, activo FROM promocion WHERE id = ?")) {
            ps.setInt(1, promocion.getId());

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "La promoción debe quedar almacenada");
                assertEquals(DESCRIPCION, rs.getString("descripcion"));
                assertEquals(15.5, rs.getDouble("porcentajeDescuento"), 0.0001);
                assertEquals(1, rs.getInt("activo"));
            }
        }
    }

    private int obtenerSecuencia() throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT seq FROM sqlite_sequence WHERE name = 'promocion'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}

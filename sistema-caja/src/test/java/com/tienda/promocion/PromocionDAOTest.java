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

    private static final String DESCRIPCION_VALIDA =
            "ISS32_PROMOCION_CARACTERIZACION";

    private static final String DESCRIPCION_LIMITE =
            "ISS32_PROMOCION_DESCUENTO_150";

    private Connection conexion;
    private int secuenciaAntes;

    /**
     * Guarda la secuencia original y elimina residuos de ejecuciones anteriores.
     *
     * @throws SQLException si falla el acceso a la base de pruebas
     */
    @BeforeEach
    void prepararBase() throws SQLException {
        conexion = ConexionDB.getInstancia().getConexion();

        eliminarPromocionesDePrueba();
        secuenciaAntes = obtenerSecuencia();
    }

    /**
     * Elimina las promociones de prueba y restaura el contador AUTOINCREMENT.
     *
     * @throws SQLException si falla la limpieza de la base de pruebas
     */
    @AfterEach
    void limpiarBase() throws SQLException {
        eliminarPromocionesDePrueba();

        try (PreparedStatement ps = conexion.prepareStatement(
                "UPDATE sqlite_sequence SET seq = ? WHERE name = 'promocion'")) {
            ps.setInt(1, secuenciaAntes);
            ps.executeUpdate();
        }
    }

    /**
     * Comprueba el registro válido y caracteriza la aceptación actual
     * de porcentajes de descuento superiores a 100.
     *
     * @throws SQLException si falla la consulta de verificación
     */
    @Test
    void agregar_guardaPromocion_aceptaDescuentoMayorA100() throws SQLException {
        // RF-15: registrar, editar y eliminar promociones y descuentos para productos específicos.
        PromocionDAO dao = new PromocionDAO();

        PromocionModel promocion = new PromocionModel();
        promocion.setDescripcion(DESCRIPCION_VALIDA);
        promocion.setPorcentajeDescuento(15.5);

        dao.agregar(promocion);

        assertTrue(promocion.getId() > 0,
                "Debe asignarse el ID generado por SQLite");

        verificarPromocion(
                promocion.getId(), DESCRIPCION_VALIDA, 15.5);

        // Caso límite:
        // B-13 (#57): no se valida que el descuento esté entre 0 y 100.
        // Se conserva el comportamiento actual: el DAO almacena el porcentaje 150.
        PromocionModel promocionFueraDeRango = new PromocionModel();
        promocionFueraDeRango.setDescripcion(DESCRIPCION_LIMITE);
        promocionFueraDeRango.setPorcentajeDescuento(150);

        dao.agregar(promocionFueraDeRango);

        assertTrue(promocionFueraDeRango.getId() > 0,
                "Actualmente también se asigna ID al descuento de 150");

        verificarPromocion(
                promocionFueraDeRango.getId(), DESCRIPCION_LIMITE, 150);
    }

    /**
     * Comprueba que la promoción almacenada conserva sus datos.
     *
     * @param id identificador de la promoción
     * @param descripcion descripción esperada
     * @param porcentaje porcentaje de descuento esperado
     * @throws SQLException si falla la consulta
     */
    private void verificarPromocion(
            int id, String descripcion, double porcentaje) throws SQLException {

        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT descripcion, porcentajeDescuento, activo "
                        + "FROM promocion WHERE id = ?")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(),
                        "La promoción debe quedar almacenada");

                assertEquals(descripcion, rs.getString("descripcion"));
                assertEquals(porcentaje,
                        rs.getDouble("porcentajeDescuento"), 0.0001);
                assertEquals(1, rs.getInt("activo"));
            }
        }
    }

    /**
     * Elimina los registros creados por esta prueba.
     *
     * @throws SQLException si falla la eliminación
     */
    private void eliminarPromocionesDePrueba() throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM promocion WHERE descripcion IN (?, ?)")) {

            ps.setString(1, DESCRIPCION_VALIDA);
            ps.setString(2, DESCRIPCION_LIMITE);
            ps.executeUpdate();
        }
    }

    /**
     * Obtiene la secuencia actual de IDs de promociones.
     *
     * @return valor actual de la secuencia, o cero si todavía no existe
     * @throws SQLException si falla la consulta
     */
    private int obtenerSecuencia() throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT seq FROM sqlite_sequence WHERE name = 'promocion'");
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}

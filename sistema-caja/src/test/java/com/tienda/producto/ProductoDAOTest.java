package com.tienda.producto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.util.ConexionDB;

/**
 * Pruebas de caracterización de {@link ProductoDAO} (ISS-30).
 *
 * <p>Corren contra la copia de la base en {@code target/test-run/data/tienda.db}.
 * Cada prueba usa productos con nombre {@code ISS30...} o código {@code ISS30-...}
 * y los borra al terminar. Al final se comprueba que la base original
 * ({@code sistema-caja/data/tienda.db}) no cambió.</p>
 */
class ProductoDAOTest {

    private static final Path BASE_ORIGINAL = Path.of("..", "..", "data", "tienda.db");
    private static byte[] baseOriginalAntes;

    private Connection conn;
    private ProductoDAO dao;

    @BeforeAll
    static void guardarBaseOriginal() throws Exception {
        baseOriginalAntes = Files.readAllBytes(BASE_ORIGINAL);
    }

    @AfterAll
    static void verificarBaseOriginalSinCambios() throws Exception {
        assertArrayEquals(baseOriginalAntes, Files.readAllBytes(BASE_ORIGINAL),
                "Las pruebas no deben modificar sistema-caja/data/tienda.db");
    }

    @BeforeEach
    void preparar() throws SQLException {
        conn = ConexionDB.getInstancia().getConexion();
        limpiar();
        dao = new ProductoDAO();
    }

    @AfterEach
    void limpiarDatos() throws SQLException {
        limpiar();
    }

    // ===================== agregar =====================

    @Test
    void agregar_productoCompleto_asignaIdYGuardaTodosLosCampos() throws SQLException {
        // RF-01
        ProductoModel p = crear("ISS30 Leche", "ISS30-DAO-001", 25.5, 10);
        p.setVencimiento("2027-03-15");
        p.setDescripcion("Leche entera 1L");

        dao.agregar(p);

        assertTrue(p.getId() > 0);
        ProductoModel guardado = buscarEnBase(p.getId());
        assertNotNull(guardado);
        assertEquals("ISS30 Leche", guardado.getNombre());
        assertEquals(25.5, guardado.getPrecio(), 0.0001);
        assertEquals(10, guardado.getCantidad());
        assertEquals("2027-03-15", guardado.getVencimiento());
        assertEquals("Leche entera 1L", guardado.getDescripcion());
        assertEquals("ISS30 Lacteos", guardado.getCategoria());
        assertEquals("Proveedor ISS30", guardado.getProveedor());
        assertEquals("ISS30-DAO-001", guardado.getCodigo());
    }

    @Test
    void agregar_sinCamposOpcionales_guardaNulos() throws SQLException {
        // RF-01: la interfaz gráfica no captura descripción ni vencimiento
        ProductoModel p = new ProductoModel();
        p.setNombre("ISS30 Solo nombre");

        dao.agregar(p);

        ProductoModel guardado = buscarEnBase(p.getId());
        assertNotNull(guardado);
        assertEquals(0.0, guardado.getPrecio(), 0.0001);
        assertEquals(0, guardado.getCantidad());
        assertNull(guardado.getCodigo());
        assertNull(guardado.getVencimiento());
        assertNull(guardado.getDescripcion());
    }

    @Test
    void agregar_precioYCantidadNegativos_seGuardanSinValidar() throws SQLException {
        // RF-01: no hay validación de valores negativos (ERS 3.2)
        ProductoModel p = crear("ISS30 Negativo", "ISS30-DAO-002", -5.0, -3);

        dao.agregar(p);

        ProductoModel guardado = buscarEnBase(p.getId());
        assertEquals(-5.0, guardado.getPrecio(), 0.0001);
        assertEquals(-3, guardado.getCantidad());
    }

    @Test
    void agregar_codigoDuplicado_noGuardaYDejaIdEnCeroEImprimeError() {
        // RF-01: el código es único; el error solo se imprime, no se lanza
        dao.agregar(crear("ISS30 Original", "ISS30-DAO-003", 10.0, 1));
        ProductoModel duplicado = crear("ISS30 Duplicado", "ISS30-DAO-003", 11.0, 2);

        String salida = capturarSalida(() -> dao.agregar(duplicado));

        assertEquals(0, duplicado.getId());
        assertTrue(salida.contains("Error al agregar producto:"));
        assertEquals(1, dao.getBuscador().buscarPorCodigo("ISS30-DAO-003").size());
    }

    @Test
    void agregar_sinNombre_noGuardaYDejaIdEnCero() {
        // RF-01: la base exige nombre (NOT NULL); el DAO solo imprime el error
        ProductoModel p = new ProductoModel();
        p.setCodigo("ISS30-DAO-004");

        String salida = capturarSalida(() -> dao.agregar(p));

        assertEquals(0, p.getId());
        assertTrue(salida.contains("Error al agregar producto:"));
        assertTrue(dao.getBuscador().buscarPorCodigo("ISS30-DAO-004").isEmpty());
    }

    @Test
    void agregar_dosProductosSinCodigo_permiteAmbos() {
        // RF-01: UNIQUE en SQLite permite varios NULL
        ProductoModel a = new ProductoModel();
        a.setNombre("ISS30 Sin codigo A");
        ProductoModel b = new ProductoModel();
        b.setNombre("ISS30 Sin codigo B");

        dao.agregar(a);
        dao.agregar(b);

        assertTrue(a.getId() > 0);
        assertTrue(b.getId() > 0);
    }

    // ===================== actualizar =====================

    @Test
    void actualizar_productoExistente_cambiaTodosLosCampos() throws SQLException {
        // RF-01, RF-02: en consola, editar también cambia la cantidad
        ProductoModel p = crear("ISS30 Antes", "ISS30-DAO-010", 10.0, 5);
        dao.agregar(p);

        p.setNombre("ISS30 Despues");
        p.setPrecio(15.75);
        p.setCantidad(42);
        p.setVencimiento("2028-01-01");
        p.setDescripcion("Nueva descripcion");
        p.setCategoria("ISS30 Nueva");
        p.setProveedor("Proveedor nuevo");
        p.setCodigo("ISS30-DAO-011");
        dao.actualizar(p);

        ProductoModel guardado = buscarEnBase(p.getId());
        assertEquals("ISS30 Despues", guardado.getNombre());
        assertEquals(15.75, guardado.getPrecio(), 0.0001);
        assertEquals(42, guardado.getCantidad());
        assertEquals("2028-01-01", guardado.getVencimiento());
        assertEquals("Nueva descripcion", guardado.getDescripcion());
        assertEquals("ISS30 Nueva", guardado.getCategoria());
        assertEquals("Proveedor nuevo", guardado.getProveedor());
        assertEquals("ISS30-DAO-011", guardado.getCodigo());
    }

    @Test
    void actualizar_conCamposNulos_borraLosValoresAnteriores() throws SQLException {
        // RF-01: el UPDATE escribe todos los campos, también los nulos
        ProductoModel p = crear("ISS30 Con datos", "ISS30-DAO-012", 10.0, 5);
        p.setDescripcion("Se va a borrar");
        dao.agregar(p);

        ProductoModel parcial = new ProductoModel();
        parcial.setId(p.getId());
        parcial.setNombre("ISS30 Con datos");
        dao.actualizar(parcial);

        ProductoModel guardado = buscarEnBase(p.getId());
        assertNull(guardado.getDescripcion());
        assertNull(guardado.getCodigo());
        assertEquals(0, guardado.getCantidad());
    }

    @Test
    void actualizar_idInexistente_noCambiaNadaNiImprimeError() {
        // RF-01
        int antes = dao.listarTodos().size();
        ProductoModel fantasma = crear("ISS30 Fantasma", "ISS30-DAO-013", 1.0, 1);
        fantasma.setId(999999);

        String salida = capturarSalida(() -> dao.actualizar(fantasma));

        assertEquals("", salida);
        assertEquals(antes, dao.listarTodos().size());
        assertTrue(dao.getBuscador().buscarPorCodigo("ISS30-DAO-013").isEmpty());
    }

    @Test
    void actualizar_codigoDeOtroProducto_noCambiaEImprimeError() throws SQLException {
        // RF-01: el código es único
        ProductoModel a = crear("ISS30 A", "ISS30-DAO-014", 1.0, 1);
        ProductoModel b = crear("ISS30 B", "ISS30-DAO-015", 2.0, 2);
        dao.agregar(a);
        dao.agregar(b);

        b.setCodigo("ISS30-DAO-014");
        String salida = capturarSalida(() -> dao.actualizar(b));

        assertTrue(salida.contains("Error al actualizar producto:"));
        assertEquals("ISS30-DAO-015", buscarEnBase(b.getId()).getCodigo());
    }

    // ===================== eliminar =====================

    @Test
    void eliminar_productoExistente_loQuitaDeLaBase() throws SQLException {
        // RF-03
        ProductoModel p = crear("ISS30 Borrar", "ISS30-DAO-020", 1.0, 1);
        dao.agregar(p);

        dao.eliminar(p.getId());

        assertNull(buscarEnBase(p.getId()));
    }

    @Test
    void eliminar_idInexistente_noFallaNiImprime() {
        // RF-03: se elimina sin confirmación y sin avisar si no existe
        int antes = dao.listarTodos().size();

        String salida = capturarSalida(() -> dao.eliminar(999999));

        assertEquals("", salida);
        assertEquals(antes, dao.listarTodos().size());
    }

    @Test
    void eliminar_productoConMovimientos_dejaLosMovimientosHuerfanos() throws SQLException {
        // RF-03, RF-05
        // B-01: la conexión no activa PRAGMA foreign_keys, así que el ON DELETE CASCADE
        // de movimiento no se aplica y quedan movimientos de un producto que ya no existe.
        ProductoModel p = crear("ISS30 Con movimientos", "ISS30-DAO-021", 1.0, 5);
        dao.agregar(p);
        insertarMovimiento(p.getId());

        dao.eliminar(p.getId());

        assertNull(buscarEnBase(p.getId()));
        assertEquals(1, contarMovimientos(p.getId()));
        borrarMovimientos(p.getId());
    }

    // ===================== listarTodos =====================

    @Test
    void listarTodos_incluyeLosProductosAgregados() {
        // RF-11: la consola lista el inventario con este método
        int antes = dao.listarTodos().size();
        ProductoModel p = crear("ISS30 Listar", "ISS30-DAO-030", 3.0, 3);
        dao.agregar(p);

        List<ProductoModel> lista = dao.listarTodos();

        assertEquals(antes + 1, lista.size());
        ProductoModel encontrado = lista.stream()
                .filter(x -> x.getId() == p.getId()).findFirst().orElse(null);
        assertNotNull(encontrado);
        assertEquals("ISS30-DAO-030", encontrado.getCodigo());
        assertEquals("ISS30 Lacteos", encontrado.getCategoria());
    }

    @Test
    void listarTodos_ordenPorIdAscendente() {
        // RF-11: no hay ORDER BY; SQLite devuelve por rowid
        ProductoModel a = crear("ISS30 Primero", "ISS30-DAO-031", 1.0, 1);
        ProductoModel b = crear("ISS30 Segundo", "ISS30-DAO-032", 1.0, 1);
        dao.agregar(a);
        dao.agregar(b);

        List<ProductoModel> lista = dao.listarTodos();
        int posA = indiceDe(lista, a.getId());
        int posB = indiceDe(lista, b.getId());

        assertTrue(posA >= 0 && posB > posA);
    }

    // ===================== getBuscador =====================

    @Test
    void getBuscador_devuelveServicioQueUsaLaMismaBase() {
        // RF-11
        ProductoModel p = crear("ISS30 Buscable", "ISS30-DAO-040", 1.0, 1);
        dao.agregar(p);

        BuscarProductoService buscador = dao.getBuscador();

        assertNotNull(buscador);
        assertEquals(1, buscador.buscarPorCodigo("ISS30-DAO-040").size());
    }

    // ===================== Utilidades =====================

    private static ProductoModel crear(String nombre, String codigo, double precio, int cantidad) {
        ProductoModel p = new ProductoModel();
        p.setNombre(nombre);
        p.setCodigo(codigo);
        p.setPrecio(precio);
        p.setCantidad(cantidad);
        p.setCategoria("ISS30 Lacteos");
        p.setProveedor("Proveedor ISS30");
        return p;
    }

    private ProductoModel buscarEnBase(int id) throws SQLException {
        String sql = "SELECT nombre, precio, cantidad, vencimiento, descripcion, categoria, proveedor, codigo "
                + "FROM producto WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ProductoModel p = new ProductoModel();
                p.setId(id);
                p.setNombre(rs.getString("nombre"));
                p.setPrecio(rs.getDouble("precio"));
                p.setCantidad(rs.getInt("cantidad"));
                p.setVencimiento(rs.getString("vencimiento"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setCategoria(rs.getString("categoria"));
                p.setProveedor(rs.getString("proveedor"));
                p.setCodigo(rs.getString("codigo"));
                return p;
            }
        }
    }

    private void insertarMovimiento(int productoId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO movimiento (fecha, tipo, cantidad, producto_id) VALUES ('2026-11-22T10:00', 'ENTRADA', 1, ?)")) {
            ps.setInt(1, productoId);
            ps.executeUpdate();
        }
    }

    private int contarMovimientos(int productoId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM movimiento WHERE producto_id = ?")) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void borrarMovimientos(int productoId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM movimiento WHERE producto_id = ?")) {
            ps.setInt(1, productoId);
            ps.executeUpdate();
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

    private static int indiceDe(List<ProductoModel> lista, int id) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private static String capturarSalida(Runnable accion) {
        PrintStream original = System.out;
        ByteArrayOutputStream capturada = new ByteArrayOutputStream();
        try (PrintStream temporal = new PrintStream(capturada, true, StandardCharsets.UTF_8)) {
            System.setOut(temporal);
            accion.run();
        } finally {
            System.setOut(original);
        }
        return capturada.toString(StandardCharsets.UTF_8);
    }
}

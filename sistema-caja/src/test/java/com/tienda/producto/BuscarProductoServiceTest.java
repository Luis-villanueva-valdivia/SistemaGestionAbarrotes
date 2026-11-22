package com.tienda.producto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.util.ConexionDB;

/**
 * Pruebas de caracterización de {@link BuscarProductoService} (ISS-30).
 *
 * <p>Corren contra la copia de la base en {@code target/test-run/data/tienda.db}.
 * Cada prueba crea sus propios productos (nombre con prefijo {@code ISS30})
 * y los borra al terminar.</p>
 */
class BuscarProductoServiceTest {

    private static final String PREFIJO = "ISS30";

    private Connection conn;
    private BuscarProductoService buscador;
    private ProductoModel refresco;
    private ProductoModel botana;

    @BeforeEach
    void prepararDatos() throws SQLException {
        conn = ConexionDB.getInstancia().getConexion();
        limpiar();
        buscador = new BuscarProductoService(conn);
        refresco = insertar(PREFIJO + " Refresco Uva", "ISS30-BUS-001", "ISS30 Bebidas", 12.5, 7, "2027-01-31");
        botana = insertar(PREFIJO + " Papas Limon", "ISS30-BUS-002", "ISS30 Botanas", 18.0, 3, null);
    }

    @AfterEach
    void limpiarDatos() throws SQLException {
        limpiar();
    }

    // ===================== buscarPorCodigo =====================

    @Test
    void buscarPorCodigo_codigoExistente_devuelveSoloEseProducto() {
        // RF-11
        List<ProductoModel> resultado = buscador.buscarPorCodigo("ISS30-BUS-001");

        assertEquals(1, resultado.size());
        assertEquals(refresco.getId(), resultado.get(0).getId());
    }

    @Test
    void buscarPorCodigo_mapeaTodosLosCampos() {
        // RF-11: la ficha trae precio, cantidad, código y vencimiento
        ProductoModel p = buscador.buscarPorCodigo("ISS30-BUS-001").get(0);

        assertEquals(PREFIJO + " Refresco Uva", p.getNombre());
        assertEquals(12.5, p.getPrecio(), 0.0001);
        assertEquals(7, p.getCantidad());
        assertEquals("2027-01-31", p.getVencimiento());
        assertEquals("Descripcion de prueba", p.getDescripcion());
        assertEquals("ISS30 Bebidas", p.getCategoria());
        assertEquals("Proveedor ISS30", p.getProveedor());
        assertEquals("ISS30-BUS-001", p.getCodigo());
    }

    @Test
    void buscarPorCodigo_codigoParcial_noEncuentraNada() {
        // RF-11: por código la comparación es exacta (=), no parcial
        assertTrue(buscador.buscarPorCodigo("ISS30-BUS").isEmpty());
    }

    @Test
    void buscarPorCodigo_distintaMayuscula_noEncuentraNada() {
        // RF-11: en SQLite '=' distingue mayúsculas
        assertTrue(buscador.buscarPorCodigo("iss30-bus-001").isEmpty());
    }

    @Test
    void buscarPorCodigo_codigoVacio_noEncuentraNada() {
        // RF-11
        assertTrue(buscador.buscarPorCodigo("").isEmpty());
    }

    // ===================== buscarPorNombre =====================

    @Test
    void buscarPorNombre_textoParcial_encuentraCoincidencias() {
        // RF-11: búsqueda por nombre con LIKE '%texto%'
        List<ProductoModel> resultado = buscador.buscarPorNombre("Refresco");

        assertTrue(contieneId(resultado, refresco.getId()));
        assertFalse(contieneId(resultado, botana.getId()));
    }

    @Test
    void buscarPorNombre_sinDistinguirMayusculas_encuentraCoincidencias() {
        // RF-11: LIKE de SQLite no distingue mayúsculas en ASCII
        assertTrue(contieneId(buscador.buscarPorNombre("refresco uva"), refresco.getId()));
    }

    @Test
    void buscarPorNombre_textoVacio_devuelveTodosLosProductos() {
        // RF-11: '%%' coincide con todos
        List<ProductoModel> resultado = buscador.buscarPorNombre("");

        assertTrue(contieneId(resultado, refresco.getId()));
        assertTrue(contieneId(resultado, botana.getId()));
        assertEquals(contarProductos(), resultado.size());
    }

    @Test
    void buscarPorNombre_inexistente_devuelveListaVacia() {
        // RF-11
        assertTrue(buscador.buscarPorNombre("ISS30 no existe xyz").isEmpty());
    }

    @Test
    void buscarPorNombre_intentoDeInyeccionSql_noDevuelveRegistros() {
        // RF-11, RNF-05: el texto va como parámetro del PreparedStatement
        assertTrue(buscador.buscarPorNombre("' OR '1'='1").isEmpty());
    }

    // ===================== buscarPorCategoria =====================

    @Test
    void buscarPorCategoria_textoParcial_encuentraCoincidencias() {
        // RF-11
        List<ProductoModel> resultado = buscador.buscarPorCategoria("ISS30 Bot");

        assertEquals(1, resultado.size());
        assertEquals(botana.getId(), resultado.get(0).getId());
    }

    @Test
    void buscarPorCategoria_inexistente_devuelveListaVacia() {
        // RF-11
        assertTrue(buscador.buscarPorCategoria("ISS30 Categoria inexistente").isEmpty());
    }

    @Test
    void buscarPorCategoria_intentoDeInyeccionSql_noDevuelveRegistros() {
        // RF-11, RNF-05
        assertTrue(buscador.buscarPorCategoria("' OR '1'='1").isEmpty());
    }

    // ===================== buscarPorId =====================

    @Test
    void buscarPorId_idExistente_devuelveListaConUnProducto() {
        // RF-11
        List<ProductoModel> resultado = buscador.buscarPorId(botana.getId());

        assertEquals(1, resultado.size());
        assertEquals(PREFIJO + " Papas Limon", resultado.get(0).getNombre());
        assertNull(resultado.get(0).getVencimiento());
    }

    @Test
    void buscarPorId_idInexistente_devuelveListaVacia() {
        // RF-11
        assertTrue(buscador.buscarPorId(999999).isEmpty());
    }

    @Test
    void buscarPorId_idCeroYNegativo_devuelveListaVacia() {
        // RF-11
        assertTrue(buscador.buscarPorId(0).isEmpty());
        assertTrue(buscador.buscarPorId(-1).isEmpty());
    }

    // ===================== Sin conexión =====================

    @Test
    void buscar_sinConexion_devuelveListaVaciaEImprimeAviso() {
        // RF-11, RNF-02: sin conexión no lanza excepción
        BuscarProductoService sinConexion = new BuscarProductoService(null);
        String salida = capturarSalida(() -> {
            assertTrue(sinConexion.buscarPorCodigo("ISS30-BUS-001").isEmpty());
            assertTrue(sinConexion.buscarPorNombre("Refresco").isEmpty());
            assertTrue(sinConexion.buscarPorCategoria("Bebidas").isEmpty());
            assertTrue(sinConexion.buscarPorId(1).isEmpty());
        });

        String aviso = "No hay conexión a la base de datos. Búsqueda de productos vacía.";
        assertEquals(4, contarApariciones(salida, aviso));
    }

    // ===================== Utilidades =====================

    private ProductoModel insertar(String nombre, String codigo, String categoria,
                                   double precio, int cantidad, String vencimiento) {
        ProductoModel p = new ProductoModel();
        p.setNombre(nombre);
        p.setCodigo(codigo);
        p.setCategoria(categoria);
        p.setPrecio(precio);
        p.setCantidad(cantidad);
        p.setVencimiento(vencimiento);
        p.setDescripcion("Descripcion de prueba");
        p.setProveedor("Proveedor ISS30");
        new ProductoDAO().agregar(p);
        assertTrue(p.getId() > 0, "El producto de prueba debe quedar guardado");
        return p;
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

    private int contarProductos() {
        return new ProductoDAO().listarTodos().size();
    }

    private static boolean contieneId(List<ProductoModel> lista, int id) {
        return lista.stream().anyMatch(p -> p.getId() == id);
    }

    private static int contarApariciones(String texto, String buscado) {
        int cuenta = 0;
        int desde = texto.indexOf(buscado);
        while (desde >= 0) {
            cuenta++;
            desde = texto.indexOf(buscado, desde + buscado.length());
        }
        return cuenta;
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

package com.tienda.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.producto.BuscarProductoService;
import com.tienda.producto.IProductoDAO;
import com.tienda.producto.ProductoModel;

/**
 * Pruebas de caracterización de {@link MovimientoController} (ISS-30).
 *
 * <p>Usan DAO falsos en memoria: no necesitan base de datos.</p>
 */
class MovimientoControllerTest {

    private ProductoDAOFalso productoDAO;
    private MovimientoDAOFalso movimientoDAO;
    private MovimientoController controller;

    @BeforeEach
    void preparar() {
        productoDAO = new ProductoDAOFalso();
        movimientoDAO = new MovimientoDAOFalso();
        controller = new MovimientoController(productoDAO, movimientoDAO);
    }

    // ===================== CRUD de productos =====================

    @Test
    void agregarProducto_delegaEnElDaoConElMismoObjeto() {
        // RF-01
        ProductoModel p = producto(0, "Arroz", 5);

        controller.agregarProducto(p);

        assertEquals(1, productoDAO.agregados.size());
        assertSame(p, productoDAO.agregados.get(0));
    }

    @Test
    void actualizarProducto_delegaEnElDaoConElMismoObjeto() {
        // RF-01
        ProductoModel p = producto(3, "Frijol", 8);

        controller.actualizarProducto(p);

        assertEquals(1, productoDAO.actualizados.size());
        assertSame(p, productoDAO.actualizados.get(0));
    }

    @Test
    void eliminarProducto_delegaElIdSinValidar() {
        // RF-03: no se valida el id antes de llamar al DAO
        controller.eliminarProducto(7);
        controller.eliminarProducto(0);
        controller.eliminarProducto(-1);

        assertEquals(List.of(7, 0, -1), productoDAO.eliminados);
    }

    @Test
    void listarProductos_devuelveLaListaDelDao() {
        // RF-11
        productoDAO.productos.add(producto(1, "Azucar", 4));

        assertSame(productoDAO.productos, controller.listarProductos());
    }

    @Test
    void listarProductos_daoSinProductos_devuelveListaVacia() {
        // RF-11
        assertTrue(controller.listarProductos().isEmpty());
    }

    // ===================== verificarStock =====================

    @Test
    void verificarStock_incluyeProductosConCantidadMenorOIgualAlUmbral() {
        // RF-04: la comparación es <= umbral
        productoDAO.productos.add(producto(1, "Bajo", 2));
        productoDAO.productos.add(producto(2, "Justo", 5));
        productoDAO.productos.add(producto(3, "Suficiente", 6));

        List<ProductoModel> bajos = controller.verificarStock(5);

        assertEquals(2, bajos.size());
        assertEquals(1, bajos.get(0).getId());
        assertEquals(2, bajos.get(1).getId());
    }

    @Test
    void verificarStock_umbralCero_soloIncluyeProductosEnCeroONegativos() {
        // RF-04
        productoDAO.productos.add(producto(1, "Agotado", 0));
        productoDAO.productos.add(producto(2, "Negativo", -2));
        productoDAO.productos.add(producto(3, "Con stock", 1));

        List<ProductoModel> bajos = controller.verificarStock(0);

        assertEquals(2, bajos.size());
    }

    @Test
    void verificarStock_umbralNegativo_noIncluyeProductosConStockCero() {
        // RF-04
        productoDAO.productos.add(producto(1, "Agotado", 0));

        assertTrue(controller.verificarStock(-1).isEmpty());
    }

    @Test
    void verificarStock_sinProductos_devuelveListaVacia() {
        // RF-04
        assertTrue(controller.verificarStock(10).isEmpty());
    }

    // ===================== Movimientos =====================

    @Test
    void registrarMovimiento_delegaEnElDaoConElMismoObjeto() {
        // RF-02
        MovimientoInventarioModel m = new MovimientoInventarioModel();
        m.setProductoId(1);
        m.setTipo("ENTRADA");
        m.setCantidad(5);

        controller.registrarMovimiento(m);

        assertEquals(1, movimientoDAO.registrados.size());
        assertSame(m, movimientoDAO.registrados.get(0));
    }

    @Test
    void registrarMovimiento_noModificaLaExistenciaPorSuCuenta() {
        // RF-02: el ajuste de existencias lo hace el DAO, no el controlador
        ProductoModel p = producto(1, "Arroz", 5);
        productoDAO.productos.add(p);
        MovimientoInventarioModel m = new MovimientoInventarioModel();
        m.setProductoId(1);
        m.setTipo("SALIDA");
        m.setCantidad(3);

        controller.registrarMovimiento(m);

        assertEquals(5, p.getCantidad());
        assertTrue(productoDAO.actualizados.isEmpty());
    }

    @Test
    void verHistorialMovimientos_pasaElIdYDevuelveLaListaDelDao() {
        // RF-05
        MovimientoInventarioModel m = new MovimientoInventarioModel();
        m.setProductoId(4);
        movimientoDAO.historial.add(m);

        List<MovimientoInventarioModel> resultado = controller.verHistorialMovimientos(4);

        assertSame(movimientoDAO.historial, resultado);
        assertEquals(List.of(4), movimientoDAO.idsConsultados);
    }

    // ===================== Búsqueda =====================

    @Test
    void getBuscador_devuelveElBuscadorDelDao() {
        // RF-11
        assertSame(productoDAO.buscador, controller.getBuscador());
    }

    // ===================== DAO falsos =====================

    private static ProductoModel producto(int id, String nombre, int cantidad) {
        ProductoModel p = new ProductoModel();
        p.setId(id);
        p.setNombre(nombre);
        p.setCantidad(cantidad);
        return p;
    }

    /** DAO de productos en memoria que registra las llamadas recibidas. */
    private static class ProductoDAOFalso implements IProductoDAO {
        final List<ProductoModel> productos = new ArrayList<>();
        final List<ProductoModel> agregados = new ArrayList<>();
        final List<ProductoModel> actualizados = new ArrayList<>();
        final List<Integer> eliminados = new ArrayList<>();
        final BuscarProductoService buscador = new BuscarProductoService(null);

        @Override
        public void agregar(ProductoModel p) {
            agregados.add(p);
        }

        @Override
        public void actualizar(ProductoModel p) {
            actualizados.add(p);
        }

        @Override
        public void eliminar(int id) {
            eliminados.add(id);
        }

        @Override
        public List<ProductoModel> listarTodos() {
            return productos;
        }

        @Override
        public BuscarProductoService getBuscador() {
            return buscador;
        }
    }

    /** DAO de movimientos en memoria que registra las llamadas recibidas. */
    private static class MovimientoDAOFalso implements IMovimientoDAO {
        final List<MovimientoInventarioModel> registrados = new ArrayList<>();
        final List<MovimientoInventarioModel> historial = new ArrayList<>();
        final List<Integer> idsConsultados = new ArrayList<>();

        @Override
        public void registrar(MovimientoInventarioModel m) {
            registrados.add(m);
        }

        @Override
        public List<MovimientoInventarioModel> listarPorProducto(int idProducto) {
            idsConsultados.add(idProducto);
            return historial;
        }
    }
}

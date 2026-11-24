package com.tienda.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tienda.producto.BuscarProductoService;
import com.tienda.producto.IProductoDAO;
import com.tienda.producto.ProductoModel;

/**
 * Prueba de caracterización de {@link MovimientoController} (ISS-30).
 *
 * <p>Usa un DAO falso en memoria: no necesita base de datos.</p>
 */
class MovimientoControllerTest {

    @Test
    void verificarStock_incluyeProductosConCantidadMenorOIgualAlUmbral() {
        // RF-04: la comparación es <= umbral
        ProductoDAOFalso productoDAO = new ProductoDAOFalso();
        productoDAO.productos.add(producto(1, "Bajo", 2));
        productoDAO.productos.add(producto(2, "Justo", 5));
        productoDAO.productos.add(producto(3, "Suficiente", 6));
        MovimientoController controller = new MovimientoController(productoDAO, null);

        List<ProductoModel> bajos = controller.verificarStock(5);

        assertEquals(2, bajos.size());
        assertEquals(1, bajos.get(0).getId());
        assertEquals(2, bajos.get(1).getId());
    }

    private static ProductoModel producto(int id, String nombre, int cantidad) {
        ProductoModel p = new ProductoModel();
        p.setId(id);
        p.setNombre(nombre);
        p.setCantidad(cantidad);
        return p;
    }

    /** DAO de productos en memoria; solo listarTodos devuelve datos. */
    private static class ProductoDAOFalso implements IProductoDAO {
        final List<ProductoModel> productos = new ArrayList<>();

        @Override
        public void agregar(ProductoModel p) {
        }

        @Override
        public void actualizar(ProductoModel p) {
        }

        @Override
        public void eliminar(int id) {
        }

        @Override
        public List<ProductoModel> listarTodos() {
            return productos;
        }

        @Override
        public BuscarProductoService getBuscador() {
            return null;
        }
    }
}

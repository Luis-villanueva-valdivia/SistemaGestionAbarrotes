package com.tienda.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tienda.producto.ProductoModel;
import com.tienda.reporte.ReporteModel;

/**
 * Prueba de caracterización de {@link VentaController} (ISS-28).
 *
 * <p>El controlador recibe su DAO por el constructor, así que se usa un DAO
 * falso y la prueba no necesita base de datos.</p>
 */
class VentaControllerTest {

    private static final double DELTA = 0.0001;

    @Test
    void calcularTotal_sumaPrecioPorCantidadSinUsarElTotalGuardadoNiElDao() {
        // RF-07: el total es la suma de precio x cantidad de cada producto; el total y el
        // descuento que ya trae la venta no se usan (las promociones no se aplican al vender)
        VentaDAOFalso daoFalso = new VentaDAOFalso();
        VentaController controller = new VentaController(daoFalso);
        VentaModel venta = new VentaModel();
        venta.setProductos(new ProductoModel[] {
            producto(1, "Coca Cola", 10.0, 2),
            producto(2, "Pepsi", 10.5, 3)
        });
        venta.setTotal(999.0);
        venta.setDescuento(50.0);

        double total = controller.calcularTotal(venta);

        assertEquals(51.5, total, DELTA);
        assertTrue(daoFalso.llamadas.isEmpty(), "calcularTotal no debe usar el DAO");
    }

    private static ProductoModel producto(int id, String nombre, double precio, int cantidad) {
        ProductoModel p = new ProductoModel();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setCantidad(cantidad);
        return p;
    }

    /** DAO falso: anota qué método se llamó y no toca la base. */
    private static class VentaDAOFalso implements IVentaDAO {
        private final List<String> llamadas = new ArrayList<>();

        @Override
        public void registrar(VentaModel v) {
            llamadas.add("registrar");
        }

        @Override
        public void historial() {
            llamadas.add("historial");
        }

        @Override
        public ReporteModel generarReporte(String periodo) {
            llamadas.add("generarReporte");
            return null;
        }

        @Override
        public void historialPorCliente(int clienteId) {
            llamadas.add("historialPorCliente");
        }

        @Override
        public void registrarVenta(VentaModel v) {
            llamadas.add("registrarVenta");
        }
    }
}

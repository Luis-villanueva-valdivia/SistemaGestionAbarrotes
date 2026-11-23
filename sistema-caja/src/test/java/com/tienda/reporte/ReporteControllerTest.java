package com.tienda.reporte;

import static com.tienda.reporte.DatosPruebaReporte.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de caracterización de {@link ReporteController}.
 *
 * El controlador crea su propio {@code ReporteDAO} (no lo recibe por el
 * constructor, ver LLA-12), así que no se le puede pasar un DAO falso: estas
 * pruebas usan la base de pruebas con los datos de {@link DatosPruebaReporte}.
 */
class ReporteControllerTest {

    private static final double DELTA = 0.0001;

    private DatosPruebaReporte datos;
    private ReporteController controller;

    @BeforeEach
    void prepararDatos() throws SQLException {
        datos = new DatosPruebaReporte();
        datos.insertar();
        controller = new ReporteController();
    }

    @AfterEach
    void borrarDatos() throws SQLException {
        datos.limpiar();
    }

    @Test
    void ventasPorPeriodo_mensual_devuelveElMismoResultadoQueElDAO() {
        // RF-16
        List<ReporteModel> lista = controller.ventasPorPeriodo("mensual");

        ReporteModel mes = lista.stream().filter(r -> MES.equals(r.getPeriodo())).findFirst().orElseThrow();
        assertEquals(TOTAL_DEL_DIA, mes.getTotalVentas(), DELTA);
        assertEquals("mes", mes.getCategoria());
    }

    @Test
    void ventasPorPeriodo_tipoNulo_lanzaNullPointerException() {
        // RF-16: caso de error. B-XX: el controlador tampoco valida el tipo
        assertThrows(NullPointerException.class, () -> controller.ventasPorPeriodo(null));
    }

    @Test
    void reporteInventario_incluyeLosProductosDePrueba() {
        // RF-17
        List<ReporteModel> lista = controller.reporteInventario();

        assertTrue(lista.stream().anyMatch(r -> NOMBRE_C.equals(r.getPeriodo()) && r.getCantidadVentas() == STOCK_C));
    }

    @Test
    void productoPorId_productoConVentas_devuelveSusTotales() {
        // RF-18
        ReporteModel r = controller.productoPorId(datos.idProductoB);

        assertNotNull(r);
        assertEquals(VENDIDO_B, r.getCantidadVentas());
        assertEquals(TOTAL_B, r.getTotalVentas(), DELTA);
    }

    @Test
    void productoPorId_idQueNoExiste_devuelveNull() {
        // RF-18: caso de error
        assertNull(controller.productoPorId(ID_INEXISTENTE));
    }

    @Test
    void productosPorCategoria_categoriaDePrueba_devuelveLosDosVendidos() {
        // RF-18
        assertEquals(2, controller.productosPorCategoria(CATEGORIA).size());
    }

    @Test
    void productosPorCategoria_categoriaQueNoExiste_devuelveListaVacia() {
        // RF-18: caso de error
        assertTrue(controller.productosPorCategoria("CategoriaQueNoExiste31").isEmpty());
    }
}

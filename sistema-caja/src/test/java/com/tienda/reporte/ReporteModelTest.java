package com.tienda.reporte;

import static com.tienda.reporte.DatosPruebaReporte.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de caracterización de {@link ReporteModel}: constructores, accesores,
 * {@code toString()} y los métodos estáticos de consulta.
 */
class ReporteModelTest {

    private static final double DELTA = 0.0001;

    // ---------- constructores, getters y setters ----------

    @Test
    void constructorVacio_dejaLosCamposEnSusValoresPorDefecto() {
        // RF-16, RF-17, RF-18
        ReporteModel r = new ReporteModel();

        assertEquals(0, r.getIdReporte());
        assertNull(r.getCategoria());
        assertNull(r.getPeriodo());
        assertEquals(0.0, r.getTotalVentas(), DELTA);
        assertEquals(0, r.getCantidadVentas());
        assertNull(r.getFechaGeneracion());
    }

    @Test
    void constructorCompleto_asignaCadaCampo() {
        // RF-16
        ReporteModel r = new ReporteModel(3, "Bebidas", "2026-11", 150.5, 4, "2026-11-23");

        assertEquals(3, r.getIdReporte());
        assertEquals("Bebidas", r.getCategoria());
        assertEquals("2026-11", r.getPeriodo());
        assertEquals(150.5, r.getTotalVentas(), DELTA);
        assertEquals(4, r.getCantidadVentas());
        assertEquals("2026-11-23", r.getFechaGeneracion());
    }

    @Test
    void setters_guardanLoQueDevuelvenLosGetters() {
        // RF-16
        ReporteModel r = new ReporteModel();
        r.setIdReporte(8);
        r.setCategoria("mes");
        r.setPeriodo("2026-10");
        r.setTotalVentas(99.9);
        r.setCantidadVentas(12);
        r.setFechaGeneracion("hoy");

        assertEquals(8, r.getIdReporte());
        assertEquals("mes", r.getCategoria());
        assertEquals("2026-10", r.getPeriodo());
        assertEquals(99.9, r.getTotalVentas(), DELTA);
        assertEquals(12, r.getCantidadVentas());
        assertEquals("hoy", r.getFechaGeneracion());
    }

    @Test
    void setters_aceptanValoresNegativosSinValidar() {
        // RF-16: caso límite; el modelo no valida montos ni cantidades
        ReporteModel r = new ReporteModel();
        r.setTotalVentas(-10.0);
        r.setCantidadVentas(-1);

        assertEquals(-10.0, r.getTotalVentas(), DELTA);
        assertEquals(-1, r.getCantidadVentas());
    }

    @Test
    void toString_muestraTodosLosCamposEnElFormatoActual() {
        // RF-16
        ReporteModel r = new ReporteModel(1, "dia", "2026-11-23", 20.0, 2, null);

        assertEquals("ReporteModel{idReporte=1, categoria='dia', periodo='2026-11-23', "
                + "totalVentas=20.0, cantidadVentas=2, fechaGeneracion='null'}", r.toString());
    }

    // ---------- métodos estáticos (consultan la base de pruebas) ----------

    @Test
    void generarReporteVentas_diario_incluyeElDiaDePrueba() throws SQLException {
        // RF-16
        DatosPruebaReporte datos = new DatosPruebaReporte();
        datos.insertar();
        try {
            List<ReporteModel> lista = ReporteModel.generarReporteVentas("diario");

            ReporteModel dia = lista.stream().filter(r -> DIA.equals(r.getPeriodo())).findFirst().orElseThrow();
            assertEquals(VENTAS_DEL_DIA, dia.getCantidadVentas());
        } finally {
            datos.limpiar();
        }
    }

    @Test
    void reporteInventarioEstatico_guardaLosCamposAlReves_queElDelDAO() throws SQLException {
        // RF-17. B-XX: aquí el nombre va en "categoria" y la categoría en "periodo";
        // ReporteDAO.reporteInventario() los guarda al revés (ver MOV-05)
        DatosPruebaReporte datos = new DatosPruebaReporte();
        datos.insertar();
        try {
            ReporteModel r = ReporteModel.reporteInventario().stream()
                    .filter(x -> x.getIdReporte() == datos.idProductoA)
                    .findFirst().orElseThrow();

            assertEquals(NOMBRE_A, r.getCategoria());
            assertEquals(CATEGORIA, r.getPeriodo());
            assertEquals(STOCK_A, r.getCantidadVentas());
        } finally {
            datos.limpiar();
        }
    }

    @Test
    void productoVendidoPorIdEstatico_productoConVentas_devuelveSusTotales() throws SQLException {
        // RF-18
        DatosPruebaReporte datos = new DatosPruebaReporte();
        datos.insertar();
        try {
            ReporteModel r = ReporteModel.productoVendidoPorId(datos.idProductoA);

            assertNotNull(r);
            assertEquals(VENDIDO_A, r.getCantidadVentas());
            assertEquals(TOTAL_A, r.getTotalVentas(), DELTA);
        } finally {
            datos.limpiar();
        }
    }

    @Test
    void productoVendidoPorIdEstatico_idQueNoExiste_devuelveNull() {
        // RF-18: caso de error
        assertNull(ReporteModel.productoVendidoPorId(ID_INEXISTENTE));
    }

    @Test
    void productosVendidosPorCategoriaEstatico_categoriaDePrueba_devuelveLosDosVendidos() throws SQLException {
        // RF-18
        DatosPruebaReporte datos = new DatosPruebaReporte();
        datos.insertar();
        try {
            assertEquals(2, ReporteModel.productosVendidosPorCategoria(CATEGORIA).size());
        } finally {
            datos.limpiar();
        }
    }

    @Test
    void productosVendidosPorCategoriaEstatico_categoriaQueNoExiste_devuelveListaVacia() {
        // RF-18: caso de error
        assertTrue(ReporteModel.productosVendidosPorCategoria("CategoriaQueNoExiste31").isEmpty());
    }
}

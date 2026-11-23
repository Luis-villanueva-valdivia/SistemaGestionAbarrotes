package com.tienda.reporte;

import static com.tienda.reporte.DatosPruebaReporte.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * Pruebas de caracterización de {@link ReporteDAO} contra la base de pruebas
 * (ISS-22). Registran lo que el DAO hace hoy.
 */
class ReporteDAOTest {

    private static final double DELTA = 0.0001;

    private DatosPruebaReporte datos;
    private ReporteDAO dao;

    @BeforeEach
    void prepararDatos() throws SQLException {
        datos = new DatosPruebaReporte();
        datos.insertar();
        dao = new ReporteDAO();
    }

    @AfterEach
    void borrarDatos() throws SQLException {
        datos.limpiar();
    }

    // ---------- reporteVentas(String) ----------

    @Test
    void reporteVentas_diario_agrupaLasVentasDelMismoDia() {
        // RF-16
        ReporteModel dia = buscarPeriodo(dao.reporteVentas("diario"), DIA);

        assertEquals(VENTAS_DEL_DIA, dia.getCantidadVentas());
        assertEquals(TOTAL_DEL_DIA, dia.getTotalVentas(), DELTA);
        assertEquals("dia", dia.getCategoria());
    }

    @Test
    void reporteVentas_semanal_agrupaPorAnioYNumeroDeSemana() {
        // RF-16
        ReporteModel semana = buscarPeriodo(dao.reporteVentas("semanal"), SEMANA);

        assertEquals(VENTAS_DEL_DIA, semana.getCantidadVentas());
        assertEquals(TOTAL_DEL_DIA, semana.getTotalVentas(), DELTA);
        assertEquals("semana", semana.getCategoria());
    }

    @Test
    void reporteVentas_mensual_agrupaPorAnioYMes() {
        // RF-16
        ReporteModel mes = buscarPeriodo(dao.reporteVentas("mensual"), MES);

        assertEquals(TOTAL_DEL_DIA, mes.getTotalVentas(), DELTA);
        assertEquals("mes", mes.getCategoria());
    }

    @Test
    void reporteVentas_anual_agrupaPorAnio() {
        // RF-16
        ReporteModel anio = buscarPeriodo(dao.reporteVentas("anual"), ANIO);

        assertEquals(TOTAL_DEL_DIA, anio.getTotalVentas(), DELTA);
        assertEquals("año", anio.getCategoria());
    }

    @Test
    void reporteVentas_tipoEnMayusculas_seTrataIgualQueEnMinusculas() {
        // RF-16: el tipo se convierte a minúsculas antes del switch
        ReporteModel semana = buscarPeriodo(dao.reporteVentas("SEMANAL"), SEMANA);

        assertEquals("semana", semana.getCategoria());
    }

    @Test
    void reporteVentas_tipoDesconocido_usaElReporteDiario() {
        // RF-16: caso límite, cualquier texto no reconocido cae en "diario"
        ReporteModel dia = buscarPeriodo(dao.reporteVentas("quincenal"), DIA);

        assertEquals("dia", dia.getCategoria());
        assertEquals(VENTAS_DEL_DIA, dia.getCantidadVentas());
    }

    @Test
    void reporteVentas_tipoVacio_usaElReporteDiario() {
        // RF-16: caso límite
        ReporteModel dia = buscarPeriodo(dao.reporteVentas(""), DIA);

        assertEquals("dia", dia.getCategoria());
    }

    @Test
    void reporteVentas_tipoNulo_lanzaNullPointerException() {
        // RF-16: caso de error. B-10 (#50): no se valida el tipo; tipo.toLowerCase() falla con null
        assertThrows(NullPointerException.class, () -> dao.reporteVentas(null));
    }

    @Test
    void reporteVentas_diario_ordenaLosPeriodosDelMasRecienteAlMasAntiguo() {
        // RF-16
        List<ReporteModel> lista = dao.reporteVentas("diario");

        for (int i = 1; i < lista.size(); i++) {
            String anterior = lista.get(i - 1).getPeriodo();
            String actual = lista.get(i).getPeriodo();
            assertTrue(anterior.compareTo(actual) >= 0, "Orden descendente: " + anterior + " antes de " + actual);
        }
    }

    // ---------- productoVendidoPorId(int) ----------

    @Test
    void productoVendidoPorId_productoConVentas_sumaPiezasEImporte() {
        // RF-18
        ReporteModel r = dao.productoVendidoPorId(datos.idProductoA);

        assertNotNull(r);
        assertEquals(datos.idProductoA, r.getIdReporte());
        assertEquals(VENDIDO_A, r.getCantidadVentas());
        assertEquals(TOTAL_A, r.getTotalVentas(), DELTA);
    }

    @Test
    void productoVendidoPorId_guardaElNombreEnPeriodoYLaCategoriaEnCategoria() {
        // RF-18: el modelo se reutiliza; el nombre del producto va en "periodo" (ver MOV-06)
        ReporteModel r = dao.productoVendidoPorId(datos.idProductoA);

        assertEquals(NOMBRE_A, r.getPeriodo());
        assertEquals(CATEGORIA, r.getCategoria());
    }

    @Test
    void productoVendidoPorId_productoSinVentas_devuelveNull() {
        // RF-18: caso límite
        assertNull(dao.productoVendidoPorId(datos.idProductoC));
    }

    @Test
    void productoVendidoPorId_idQueNoExiste_devuelveNull() {
        // RF-18: caso de error
        assertNull(dao.productoVendidoPorId(ID_INEXISTENTE));
    }

    @Test
    void productoVendidoPorId_idNegativo_devuelveNull() {
        // RF-18: caso de error
        assertNull(dao.productoVendidoPorId(-1));
    }

    // ---------- productosVendidosPorCategoria(String) ----------

    @Test
    void productosVendidosPorCategoria_categoriaExacta_devuelveSoloLosVendidosOrdenadosPorPiezas() {
        // RF-18: C no tiene ventas y no aparece; A (4 piezas) va antes que B (1 pieza)
        List<ReporteModel> lista = dao.productosVendidosPorCategoria(CATEGORIA);

        assertEquals(2, lista.size());
        assertEquals(NOMBRE_A, lista.get(0).getPeriodo());
        assertEquals(VENDIDO_A, lista.get(0).getCantidadVentas());
        assertEquals(TOTAL_A, lista.get(0).getTotalVentas(), DELTA);
        assertEquals(NOMBRE_B, lista.get(1).getPeriodo());
        assertEquals(VENDIDO_B, lista.get(1).getCantidadVentas());
        assertEquals(TOTAL_B, lista.get(1).getTotalVentas(), DELTA);
    }

    @Test
    void productosVendidosPorCategoria_textoParcialEnMinusculas_tambienEncuentra() {
        // RF-18: usa LIKE '%texto%', que en SQLite no distingue mayúsculas
        List<ReporteModel> lista = dao.productosVendidosPorCategoria("pruebareporte31");

        assertEquals(2, lista.size());
    }

    @Test
    void productosVendidosPorCategoria_textoVacio_devuelveLosProductosVendidosDeTodasLasCategorias() {
        // RF-18: caso límite, '%%' coincide con todo
        List<ReporteModel> lista = dao.productosVendidosPorCategoria("");

        assertTrue(lista.stream().anyMatch(r -> NOMBRE_A.equals(r.getPeriodo())));
        assertTrue(lista.stream().anyMatch(r -> NOMBRE_B.equals(r.getPeriodo())));
    }

    @Test
    void productosVendidosPorCategoria_categoriaQueNoExiste_devuelveListaVacia() {
        // RF-18: caso de error
        List<ReporteModel> lista = dao.productosVendidosPorCategoria("CategoriaQueNoExiste31");

        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }

    // ---------- reporteInventario() ----------

    @Test
    void reporteInventario_incluyeTodosLosProductosConSuStock() {
        // RF-17: también los que no tienen ventas y los que tienen stock 0
        List<ReporteModel> lista = dao.reporteInventario();

        assertEquals(STOCK_A, buscarNombre(lista, NOMBRE_A).getCantidadVentas());
        assertEquals(STOCK_B, buscarNombre(lista, NOMBRE_B).getCantidadVentas());
        assertEquals(STOCK_C, buscarNombre(lista, NOMBRE_C).getCantidadVentas());
    }

    @Test
    void reporteInventario_guardaElNombreEnPeriodoYNoLlenaElTotal() {
        // RF-17: el stock va en "cantidadVentas" y el nombre en "periodo" (ver MOV-06)
        ReporteModel r = buscarNombre(dao.reporteInventario(), NOMBRE_A);

        assertEquals(datos.idProductoA, r.getIdReporte());
        assertEquals(CATEGORIA, r.getCategoria());
        assertEquals(0.0, r.getTotalVentas(), DELTA);
    }

    @Test
    void reporteInventario_ordenaPorNombre() {
        // RF-17
        List<ReporteModel> lista = dao.reporteInventario();
        int posA = posicion(lista, NOMBRE_A);
        int posB = posicion(lista, NOMBRE_B);
        int posC = posicion(lista, NOMBRE_C);

        assertTrue(posA < posB && posB < posC);
    }

    // ---------- auxiliares ----------

    private static ReporteModel buscarPeriodo(List<ReporteModel> lista, String periodo) {
        assertFalse(lista.isEmpty(), "El reporte no debe venir vacío");
        return lista.stream()
                .filter(r -> periodo.equals(r.getPeriodo()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el periodo " + periodo));
    }

    private static ReporteModel buscarNombre(List<ReporteModel> lista, String nombre) {
        return lista.get(posicion(lista, nombre));
    }

    private static int posicion(List<ReporteModel> lista, String nombre) {
        for (int i = 0; i < lista.size(); i++) {
            if (nombre.equals(lista.get(i).getPeriodo())) {
                return i;
            }
        }
        throw new AssertionError("No se encontró el producto " + nombre);
    }
}

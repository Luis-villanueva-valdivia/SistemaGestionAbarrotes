package com.tienda.inventario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.producto.BuscarProductoService;
import com.tienda.producto.IProductoDAO;
import com.tienda.producto.ProductoModel;

/**
 * Pruebas de caracterización de {@link InventarioView} (ISS-30).
 *
 * <p>Simulan la entrada con {@code System.setIn}, capturan {@code System.out}
 * y usan un {@link MovimientoController} con DAO falsos: no tocan la base.</p>
 */
class InventarioViewTest {

    private InventarioView vista;
    private ProductoDAOFalso productoDAO;
    private MovimientoDAOFalso movimientoDAO;
    private MovimientoController controller;

    @BeforeEach
    void preparar() {
        vista = new InventarioView();
        productoDAO = new ProductoDAOFalso();
        movimientoDAO = new MovimientoDAOFalso();
        controller = new MovimientoController(productoDAO, movimientoDAO);
    }

    // ===================== mostrarProductos =====================

    @Test
    void mostrarProductos_listaVacia_muestraAviso() {
        // RF-11
        String salida = capturarSalida(() -> vista.mostrarProductos(new ArrayList<>()));

        assertEquals("No hay productos registrados en el inventario." + System.lineSeparator(), salida);
    }

    @Test
    void mostrarProductos_listaNula_muestraAviso() {
        // RF-11
        String salida = capturarSalida(() -> vista.mostrarProductos(null));

        assertTrue(salida.contains("No hay productos registrados en el inventario."));
    }

    @Test
    void mostrarProductos_conProductos_muestraTablaConEncabezadoYFilas() {
        // RF-11: la consola muestra el vencimiento (la gráfica no)
        ProductoModel p = producto(3, "Atun", 21.5, 12);
        p.setCodigo("AT001");
        p.setVencimiento("2027-05-01");

        String salida = capturarSalida(() -> vista.mostrarProductos(List.of(p)));

        assertTrue(salida.contains("================= INVENTARIO ================="));
        assertTrue(salida.contains(String.format("%-5s %-20s %-10s %-8s %-12s %-15s",
                "ID", "Nombre", "Precio", "Stock", "Código", "Vencimiento")));
        assertTrue(salida.contains(String.format("%-5d %-20s $%-9.2f %-8d %-12s %-15s",
                3, "Atun", 21.5, 12, "AT001", "2027-05-01")));
    }

    @Test
    void mostrarProductos_sinCodigoNiVencimiento_muestraGuion() {
        // RF-11
        ProductoModel p = producto(4, "Sal", 8.0, 2);

        String salida = capturarSalida(() -> vista.mostrarProductos(List.of(p)));

        assertTrue(salida.contains(String.format("%-5d %-20s $%-9.2f %-8d %-12s %-15s",
                4, "Sal", 8.0, 2, "-", "-")));
    }

    // ===================== pedirDatosProducto =====================

    @Test
    void pedirDatosProducto_entradaValida_llenaTodosLosCampos() {
        // RF-01: en consola sí se capturan descripción y vencimiento
        String entrada = lineas("  Galletas  ", "GAL01", "15.5", "30", "2027-02-28",
                "Galletas de vainilla", "Botanas", "Gamesa");

        ProductoModel p = conEntrada(entrada, vista::pedirDatosProducto);

        assertNotNull(p);
        assertEquals("Galletas", p.getNombre());
        assertEquals("GAL01", p.getCodigo());
        assertEquals(15.5, p.getPrecio(), 0.0001);
        assertEquals(30, p.getCantidad());
        assertEquals("2027-02-28", p.getVencimiento());
        assertEquals("Galletas de vainilla", p.getDescripcion());
        assertEquals("Botanas", p.getCategoria());
        assertEquals("Gamesa", p.getProveedor());
        assertEquals(0, p.getId());
    }

    @Test
    void pedirDatosProducto_vencimientoVacio_loDejaNulo() {
        // RF-01
        ProductoModel p = conEntrada(lineas("Agua", "AG01", "9", "5", "", "", "", ""),
                vista::pedirDatosProducto);

        assertNull(p.getVencimiento());
        assertEquals("", p.getDescripcion());
        assertEquals("", p.getCategoria());
        assertEquals("", p.getProveedor());
    }

    @Test
    void pedirDatosProducto_precioNoNumerico_cancelaYDevuelveNulo() {
        // RF-01, RNF-02: si el precio no es número, la operación se cancela
        String[] salida = new String[1];
        ProductoModel p = conEntrada(lineas("Agua", "AG01", "abc"), sc -> {
            ProductoModel[] r = new ProductoModel[1];
            salida[0] = capturarSalida(() -> r[0] = vista.pedirDatosProducto(sc));
            return r[0];
        });

        assertNull(p);
        assertTrue(salida[0].contains("Entrada numérica inválida. Operación cancelada."));
    }

    @Test
    void pedirDatosProducto_cantidadDecimal_cancelaYDevuelveNulo() {
        // RF-01
        assertNull(conEntrada(lineas("Agua", "AG01", "9", "2.5"), vista::pedirDatosProducto));
    }

    @Test
    void pedirDatosProducto_precioConComa_cancelaYDevuelveNulo() {
        // RF-01: en consola no se acepta coma decimal
        assertNull(conEntrada(lineas("Agua", "AG01", "9,5"), vista::pedirDatosProducto));
    }

    @Test
    void pedirDatosProducto_valoresNegativos_seAceptan() {
        // RF-01: no hay validación de negativos (ERS 3.2)
        ProductoModel p = conEntrada(lineas("Agua", "AG01", "-1", "-5", "", "", "", ""),
                vista::pedirDatosProducto);

        assertEquals(-1.0, p.getPrecio(), 0.0001);
        assertEquals(-5, p.getCantidad());
    }

    // ===================== pedirIdProducto =====================

    @Test
    void pedirIdProducto_numeroValido_loDevuelve() {
        // RF-01, RF-03
        assertEquals(12, conEntrada(lineas(" 12 "), vista::pedirIdProducto));
    }

    @Test
    void pedirIdProducto_textoNoNumerico_devuelveMenosUno() {
        // RF-01, RF-03
        String[] salida = new String[1];
        int id = conEntrada(lineas("doce"), sc -> {
            int[] r = new int[1];
            salida[0] = capturarSalida(() -> r[0] = vista.pedirIdProducto(sc));
            return r[0];
        });

        assertEquals(-1, id);
        assertTrue(salida[0].contains("ID inválido."));
    }

    @Test
    void pedirIdProducto_ceroYNegativo_losDevuelveSinValidar() {
        // RF-01, RF-03: la validación id > 0 la hace quien llama
        assertEquals(0, conEntrada(lineas("0"), vista::pedirIdProducto));
        assertEquals(-7, conEntrada(lineas("-7"), vista::pedirIdProducto));
    }

    // ===================== Alertas de stock =====================

    @Test
    void mostrarAlertaStock_muestraNombreIdCantidadYUmbral() {
        // RF-04
        ProductoModel p = producto(9, "Huevo", 30.0, 2);

        String salida = capturarSalida(() -> vista.mostrarAlertaStock(p, 5));

        assertTrue(salida.contains("⚠️ ALERTA DE STOCK ⚠️"));
        assertTrue(salida.contains(
                "El producto 'Huevo' (ID: 9) tiene solo 2 unidades disponibles (umbral mínimo: 5)."));
    }

    @Test
    void mostrarResumenAlertasStock_sinProductosBajos_muestraMensajeDeStockSuficiente() {
        // RF-04
        String vacia = capturarSalida(() -> vista.mostrarResumenAlertasStock(List.of(), 5));
        String nula = capturarSalida(() -> vista.mostrarResumenAlertasStock(null, 5));

        assertEquals("✔️ Todos los productos tienen stock suficiente." + System.lineSeparator(), vacia);
        assertEquals(vacia, nula);
    }

    @Test
    void mostrarResumenAlertasStock_conProductosBajos_muestraUnaAlertaPorProducto() {
        // RF-04
        List<ProductoModel> bajos = List.of(producto(1, "Leche", 20.0, 1), producto(2, "Pan", 3.0, 0));

        String salida = capturarSalida(() -> vista.mostrarResumenAlertasStock(bajos, 3));

        assertTrue(salida.contains("========== ALERTAS DE STOCK BAJO =========="));
        assertEquals(2, contarApariciones(salida, "⚠️ ALERTA DE STOCK ⚠️"));
        assertTrue(salida.contains("'Leche' (ID: 1)"));
        assertTrue(salida.contains("'Pan' (ID: 2)"));
    }

    // ===================== Historial de movimientos =====================

    @Test
    void mostrarHistorialMovimientos_sinMovimientos_muestraAviso() {
        // RF-05
        String salida = capturarSalida(() -> vista.mostrarHistorialMovimientos(5, List.of()));
        String nula = capturarSalida(() -> vista.mostrarHistorialMovimientos(5, null));

        assertTrue(salida.contains("===== HISTORIAL DE MOVIMIENTOS — Producto ID: 5 ====="));
        assertTrue(salida.contains("Sin movimientos registrados para este producto."));
        assertEquals(salida, nula);
    }

    @Test
    void mostrarHistorialMovimientos_conMovimientos_muestraUnaFilaPorMovimiento() {
        // RF-05
        MovimientoInventarioModel m = new MovimientoInventarioModel();
        m.setId(40);
        m.setFecha("2026-11-22T10:00");
        m.setTipo("SALIDA");
        m.setCantidad(3);

        String salida = capturarSalida(() -> vista.mostrarHistorialMovimientos(5, List.of(m)));

        assertTrue(salida.contains(String.format("%-5s %-20s %-10s %-8s", "ID", "Fecha", "Tipo", "Cantidad")));
        assertTrue(salida.contains(String.format("%-5d %-20s %-10s %-8d", 40, "2026-11-22T10:00", "SALIDA", 3)));
        assertFalse(salida.contains("Sin movimientos registrados"));
    }

    // ===================== pedirDatosMovimiento =====================

    @Test
    void pedirDatosMovimiento_entradaValida_convierteTipoAMayusculasYPoneFechaActual() {
        // RF-02
        LocalDateTime antes = LocalDateTime.now();

        MovimientoInventarioModel m = conEntrada(lineas("3", " ajuste ", "-2"), vista::pedirDatosMovimiento);

        LocalDateTime despues = LocalDateTime.now();
        assertNotNull(m);
        assertEquals(3, m.getProductoId());
        assertEquals("AJUSTE", m.getTipo());
        assertEquals(-2, m.getCantidad());
        LocalDateTime fecha = LocalDateTime.parse(m.getFecha());
        assertFalse(fecha.isBefore(antes));
        assertFalse(fecha.isAfter(despues));
    }

    @Test
    void pedirDatosMovimiento_tipoInvalido_seAceptaSinValidar() {
        // RF-02: la consola no valida el tipo; el DAO lo trata como ajuste
        MovimientoInventarioModel m = conEntrada(lineas("3", "regalo", "1"), vista::pedirDatosMovimiento);

        assertEquals("REGALO", m.getTipo());
    }

    @Test
    void pedirDatosMovimiento_idNoNumerico_cancelaYDevuelveNulo() {
        // RF-02, RNF-02
        assertNull(conEntrada(lineas("x"), vista::pedirDatosMovimiento));
    }

    @Test
    void pedirDatosMovimiento_cantidadNoNumerica_cancelaYDevuelveNulo() {
        // RF-02, RNF-02
        assertNull(conEntrada(lineas("3", "ENTRADA", "muchos"), vista::pedirDatosMovimiento));
    }

    // ===================== Mensajes =====================

    @Test
    void mostrarMensaje_agregaPrefijoInfo() {
        // RF-01, RF-02, RNF-02: mensajes al usuario de la consola de inventario
        assertEquals("[INFO] Listo" + System.lineSeparator(), capturarSalida(() -> vista.mostrarMensaje("Listo")));
    }

    @Test
    void mostrarError_agregaPrefijoError() {
        // RF-01, RF-02, RNF-02
        assertEquals("[ERROR] Falla" + System.lineSeparator(), capturarSalida(() -> vista.mostrarError("Falla")));
    }

    // ===================== mostrarMenu =====================

    @Test
    void mostrarMenu_opcionCero_muestraMenuYRegresa() {
        // RF-01, RF-02, RF-03, RF-05
        String salida = ejecutarMenu(lineas("0"));

        assertTrue(salida.contains("--- Inventario ---"));
        assertTrue(salida.contains("1) Listar productos"));
        assertTrue(salida.contains("6) Ver historial de movimientos por producto"));
        assertTrue(salida.contains("0) Volver"));
        assertEquals(1, contarApariciones(salida, "--- Inventario ---"));
    }

    @Test
    void mostrarMenu_opcionInvalida_avisaYVuelveAMostrarElMenu() {
        // RF-01
        String salida = ejecutarMenu(lineas("9", "0"));

        assertTrue(salida.contains("Opción inválida"));
        assertEquals(2, contarApariciones(salida, "--- Inventario ---"));
    }

    @Test
    void mostrarMenu_opcionUno_listaLosProductosDelControlador() {
        // RF-11
        productoDAO.productos.add(producto(1, "Cafe", 50.0, 4));

        String salida = ejecutarMenu(lineas("1", "0"));

        assertTrue(salida.contains("================= INVENTARIO ================="));
        assertTrue(salida.contains("Cafe"));
    }

    @Test
    void mostrarMenu_opcionDos_agregaElProductoCapturado() {
        // RF-01
        ejecutarMenu(lineas("2", "Te", "TE01", "12", "6", "", "Te verde", "Bebidas", "Prov", "0"));

        assertEquals(1, productoDAO.agregados.size());
        assertEquals("Te", productoDAO.agregados.get(0).getNombre());
        assertEquals(6, productoDAO.agregados.get(0).getCantidad());
    }

    @Test
    void mostrarMenu_opcionDosConPrecioInvalido_noAgregaNada() {
        // RF-01
        ejecutarMenu(lineas("2", "Te", "TE01", "caro", "0"));

        assertTrue(productoDAO.agregados.isEmpty());
    }

    @Test
    void mostrarMenu_opcionTres_cambiaNombrePrecioYCantidad() {
        // RF-01, RF-02: en consola, editar cambia la cantidad sin generar movimiento
        ProductoModel existente = producto(5, "Viejo", 10.0, 3);
        productoDAO.productos.add(existente);

        String salida = ejecutarMenu(lineas("3", "5", "Nuevo", "11.5", "20", "0"));

        assertTrue(salida.contains("Ingrese nuevos datos (dejar vacío para mantener)"));
        assertTrue(salida.contains("Nombre (actual: Viejo): "));
        assertEquals(1, productoDAO.actualizados.size());
        assertSame(existente, productoDAO.actualizados.get(0));
        assertEquals("Nuevo", existente.getNombre());
        assertEquals(11.5, existente.getPrecio(), 0.0001);
        assertEquals(20, existente.getCantidad());
        assertTrue(movimientoDAO.registrados.isEmpty());
    }

    @Test
    void mostrarMenu_opcionTresConCamposVacios_conservaLosValores() {
        // RF-01
        ProductoModel existente = producto(5, "Igual", 10.0, 3);
        productoDAO.productos.add(existente);

        ejecutarMenu(lineas("3", "5", "", "", "", "0"));

        assertEquals("Igual", existente.getNombre());
        assertEquals(10.0, existente.getPrecio(), 0.0001);
        assertEquals(3, existente.getCantidad());
        assertEquals(1, productoDAO.actualizados.size());
    }

    @Test
    void mostrarMenu_opcionTresConNumerosInvalidos_conservaPrecioYCantidadSinAvisar() {
        // RF-01: los catch vacíos ignoran el error y se guarda igual
        ProductoModel existente = producto(5, "Igual", 10.0, 3);
        productoDAO.productos.add(existente);

        String salida = ejecutarMenu(lineas("3", "5", "", "diez", "tres", "0"));

        assertEquals(10.0, existente.getPrecio(), 0.0001);
        assertEquals(3, existente.getCantidad());
        assertEquals(1, productoDAO.actualizados.size());
        assertFalse(salida.contains("inválid"));
    }

    @Test
    void mostrarMenu_opcionTresConIdInexistente_avisaYNoActualiza() {
        // RF-01
        String salida = ejecutarMenu(lineas("3", "99", "0"));

        assertTrue(salida.contains("Producto no encontrado."));
        assertTrue(productoDAO.actualizados.isEmpty());
    }

    @Test
    void mostrarMenu_opcionTresConIdCero_noBuscaNiActualiza() {
        // RF-01
        String salida = ejecutarMenu(lineas("3", "0", "0"));

        assertFalse(salida.contains("Producto no encontrado."));
        assertTrue(productoDAO.actualizados.isEmpty());
    }

    @Test
    void mostrarMenu_opcionCuatro_eliminaSinConfirmar() {
        // RF-03: se elimina al ingresar el ID, sin confirmación
        ejecutarMenu(lineas("4", "8", "0"));

        assertEquals(List.of(8), productoDAO.eliminados);
    }

    @Test
    void mostrarMenu_opcionCuatroConIdInvalido_noElimina() {
        // RF-03
        ejecutarMenu(lineas("4", "-3", "4", "abc", "0"));

        assertTrue(productoDAO.eliminados.isEmpty());
    }

    @Test
    void mostrarMenu_opcionCinco_registraElMovimientoCapturado() {
        // RF-02
        ejecutarMenu(lineas("5", "2", "entrada", "7", "0"));

        assertEquals(1, movimientoDAO.registrados.size());
        MovimientoInventarioModel m = movimientoDAO.registrados.get(0);
        assertEquals(2, m.getProductoId());
        assertEquals("ENTRADA", m.getTipo());
        assertEquals(7, m.getCantidad());
    }

    @Test
    void mostrarMenu_opcionCincoConCantidadInvalida_noRegistra() {
        // RF-02
        ejecutarMenu(lineas("5", "2", "ENTRADA", "siete", "0"));

        assertTrue(movimientoDAO.registrados.isEmpty());
    }

    @Test
    void mostrarMenu_opcionSeis_muestraElHistorialDelProducto() {
        // RF-05
        MovimientoInventarioModel m = new MovimientoInventarioModel();
        m.setId(1);
        m.setFecha("2026-11-22T10:00");
        m.setTipo("ENTRADA");
        m.setCantidad(4);
        movimientoDAO.historial.add(m);

        String salida = ejecutarMenu(lineas("6", "2", "0"));

        assertEquals(List.of(2), movimientoDAO.idsConsultados);
        assertTrue(salida.contains("===== HISTORIAL DE MOVIMIENTOS — Producto ID: 2 ====="));
        assertTrue(salida.contains("2026-11-22T10:00"));
    }

    // ===================== Utilidades =====================

    private String ejecutarMenu(String entrada) {
        return conEntradaYSalida(entrada, sc -> vista.mostrarMenu(sc, controller));
    }

    private static String lineas(String... valores) {
        return String.join("\n", valores) + "\n";
    }

    /** Ejecuta la acción con {@code System.in} simulado y devuelve su resultado. */
    private static <T> T conEntrada(String entrada, java.util.function.Function<Scanner, T> accion) {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            Object[] resultado = new Object[1];
            capturarSalida(() -> resultado[0] = accion.apply(scanner));
            @SuppressWarnings("unchecked")
            T valor = (T) resultado[0];
            return valor;
        } finally {
            System.setIn(original);
        }
    }

    /** Ejecuta la acción con {@code System.in} simulado y devuelve lo impreso. */
    private static String conEntradaYSalida(String entrada, Consumer<Scanner> accion) {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            return capturarSalida(() -> accion.accept(scanner));
        } finally {
            System.setIn(original);
        }
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

    private static int contarApariciones(String texto, String buscado) {
        int cuenta = 0;
        int desde = texto.indexOf(buscado);
        while (desde >= 0) {
            cuenta++;
            desde = texto.indexOf(buscado, desde + buscado.length());
        }
        return cuenta;
    }

    private static ProductoModel producto(int id, String nombre, double precio, int cantidad) {
        ProductoModel p = new ProductoModel();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setCantidad(cantidad);
        return p;
    }

    /** DAO de productos en memoria que registra las llamadas recibidas. */
    private static class ProductoDAOFalso implements IProductoDAO {
        final List<ProductoModel> productos = new ArrayList<>();
        final List<ProductoModel> agregados = new ArrayList<>();
        final List<ProductoModel> actualizados = new ArrayList<>();
        final List<Integer> eliminados = new ArrayList<>();

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
            return new BuscarProductoService(null);
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

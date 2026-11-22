package com.tienda;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import com.tienda.producto.ProductoModel;
import com.tienda.reporte.ReporteModel;
import com.tienda.reporte.ReporteController;
import com.tienda.reporte.ReporteView;
import com.tienda.venta.IVentaDAO;
import com.tienda.venta.VentaController;
import com.tienda.venta.VentaDAO;
import com.tienda.venta.VentaModel;
import com.tienda.util.ConexionDB;

/**
 * Ejemplos de referencia de los cuatro tipos de pruebas
 * preparados para ISS-22.
 *
 * Estas pruebas muestran los patrones que deben seguir los responsables
 * de cada módulo. No sustituyen las pruebas específicas de cada módulo.
 */
class EjemplosBasePruebasTest {

    /**
     * Ejemplo de prueba de controlador con un DAO falso, sin acceder a SQLite.
     */
    @Test
    void calcularTotal_conDaoFalso_sumaPrecioPorCantidad() {
        VentaController controller = new VentaController(new VentaDAOFalso());
        VentaModel venta = new VentaModel();
        venta.setProductos(new ProductoModel[] {
                crearProducto(10.0, 2),
                crearProducto(7.5, 4)
        });

        assertEquals(50.0, controller.calcularTotal(venta), 0.0001);
    }

    /**
     * Ejemplo de prueba de DAO. Verifica que la conexión y la consulta usan
     * target/test-run/data/tienda.db y que la base original no cambia.
     *
     * @throws Exception si no se puede leer alguno de los archivos de SQLite
     */
    @Test
    void listarTodos_conBaseAislada_consultaCopiaYNoModificaOriginal() throws Exception {
        Path copiaPrueba = Path.of("data", "tienda.db").toAbsolutePath().normalize();
        Path baseOriginal = Path.of("..", "..", "data", "tienda.db")
                .toAbsolutePath().normalize();

        assertTrue(Files.isRegularFile(copiaPrueba),
                "Debe existir la copia data/tienda.db dentro de target/test-run");
        assertTrue(Files.isRegularFile(baseOriginal),
                "Debe existir la base original en sistema-caja/data");
        assertNotEquals(baseOriginal.toRealPath(), copiaPrueba.toRealPath(),
                "La conexión de pruebas no debe apuntar al archivo original");

        byte[] contenidoOriginalAntes = Files.readAllBytes(baseOriginal);

        assertNotNull(ConexionDB.getInstancia().getConexion(),
                "Debe abrirse la conexión contra la copia de pruebas");
        List<VentaModel> ventas = new VentaDAO().listarTodos();
        assertNotNull(ventas, "El DAO debe devolver una lista, aunque no existan ventas");

        assertArrayEquals(contenidoOriginalAntes, Files.readAllBytes(baseOriginal),
                "La base original no debe modificarse durante las pruebas");
    }

    /**
     * Ejemplo de captura de System.out para un método que imprime un ticket.
     */
    @Test
    void generarTicket_conVentaValida_imprimeDatosDeLaVenta() {
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salidaCapturada = new ByteArrayOutputStream();

        try (PrintStream salidaTemporal = new PrintStream(
                salidaCapturada, true, StandardCharsets.UTF_8)) {
            System.setOut(salidaTemporal);

            VentaModel venta = new VentaModel();
            venta.setCliente("Cliente de prueba");
            venta.setMetodoPago("Efectivo");
            venta.setProductos(new ProductoModel[] { crearProducto(15.0, 2) });

            new VentaController(new VentaDAOFalso()).generarTicket(venta);
        } finally {
            System.setOut(salidaOriginal);
        }

        String resultado = salidaCapturada.toString(StandardCharsets.UTF_8);
        assertTrue(resultado.contains("===== TICKET ====="));
        assertTrue(resultado.contains("Cliente: Cliente de prueba"));
        assertTrue(resultado.contains("Método de pago: Efectivo"));
    }

    /**
     * Ejemplo de prueba de una vista de consola simulando System.in.
     */
    @Test
    void mostrarMenu_conEntradaCero_muestraMenuYRegresa() {
        InputStream entradaOriginal = System.in;
        PrintStream salidaOriginal = System.out;
        ByteArrayOutputStream salidaCapturada = new ByteArrayOutputStream();
        byte[] entradaSimulada = "0\n".getBytes(StandardCharsets.UTF_8);

        try (PrintStream salidaTemporal = new PrintStream(
                salidaCapturada, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(entradaSimulada));
            System.setOut(salidaTemporal);

            try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
                ReporteView vista = new ReporteView();
                vista.mostrarMenu(scanner, new ReporteController());
            }
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(salidaOriginal);
        }

        String resultado = salidaCapturada.toString(StandardCharsets.UTF_8);
        assertTrue(resultado.contains("--- Reportes ---"));
        assertTrue(resultado.contains("0) Volver"));
    }

    private static ProductoModel crearProducto(double precio, int cantidad) {
        ProductoModel producto = new ProductoModel();
        producto.setPrecio(precio);
        producto.setCantidad(cantidad);
        producto.setNombre("Producto de prueba");
        return producto;
    }

    /** DAO falso: los métodos no se ejecutan porque estos ejemplos no registran ventas. */
    private static class VentaDAOFalso implements IVentaDAO {
        @Override
        public void registrar(VentaModel venta) {
            // Sin operación para la prueba.
        }

        @Override
        public void historial() {
            // Sin operación para la prueba.
        }

        @Override
        public ReporteModel generarReporte(String periodo) {
            return null;
        }

        @Override
        public void historialPorCliente(int clienteId) {
            // Sin operación para la prueba.
        }

        @Override
        public void registrarVenta(VentaModel venta) {
            // Sin operación para la prueba.
        }
    }
}

package com.tienda.venta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.producto.ProductoModel;
import com.tienda.util.ConexionDB;

/**
 * Pruebas de caracterización de {@link VentaView} (ISS-28).
 *
 * <p>Se simula lo que escribe el cajero con un {@link Scanner} sobre un texto
 * y se captura {@code System.out}. {@code pedirDatosVenta} solo lee de la base
 * de pruebas (productos y clientes); no guarda nada.</p>
 *
 * <p>Datos de la base de pruebas que se usan: producto 1 «Coca Cola» a $10.00,
 * producto 2 «Pepsi» a $10.50 y el cliente con teléfono 5512345678 («Juan Pérez»).</p>
 */
class VentaViewTest {

    private static final double DELTA = 0.0001;
    private static final String TEL_JUAN = "5512345678";
    private static Locale localeOriginal;

    private VentaView vista;
    private PrintStream salidaOriginal;
    private ByteArrayOutputStream salida;

    @BeforeAll
    static void prepararTodo() {
        localeOriginal = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("es-MX"));
        // Abre la conexión antes de capturar, para que "Conexión establecida" no salga en las pruebas
        ConexionDB.getInstancia().getConexion();
    }

    @AfterAll
    static void restaurarTodo() {
        Locale.setDefault(localeOriginal);
    }

    @BeforeEach
    void preparar() {
        vista = new VentaView();
        salidaOriginal = System.out;
        salida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restaurar() {
        System.setOut(salidaOriginal);
    }

    private String salida() {
        return salida.toString(StandardCharsets.UTF_8);
    }

    /** Une las líneas que escribe el cajero, cada una terminada en salto de línea. */
    private static Scanner entrada(String... lineas) {
        String texto = String.join("\n", lineas) + "\n";
        return new Scanner(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    // ===================== mostrarVenta =====================

    @Test
    void mostrarVenta_null_imprimeVentaNoEncontrada() {
        // RF-08
        vista.mostrarVenta(null);

        assertEquals("Venta no encontrada." + System.lineSeparator(), salida());
    }

    @Test
    void mostrarVenta_imprimeElTicketDeConsola() {
        // RF-08: formato de la figura C.2 de la ERS
        VentaModel v = new VentaModel();
        v.setId(21);
        Date fecha = new Date(0);
        v.setFecha(fecha);
        v.setCliente("");
        v.setProductos(new ProductoModel[] { producto(1, "Coca Cola", 10.0, 2) });
        v.setTotal(20.0);

        vista.mostrarVenta(v);

        String n = System.lineSeparator();
        assertEquals("===== TICKET =====" + n
                + "ID: 21" + n
                + "Fecha: " + fecha + n
                + "Cliente: " + n
                + "Productos:" + n
                + " - Coca Cola x2  @ $10.00  = $20.00" + n
                + "Total: $20.00" + n
                + "==================" + n, salida());
    }

    @Test
    void mostrarVenta_usaElTotalGuardadoSinRecalcular() {
        // RF-08: a diferencia de generarTicket, aquí se imprime v.getTotal()
        VentaModel v = new VentaModel();
        v.setProductos(new ProductoModel[] { producto(1, "Coca Cola", 10.0, 2) });
        v.setTotal(5.0);

        vista.mostrarVenta(v);

        assertTrue(salida().contains("Total: $5.00"));
    }

    @Test
    void mostrarVenta_sinProductosNiDatos_imprimeValoresPorDefecto() {
        // RF-08
        vista.mostrarVenta(new VentaModel());

        String n = System.lineSeparator();
        assertTrue(salida().contains("ID: 0" + n + "Fecha: null" + n + "Cliente: null" + n
                + "Productos:" + n + "Total: $0.00" + n));
    }

    // ===================== mostrarHistorial =====================

    @Test
    void mostrarHistorial_listaNull_imprimeAviso() {
        // RF-10
        vista.mostrarHistorial(null);

        assertEquals("No hay ventas registradas." + System.lineSeparator(), salida());
    }

    @Test
    void mostrarHistorial_listaVacia_imprimeAviso() {
        // RF-10
        vista.mostrarHistorial(new ArrayList<>());

        assertEquals("No hay ventas registradas." + System.lineSeparator(), salida());
    }

    @Test
    void mostrarHistorial_imprimeEncabezadoUnaLineaPorVentaYPie() {
        // RF-10
        VentaModel a = resumen(5, "Juan Pérez", 30.5);
        VentaModel b = resumen(4, "", 10.0);

        vista.mostrarHistorial(List.of(a, b));

        String n = System.lineSeparator();
        assertEquals("===== HISTORIAL DE VENTAS =====" + n
                + "ID:5  Fecha:" + a.getFecha() + "  Cliente:Juan Pérez  Total:$30.50" + n
                + "ID:4  Fecha:" + b.getFecha() + "  Cliente:  Total:$10.00" + n
                + "===============================" + n, salida());
    }

    // ===================== pedirDatosVenta: cliente =====================

    @Test
    void pedirDatosVenta_sinTelefonoEfectivo_armaLaVentaCompleta() {
        // RF-06, RF-07: venta sin cliente; el total se calcula con los productos
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "2", "2", "1", "", "1", "50"));

        assertNotNull(v);
        assertEquals("", v.getCliente());
        assertNull(v.getClienteId());
        assertEquals(2, v.getProductos().length);
        assertEquals("1|Coca Cola|10.0|2", texto(v.getProductos()[0]));
        assertEquals("2|Pepsi|10.5|1", texto(v.getProductos()[1]));
        assertEquals(30.5, v.getTotal(), DELTA);
        assertNotNull(v.getFecha());
        assertEquals("Efectivo", v.getMetodoPago());
        assertEquals(50.0, v.getMontoPagado(), DELTA);
        assertEquals(19.5, v.getCambio(), DELTA);
        assertNull(v.getNumeroTarjeta());
    }

    @Test
    void pedirDatosVenta_muestraLosMensajesEnOrden() {
        // RF-06
        vista.pedirDatosVenta(entrada("", "1", "1", "", "1", "10"));

        String n = System.lineSeparator();
        assertEquals("Teléfono cliente (vacío para venta sin cliente): "
                + "ID producto (vacío para terminar): "
                + "Cantidad: "
                + "ID producto (vacío para terminar): "
                + "Seleccione método de pago: 1) Efectivo 2) Tarjeta" + n
                + "Opción: "
                + "Monto recibido: "
                + "Pago en efectivo procesado: $10.0" + n
                + "   Cambio: $0.0" + n, salida());
    }

    @Test
    void pedirDatosVenta_telefonoRegistrado_verificaAlClientePeroNoGuardaSuId() {
        // RF-06
        // B-05: en consola el cliente se verifica por teléfono, pero no se asigna
        // clienteId; la venta se guarda sin cliente asociado
        VentaModel v = vista.pedirDatosVenta(entrada(TEL_JUAN, "1", "1", "", "2", "4111"));

        assertTrue(salida().contains("Cliente verificado: Juan Pérez"));
        assertEquals("Juan Pérez", v.getCliente());
        assertNull(v.getClienteId());
    }

    @Test
    void pedirDatosVenta_telefonoConEspacios_seRecortaAntesDeBuscar() {
        // RF-06
        VentaModel v = vista.pedirDatosVenta(entrada("  " + TEL_JUAN + "  ", "1", "1", "", "2", "4111"));

        assertEquals("Juan Pérez", v.getCliente());
    }

    @Test
    void pedirDatosVenta_telefonoNoRegistradoYNoContinuar_devuelveNull() {
        // RF-06: cualquier respuesta distinta de "s" cancela la venta
        VentaModel v = vista.pedirDatosVenta(entrada("0000000000", "n"));

        assertNull(v);
        assertTrue(salida().contains("No existe un cliente registrado con ese teléfono."));
        assertTrue(salida().contains("Continuar sin cliente? (s/n): "));
    }

    @Test
    void pedirDatosVenta_telefonoNoRegistradoYRespuestaVacia_devuelveNull() {
        // RF-06
        assertNull(vista.pedirDatosVenta(entrada("0000000000", "")));
    }

    @Test
    void pedirDatosVenta_telefonoNoRegistradoYContinuarConMayuscula_sigueSinCliente() {
        // RF-06: la respuesta se pasa a minúsculas, así que "S" también continúa.
        // En consola no se registra un cliente nuevo (en la gráfica sí).
        VentaModel v = vista.pedirDatosVenta(entrada("0000000000", "S", "1", "1", "", "2", "4111"));

        assertNotNull(v);
        assertEquals("", v.getCliente());
        assertNull(v.getClienteId());
    }

    // ===================== pedirDatosVenta: productos =====================

    @Test
    void pedirDatosVenta_sinProductos_avisaYDevuelveNull() {
        // RF-06: no se puede registrar una venta sin productos
        VentaModel v = vista.pedirDatosVenta(entrada("", ""));

        assertNull(v);
        assertTrue(salida().contains("No se puede registrar una venta sin productos."));
        assertFalse(salida().contains("Seleccione método de pago"));
    }

    @Test
    void pedirDatosVenta_idNoNumerico_avisaYVuelveAPedir() {
        // RF-06
        VentaModel v = vista.pedirDatosVenta(entrada("", "abc", "1", "1", "", "2", "4111"));

        assertTrue(salida().contains("ID inválido."));
        assertEquals(1, v.getProductos().length);
    }

    @Test
    void pedirDatosVenta_idQueNoExiste_avisaYVuelveAPedir() {
        // RF-06
        VentaModel v = vista.pedirDatosVenta(entrada("", "99999", "1", "1", "", "2", "4111"));

        assertTrue(salida().contains("Producto no encontrado para ID=99999"));
        assertEquals(1, v.getProductos().length);
    }

    @Test
    void pedirDatosVenta_cantidadNoNumerica_descartaEseProducto() {
        // RF-06: se avisa y se vuelve a pedir el ID, no la cantidad
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "dos", "2", "1", "", "2", "4111"));

        assertTrue(salida().contains("Cantidad inválida."));
        assertEquals(1, v.getProductos().length);
        assertEquals(2, v.getProductos()[0].getId());
    }

    @Test
    void pedirDatosVenta_cantidadCeroONegativa_seAcepta() {
        // RF-06
        // B-04: no se validan cantidades en 0 o negativas; el total queda en -20
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "0", "1", "-2", "", "2", "4111"));

        assertEquals(2, v.getProductos().length);
        assertEquals(0, v.getProductos()[0].getCantidad());
        assertEquals(-2, v.getProductos()[1].getCantidad());
        assertEquals(-20.0, v.getTotal(), DELTA);
    }

    @Test
    void pedirDatosVenta_cantidadMayorAlStock_seAcepta() {
        // RF-06: en la consola no hay validación de stock
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "100000", "", "2", "4111"));

        assertEquals(100000, v.getProductos()[0].getCantidad());
        assertEquals(1000000.0, v.getTotal(), DELTA);
    }

    @Test
    void pedirDatosVenta_mismoProductoDosVeces_quedanDosRenglones() {
        // RF-06: no se agrupan las cantidades del mismo producto
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "1", "1", "3", "", "2", "4111"));

        assertEquals(2, v.getProductos().length);
        assertEquals(40.0, v.getTotal(), DELTA);
    }

    @Test
    void pedirDatosVenta_copiaElProductoConLaCantidadPedida() {
        // RF-06: solo se copian id, nombre y precio; el resto de campos queda vacío
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "3", "", "2", "4111"));

        ProductoModel p = v.getProductos()[0];
        assertEquals(3, p.getCantidad());
        assertNull(p.getCodigo());
        assertNull(p.getCategoria());
    }

    // ===================== pedirDatosVenta: método de pago =====================

    @Test
    void pedirDatosVenta_opcionDePagoInvalida_vuelveAPreguntar() {
        // RF-06
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "1", "", "3", "efectivo", "2", "4111"));

        String texto = salida();
        assertEquals(2, texto.split("Método de pago inválido. Seleccione 1 o 2.", -1).length - 1);
        assertEquals("Tarjeta", v.getMetodoPago());
    }

    @Test
    void pedirDatosVenta_efectivoInsuficiente_vuelveAPedirElMonto() {
        // RF-06: el monto recibido debe cubrir el total
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "2", "", "1", "5", "20"));

        assertTrue(salida().contains("Efectivo insuficiente"));
        assertTrue(salida().contains("Intentar nuevamente con un monto suficiente."));
        assertEquals(20.0, v.getMontoPagado(), DELTA);
        assertEquals(0.0, v.getCambio(), DELTA);
    }

    @Test
    void pedirDatosVenta_montoNoNumerico_avisaYVuelveAPedir() {
        // RF-06
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "1", "", "1", "diez", "10"));

        assertTrue(salida().contains("Monto inválido."));
        assertEquals(10.0, v.getMontoPagado(), DELTA);
    }

    @Test
    void pedirDatosVenta_montoConComaDecimal_seAcepta() {
        // RF-06: la coma se cambia por punto antes de convertir
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "1", "", "1", "12,5"));

        assertEquals(12.5, v.getMontoPagado(), DELTA);
        assertEquals(2.5, v.getCambio(), DELTA);
    }

    @Test
    void pedirDatosVenta_tarjetaVacia_vuelveAPedirElNumero() {
        // RF-06: el número de tarjeta no puede estar vacío
        VentaModel v = vista.pedirDatosVenta(entrada("", "1", "2", "", "2", "", "   ", "4111111111111111"));

        assertEquals(2, salida().split("Número de tarjeta inválido. Intente de nuevo.", -1).length - 1);
        assertEquals("Tarjeta", v.getMetodoPago());
        assertEquals("4111111111111111", v.getNumeroTarjeta());
        assertEquals(20.0, v.getMontoPagado(), DELTA);
        assertEquals(0.0, v.getCambio(), DELTA);
    }

    // ===================== Ayudas =====================

    private static ProductoModel producto(int id, String nombre, double precio, int cantidad) {
        ProductoModel p = new ProductoModel();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setCantidad(cantidad);
        return p;
    }

    private static VentaModel resumen(int id, String cliente, double total) {
        VentaModel v = new VentaModel();
        v.setId(id);
        v.setFecha(new Date(1_000_000L * id));
        v.setCliente(cliente);
        v.setTotal(total);
        return v;
    }

    private static String texto(ProductoModel p) {
        return p.getId() + "|" + p.getNombre() + "|" + p.getPrecio() + "|" + p.getCantidad();
    }
}

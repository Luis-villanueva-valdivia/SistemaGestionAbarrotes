package com.tienda.cliente;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Prueba de caracterización para {@link ClienteDAO} contra la base de datos aislada.
 */
class ClienteDAOTest {

    // RF-12
    @Test
    void crudCliente_enBaseAislada_funcionaYNoModificaBaseOriginal() throws Exception {
        Path baseOriginal = Path.of("..", "..", "data", "tienda.db").toAbsolutePath().normalize();
        byte[] contenidoOriginalAntes = Files.readAllBytes(baseOriginal);

        ClienteDAO dao = new ClienteDAO();
        ClienteModel nuevo = new ClienteModel();
        nuevo.setNombre("Cliente Prueba DAO");
        nuevo.setDireccion("Calle Prueba 42");
        nuevo.setTelefono("9995550199");
        nuevo.setEmail("prueba.dao@correo.com");

        dao.registrar(nuevo);
        assertTrue(nuevo.getId() > 0);

        ClienteModel encontrado = dao.buscar(nuevo.getId());
        assertNotNull(encontrado);
        assertEquals("Cliente Prueba DAO", encontrado.getNombre());

        encontrado.setNombre("Cliente Actualizado DAO");
        dao.actualizar(encontrado);
        assertEquals("Cliente Actualizado DAO", dao.buscar(nuevo.getId()).getNombre());

        dao.eliminar(nuevo.getId());
        assertNull(dao.buscar(nuevo.getId()));

        assertArrayEquals(contenidoOriginalAntes, Files.readAllBytes(baseOriginal),
                "La base de datos original no debe modificarse durante las pruebas");
    }
}
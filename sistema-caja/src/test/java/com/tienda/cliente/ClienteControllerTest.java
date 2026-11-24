package com.tienda.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Prueba unitaria de caracterización para {@link ClienteController}.
 */
class ClienteControllerTest {

    // RF-12
    @Test
    void registrarCliente_conDatosValidos_delegaEnDaoYAsignaId() {
        ClienteDAOFalso dao = new ClienteDAOFalso();
        ClienteController controller = new ClienteController(dao);
        ClienteModel cliente = new ClienteModel();
        cliente.setNombre("Ana Pérez");
        cliente.setEmail("ana@correo.com");
        cliente.setTelefono("9991112233");
        cliente.setDireccion("Calle 10 #123");

        controller.registrarCliente(cliente);

        assertEquals(1, dao.clientes.size());
        assertEquals(1, cliente.getId());
        assertEquals("Ana Pérez", dao.clientes.get(0).getNombre());
    }

    /** Implementación falsa en memoria de {@link IClienteDAO}. */
    private static class ClienteDAOFalso implements IClienteDAO {
        private final List<ClienteModel> clientes = new ArrayList<>();
        private int secuenciaId = 1;

        @Override
        public void registrar(ClienteModel c) {
            if (c != null) {
                c.setId(secuenciaId++);
                clientes.add(c);
            }
        }

        @Override
        public void actualizar(ClienteModel c) {}

        @Override
        public ClienteModel buscar(int id) {
            return null;
        }

        @Override
        public List<ClienteModel> listarFrecuentes() {
            return new ArrayList<>();
        }

        @Override
        public List<ClienteModel> listarTodos() {
            return new ArrayList<>(clientes);
        }

        @Override
        public void eliminar(int id) {}

        @Override
        public ClienteModel buscarPorTelefono(String telefono) {
            return null;
        }

        @Override
        public int contarVentasMes(int clienteId) {
            return 0;
        }
    }
}
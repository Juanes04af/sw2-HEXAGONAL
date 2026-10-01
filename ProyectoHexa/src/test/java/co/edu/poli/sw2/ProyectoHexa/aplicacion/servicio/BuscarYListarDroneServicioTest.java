package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronNoEncontradoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas de los casos de uso de consulta:
 * {@link BuscarDroneServicio} y {@link ListarDronesServicio}.
 * Usa {@link RepositorioEnMemoria}, por lo que no requiere base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class BuscarYListarDroneServicioTest {

    private RepositorioEnMemoria repositorio;
    private BuscarDroneServicio buscar;
    private ListarDronesServicio listar;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria();
        buscar = new BuscarDroneServicio(repositorio);
        listar = new ListarDronesServicio(repositorio);
    }

    @Test
    @DisplayName("Buscar por id devuelve el dron con sus datos")
    void buscar_existente_devuelveDron() {
        Drone guardado = repositorio.crear(
                new Vigilancia(0, "VIG-001", "DJI", "Mavic 3", 0.9, true));

        Drone encontrado = buscar.buscarPorId(guardado.getId());

        assertEquals(guardado.getId(), encontrado.getId());
        assertEquals("VIG-001", encontrado.getSerial());
        assertTrue(((Vigilancia) encontrado).isDeteccionTermica());
    }

    @Test
    @DisplayName("Buscar un id inexistente lanza excepcion con el id en el mensaje")
    void buscar_inexistente_lanzaExcepcion() {
        DronNoEncontradoException ex = assertThrows(DronNoEncontradoException.class,
                () -> buscar.buscarPorId(77));

        assertTrue(ex.getMessage().contains("77"));
    }

    @Test
    @DisplayName("Listar sin registros devuelve una lista vacia, no null")
    void listar_vacio_devuelveListaVacia() {
        List<Drone> drones = listar.listar();

        assertNotNull(drones);
        assertTrue(drones.isEmpty());
    }

    @Test
    @DisplayName("Listar devuelve todos los drones en orden de creacion")
    void listar_devuelveTodosEnOrden() {
        repositorio.crear(new Vigilancia(0, "VIG-A", "DJI", "M", 1.0, false));
        repositorio.crear(new Vigilancia(0, "VIG-B", "DJI", "M", 1.0, true));
        repositorio.crear(new Vigilancia(0, "VIG-C", "DJI", "M", 1.0, false));

        List<Drone> drones = listar.listar();

        assertEquals(3, drones.size());
        assertEquals("VIG-A", drones.get(0).getSerial());
        assertEquals("VIG-C", drones.get(2).getSerial());
    }
}

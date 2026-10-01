package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronNoEncontradoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del caso de uso de eliminacion ({@link EliminarDroneServicio}).
 * Usa {@link RepositorioEnMemoria}, por lo que no requiere base de datos.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class EliminarDroneServicioTest {

    private RepositorioEnMemoria repositorio;
    private EliminarDroneServicio servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria();
        servicio = new EliminarDroneServicio(repositorio);
    }

    @Test
    @DisplayName("Eliminar quita el dron del repositorio")
    void eliminar_quitaDron() {
        Drone d = repositorio.crear(new Vigilancia(0, "VIG-001", "DJI", "M", 1.0, false));

        servicio.eliminar(d);

        assertTrue(repositorio.listar().isEmpty());
        assertThrows(DronNoEncontradoException.class,
                () -> repositorio.buscarPorId(d.getId()));
    }

    @Test
    @DisplayName("Eliminar solo borra el dron indicado")
    void eliminar_soloBorraElIndicado() {
        Drone a = repositorio.crear(new Vigilancia(0, "VIG-A", "DJI", "M", 1.0, false));
        Drone b = repositorio.crear(new Vigilancia(0, "VIG-B", "DJI", "M", 1.0, false));

        servicio.eliminar(a);

        assertEquals(1, repositorio.listar().size());
        assertEquals(b.getId(), repositorio.listar().get(0).getId());
    }

    @Test
    @DisplayName("Eliminar sin seleccion lanza excepcion y no llama al repositorio")
    void eliminar_sinSeleccion_lanzaExcepcion() {
        DronValidacionException ex = assertThrows(DronValidacionException.class,
                () -> servicio.eliminar(null));

        assertEquals("Debe seleccionar un drone para eliminar.", ex.getMessage());
        assertEquals(0, repositorio.llamadasEliminar);
    }

    @Test
    @DisplayName("Eliminar un dron inexistente lanza DronNoEncontradoException")
    void eliminar_inexistente_lanzaExcepcion() {
        Vigilancia fantasma = new Vigilancia(404, "X", "DJI", "M", 1.0, false);

        assertThrows(DronNoEncontradoException.class, () -> servicio.eliminar(fantasma));
    }
}

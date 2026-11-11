package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.BuscarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.CrearDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.EliminarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.ListarDronesServicio;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio.ModificarDroneServicio;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronDuplicadoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronNoEncontradoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Prueba de integracion de los servicios del CRUD con PostgreSQL real.
 *
 * @author Alejandra Cano y Juan Rosero
 */
@Tag("integration")
class DroneCrudServiciosIT {

    private final PostgresDroneRepository repositorio = new PostgresDroneRepository();
    private final CrearDroneServicio crear = new CrearDroneServicio(repositorio);
    private final ModificarDroneServicio modificar = new ModificarDroneServicio(repositorio);
    private final BuscarDroneServicio buscar = new BuscarDroneServicio(repositorio);
    private final ListarDronesServicio listar = new ListarDronesServicio(repositorio);
    private final EliminarDroneServicio eliminar = new EliminarDroneServicio(repositorio);
    private final List<Drone> creados = new ArrayList<>();

    @BeforeAll
    static void requierePostgres() {
        PostgresDisponible.requerir();
    }

    @AfterEach
    void limpiar() {
        for (Drone d : creados) {
            try {
                eliminar.eliminar(d);
            } catch (RuntimeException ignorada) {
                // La prueba pudo haberlo eliminado ya.
            }
        }
        creados.clear();
    }

    private static String serialUnico() {
        return "TEST-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Ciclo completo: crear, buscar, modificar, listar y eliminar")
    void cicloCompleto() {
        String serial = serialUnico();
        Drone creado = crear.crear("  " + serial + "  ", " DJI ", " Mavic 3 ", "1,5", true);
        creados.add(creado);
        int id = creado.getId();

        Vigilancia leido = (Vigilancia) buscar.buscarPorId(id);
        assertEquals(serial, leido.getSerial());
        assertEquals("DJI", leido.getFabricante());
        assertEquals("Mavic 3", leido.getModelo());
        assertEquals(1.5, leido.getPeso(), 0.0001);
        assertTrue(leido.isDeteccionTermica());

        modificar.modificar(leido, serial + "-B", "Autel", "EVO II", "2.0", false);
        Vigilancia editado = (Vigilancia) buscar.buscarPorId(id);
        assertEquals(serial + "-B", editado.getSerial());
        assertEquals("Autel", editado.getFabricante());
        assertEquals("EVO II", editado.getModelo());
        assertEquals(2.0, editado.getPeso(), 0.0001);
        assertFalse(editado.isDeteccionTermica());

        assertTrue(listar.listar().stream().anyMatch(d -> d.getId() == id));

        eliminar.eliminar(editado);
        assertThrows(DronNoEncontradoException.class, () -> buscar.buscarPorId(id));
    }

    @Test
    @DisplayName("Crear con un serial repetido lanza DronDuplicadoException")
    void crear_serialDuplicado() {
        String serial = serialUnico();
        creados.add(crear.crear(serial, "DJI", "Mavic", "0.9", false));

        assertThrows(DronDuplicadoException.class,
                () -> crear.crear(serial, "Autel", "EVO", "1.1", true));
    }

    @Test
    @DisplayName("Un dato invalido se rechaza en el servicio y nunca llega a la BD")
    void crear_datoInvalido_noLlegaALaBD() {
        int antes = listar.listar().size();

        assertThrows(DronValidacionException.class,
                () -> crear.crear("", "DJI", "M", "1", false));
        assertThrows(DronValidacionException.class,
                () -> crear.crear(serialUnico(), "12345", "M", "1", false));
        assertThrows(DronValidacionException.class,
                () -> crear.crear(serialUnico(), "DJI", "M", "-3", false));

        assertEquals(antes, listar.listar().size());
    }

    @Test
    @DisplayName("Modificar con el serial de otro dron lanza DronDuplicadoException")
    void modificar_serialDeOtro() {
        Drone a = crear.crear(serialUnico(), "DJI", "M", "1", false);
        Drone b = crear.crear(serialUnico(), "DJI", "M", "1", false);
        creados.add(a);
        creados.add(b);

        assertThrows(DronDuplicadoException.class,
                () -> modificar.modificar(b, a.getSerial(), "DJI", "M", "1", false));
    }

    @Test
    @DisplayName("Modificar un dron inexistente lanza DronNoEncontradoException")
    void modificar_inexistente() {
        Vigilancia fantasma = new Vigilancia(-1, serialUnico(), "DJI", "M", 1.0, false);

        assertThrows(DronNoEncontradoException.class,
                () -> modificar.modificar(fantasma, "X-1", "DJI", "M", "1", false));
    }
}

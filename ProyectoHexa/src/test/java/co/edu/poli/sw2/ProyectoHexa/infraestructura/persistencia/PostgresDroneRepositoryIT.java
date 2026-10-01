package co.edu.poli.sw2.ProyectoHexa.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronDuplicadoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronNoEncontradoException;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas de integracion del adaptador de salida
 * {@link PostgresDroneRepository} contra la base de datos real.
 *
 * <p>
 * Requiere PostgreSQL encendido y un .env valido. Cada prueba usa
 * seriales con prefijo {@code TEST-} y borra lo que crea al terminar,
 * para no dejar basura en la base de datos compartida.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
class PostgresDroneRepositoryIT {

    private final PostgresDroneRepository repositorio = new PostgresDroneRepository();
    private final List<Drone> creados = new ArrayList<>();

    private String serialUnico() {
        return "TEST-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Drone crearDePrueba(boolean termica) {
        Drone d = repositorio.crear(
                new Vigilancia(0, serialUnico(), "DJI", "Mavic 3", 0.9, termica));
        creados.add(d);
        return d;
    }

    @AfterEach
    void limpiar() {
        for (Drone d : creados) {
            try {
                repositorio.eliminar(d);
            } catch (RuntimeException ignorada) {
                // La prueba pudo haberlo eliminado ya.
            }
        }
        creados.clear();
    }

    @Test
    @DisplayName("Crear asigna un id generado por la base de datos")
    void crear_asignaId() {
        Drone d = crearDePrueba(true);

        assertTrue(d.getId() > 0);
    }

    @Test
    @DisplayName("Buscar por id recupera los datos guardados en ambas tablas")
    void buscarPorId_recuperaDatos() {
        Drone d = crearDePrueba(true);

        Drone leido = repositorio.buscarPorId(d.getId());

        assertTrue(leido instanceof Vigilancia);
        assertEquals(d.getSerial(), leido.getSerial());
        assertEquals("DJI", leido.getFabricante());
        assertEquals("Mavic 3", leido.getModelo());
        assertEquals(0.9, leido.getPeso(), 0.0001);
        assertTrue(((Vigilancia) leido).isDeteccionTermica());
    }

    @Test
    @DisplayName("Modificar actualiza la tabla padre y la tabla hija")
    void modificar_actualizaAmbasTablas() {
        Vigilancia d = (Vigilancia) crearDePrueba(false);

        d.setFabricante("Autel");
        d.setModelo("EVO II");
        d.setPeso(1.2);
        d.setDeteccionTermica(true);
        repositorio.modificar(d);

        Vigilancia leido = (Vigilancia) repositorio.buscarPorId(d.getId());
        assertEquals("Autel", leido.getFabricante());
        assertEquals("EVO II", leido.getModelo());
        assertEquals(1.2, leido.getPeso(), 0.0001);
        assertTrue(leido.isDeteccionTermica());
    }

    @Test
    @DisplayName("Listar incluye el dron recien creado")
    void listar_incluyeCreado() {
        Drone d = crearDePrueba(false);

        boolean encontrado = repositorio.listar().stream()
                .anyMatch(x -> x.getId() == d.getId());

        assertTrue(encontrado);
    }

    @Test
    @DisplayName("Listar solo devuelve drones de vigilancia")
    void listar_soloVigilancia() {
        crearDePrueba(false);

        for (Drone d : repositorio.listar()) {
            assertEquals("VIGILANCIA", d.getTipo());
        }
    }

    @Test
    @DisplayName("Eliminar borra el dron (y su fila hija por cascada)")
    void eliminar_borraDron() {
        Drone d = crearDePrueba(false);

        repositorio.eliminar(d);

        assertThrows(DronNoEncontradoException.class,
                () -> repositorio.buscarPorId(d.getId()));
    }

    @Test
    @DisplayName("Crear con un serial repetido lanza DronDuplicadoException")
    void crear_serialDuplicado_lanzaExcepcion() {
        Drone original = crearDePrueba(false);
        Vigilancia repetido = new Vigilancia(0, original.getSerial(), "DJI", "M", 1.0, false);

        assertThrows(DronDuplicadoException.class, () -> repositorio.crear(repetido));
    }

    @Test
    @DisplayName("Un serial duplicado no deja registros huerfanos (rollback)")
    void crear_duplicado_haceRollback() {
        Drone original = crearDePrueba(false);
        int antes = repositorio.listar().size();

        assertThrows(DronDuplicadoException.class, () -> repositorio.crear(
                new Vigilancia(0, original.getSerial(), "DJI", "M", 1.0, false)));

        assertEquals(antes, repositorio.listar().size());
    }

    @Test
    @DisplayName("Buscar un id inexistente lanza DronNoEncontradoException")
    void buscar_inexistente_lanzaExcepcion() {
        assertThrows(DronNoEncontradoException.class,
                () -> repositorio.buscarPorId(-1));
    }

    @Test
    @DisplayName("Modificar un id inexistente lanza DronNoEncontradoException")
    void modificar_inexistente_lanzaExcepcion() {
        Vigilancia fantasma = new Vigilancia(-1, serialUnico(), "DJI", "M", 1.0, false);

        assertThrows(DronNoEncontradoException.class,
                () -> repositorio.modificar(fantasma));
    }

    @Test
    @DisplayName("Eliminar un id inexistente lanza DronNoEncontradoException")
    void eliminar_inexistente_lanzaExcepcion() {
        Vigilancia fantasma = new Vigilancia(-1, "X", "DJI", "M", 1.0, false);

        assertThrows(DronNoEncontradoException.class,
                () -> repositorio.eliminar(fantasma));
    }

    @Test
    @DisplayName("El repositorio rechaza drones que no son de vigilancia")
    void crear_tipoNoVigilancia_lanzaExcepcion() {
        Drone otroTipo = new Drone(0, serialUnico(), "DJI", "Agras", 30.0) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getTipo() {
                return "AGRICULTURA";
            }
        };

        assertThrows(DronValidacionException.class, () -> repositorio.crear(otroTipo));
    }
}

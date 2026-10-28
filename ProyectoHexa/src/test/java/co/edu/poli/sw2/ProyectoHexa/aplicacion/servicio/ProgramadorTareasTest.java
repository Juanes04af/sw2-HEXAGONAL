package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.DroneCommand;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Vigilancia;

/**
 * Pruebas del invocador {@link ProgramadorTareas}.
 *
 * @author Alejandra Cano y Juan Rosero
 */
class ProgramadorTareasTest {

    private ProgramadorTareas programador;
    private List<String> ejecutadas;
    private Drone drone;

    @BeforeEach
    void preparar() {
        programador = new ProgramadorTareas();
        ejecutadas = new ArrayList<>();
        drone = new Vigilancia(1, "VIG-001", "DJI", "M", 1.0, false);
    }

    /** Crea un comando de prueba que anota su nombre al ejecutarse. */
    private DroneCommand comando(String nombre) {
        return new DroneCommand() {
            @Override
            public String ejecutar() {
                ejecutadas.add(nombre);
                return "hecho " + nombre;
            }

            @Override
            public String getNombre() {
                return nombre;
            }

            @Override
            public Drone getDrone() {
                return drone;
            }
        };
    }

    @Test
    @DisplayName("Ejecuta las tareas en el orden en que se registraron")
    void ejecuta_enOrden() {
        programador.agregar(comando("A"));
        programador.agregar(comando("B"));
        programador.agregar(comando("C"));

        List<ResultadoTarea> r = programador.ejecutarTodas();

        assertEquals(List.of("A", "B", "C"), ejecutadas);
        assertEquals(1, r.get(0).getOrden());
        assertEquals("hecho C", r.get(2).getMensaje());
        assertEquals(3, r.get(2).getOrden());
    }

    @Test
    @DisplayName("Agregar no ejecuta la tarea: se ejecuta solo con la mision")
    void agregar_noEjecuta() {
        programador.agregar(comando("A"));

        assertTrue(ejecutadas.isEmpty());
        assertEquals(1, programador.getTareas().size());
    }

    @Test
    @DisplayName("Al ejecutar, la lista queda vacia para la proxima mision")
    void ejecutar_vaciaLista() {
        programador.agregar(comando("A"));

        programador.ejecutarTodas();

        assertTrue(programador.getTareas().isEmpty());
    }

    @Test
    @DisplayName("Ejecutar sin tareas es rechazado")
    void ejecutar_sinTareas_rechazado() {
        assertThrows(DronValidacionException.class, () -> programador.ejecutarTodas());
    }

    @Test
    @DisplayName("Si una tarea falla, se registra el error y la mision continua")
    void tareaQueFalla_noDetieneMision() {
        programador.agregar(comando("A"));
        programador.agregar(new DroneCommand() {
            @Override
            public String ejecutar() {
                throw new IllegalStateException("sensor sin bateria");
            }

            @Override
            public String getNombre() {
                return "X";
            }

            @Override
            public Drone getDrone() {
                return drone;
            }
        });
        programador.agregar(comando("C"));

        List<ResultadoTarea> r = programador.ejecutarTodas();

        assertEquals(3, r.size());
        assertFalse(r.get(1).isExitosa());
        assertTrue(r.get(1).getMensaje().contains("sensor sin bateria"));
        assertEquals(List.of("A", "C"), ejecutadas);
    }

    @Test
    @DisplayName("Quitar elimina la tarea indicada y conserva el orden del resto")
    void quitar_conservaOrden() {
        programador.agregar(comando("A"));
        programador.agregar(comando("B"));
        programador.agregar(comando("C"));

        programador.quitar(1);
        programador.ejecutarTodas();

        assertEquals(List.of("A", "C"), ejecutadas);
    }

    @Test
    @DisplayName("Quitar una posicion inexistente es rechazado")
    void quitar_posicionInvalida() {
        programador.agregar(comando("A"));

        assertThrows(DronValidacionException.class, () -> programador.quitar(-1));
        assertThrows(DronValidacionException.class, () -> programador.quitar(1));
    }

    @Test
    @DisplayName("Limpiar borra todas las tareas")
    void limpiar() {
        programador.agregar(comando("A"));
        programador.agregar(comando("B"));

        programador.limpiar();

        assertTrue(programador.getTareas().isEmpty());
    }

    @Test
    @DisplayName("La lista de tareas no se puede modificar desde fuera")
    void getTareas_soloLectura() {
        assertThrows(UnsupportedOperationException.class,
                () -> programador.getTareas().add(comando("X")));
    }
}

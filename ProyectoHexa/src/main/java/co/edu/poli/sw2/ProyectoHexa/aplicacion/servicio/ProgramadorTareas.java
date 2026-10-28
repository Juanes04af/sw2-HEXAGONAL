package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.DroneCommand;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;

/**
 * <b>Invocador</b> del patron Command.
 *
 * <p>
 * Guarda la secuencia de tareas programadas y, al ejecutar la mision,
 * invoca {@link DroneCommand#ejecutar()} sobre cada una en el orden en
 * que se registraron. No sabe que hace cada tarea: solo trabaja con la
 * interfaz {@link DroneCommand}.
 * </p>
 *
 * <p>
 * Si una tarea falla, su error queda en el resultado y la mision
 * continua con las siguientes. Al terminar, la lista queda vacia para
 * programar la proxima mision.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ProgramadorTareas {

    /** Tareas programadas, en orden de registro. */
    private final List<DroneCommand> tareas = new ArrayList<>();

    /**
     * Agrega una tarea al final de la secuencia.
     *
     * @param tarea comando a programar
     * @throws NullPointerException si la tarea es nula
     */
    public synchronized void agregar(DroneCommand tarea) {
        tareas.add(Objects.requireNonNull(tarea, "La tarea es obligatoria."));
    }

    /**
     * Quita una tarea de la secuencia.
     *
     * @param indice posicion de la tarea, empezando en 0
     * @throws DronValidacionException si la posicion no existe
     */
    public synchronized void quitar(int indice) {
        if (indice < 0 || indice >= tareas.size()) {
            throw new DronValidacionException("Seleccione una tarea de la lista para quitar.");
        }
        tareas.remove(indice);
    }

    /**
     * Borra todas las tareas programadas.
     */
    public synchronized void limpiar() {
        tareas.clear();
    }

    /**
     * Devuelve las tareas programadas, en orden.
     *
     * @return vista de solo lectura
     */
    public synchronized List<DroneCommand> getTareas() {
        return Collections.unmodifiableList(new ArrayList<>(tareas));
    }

    /**
     * Ejecuta todas las tareas en el orden registrado.
     *
     * @return un resultado por tarea, en el mismo orden
     * @throws DronValidacionException si no hay tareas programadas
     */
    public synchronized List<ResultadoTarea> ejecutarTodas() {
        if (tareas.isEmpty()) {
            throw new DronValidacionException("No hay tareas programadas en la mision.");
        }

        List<ResultadoTarea> resultados = new ArrayList<>();
        int orden = 1;
        for (DroneCommand tarea : tareas) {
            try {
                resultados.add(new ResultadoTarea(orden, tarea.getNombre(), tarea.getDrone(),
                        tarea.ejecutar(), true));
            } catch (RuntimeException e) {
                resultados.add(new ResultadoTarea(orden, tarea.getNombre(), tarea.getDrone(),
                        "Error: " + e.getMessage(), false));
            }
            orden++;
        }
        tareas.clear();
        return resultados;
    }
}

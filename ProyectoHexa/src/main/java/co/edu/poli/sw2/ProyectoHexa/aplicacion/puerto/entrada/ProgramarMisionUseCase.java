package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import java.util.List;

import co.edu.poli.sw2.ProyectoHexa.dominio.command.DroneCommand;
import co.edu.poli.sw2.ProyectoHexa.dominio.command.TipoTarea;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de entrada para programar la secuencia de tareas de una
 * mision y ejecutarla.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface ProgramarMisionUseCase {

    /**
     * Agrega una tarea al final de la mision.
     *
     * @param tipo  tarea elegida
     * @param drone dron que la ejecutara
     */
    void programar(TipoTarea tipo, Drone drone);

    /**
     * Quita una tarea de la mision.
     *
     * @param indice posicion de la tarea, empezando en 0
     */
    void quitar(int indice);

    /**
     * Borra todas las tareas programadas.
     */
    void limpiar();

    /**
     * Devuelve las tareas programadas, en orden.
     *
     * @return tareas pendientes
     */
    List<DroneCommand> tareasProgramadas();

    /**
     * Ejecuta las tareas en el orden registrado.
     *
     * @return un resultado por tarea
     */
    List<ResultadoTarea> ejecutarMision();
}

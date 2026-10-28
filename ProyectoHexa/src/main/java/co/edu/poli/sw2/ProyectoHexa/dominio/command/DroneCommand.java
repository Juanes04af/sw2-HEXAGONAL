package co.edu.poli.sw2.ProyectoHexa.dominio.command;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Comando del patron <b>Command</b>.
 *
 * <p>
 * Encapsula una tarea que un dron ejecutara durante una mision como un
 * objeto independiente. Asi las tareas pueden guardarse en una lista,
 * ordenarse y ejecutarse despues, sin que quien las ejecuta (el
 * invocador) sepa que hace cada una.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface DroneCommand {

    /**
     * Ejecuta la tarea.
     *
     * @return descripcion del resultado obtenido
     */
    String ejecutar();

    /**
     * Obtiene el nombre de la tarea, para mostrarlo al usuario.
     *
     * @return nombre de la tarea
     */
    String getNombre();

    /**
     * Obtiene el dron que ejecutara la tarea.
     *
     * @return dron asociado
     */
    Drone getDrone();
}

package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de entrada encargado de eliminar drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface EliminarDroneUseCase {

    /**
     * Elimina un dron.
     *
     * @param drone dron que se desea eliminar
     */
    void eliminar(Drone drone);
}
package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de entrada encargado de buscar un dron.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface BuscarDroneUseCase {

    /**
     * Busca un dron utilizando su identificador.
     *
     * @param id identificador del dron
     * @return dron encontrado
     */
    Drone buscarPorId(int id);
}
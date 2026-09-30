package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import java.util.List;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de entrada encargado de listar drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface ListarDronesUseCase {

    /**
     * Obtiene todos los drones registrados.
     *
     * @return lista de drones
     */
    List<Drone> listar();
}
package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida;

import java.util.List;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Puerto de salida que define las operaciones necesarias
 * para almacenar y recuperar drones.
 *
 * La aplicacion conoce esta interfaz, pero no conoce
 * PostgreSQL ni JDBC.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public interface DroneRepository {

    /**
     * Guarda un nuevo dron.
     *
     * @param drone dron a almacenar
     * @return dron almacenado con su identificador
     */
    Drone crear(Drone drone);

    /**
     * Actualiza un dron existente.
     *
     * @param drone dron modificado
     * @return dron actualizado
     */
    Drone modificar(Drone drone);

    /**
     * Elimina un dron.
     *
     * @param drone dron a eliminar
     */
    void eliminar(Drone drone);

    /**
     * Busca un dron por identificador.
     *
     * @param id identificador
     * @return dron encontrado
     */
    Drone buscarPorId(int id);

    /**
     * Obtiene todos los drones almacenados.
     *
     * @return lista de drones
     */
    List<Drone> listar();
}
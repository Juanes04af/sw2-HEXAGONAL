package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.BuscarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Servicio encargado de buscar drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class BuscarDroneServicio implements BuscarDroneUseCase {

    private final DroneRepository droneRepository;

    /**
     * Construye el servicio.
     *
     * @param droneRepository repositorio utilizado
     */
    public BuscarDroneServicio(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drone buscarPorId(int id) {
        return droneRepository.buscarPorId(id);
    }
}
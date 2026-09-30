package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.List;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ListarDronesUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Servicio encargado de listar todos los drones.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class ListarDronesServicio implements ListarDronesUseCase {

    private final DroneRepository droneRepository;

    /**
     * Construye el servicio.
     *
     * @param droneRepository repositorio utilizado
     */
    public ListarDronesServicio(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Drone> listar() {
        return droneRepository.listar();
    }
}
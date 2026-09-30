package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.EliminarDroneUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.salida.DroneRepository;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Servicio encargado de eliminar un dron.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class EliminarDroneServicio implements EliminarDroneUseCase {

    private final DroneRepository droneRepository;

    /**
     * Construye el servicio.
     *
     * @param droneRepository repositorio utilizado
     */
    public EliminarDroneServicio(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eliminar(Drone drone) {

        if (drone == null) {
            throw new DronValidacionException(
                    "Debe seleccionar un drone para eliminar.");
        }

        droneRepository.eliminar(drone);
    }
}
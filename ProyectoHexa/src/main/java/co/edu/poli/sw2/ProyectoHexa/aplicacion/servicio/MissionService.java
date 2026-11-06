package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.mediator.DroneMediator;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ExecuteMissionUseCase;
import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ResultadoMision;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Servicio que atiende la solicitud de iniciar una operacion de
 * vigilancia y la entrega al mediador.
 *
 * <p>
 * Trabaja con drones ya existentes: el dron llega convertido al modelo
 * {@link Drone} y el servicio solo lo pasa al {@link DroneMediator}. No
 * crea, modifica ni elimina drones, ni cambia el modelo.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class MissionService implements ExecuteMissionUseCase {

    /** Mediador que coordina la mision. */
    private final DroneMediator mediator;

    /**
     * Construye el servicio.
     *
     * @param mediator mediador de la mision
     */
    public MissionService(DroneMediator mediator) {
        this.mediator = Objects.requireNonNull(mediator);
    }

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si no se selecciono un dron
     */
    @Override
    public ResultadoMision ejecutarMision(Drone drone, int nivelBateria) {
        if (drone == null) {
            throw new DronValidacionException("Seleccione un drone para la operacion.");
        }
        return mediator.iniciarMision(drone, nivelBateria);
    }
}

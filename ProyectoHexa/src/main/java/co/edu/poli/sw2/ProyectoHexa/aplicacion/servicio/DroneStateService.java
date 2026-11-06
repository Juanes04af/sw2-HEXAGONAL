package co.edu.poli.sw2.ProyectoHexa.aplicacion.servicio;

import java.util.HashMap;
import java.util.Map;

import co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada.ManageDroneStateUseCase;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.DronValidacionException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.DroneConEstado;
import co.edu.poli.sw2.ProyectoHexa.dominio.state.DroneState;

/**
 * Caso de uso que gestiona el estado operativo de los drones.
 *
 * <p>
 * Mantiene un {@link DroneConEstado} (wrapper del dron) por cada id.
 * Cada consulta a la base de datos crea objetos {@link Drone} nuevos;
 * por eso, antes de operar se le entrega al wrapper la instancia mas
 * reciente del dron. El estado se conserva y la operacion la decide el
 * propio estado (patron State).
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public class DroneStateService implements ManageDroneStateUseCase {

    /** Un contexto por dron, por id. */
    private final Map<Integer, DroneConEstado> contextos = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public synchronized void encender(Drone drone) {
        contextoDe(drone).encender();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void apagar(Drone drone) {
        contextoDe(drone).apagar();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void despegar(Drone drone) {
        contextoDe(drone).despegar();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void iniciarAterrizaje(Drone drone) {
        contextoDe(drone).iniciarAterrizaje();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void finalizarAterrizaje(Drone drone) {
        contextoDe(drone).finalizarAterrizaje();
    }

    /**
     * {@inheritDoc}
     *
     * @throws DronValidacionException si el dron es nulo
     */
    @Override
    public synchronized DroneState estadoDe(Drone drone) {
        return contextoDe(drone).getEstadoActual();
    }

    /**
     * Obtiene (o crea, APAGADO) el contexto del dron y lo apunta a la
     * instancia recibida.
     */
    private DroneConEstado contextoDe(Drone drone) {
        if (drone == null) {
            throw new DronValidacionException("Seleccione un drone.");
        }
        DroneConEstado contexto = contextos.computeIfAbsent(drone.getId(), id -> new DroneConEstado(drone));
        contexto.setDrone(drone);
        return contexto;
    }
}

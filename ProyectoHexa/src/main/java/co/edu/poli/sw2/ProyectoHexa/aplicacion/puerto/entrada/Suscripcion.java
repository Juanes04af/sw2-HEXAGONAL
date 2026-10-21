package co.edu.poli.sw2.ProyectoHexa.aplicacion.puerto.entrada;

import co.edu.poli.sw2.ProyectoHexa.dominio.clima.Ciudad;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Par dron-ciudad que representa una suscripcion vigente.
 *
 * <p>
 * El dron es la misma instancia registrada en el notificador, por lo
 * que su {@link Drone#getClimaActual()} refleja la ultima notificacion.
 * </p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class Suscripcion {

    /** Dron suscrito. */
    private final Drone drone;

    /** Ciudad a la que esta suscrito. */
    private final Ciudad ciudad;

    /**
     * Crea una suscripcion.
     *
     * @param drone  dron suscrito
     * @param ciudad ciudad asociada
     */
    public Suscripcion(Drone drone, Ciudad ciudad) {
        this.drone = drone;
        this.ciudad = ciudad;
    }

    /**
     * Obtiene el dron suscrito.
     *
     * @return dron
     */
    public Drone getDrone() {
        return drone;
    }

    /**
     * Obtiene la ciudad de la suscripcion.
     *
     * @return ciudad
     */
    public Ciudad getCiudad() {
        return ciudad;
    }
}

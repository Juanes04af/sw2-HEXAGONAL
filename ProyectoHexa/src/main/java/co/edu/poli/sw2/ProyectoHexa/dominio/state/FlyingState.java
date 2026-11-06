package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado concreto <b>VOLANDO</b> del patron State.
 * En vuelo: unicamente puede iniciar el aterrizaje.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class FlyingState implements DroneState {

    /** Lo que este estado permite; se informa al rechazar una operacion. */
    private static final String PERMITIDAS = "Solo puede iniciar el aterrizaje.";

    /** Contexto al que pertenece este estado. */
    private final DroneConEstado contexto;

    /**
     * Crea el estado para un contexto.
     *
     * @param contexto dron envuelto que cambiara de estado
     */
    FlyingState(DroneConEstado contexto) {
        this.contexto = Objects.requireNonNull(contexto);
    }

    /** {@inheritDoc} */
    @Override
    public void encender(Drone drone) {
        rechazar(drone, "encender");
    }

    /** {@inheritDoc} */
    @Override
    public void apagar(Drone drone) {
        rechazar(drone, "apagar");
    }

    /** {@inheritDoc} */
    @Override
    public void despegar(Drone drone) {
        rechazar(drone, "despegar");
    }

    /** {@inheritDoc} */
    @Override
    public void iniciarAterrizaje(Drone drone) {
        contexto.cambiarEstado(new LandingState(contexto));
    }

    /** {@inheritDoc} */
    @Override
    public void finalizarAterrizaje(Drone drone) {
        rechazar(drone, "finalizar aterrizaje");
    }

    private void rechazar(Drone drone, String operacion) {
        throw new TransicionInvalidaException(drone, this, operacion, PERMITIDAS);
    }

    /**
     * Devuelve el nombre del estado, para mostrarlo al usuario.
     *
     * @return "VOLANDO"
     */
    @Override
    public String toString() {
        return "VOLANDO";
    }
}

package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado concreto <b>ATERRIZANDO</b> del patron State.
 * Aterrizando: no puede volar hasta finalizar el aterrizaje.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class LandingState implements DroneState {

    /** Lo que este estado permite; se informa al rechazar una operacion. */
    private static final String PERMITIDAS = "Solo puede finalizar el aterrizaje.";

    /** Contexto al que pertenece este estado. */
    private final DroneConEstado contexto;

    /**
     * Crea el estado para un contexto.
     *
     * @param contexto dron envuelto que cambiara de estado
     */
    LandingState(DroneConEstado contexto) {
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
        rechazar(drone, "iniciar aterrizaje");
    }

    /** {@inheritDoc} */
    @Override
    public void finalizarAterrizaje(Drone drone) {
        contexto.cambiarEstado(new ReadyState(contexto));
    }

    private void rechazar(Drone drone, String operacion) {
        throw new TransicionInvalidaException(drone, this, operacion, PERMITIDAS);
    }

    /**
     * Devuelve el nombre del estado, para mostrarlo al usuario.
     *
     * @return "ATERRIZANDO"
     */
    @Override
    public String toString() {
        return "ATERRIZANDO";
    }
}

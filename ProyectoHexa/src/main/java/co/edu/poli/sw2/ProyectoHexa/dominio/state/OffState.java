package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import java.util.Objects;
import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado concreto <b>APAGADO</b> del patron State.
 * El dron no esta operativo: unicamente puede encenderse.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class OffState implements DroneState {

    /** Lo que este estado permite; se informa al rechazar una operacion. */
    private static final String PERMITIDAS = "Solo puede encenderse.";

    /** Contexto al que pertenece este estado. */
    private final DroneConEstado contexto;

    /**
     * Crea el estado para un contexto.
     *
     * @param contexto dron envuelto que cambiara de estado
     */
    OffState(DroneConEstado contexto) {
        this.contexto = Objects.requireNonNull(contexto);
    }

    /** {@inheritDoc} */
    @Override
    public void encender(Drone drone) {
        contexto.cambiarEstado(new ReadyState(contexto));
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
        rechazar(drone, "finalizar aterrizaje");
    }

    private void rechazar(Drone drone, String operacion) {
        throw new TransicionInvalidaException(drone, this, operacion, PERMITIDAS);
    }

    /**
     * Devuelve el nombre del estado, para mostrarlo al usuario.
     *
     * @return "APAGADO"
     */
    @Override
    public String toString() {
        return "APAGADO";
    }
}

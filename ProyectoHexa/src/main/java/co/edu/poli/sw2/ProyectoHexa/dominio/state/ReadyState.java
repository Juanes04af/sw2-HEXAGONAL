package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado concreto <b>EN ESPERA</b> del patron State.
 * Encendido, en tierra y preparado: puede despegar o apagarse.
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class ReadyState implements DroneState {

    /** Lo que este estado permite; se informa al rechazar una operacion. */
    private static final String PERMITIDAS = "Puede despegar o apagarse.";

    /** Contexto al que pertenece este estado. */
    private final DroneConEstado contexto;

    /**
     * Crea el estado para un contexto.
     *
     * @param contexto dron envuelto que cambiara de estado
     */
    ReadyState(DroneConEstado contexto) {
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
        contexto.cambiarEstado(new OffState(contexto));
    }

    /** {@inheritDoc} */
    @Override
    public void despegar(Drone drone) {
        contexto.cambiarEstado(new FlyingState(contexto));
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
     * @return "EN ESPERA"
     */
    @Override
    public String toString() {
        return "EN ESPERA";
    }
}

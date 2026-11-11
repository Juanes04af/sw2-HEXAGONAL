package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado concreto <b>ATERRIZANDO</b> del patron State.
 * El dron esta aterrizando: solo puede finalizar el aterrizaje.
 *
 * <p>Las operaciones que no se sobrescriben aqui las rechaza
 * {@link EstadoBase}.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class LandingState extends EstadoBase {

    /**
     * Crea el estado para un contexto.
     *
     * @param contexto dron envuelto que cambiara de estado
     */
    LandingState(DroneConEstado contexto) {
        super(contexto, "ATERRIZANDO", "Solo puede finalizar el aterrizaje.");
    }

    /** Transicion permitida: pasa a {@link ReadyState}. */
    @Override
    public void finalizarAterrizaje(Drone drone) {
        contexto.cambiarEstado(new ReadyState(contexto));
    }
}

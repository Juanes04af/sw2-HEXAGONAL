package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado concreto <b>VOLANDO</b> del patron State.
 * El dron esta en el aire: solo puede iniciar el aterrizaje.
 *
 * <p>Las operaciones que no se sobrescriben aqui las rechaza
 * {@link EstadoBase}.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class FlyingState extends EstadoBase {

    /**
     * Crea el estado para un contexto.
     *
     * @param contexto dron envuelto que cambiara de estado
     */
    FlyingState(DroneConEstado contexto) {
        super(contexto, "VOLANDO", "Solo puede iniciar el aterrizaje.");
    }

    /** Transicion permitida: pasa a {@link LandingState}. */
    @Override
    public void iniciarAterrizaje(Drone drone) {
        contexto.cambiarEstado(new LandingState(contexto));
    }
}

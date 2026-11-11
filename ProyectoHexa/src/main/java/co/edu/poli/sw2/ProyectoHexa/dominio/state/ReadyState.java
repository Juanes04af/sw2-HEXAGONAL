package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado concreto <b>EN ESPERA</b> del patron State.
 * El dron esta encendido en tierra: puede despegar o apagarse.
 *
 * <p>Las operaciones que no se sobrescriben aqui las rechaza
 * {@link EstadoBase}.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class ReadyState extends EstadoBase {

    /**
     * Crea el estado para un contexto.
     *
     * @param contexto dron envuelto que cambiara de estado
     */
    ReadyState(DroneConEstado contexto) {
        super(contexto, "EN ESPERA", "Puede despegar o apagarse.");
    }

    /** Transicion permitida: pasa a {@link FlyingState}. */
    @Override
    public void despegar(Drone drone) {
        contexto.cambiarEstado(new FlyingState(contexto));
    }

    /** Transicion permitida: pasa a {@link OffState}. */
    @Override
    public void apagar(Drone drone) {
        contexto.cambiarEstado(new OffState(contexto));
    }
}

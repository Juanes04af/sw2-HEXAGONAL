package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Estado concreto <b>APAGADO</b> del patron State.
 * El dron no esta operativo: unicamente puede encenderse.
 *
 * <p>Las operaciones que no se sobrescriben aqui las rechaza
 * {@link EstadoBase}.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
public final class OffState extends EstadoBase {

    /**
     * Crea el estado para un contexto.
     *
     * @param contexto dron envuelto que cambiara de estado
     */
    OffState(DroneConEstado contexto) {
        super(contexto, "APAGADO", "Solo puede encenderse.");
    }

    /** Transicion permitida: pasa a {@link ReadyState}. */
    @Override
    public void encender(Drone drone) {
        contexto.cambiarEstado(new ReadyState(contexto));
    }
}

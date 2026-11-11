package co.edu.poli.sw2.ProyectoHexa.dominio.state;

import java.util.Objects;

import co.edu.poli.sw2.ProyectoHexa.dominio.excepcion.TransicionInvalidaException;
import co.edu.poli.sw2.ProyectoHexa.dominio.modelo.Drone;

/**
 * Base comun de los estados concretos del patron State.
 *
 * <p>Por defecto <b>todas</b> las operaciones se rechazan con
 * {@link TransicionInvalidaException}. Cada estado concreto sobrescribe solo
 * las operaciones que si permite y en ellas hace la transicion. Asi el codigo
 * de rechazo esta en un solo lugar y no se repite en los cuatro estados.</p>
 *
 * <p>La clase y su constructor son de paquete: solo los estados de este
 * paquete pueden extenderla.</p>
 *
 * @author Alejandra Cano y Juan Rosero
 */
abstract class EstadoBase implements DroneState {

    /** Contexto al que pertenece este estado. */
    protected final DroneConEstado contexto;

    /** Nombre visible del estado (APAGADO, EN ESPERA...). */
    private final String nombre;

    /** Lo que este estado permite; se informa al rechazar una operacion. */
    private final String permitidas;

    EstadoBase(DroneConEstado contexto, String nombre, String permitidas) {
        this.contexto = Objects.requireNonNull(contexto);
        this.nombre = nombre;
        this.permitidas = permitidas;
    }

    @Override
    public void encender(Drone drone) {
        rechazar(drone, "encender");
    }

    @Override
    public void apagar(Drone drone) {
        rechazar(drone, "apagar");
    }

    @Override
    public void despegar(Drone drone) {
        rechazar(drone, "despegar");
    }

    @Override
    public void iniciarAterrizaje(Drone drone) {
        rechazar(drone, "iniciar aterrizaje");
    }

    @Override
    public void finalizarAterrizaje(Drone drone) {
        rechazar(drone, "finalizar aterrizaje");
    }

    /** Rechaza la operacion: lanza la excepcion y el estado no cambia. */
    private void rechazar(Drone drone, String operacion) {
        throw new TransicionInvalidaException(drone, this, operacion, permitidas);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
